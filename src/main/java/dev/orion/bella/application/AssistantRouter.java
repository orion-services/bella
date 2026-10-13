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

import java.util.function.Supplier;

import dev.langchain4j.guardrail.InputGuardrailException;
import io.smallrye.mutiny.Multi;

import dev.orion.bella.adapter.out.ai.AdministrativeAgent;
import dev.orion.bella.adapter.out.ai.TeacherAgent;
import dev.orion.bella.domain.model.AgentKind;
import dev.orion.bella.domain.model.Intention;
import dev.orion.bella.domain.model.RagQuery;
import dev.orion.bella.domain.model.RagResponse;
import dev.orion.bella.domain.port.in.RoutedAnswer;
import dev.orion.bella.domain.port.in.RouterUseCase;
import dev.orion.bella.domain.port.out.EmbeddingRepository;
import dev.orion.bella.domain.port.out.QuestionRouter;

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

    /**
     * Reply returned when the input guardrail rejects the student message.
     */
    static final String BLOCKED_REPLY = "Desculpe, não posso atender essa mensagem. "
            + "Posso ajudar com programação da disciplina ou com informações do curso.";

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
    public RoutedAnswer answer(String memoryId, String prompt) {
        Intention intent = questionRouter.classify(prompt);
        RagQuery query = new RagQuery(prompt, maxResults, minScore, intent.corpus());
        RagResponse ragResponse = embeddingRepository.searchChunks(query);
        String context = ragResponse.getContexts().isEmpty()
                ? DEFAULT_CONTEXT : String.join("\n\n", ragResponse.getContexts());

        if (intent == Intention.COURSE) {
            return new RoutedAnswer(AgentKind.ADMINISTRATIVE,
                    guarded(() -> administrativeAgent.answer(memoryId, context, prompt)));
        }
        return new RoutedAnswer(AgentKind.DISCIPLINE,
                guarded(() -> teacherAgent.answer(memoryId, context, prompt)));
    }

    /**
     * Turns a rejected message into the fixed reply, whether the guardrail fails the stream
     * or throws before the stream starts. Other failures keep propagating.
     *
     * @param call invocation of an assistant
     * @return the assistant stream, or a single blocked reply when the input guardrail rejects it
     */
    private static Multi<String> guarded(Supplier<Multi<String>> call) {
        try {
            return call.get()
                    .onFailure(AssistantRouter::causedByInputGuardrail)
                    .recoverWithItem(BLOCKED_REPLY);
        } catch (RuntimeException exception) {
            if (causedByInputGuardrail(exception)) {
                return Multi.createFrom().item(BLOCKED_REPLY);
            }
            throw exception;
        }
    }

    private static boolean causedByInputGuardrail(Throwable failure) {
        for (Throwable current = failure; current != null; current = current.getCause()) {
            if (current instanceof InputGuardrailException) {
                return true;
            }
        }
        return false;
    }
}
