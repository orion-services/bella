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
package dev.orion.bella.domain.port.out;

/**
 * Driven port that remembers, for one conversation turn, that a skill was activated.
 *
 * <p>The AI adapter marks the conversation when {@code activate_skill} runs. The
 * application consumes that mark when the agent reply is saved, so the flag does
 * not leak into the next reply of the same conversation.</p>
 *
 * @author Rodrigo Prestes Machado
 */
public interface SkillActivation {

    /**
     * Records that the current turn of this conversation activated a skill.
     *
     * @param conversationId chat id used as the assistant memory id
     */
    void mark(String conversationId);

    /**
     * Returns whether this conversation had a skill activation pending, and clears it.
     *
     * @param conversationId chat id used as the assistant memory id
     * @return {@code true} when a skill was marked and had not been consumed yet
     */
    boolean consume(String conversationId);
}
