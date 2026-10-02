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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import dev.orion.bella.domain.model.AgentMessage;
import dev.orion.bella.domain.model.Chat;
import dev.orion.bella.domain.model.User;
import dev.orion.bella.domain.model.UserMessage;
import dev.orion.bella.domain.port.in.ConversationUseCase;
import dev.orion.bella.domain.port.in.RouterUseCase;
import dev.orion.bella.domain.port.out.Repository;
import io.smallrye.mutiny.Multi;

/**
 * Application service that orchestrates the authenticated web conversation flow:
 * explicit, user-created conversations (as opposed to the WhatsApp channel's
 * inactivity-based chats, see {@link ChatService}), each with memory isolated by
 * conversation id.
 *
 * <p>Framework-agnostic (plain Java), wired by
 * {@code dev.orion.bella.adapter.config.ApplicationBeans}.
 *
 * @author Rodrigo Prestes Machado
 */
public class ConversationService implements ConversationUseCase {

    /** Repository used to load and store conversations. */
    private final Repository repository;

    /** Chooses the teacher or the administrative agent and retrieves the matching corpus. */
    private final RouterUseCase routerUseCase;

    /**
     * Creates the conversation service with its driven ports.
     *
     * @param repository       port used to persist conversations
     * @param routerUseCase routes the message and retrieves the matching corpus
     */
    public ConversationService(Repository repository, RouterUseCase routerUseCase) {
        this.repository = repository;
        this.routerUseCase = routerUseCase;
    }

    @Override
    public Chat createConversation(User user, String title) {
        Chat chat = Chat.start(user);
        chat.setTitle(title);
        repository.save(chat);
        return chat;
    }

    @Override
    public List<Chat> listConversations(String orionUserHash) {
        return repository.findAllByOrionUserHash(orionUserHash);
    }

    @Override
    public Optional<Chat> getConversation(String conversationId) {
        return repository.findConversationById(conversationId);
    }

    @Override
    public Chat getOwnedConversation(String conversationId, String orionUserHash) {
        return ownedConversation(conversationId, orionUserHash);
    }

    @Override
    public Chat renameConversation(String conversationId, String orionUserHash, String title) {
        Chat chat = ownedConversation(conversationId, orionUserHash);
        chat.setTitle(title);
        repository.save(chat);
        return chat;
    }

    @Override
    public void deleteConversation(String conversationId, String orionUserHash) {
        ownedConversation(conversationId, orionUserHash);
        repository.deleteConversation(conversationId);
    }

    @Override
    public Multi<String> chat(User user, String conversationId, String prompt) {
        Chat chat = ownedConversation(conversationId, user.getOrionUserHash());

        UserMessage userMessage = new UserMessage();
        userMessage.setUser(user);
        userMessage.setMessage(prompt);
        userMessage.setTimestamp(new Date());
        chat.addMessage(userMessage);
        repository.save(chat);

        StringBuilder buffer = new StringBuilder();

        return routerUseCase.answer(conversationId, prompt)
                .invoke(buffer::append)
                .onCompletion().invoke(() -> {
                    AgentMessage agentMessage = new AgentMessage();
                    agentMessage.setMessage(buffer.toString());
                    agentMessage.setTimestamp(new Date());
                    chat.addMessage(agentMessage);
                    repository.save(chat);
                });
    }

    /**
     * Loads the conversation and validates that it belongs to the given owner.
     *
     * @param conversationId the conversation id
     * @param orionUserHash  the Orion Users hash of the caller
     * @return the owned conversation
     * @throws NoSuchElementException if the conversation does not exist
     * @throws SecurityException      if the conversation belongs to a different owner
     */
    private Chat ownedConversation(String conversationId, String orionUserHash) {
        Chat chat = repository.findConversationById(conversationId)
                .orElseThrow(() -> new NoSuchElementException("Conversa não encontrada: " + conversationId));
        String owner = chat.getUser() != null ? chat.getUser().getOrionUserHash() : null;
        if (owner == null || !owner.equals(orionUserHash)) {
            throw new SecurityException("Acesso negado à conversa " + conversationId);
        }
        return chat;
    }

}
