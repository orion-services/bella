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
 * Document to be ingested into the embedding store.
 *
 * @author Rodrigo Prestes Machado
 */
public class Document {

    /** Full textual content of the document to be embedded. */
    private final String text;
    /** Origin of the document, such as a file path or a URL. */
    private final String source;
    /** Corpus this document belongs to, such as {@link RagCorpus#DISCIPLINE}. */
    private final String corpus;

    /**
     * Creates a document in the discipline corpus.
     *
     * @param text   full textual content of the document
     * @param source origin of the document (file path, URL, etc.)
     */
    public Document(String text, String source) {
        this(text, source, RagCorpus.DISCIPLINE);
    }

    /**
     * Creates a document with an explicit corpus.
     *
     * @param text   full textual content of the document
     * @param source origin of the document (file path, URL, etc.)
     * @param corpus corpus name stored as chunk metadata
     */
    public Document(String text, String source, String corpus) {
        this.text = text;
        this.source = source;
        this.corpus = corpus;
    }

    public String getText() {
        return text;
    }

    public String getSource() {
        return source;
    }

    public String getCorpus() {
        return corpus;
    }
}
