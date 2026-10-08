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
package dev.orion.bella.domain.port.out;

import java.util.List;
import java.util.Optional;

import dev.orion.bella.domain.model.Chat;

/**
 * Driven port for persisting and loading chat sessions.
 *
 * @author Rodrigo Prestes Machado
 */
public interface Repository {

    /**
     * Finds the most recent visible chat for the user identified by phone number.
     * Conversations hidden by a logical delete are ignored.
     *
     * @param phoneNumber the user phone number
     * @return the last visible chat, or empty when the user has no visible chat yet
     */
    Optional<Chat> findLastByPhone(String phoneNumber);

    /**
     * Saves the given chat as the last active chat for its owner.
     *
     * @param chat the chat to persist
     */
    void save(Chat chat);

    /**
     * Finds a visible chat/conversation by its unique identifier.
     * A logically deleted conversation is treated as absent.
     *
     * @param id the chat id
     * @return the chat, or empty when no visible chat with that id exists
     */
    Optional<Chat> findConversationById(String id);

    /**
     * Finds all visible chats/conversations owned by the given Orion Users hash, most
     * recently started first. Logically deleted conversations are omitted.
     *
     * @param orionUserHash the Orion Users hash that identifies the owner
     * @return the owner's visible chats, in reverse chronological order
     */
    List<Chat> findAllByOrionUserHash(String orionUserHash);

    /**
     * Hides the chat/conversation with the given identifier, if it exists.
     * The chat row and its messages stay stored for later analysis.
     *
     * @param id the chat id to hide
     */
    void deleteConversation(String id);

}
