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

import dev.orion.bella.adapter.out.ai.AdministrativeAgent;
import dev.orion.bella.adapter.out.ai.TeacherAgent;
import dev.orion.bella.domain.model.Intention;
import dev.orion.bella.domain.model.RagQuery;
import dev.orion.bella.domain.model.RagResponse;
import dev.orion.bella.domain.port.in.RouterUseCase;
import dev.orion.bella.domain.port.out.EmbeddingRepository;
import dev.orion.bella.domain.port.out.QuestionRouter;
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
