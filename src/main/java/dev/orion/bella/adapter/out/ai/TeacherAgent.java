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
package dev.orion.bella.adapter.out.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.skills.Skills;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * LangChain4j AI service for the teacher agent: Bella's programming tutor for the
 * Construção de Páginas Web II course.
 *
 * <p>{@code toolProviderSupplier} is set so streaming tool calls, including
 * {@code activate_skill}, run on a worker thread. The Redis chat memory blocks, and
 * that call cannot happen on the Vert.x event loop.
 *
 * @author Rodrigo Prestes Machado
 */
@RegisterAiService(toolProviderSupplier = RegisterAiService.BeanIfExistsToolProviderSupplier.class)
@ApplicationScoped
@Skills("chiu")
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
