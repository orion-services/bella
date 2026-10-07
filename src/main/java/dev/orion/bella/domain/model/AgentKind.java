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
 * Which assistant produced an {@link AgentMessage}.
 *
 * <p>This is the persisted identity of the reply. It is not the routing
 * {@link Intention}: a course question ({@link Intention#COURSE}) is answered
 * by the administrative agent and stored as {@link #ADMINISTRATIVE}.</p>
 *
 * @author Rodrigo Prestes Machado
 */
public enum AgentKind {

    /** Programming and discipline content, produced by the teacher agent. */
    DISCIPLINE,

    /** Administrative and institutional content, produced by the administrative agent. */
    ADMINISTRATIVE
}
