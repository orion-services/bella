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

import java.util.OptionalDouble;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Input guardrail that stops a prompt injection before Bella answers or calls tools.
 *
 * <p>A second model scores only the student message. A score above {@code 0.7}
 * rejects the message. If the score cannot be read, the message is allowed so a
 * scoring failure does not stop the chat.
 *
 * @author Rodrigo Prestes Machado
 */
@ApplicationScoped
public class PromptInjectionGuard implements InputGuardrail {

    /**
     * Scores at or below this value are treated as safe.
     */
    private static final double REJECTION_THRESHOLD = 0.7;

    /**
     * Marker that the teacher and the administrative agent place immediately before the
     * student text. The RAG context is not scored.
     */
    static final String STUDENT_MESSAGE_MARKER = "Mensagem do estudante: ";

    /**
     * First decimal from 0 to 1 found in the detector reply.
     */
    private static final Pattern SCORE = Pattern.compile("\\b(0(?:\\.\\d+)?|1(?:\\.0+)?)\\b");

    /**
     * Model that scores the student message.
     */
    private final PromptInjectionDetectionService detectionService;

    /**
     * Creates the guardrail with the injection detector.
     *
     * @param detectionService model that scores the student message
     */
    public PromptInjectionGuard(PromptInjectionDetectionService detectionService) {
        this.detectionService = detectionService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        String studentMessage = studentMessage(userMessage.singleText());
        String rawScore;
        try {
            rawScore = detectionService.score(studentMessage);
        } catch (RuntimeException exception) {
            Log.warn("Prompt injection check failed; allowing the message", exception);
            return success();
        }
        OptionalDouble score = parseScore(rawScore);
        if (score.isEmpty()) {
            Log.warn("Prompt injection score could not be read; allowing the message");
            return success();
        }
        if (score.getAsDouble() > REJECTION_THRESHOLD) {
            Log.warnf("Prompt injection blocked (score %.2f)", score.getAsDouble());
            return failure("Prompt injection detected");
        }
        return success();
    }

    private static String studentMessage(String userMessage) {
        int marker = userMessage.indexOf(STUDENT_MESSAGE_MARKER);
        if (marker < 0) {
            return userMessage;
        }
        return userMessage.substring(marker + STUDENT_MESSAGE_MARKER.length()).trim();
    }

    private static OptionalDouble parseScore(String text) {
        if (text == null || text.isBlank()) {
            return OptionalDouble.empty();
        }
        Matcher matcher = SCORE.matcher(text);
        if (!matcher.find()) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(Double.parseDouble(matcher.group(1)));
    }
}
