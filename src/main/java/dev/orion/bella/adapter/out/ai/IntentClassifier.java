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
 * Stateless classifier used by {@link LlmQuestionRouter}.
 *
 * <p>No {@code @MemoryId}: a classification must not be written into the
 * student conversation.
 *
 * @author Rodrigo Prestes Machado
 */
@RegisterAiService
@ApplicationScoped
public interface IntentClassifier {

    /**
     * Classifies a student message as {@code COURSE} or {@code DISCIPLINE}.
     *
     * @param question the raw student message
     * @return the single-word label produced by the model
     */
    @SystemMessage(fromResource = "/prompts/router.md")
    @UserMessage("{question}")
    String classify(String question);
}
