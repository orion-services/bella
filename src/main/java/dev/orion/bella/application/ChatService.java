/*
 * Copyright 2026 Rodrigo Prestes Machado
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.orion.bella.application;

import java.util.Date;

import dev.orion.bella.domain.model.AgentMessage;
import dev.orion.bella.domain.model.Chat;
import dev.orion.bella.domain.model.User;
import dev.orion.bella.domain.model.UserMessage;
import dev.orion.bella.domain.port.in.ChatUseCase;
import dev.orion.bella.domain.port.in.RoutedAnswer;
import dev.orion.bella.domain.port.in.RouterUseCase;
import dev.orion.bella.domain.port.out.Repository;
import io.smallrye.mutiny.Multi;

/**
 * Application service that orchestrates chat acceptance and RAG-grounded
 * assistant replies.
 *
 * <p>This class is deliberately framework-agnostic (plain Java) so it can be
 * unit tested without a CDI container. Its lifecycle and wiring are handled by
 * {@code dev.orion.bella.adapter.config.ApplicationBeans}.
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
        RoutedAnswer routed = routerUseCase.answer(chat.getId(), message);

        return routed.getChunks()
                .invoke(buffer::append)
                .onCompletion().invoke(() -> {
                    AgentMessage agentMessage = new AgentMessage();
                    agentMessage.setMessage(buffer.toString());
                    agentMessage.setTimestamp(new Date());
                    agentMessage.setAgent(routed.getAgent());
                    agentMessage.setCopied(false);
                    chat.addMessage(agentMessage);
                    chatRepository.save(chat);
                });
    }

}
