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
package dev.orion.bella.domain.port.in;

import dev.orion.bella.domain.model.AgentKind;
import io.smallrye.mutiny.Multi;

/**
 * Assistant reply chosen by {@link RouterUseCase}: the agent that will produce
 * the text, and the stream of plain-text chunks.
 *
 * @author Rodrigo Prestes Machado
 */
public final class RoutedAnswer {

    /** Agent that produces the chunks. */
    private final AgentKind agent;

    /** Plain-text chunks of the reply. */
    private final Multi<String> chunks;

    /**
     * Creates a routed answer.
     *
     * @param agent  the agent that produces the reply
     * @param chunks the reply stream
     */
    public RoutedAnswer(AgentKind agent, Multi<String> chunks) {
        this.agent = agent;
        this.chunks = chunks;
    }

    /**
     * Returns the agent that produces this reply.
     *
     * @return the agent kind
     */
    public AgentKind getAgent() {
        return agent;
    }

    /**
     * Returns the reply stream.
     *
     * @return plain-text chunks
     */
    public Multi<String> getChunks() {
        return chunks;
    }
}
