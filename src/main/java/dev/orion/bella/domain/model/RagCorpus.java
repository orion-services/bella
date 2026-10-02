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
package dev.orion.bella.domain.model;

/**
 * Names of the RAG corpora stored side by side in the same embedding table.
 *
 * <p>Retrieval filters on this value so programming questions do not pull
 * institutional text, and course questions do not pull lesson pages.
 *
 * @author Rodrigo Prestes Machado
 */
public final class RagCorpus {

    /** Lesson pages and other programming material of the discipline. */
    public static final String DISCIPLINE = "discipline";

    /** Institutional documents: PPC, academic calendar and course rules. */
    public static final String COURSE = "course";

    private RagCorpus() {
    }
}
