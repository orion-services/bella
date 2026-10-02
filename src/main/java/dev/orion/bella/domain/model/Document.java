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
