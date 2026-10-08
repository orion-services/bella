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
package dev.orion.bella.adapter.in.rest;

import java.util.List;
import java.util.NoSuchElementException;

import dev.orion.bella.adapter.in.rest.dto.ChatbotRequest;
import dev.orion.bella.adapter.in.rest.dto.ConversationRequest;
import dev.orion.bella.adapter.in.rest.dto.MemoryResponse;
import dev.orion.bella.domain.model.Chat;
import dev.orion.bella.domain.model.User;
import dev.orion.bella.domain.port.in.ConversationUseCase;
import dev.orion.bella.domain.port.out.AuthPort;
import io.quarkus.logging.Log;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Multi;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST resource that exposes the authenticated (Orion Users) web conversation flow:
 * multiple named conversations per user, memory lookup and the streaming chatbot
 * endpoint used by the Vue frontend. Mirrors the contract of the RAG project's
 * {@code RagController}, adapted to the Bella domain.
 *
 * <p>All endpoints here require a valid Orion Users JWT (see
 * {@code mp.jwt.verify.*} in {@code application.properties} and {@link JwtAuthFilter}).
 * The legacy {@code /bella/chat} (phone-based) endpoint, used exclusively by the
 * WhatsApp channel, is untouched and lives in {@link BellaResource}.
 *
 * @author Rodrigo Prestes Machado
 */
@Path("/bella")
public class ConversationResource {

    /** Driving port used to manage and chat within conversations. */
    private final ConversationUseCase conversationUseCase;

    /** Port used to resolve the authenticated user from the request's JWT. */
    private final AuthPort authPort;

    /** JAX-RS request context, used to read the JWT stashed by {@link JwtAuthFilter}. */
    @Context
    ContainerRequestContext requestContext;

    /**
     * Creates the resource with its driving ports.
     *
     * @param conversationUseCase application port for conversation management and chat
     * @param authPort            port used to resolve the authenticated user from the JWT
     */
    @Inject
    public ConversationResource(ConversationUseCase conversationUseCase, AuthPort authPort) {
        this.conversationUseCase = conversationUseCase;
        this.authPort = authPort;
    }

    /**
     * Resolves the authenticated user from the JWT stored in the request context by
     * {@link JwtAuthFilter}.
     *
     * @return the authenticated user
     * @throws WebApplicationException with a 401 status if no JWT is present or invalid
     */
    private User authenticatedUser() {
        String jwtToken = (String) requestContext.getProperty("jwt.token");
        if (jwtToken == null) {
            throw new WebApplicationException("Token de autenticação não encontrado", Response.Status.UNAUTHORIZED);
        }
        try {
            return authPort.resolveUser(jwtToken);
        } catch (Exception e) {
            Log.warn("Failed to resolve user from JWT", e);
            throw new WebApplicationException("Token de autenticação inválido", Response.Status.UNAUTHORIZED);
        }
    }

    /**
     * Creates a new conversation for the authenticated user.
     *
     * @param userId  path variable (ignored; resolved from JWT)
     * @param request conversation creation request with title
     * @return the created conversation
     */
    @POST
    @Path("/users/{userId}/conversations")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    @Blocking
    public Chat createConversation(@PathParam("userId") String userId, @Valid ConversationRequest request) {
        User user = authenticatedUser();
        Log.info("Creating conversation for user: " + user.getOrionUserHash());
        return conversationUseCase.createConversation(user, request.title);
    }

    /**
     * Returns all conversations belonging to the authenticated user.
     *
     * @param userId path variable (ignored; resolved from JWT)
     * @return the user's conversations
     */
    @GET
    @Path("/users/{userId}/conversations")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    @Blocking
    public List<Chat> getUserConversations(@PathParam("userId") String userId) {
        User user = authenticatedUser();
        Log.info("Getting conversations for user: " + user.getOrionUserHash());
        return conversationUseCase.listConversations(user.getOrionUserHash());
    }

    /**
     * Retrieves a conversation by its unique identifier, if it belongs to the
     * authenticated user.
     *
     * @param conversationId the conversation's unique identifier
     * @return the matching conversation
     * @throws WebApplicationException with 404 when the conversation does not exist, or 403
     *         when it belongs to another user
     */
    @GET
    @Path("/conversations/{conversationId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    @Blocking
    public Chat getConversation(@PathParam("conversationId") String conversationId) {
        User user = authenticatedUser();
        Log.info("Getting conversation: " + conversationId);
        return handleOwnership(() ->
                conversationUseCase.getOwnedConversation(conversationId, user.getOrionUserHash()));
    }

    /**
     * Updates the title of a conversation owned by the authenticated user.
     *
     * @param conversationId conversation identifier
     * @param request        body with the new title
     * @return the updated conversation
     */
    @PATCH
    @Path("/conversations/{conversationId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    @Blocking
    public Chat updateConversation(@PathParam("conversationId") String conversationId,
            @Valid ConversationRequest request) {
        User user = authenticatedUser();
        Log.info("Updating conversation title: " + conversationId);
        return handleOwnership(() ->
                conversationUseCase.renameConversation(conversationId, user.getOrionUserHash(), request.title));
    }

    /**
     * Hides a conversation owned by the authenticated user.
     * The conversation and its messages stay stored for later analysis.
     *
     * @param conversationId the conversation to delete
     * @param userId         query param (used for logging only; authorisation is JWT-based)
     * @return HTTP 200 OK on success
     */
    @DELETE
    @Path("/conversations/{conversationId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    @Blocking
    public Response deleteConversation(@PathParam("conversationId") String conversationId,
            @QueryParam("userId") String userId) {
        User user = authenticatedUser();
        Log.info("Hiding conversation " + conversationId + " by user " + user.getOrionUserHash());
        handleOwnership(() -> {
            conversationUseCase.deleteConversation(conversationId, user.getOrionUserHash());
            return null;
        });
        return Response.ok().build();
    }

    /**
     * Returns the message history of a conversation owned by the authenticated user.
     *
     * @param userId         query param (ignored; resolved from JWT)
     * @param conversationId the conversation identifier
     * @return the conversation memory, or {@code null} when {@code conversationId} is missing
     * @throws WebApplicationException with 404 when the conversation does not exist, or 403
     *         when it belongs to another user
     */
    @GET
    @Path("/memory")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    @Blocking
    public MemoryResponse getMemory(@QueryParam("userId") String userId,
            @QueryParam("conversationId") String conversationId) {
        if (conversationId == null) {
            return null;
        }
        User user = authenticatedUser();
        Log.info("Memory Conversation: " + conversationId);
        Chat chat = handleOwnership(() ->
                conversationUseCase.getOwnedConversation(conversationId, user.getOrionUserHash()));
        return MemoryResponse.fromChat(chat);
    }

    /**
     * Streams a chatbot response for an authenticated user within a conversation.
     *
     * @param request the chatbot request containing the conversation ID and prompt
     * @return server-sent event stream of response tokens
     */
    @POST
    @Path("/chatbot")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RolesAllowed("user")
    @Blocking
    public Multi<String> chatbot(@Valid ChatbotRequest request) {
        User user;
        try {
            user = authenticatedUser();
        } catch (WebApplicationException e) {
            return Multi.createFrom().item("data: Erro: Token de autenticação não encontrado\n\n");
        }
        Log.info("Chatbot POST - Conversation: " + request.conversationId);
        try {
            return conversationUseCase.chat(user, request.conversationId, request.prompt)
                    .onFailure().recoverWithMulti(e -> {
                        String msg = e instanceof SecurityException
                                ? "Erro: Acesso negado à conversa"
                                : "Erro: " + (e.getMessage() != null ? e.getMessage() : "Erro desconhecido");
                        return Multi.createFrom().item("data: " + msg + "\n\n");
                    });
        } catch (SecurityException | NoSuchElementException e) {
            String msg = e instanceof SecurityException ? "Acesso negado à conversa" : e.getMessage();
            return Multi.createFrom().item("data: Erro: " + msg + "\n\n");
        }
    }

    /**
     * Marks an agent reply in a conversation owned by the authenticated user as copied.
     *
     * @param conversationId the conversation that contains the reply
     * @param sequence       zero-based position of the reply in the conversation
     * @return HTTP 204 No Content
     * @throws WebApplicationException with 404 when the conversation or the sequence does not
     *         exist, 403 when the conversation belongs to another user, or 400 when the
     *         sequence is not an agent reply
     */
    @PATCH
    @Path("/conversations/{conversationId}/messages/{sequence}/copied")
    @RolesAllowed("user")
    @Blocking
    public Response markMessageCopied(@PathParam("conversationId") String conversationId,
            @PathParam("sequence") int sequence) {
        User user = authenticatedUser();
        Log.info("Marking message " + sequence + " as copied in conversation " + conversationId);
        try {
            handleOwnership(() -> {
                conversationUseCase.markAgentMessageCopied(conversationId, user.getOrionUserHash(), sequence);
                return null;
            });
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(e.getMessage(), Response.Status.BAD_REQUEST);
        }
        return Response.noContent().build();
    }

    /**
     * Runs a conversation-owning operation, translating domain ownership failures into
     * the appropriate JAX-RS HTTP responses.
     *
     * @param <T>       the operation's result type
     * @param operation the operation to run
     * @return the operation's result
     */
    private <T> T handleOwnership(java.util.function.Supplier<T> operation) {
        try {
            return operation.get();
        } catch (NoSuchElementException e) {
            throw new WebApplicationException(e.getMessage(), Response.Status.NOT_FOUND);
        } catch (SecurityException e) {
            throw new WebApplicationException(e.getMessage(), Response.Status.FORBIDDEN);
        }
    }

}
