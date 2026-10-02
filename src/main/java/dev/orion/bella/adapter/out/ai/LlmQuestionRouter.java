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
package dev.orion.bella.adapter.out.ai;

import java.util.Locale;

import dev.orion.bella.domain.model.Intention;
import dev.orion.bella.domain.port.out.QuestionRouter;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Classifies student messages with the chat model and falls back to the teacher agent
 * when the label is missing or the call fails.
 *
 * @author Rodrigo Prestes Machado
 */
@ApplicationScoped
public class LlmQuestionRouter implements QuestionRouter {

    private final IntentClassifier intentClassifier;

    /**
     * Creates the router with the stateless classifier.
     *
     * @param intentClassifier AI service that returns {@code COURSE} or {@code DISCIPLINE}
     */
    @Inject
    public LlmQuestionRouter(IntentClassifier intentClassifier) {
        this.intentClassifier = intentClassifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Intention classify(String question) {
        if (question == null || question.isBlank()) {
            return Intention.DISCIPLINE;
        }
        try {
            Intention intent = parse(intentClassifier.classify(question));
            Log.info("Question routed to " + intent);
            return intent;
        } catch (RuntimeException e) {
            Log.warn("Question routing failed; falling back to the teacher agent", e);
            return Intention.DISCIPLINE;
        }
    }

    /**
     * Maps the model output to an intent. Anything other than a reply that
     * starts with {@code COURSE} stays with the teacher agent.
     *
     * @param raw the classifier output, possibly with extra whitespace
     * @return the parsed intent
     */
    static Intention parse(String raw) {
        if (raw == null) {
            return Intention.DISCIPLINE;
        }
        String token = raw.trim().toUpperCase(Locale.ROOT);
        if (token.startsWith("COURSE")) {
            return Intention.COURSE;
        }
        return Intention.DISCIPLINE;
    }
}
