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
package dev.orion.bella.domain.port.out;

import dev.orion.bella.domain.model.Intention;

/**
 * Driven port that decides whether a student message is about programming
 * or about the course as an institution.
 *
 * @author Rodrigo Prestes Machado
 */
public interface QuestionRouter {

    /**
     * Classifies the student message.
     *
     * @param question the raw student message
     * @return the intent used to pick the assistant and the RAG corpus
     */
    Intention classify(String question);
}
