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
 * Domain model that represents a reply sent by the agent.
 *
 * @author Rodrigo Prestes Machado
 */
public class AgentMessage extends Message {

    /**
     * Assistant that produced this reply. Absent on replies stored before the
     * agent was recorded.
     */
    private AgentKind agent;

    /**
     * Whether the student copied this reply to the clipboard. Starts false.
     */
    private boolean copied;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return "AGENT";
    }

    /**
     * Returns the assistant that produced this reply.
     *
     * @return the agent kind, or {@code null} when it was not recorded
     */
    public AgentKind getAgent() {
        return agent;
    }

    /**
     * Sets the assistant that produced this reply.
     *
     * @param agent the agent kind to set
     */
    public void setAgent(AgentKind agent) {
        this.agent = agent;
    }

    /**
     * Returns whether this reply was copied to the clipboard.
     *
     * @return {@code true} when the reply was copied
     */
    public boolean isCopied() {
        return copied;
    }

    /**
     * Sets whether this reply was copied to the clipboard.
     *
     * @param copied {@code true} when the reply was copied
     */
    public void setCopied(boolean copied) {
        this.copied = copied;
    }

}
