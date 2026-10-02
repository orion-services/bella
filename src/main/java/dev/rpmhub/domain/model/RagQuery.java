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
 * Domain representation of a RAG query — pure Java, no framework annotations.
 *
 * @author Rodrigo Prestes Machado
 */
public class RagQuery {

    /** Natural-language question to be converted into an embedding and searched. */
    private final String query;
    /** Maximum number of embedding chunks to retrieve from the vector store. */
    private final int maxResults;
    /** Minimum cosine-similarity score required for a chunk to be included. */
    private final double minScore;
    /**
     * Corpus metadata value that retrieved chunks must match, or {@code null}
     * to search every chunk.
     */
    private final String corpus;

    /**
     * Creates a RagQuery that searches every corpus.
     *
     * @param query      natural-language question text
     * @param maxResults maximum number of chunks to return
     * @param minScore   minimum similarity score threshold (0.0 to 1.0)
     */
    public RagQuery(String query, int maxResults, double minScore) {
        this(query, maxResults, minScore, null);
    }

    /**
     * Creates a RagQuery restricted to one corpus.
     *
     * @param query      natural-language question text
     * @param maxResults maximum number of chunks to return
     * @param minScore   minimum similarity score threshold (0.0 to 1.0)
     * @param corpus     corpus metadata value, or {@code null} to search every chunk
     */
    public RagQuery(String query, int maxResults, double minScore, String corpus) {
        this.query = query;
        this.maxResults = maxResults;
        this.minScore = minScore;
        this.corpus = corpus;
    }

    public String getQuery() {
        return query;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public double getMinScore() {
        return minScore;
    }

    public String getCorpus() {
        return corpus;
    }
}
