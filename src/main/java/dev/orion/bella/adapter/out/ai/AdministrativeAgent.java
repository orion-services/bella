/*
 * Copyright (c) 2026 Rodrigo Prestes Machado
 * All rights reserved.
 *
 * This source code is proprietary and confidential.
 * Unauthorized copying, modification, distribution, or use
 * of this software, via any medium, is strictly prohibited
 * without the express prior written permission of the copyright holder.
 */
package dev.rpmhub.adapter.out.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * LangChain4j AI service that answers administrative questions about the course
 * (PPC, academic calendar, rules) and shares conversational memory with the teacher agent.
 *
 * @author Rodrigo Prestes Machado
 */
@RegisterAiService
@ApplicationScoped
public interface AdministrativeAgent {

    /**
     * Streams a reply grounded in institutional documents.
     *
     * @param memoryId same conversation id used by {@link TeacherAgent}, so follow-up
     *                 turns stay in one memory
     * @param context  passages retrieved from the course corpus (may be empty)
     * @param prompt   the student message
     * @return a multi that emits the response chunks
     */
    @SystemMessage(fromResource = "/prompts/administrative.md")
    @UserMessage("Contexto: {context}\n\nPergunta: {prompt}")
    Multi<String> answer(@MemoryId String memoryId, String context, String prompt);
}
