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

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * AI service that scores whether a student message is a prompt injection.
 *
 * <p>It has no tools and no chat memory, so a malicious message cannot call
 * the teacher's functions or ride on a previous conversation.
 *
 * @author Rodrigo Prestes Machado
 */
@RegisterAiService(chatMemoryProviderSupplier = RegisterAiService.NoChatMemoryProviderSupplier.class)
@ApplicationScoped
public interface PromptInjectionDetectionService {

    /**
     * Scores how likely the student message is a prompt injection.
     *
     * @param userQuery the student message, without the RAG context
     * @return a decimal between 0.0 and 1.0; the model may add surrounding text
     */
    @SystemMessage(fromResource = "/prompts/prompt-injection-system.txt")
    @UserMessage(fromResource = "/prompts/prompt-injection-user.txt")
    String score(String userQuery);
}
