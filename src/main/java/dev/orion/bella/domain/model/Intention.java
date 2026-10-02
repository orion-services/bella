/*
 * Copyright (c) 2026 Rodrigo Prestes Machado
 * All rights reserved.
 *
 * This source code is proprietary and confidential.
 * Unauthorized copying, modification, distribution, or use
 * of this software, via any medium, is strictly prohibited
 * without the express prior written permission of the copyright holder.
 */
package dev.rpmhub.domain.model;

/**
 * Which assistant should answer a student message.
 *
 * @author Rodrigo Prestes Machado
 */
public enum Intention {

    /** Programming and discipline content, answered by the teacher agent. */
    DISCIPLINE(RagCorpus.DISCIPLINE),

    /** Administrative and institutional questions, answered by the administrative agent. */
    COURSE(RagCorpus.COURSE);

    private final String corpus;

    Intention(String corpus) {
        this.corpus = corpus;
    }

    /**
     * Embedding metadata value used to retrieve context for this intention.
     *
     * @return the corpus name stored on ingested chunks
     */
    public String corpus() {
        return corpus;
    }
}
