/*
 * Copyright (c) 2026 Rodrigo Prestes Machado
 * All rights reserved.
 *
 * This source code is proprietary and confidential.
 * Unauthorized copying, modification, distribution, or use
 * of this software, via any medium, is strictly prohibited
 * without the express prior written permission of the copyright holder.
 */
package dev.rpmhub.domain.port.out;

import java.util.List;
import java.util.Optional;

import dev.rpmhub.domain.model.Chat;

/**
 * Driven port for persisting and loading chat sessions.
 *
 * @author Rodrigo Prestes Machado
 */
public interface Repository {

    /**
     * Finds the most recent chat for the user identified by phone number.
     *
     * @param phoneNumber the user phone number
     * @return the last chat, or empty when the user has no chat yet
     */
    Optional<Chat> findLastByPhone(String phoneNumber);

    /**
     * Saves the given chat as the last active chat for its owner.
     *
     * @param chat the chat to persist
     */
    void save(Chat chat);

    /**
     * Finds a chat/conversation by its unique identifier.
     *
     * @param id the chat id
     * @return the chat, or empty when no chat with that id exists
     */
    Optional<Chat> findConversationById(String id);

    /**
     * Finds all chats/conversations owned by the given Orion Users hash, most recently
     * started first.
     *
     * @param orionUserHash the Orion Users hash that identifies the owner
     * @return the owner's chats, in reverse chronological order
     */
    List<Chat> findAllByOrionUserHash(String orionUserHash);

    /**
     * Deletes the chat/conversation with the given identifier, if it exists.
     *
     * @param id the chat id to delete
     */
    void deleteConversation(String id);

}
