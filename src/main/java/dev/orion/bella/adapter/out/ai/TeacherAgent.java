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
 * LangChain4j AI service for the teacher agent: Bella's programming tutor for the
 * Construção de Páginas Web II course.
 *
 * @author Rodrigo Prestes Machado
 */
@RegisterAiService
@ApplicationScoped
public interface TeacherAgent {

    /**
     * Streams a teacher reply grounded in the conversation and optional RAG context.
     *
     * @param memoryId stable identifier of the conversation, used to isolate conversational memory.
     *                 On WhatsApp this is the chat id (a new id after 30 minutes of inactivity);
     *                 on the web it is the conversation id chosen by the user
     * @param context  relevant passages retrieved from the vector store (may be empty)
     * @param prompt   the student message
     * @return a multi that emits the response chunks
     */
    @SystemMessage(fromResource = "/prompts/teacher.md")
    @UserMessage("Contexto: {context}\n\nPergunta: {prompt}")
    Multi<String> answer(@MemoryId String memoryId, String context, String prompt);

}
