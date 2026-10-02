/*
 * Copyright (c) 2026 Rodrigo Prestes Machado
 * All rights reserved.
 *
 * This source code is proprietary and confidential.
 * Unauthorized copying, modification, distribution, or use
 * of this software, via any medium, is strictly prohibited
 * without the express prior written permission of the copyright holder.
 */
package dev.rpmhub.adapter.out.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Stateless classifier used by {@link LlmQuestionRouter}.
 *
 * <p>No {@code @MemoryId}: a classification must not be written into the
 * student conversation.
 *
 * @author Rodrigo Prestes Machado
 */
@RegisterAiService
@ApplicationScoped
public interface IntentClassifier {

    /**
     * Classifies a student message as {@code COURSE} or {@code DISCIPLINE}.
     *
     * @param question the raw student message
     * @return the single-word label produced by the model
     */
    @SystemMessage(fromResource = "/prompts/router.md")
    @UserMessage("{question}")
    String classify(String question);
}
