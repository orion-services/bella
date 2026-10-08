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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import dev.orion.bella.domain.port.out.SkillActivation;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * In-memory {@link SkillActivation} shared by the tool worker thread and the
 * thread that saves the agent reply.
 *
 * <p>Request scope does not cross that worker thread, so the mark lives in an
 * application-scoped map keyed by conversation id.</p>
 *
 * @author Rodrigo Prestes Machado
 */
@ApplicationScoped
public class InMemorySkillActivation implements SkillActivation {

    private final ConcurrentMap<String, Boolean> activated = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void mark(String conversationId) {
        if (conversationId != null) {
            activated.put(conversationId, Boolean.TRUE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean consume(String conversationId) {
        return activated.remove(conversationId) != null;
    }
}
