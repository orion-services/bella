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
package dev.orion.bella.adapter.config;

import dev.orion.bella.adapter.out.ai.AdministrativeAgent;
import dev.orion.bella.adapter.out.ai.TeacherAgent;
import dev.orion.bella.application.AssistantRouter;
import dev.orion.bella.application.ChatService;
import dev.orion.bella.application.ConversationService;
import dev.orion.bella.application.IngestService;
import dev.orion.bella.domain.port.in.ChatUseCase;
import dev.orion.bella.domain.port.in.ConversationUseCase;
import dev.orion.bella.domain.port.in.IngestDocumentsPort;
import dev.orion.bella.domain.port.in.RouterUseCase;
import dev.orion.bella.domain.port.out.EmbeddingRepository;
import dev.orion.bella.domain.port.out.IngestPort;
import dev.orion.bella.domain.port.out.QuestionRouter;
import dev.orion.bella.domain.port.out.Repository;
import dev.orion.bella.domain.port.out.WebScraperPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * CDI wiring for the application layer.
 *
 * <p>The {@code application} package is kept free of framework annotations so
 * its classes remain plain Java and easy to unit test. This class is the only
 * place responsible for instantiating application services and exposing them
 * as CDI beans through their driving ports.
 *
 * @author Rodrigo Prestes Machado
 */
@ApplicationScoped
public class ApplicationBeans {

    @Inject
    Repository chatRepository;

    /** Port for embedding-based retrieval, used by the RAG use cases. */
    @Inject
    EmbeddingRepository embeddingRepository;

    /** Port for document ingestion into the embedding store. */
    @Inject
    IngestPort ingestPort;

    /** Port used to scrape URLs into ingestible documents. */
    @Inject
    WebScraperPort webScraperPort;

    /** LangChain4j AI service that streams programming replies grounded in RAG context. */
    @Inject
    TeacherAgent teacherAgent;

    /** LangChain4j AI service that streams administrative course replies. */
    @Inject
    AdministrativeAgent administrativeAgent;

    /** Classifies a message as programming or administrative before retrieval. */
    @Inject
    QuestionRouter questionRouter;

    /** Number of context chunks retrieved per message. */
    @ConfigProperty(name = "rag.max-results", defaultValue = "3")
    int ragMaxResults;

    /** Minimum similarity score required for a retrieved chunk to be used as context. */
    @ConfigProperty(name = "rag.min-score", defaultValue = "0.6")
    double ragMinScore;

    /** Maximum idle time, in minutes, between user messages before a new chat session starts. */
    @ConfigProperty(name = "chat.inactivity-threshold-minutes", defaultValue = "30")
    long chatInactivityThresholdMinutes;

    /**
     * Produces the {@link ChatUseCase} bean backed by a plain {@link ChatService}.
     *
     * @return the chat use case implementation
     */
    @Produces
    @ApplicationScoped
    public ChatUseCase chatUseCase() {
        long inactivityThresholdMs = chatInactivityThresholdMinutes * 60_000L;
        return new ChatService(chatRepository, routerUseCase(), inactivityThresholdMs);
    }

    /**
     * Produces the {@link ConversationUseCase} bean backed by a plain
     * {@link ConversationService}, used by the authenticated web chat flow.
     *
     * @return the conversation use case implementation
     */
    @Produces
    @ApplicationScoped
    public ConversationUseCase conversationUseCase() {
        return new ConversationService(chatRepository, routerUseCase());
    }

    private RouterUseCase routerUseCase() {
        return new AssistantRouter(embeddingRepository, teacherAgent, administrativeAgent, questionRouter,
                ragMaxResults, ragMinScore);
    }

    /**
     * Produces the {@link IngestDocumentsPort} bean backed by a plain {@link IngestService}.
     *
     * @return the local-directory ingestion use case implementation
     */
    @Produces
    @ApplicationScoped
    public IngestDocumentsPort ingestDocumentsPort() {
        return new IngestService(ingestPort, webScraperPort);
    }

}
