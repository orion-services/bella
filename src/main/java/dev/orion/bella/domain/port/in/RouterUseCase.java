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
package dev.orion.bella.domain.port.in;

import io.smallrye.mutiny.Multi;

/**
 * Driving port that answers a student message by choosing the teacher or the
 * administrative agent and grounding the reply in the matching RAG corpus.
 *
 * @author Rodrigo Prestes Machado
 */
public interface RouterUseCase {

    /**
     * Classifies the prompt, retrieves context from the corresponding corpus
     * and streams the chosen assistant's reply.
     *
     * @param memoryId conversation id shared by both assistants
     * @param prompt   the student message
     * @return a multi that emits the assistant response as plain text chunks
     */
    Multi<String> answer(String memoryId, String prompt);
}
