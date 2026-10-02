/*
 * Copyright (c) 2026 Rodrigo Prestes Machado
 * All rights reserved.
 *
 * This source code is proprietary and confidential.
 * Unauthorized copying, modification, distribution, or use
 * of this software, via any medium, is strictly prohibited
 * without the express prior written permission of the copyright holder.
 */
package dev.rpmhub.domain.port.out;

import dev.rpmhub.domain.model.Intention;

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
