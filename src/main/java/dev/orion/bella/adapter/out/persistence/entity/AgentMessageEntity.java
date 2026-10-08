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
package dev.orion.bella.adapter.out.persistence.entity;

import dev.orion.bella.domain.model.AgentKind;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * JPA entity that persists an agent reply inside a chat.
 *
 * <p>The columns live on the shared {@code message} table
 * ({@link jakarta.persistence.InheritanceType#SINGLE_TABLE}). User rows leave
 * {@code agent} null and rely on the database default for {@code copied} and
 * {@code skill_activated}.</p>
 *
 * @author Rodrigo Prestes Machado
 */
@Entity
@DiscriminatorValue("AGENT")
public class AgentMessageEntity extends MessageEntity {

    /**
     * Whether the reply was copied to the clipboard.
     */
    @Column(name = "copied", nullable = false, columnDefinition = "boolean not null default false")
    private boolean copied;

    /**
     * Whether the reply activated a skill during the turn.
     */
    @Column(name = "skill_activated", nullable = false, columnDefinition = "boolean not null default false")
    private boolean skillActivated;

    /**
     * Assistant that produced the reply. Null for replies stored before the agent was recorded.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "agent", length = 32)
    private AgentKind agent;

    /**
     * Returns whether the reply was copied to the clipboard.
     *
     * @return {@code true} when the reply was copied
     */
    public boolean isCopied() {
        return copied;
    }

    /**
     * Sets whether the reply was copied to the clipboard.
     *
     * @param copied {@code true} when the reply was copied
     */
    public void setCopied(boolean copied) {
        this.copied = copied;
    }

    /**
     * Returns whether the reply activated a skill during the turn.
     *
     * @return {@code true} when a skill was activated
     */
    public boolean isSkillActivated() {
        return skillActivated;
    }

    /**
     * Sets whether the reply activated a skill during the turn.
     *
     * @param skillActivated {@code true} when a skill was activated
     */
    public void setSkillActivated(boolean skillActivated) {
        this.skillActivated = skillActivated;
    }

    /**
     * Returns the assistant that produced the reply.
     *
     * @return the agent kind, or {@code null} when it was not recorded
     */
    public AgentKind getAgent() {
        return agent;
    }

    /**
     * Sets the assistant that produced the reply.
     *
     * @param agent the agent kind to set
     */
    public void setAgent(AgentKind agent) {
        this.agent = agent;
    }

}
