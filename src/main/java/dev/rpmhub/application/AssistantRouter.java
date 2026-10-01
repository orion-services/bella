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

import dev.rpmhub.adapter.out.ai.AdministrativeAgent;
import dev.rpmhub.adapter.out.ai.TeacherAgent;
import dev.rpmhub.domain.model.Intention;
import dev.rpmhub.domain.model.RagQuery;
import dev.rpmhub.domain.model.RagResponse;
import dev.rpmhub.domain.port.in.RouterUseCase;
import dev.rpmhub.domain.port.out.EmbeddingRepository;
import dev.rpmhub.domain.port.out.QuestionRouter;
import io.smallrye.mutiny.Multi;

/**
 * Picks the teacher or the administrative agent and retrieves context only from that corpus.
 *
 * <p>Both agents are called with the same memory id, so a follow-up stays in
 * the conversation the student already has.
 *
 * @author Rodrigo Prestes Machado
 */
public class AssistantRouter implements RouterUseCase {

    private static final String DEFAULT_CONTEXT = "";

    private final EmbeddingRepository embeddingRepository;
    private final TeacherAgent teacherAgent;
    private final AdministrativeAgent administrativeAgent;
    private final QuestionRouter questionRouter;
    private final int maxResults;
    private final double minScore;

    /**
     * Creates the router with the retrieval and assistant collaborators.
     *
     * @param embeddingRepository port for vector-similarity search
     * @param teacherAgent         assistant for programming questions
     * @param administrativeAgent  assistant for administrative course questions
     * @param questionRouter       classifier that chooses the assistant
     * @param maxResults           number of context chunks retrieved per message
     * @param minScore             minimum similarity score required for a chunk
     */
    public AssistantRouter(EmbeddingRepository embeddingRepository, TeacherAgent teacherAgent,
            AdministrativeAgent administrativeAgent, QuestionRouter questionRouter, int maxResults, double minScore) {
        this.embeddingRepository = embeddingRepository;
        this.teacherAgent = teacherAgent;
        this.administrativeAgent = administrativeAgent;
        this.questionRouter = questionRouter;
        this.maxResults = maxResults;
        this.minScore = minScore;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Multi<String> answer(String memoryId, String prompt) {
        Intention intent = questionRouter.classify(prompt);
        RagQuery query = new RagQuery(prompt, maxResults, minScore, intent.corpus());
        RagResponse ragResponse = embeddingRepository.searchChunks(query);
        String context = ragResponse.getContexts().isEmpty()
                ? DEFAULT_CONTEXT : String.join("\n\n", ragResponse.getContexts());

        if (intent == Intention.COURSE) {
            return administrativeAgent.answer(memoryId, context, prompt);
        }
        return teacherAgent.answer(memoryId, context, prompt);
    }
}
