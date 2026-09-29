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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import dev.rpmhub.adapter.out.ai.TwrAgent;
import dev.rpmhub.domain.model.AgentMessage;
import dev.rpmhub.domain.model.Chat;
import dev.rpmhub.domain.model.RagQuery;
import dev.rpmhub.domain.model.RagResponse;
import dev.rpmhub.domain.model.User;
import dev.rpmhub.domain.model.UserMessage;
import dev.rpmhub.domain.port.in.ConversationUseCase;
import dev.rpmhub.domain.port.out.EmbeddingRepository;
import dev.rpmhub.domain.port.out.Repository;
import io.smallrye.mutiny.Multi;

/**
 * Application service that orchestrates the authenticated web conversation flow:
 * explicit, user-created conversations (as opposed to the WhatsApp channel's
 * inactivity-based chats, see {@link ChatService}), each with memory isolated by
 * conversation id.
 *
 * <p>Framework-agnostic (plain Java), wired by
 * {@code dev.rpmhub.adapter.config.ApplicationBeans}.
 *
 * @author Rodrigo Prestes Machado
 */
public class ConversationService implements ConversationUseCase {

    /** Fallback context string used when no relevant chunk is found. */
    private static final String DEFAULT_CONTEXT = "";

    /** Repository used to load and store conversations. */
    private final Repository repository;

    /** Repository used to search embedding chunks relevant to the message. */
    private final EmbeddingRepository embeddingRepository;

    /** AI service used to generate a streaming reply grounded in retrieved context. */
    private final TwrAgent twrAgent;

    /** Number of context chunks retrieved per message ({@code rag.max-results}). */
    private final int maxResults;

    /**
     * Minimum similarity score required for a retrieved chunk to be used as context
     * ({@code rag.min-score}).
     */
    private final double minScore;

    /**
     * Creates the conversation service with its driven ports.
     *
     * @param repository           port used to persist conversations
     * @param embeddingRepository  port for vector-similarity search
     * @param twrAgent             AI service used to generate contextual replies
     * @param maxResults           number of context chunks retrieved per message
     * @param minScore             minimum similarity score required for a retrieved chunk
     */
    public ConversationService(Repository repository, EmbeddingRepository embeddingRepository,
            TwrAgent twrAgent, int maxResults, double minScore) {
        this.repository = repository;
        this.embeddingRepository = embeddingRepository;
        this.twrAgent = twrAgent;
        this.maxResults = maxResults;
        this.minScore = minScore;
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

        RagQuery query = new RagQuery(prompt, maxResults, minScore);
        RagResponse ragResponse = embeddingRepository.searchChunks(query);
        String context = ragResponse.getContexts().isEmpty()
                ? DEFAULT_CONTEXT : String.join("\n\n", ragResponse.getContexts());

        StringBuilder buffer = new StringBuilder();

        return twrAgent.answer(conversationId, context, prompt)
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
