/*
 * Copyright (c) 2026 Rodrigo Prestes Machado
 * All rights reserved.
 *
 * This source code is proprietary and confidential.
 * Unauthorized copying, modification, distribution, or use
 * of this software, via any medium, is strictly prohibited
 * without the express prior written permission of the copyright holder.
 */
package dev.rpmhub.application;

import java.util.Date;

import dev.rpmhub.domain.model.AgentMessage;
import dev.rpmhub.domain.model.Chat;
import dev.rpmhub.domain.model.User;
import dev.rpmhub.domain.model.UserMessage;
import dev.rpmhub.domain.port.in.ChatUseCase;
import dev.rpmhub.domain.port.in.RouterUseCase;
import dev.rpmhub.domain.port.out.Repository;
import io.smallrye.mutiny.Multi;

/**
 * Application service that orchestrates chat acceptance and RAG-grounded
 * assistant replies.
 *
 * <p>This class is deliberately framework-agnostic (plain Java) so it can be
 * unit tested without a CDI container. Its lifecycle and wiring are handled by
 * {@code dev.rpmhub.adapter.config.ApplicationBeans}.
 *
 * @author Rodrigo Prestes Machado
 */
public class ChatService implements ChatUseCase {

    /**
     * Repository used to load and store the last chat per user.
     */
    private final Repository chatRepository;

    /** Chooses the teacher or the administrative agent and retrieves the matching corpus. */
    private final RouterUseCase routerUseCase;

    /** Maximum idle time, in milliseconds, before a new chat session starts ({@code chat.inactivity-threshold-minutes}). */
    private final long inactivityThresholdMs;

    /**
     * Creates the chat service with its driven ports.
     *
     * @param chatRepository        port used to persist chats
     * @param routerUseCase      routes the message and retrieves the matching corpus
     * @param inactivityThresholdMs maximum idle time, in milliseconds, before a new chat session starts
     */
    public ChatService(Repository chatRepository, RouterUseCase routerUseCase, long inactivityThresholdMs) {
        this.chatRepository = chatRepository;
        this.routerUseCase = routerUseCase;
        this.inactivityThresholdMs = inactivityThresholdMs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Multi<String> chat(String phoneNumber, String message) {
        User user = new User();
        user.setPhoneNumber(phoneNumber);

        UserMessage userMessage = new UserMessage();
        userMessage.setUser(user);
        userMessage.setMessage(message);
        userMessage.setTimestamp(new Date());

        Chat lastChat = chatRepository.findLastByPhone(phoneNumber).orElse(null);
        Chat chat = Chat.accept(lastChat, userMessage, inactivityThresholdMs);
        chatRepository.save(chat);

        StringBuilder buffer = new StringBuilder();

        return routerUseCase.answer(chat.getId(), message)
                .invoke(buffer::append)
                .onCompletion().invoke(() -> {
                    AgentMessage agentMessage = new AgentMessage();
                    agentMessage.setMessage(buffer.toString());
                    agentMessage.setTimestamp(new Date());
                    chat.addMessage(agentMessage);
                    chatRepository.save(chat);
                });
    }

}
