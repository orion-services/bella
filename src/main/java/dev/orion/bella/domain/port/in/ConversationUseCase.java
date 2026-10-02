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
package dev.orion.bella.domain.port.in;

import java.util.List;
import java.util.Optional;

import dev.orion.bella.domain.model.Chat;
import dev.orion.bella.domain.model.User;
import io.smallrye.mutiny.Multi;

/**
 * Driving port for the authenticated (web/Orion Users) conversation flow: multiple
 * named conversations per user, each with its own isolated memory, as opposed to the
 * single inactivity-based chat used by the WhatsApp channel (see {@link ChatUseCase}).
 *
 * @author Rodrigo Prestes Machado
 */
public interface ConversationUseCase {

    /**
     * Creates a new, empty conversation for the given user.
     *
     * @param user  the authenticated user (must have an Orion Users hash)
     * @param title human-readable title for the conversation
     * @return the created conversation
     */
    Chat createConversation(User user, String title);

    /**
     * Lists all conversations owned by the given Orion Users hash, most recent first.
     *
     * @param orionUserHash the Orion Users hash that identifies the owner
     * @return the owner's conversations
     */
    List<Chat> listConversations(String orionUserHash);

    /**
     * Retrieves a single conversation by id.
     *
     * @param conversationId the conversation id
     * @return the conversation, or empty if not found
     */
    Optional<Chat> getConversation(String conversationId);

    /**
     * Retrieves a conversation by id when it belongs to the given owner.
     *
     * @param conversationId the conversation id
     * @param orionUserHash  the Orion Users hash of the caller, for ownership validation
     * @return the owned conversation
     * @throws java.util.NoSuchElementException if the conversation does not exist
     * @throws SecurityException                if the conversation belongs to a different owner
     */
    Chat getOwnedConversation(String conversationId, String orionUserHash);

    /**
     * Renames a conversation, if it belongs to the given owner.
     *
     * @param conversationId the conversation id
     * @param orionUserHash  the Orion Users hash of the caller, for ownership validation
     * @param title          the new title
     * @return the updated conversation
     */
    Chat renameConversation(String conversationId, String orionUserHash, String title);

    /**
     * Deletes a conversation, if it belongs to the given owner.
     *
     * @param conversationId the conversation id
     * @param orionUserHash  the Orion Users hash of the caller, for ownership validation
     */
    void deleteConversation(String conversationId, String orionUserHash);

    /**
     * Accepts a prompt within an existing conversation and streams the assistant's
     * RAG-grounded reply, isolating conversational memory per conversation id.
     *
     * @param user           the authenticated user
     * @param conversationId the target conversation, which must belong to the user
     * @param prompt         the student's message
     * @return a multi that emits the assistant response as plain text chunks
     */
    Multi<String> chat(User user, String conversationId, String prompt);

}
