/*
 * Copyright (c) 2026 Rodrigo Prestes Machado
 * All rights reserved.
 *
 * This source code is proprietary and confidential.
 * Unauthorized copying, modification, distribution, or use
 * of this software, via any medium, is strictly prohibited
 * without the express prior written permission of the copyright holder.
 */
package dev.rpmhub.adapter.in.rest.dto;

import java.util.List;

import dev.rpmhub.domain.model.Chat;
import dev.rpmhub.domain.model.Message;

/**
 * Response body for {@code GET /twr/memory}: the persisted message history of a
 * conversation, mirroring the shape the frontend expects from the RAG chatbot memory
 * endpoint.
 *
 * @author Rodrigo Prestes Machado
 */
public class MemoryResponse {

    /** Orion Users hash of the conversation owner. */
    public String userId;

    /** Identifier of the conversation this memory belongs to. */
    public String conversationId;

    /** Messages in chronological order. */
    public List<Message> messages;

    /**
     * Builds a memory response from the given conversation.
     *
     * @param chat the conversation to expose
     * @return the memory response
     */
    public static MemoryResponse fromChat(Chat chat) {
        MemoryResponse response = new MemoryResponse();
        response.userId = chat.getUser() != null ? chat.getUser().getOrionUserHash() : null;
        response.conversationId = chat.getId();
        response.messages = chat.getMessages();
        return response;
    }

}
