/*
 * Copyright (c) 2026 Rodrigo Prestes Machado
 * All rights reserved.
 *
 * This source code is proprietary and confidential.
 * Unauthorized copying, modification, distribution, or use
 * of this software, via any medium, is strictly prohibited
 * without the express prior written permission of the copyright holder.
 */
package dev.rpmhub.domain.port.in;

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
