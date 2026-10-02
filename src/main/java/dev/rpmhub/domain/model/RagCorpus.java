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
