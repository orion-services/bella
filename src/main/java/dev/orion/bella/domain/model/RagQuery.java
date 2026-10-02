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
