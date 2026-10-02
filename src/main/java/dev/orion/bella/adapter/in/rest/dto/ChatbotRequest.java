/*
 * Copyright (c) 2026 Rodrigo Prestes Machado
 * All rights reserved.
 *
 * This source code is proprietary and confidential.
 * Unauthorized copying, modification, distribution, or use
 * of this software, via any medium, is strictly prohibited
 * without the express prior written permission of the copyright holder.
 */
package dev.rpmhub.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body used to send a prompt within an existing conversation.
 *
 * @author Rodrigo Prestes Machado
 */
public class ChatbotRequest {

    /** Identifier of the target conversation. */
    @NotBlank
    public String conversationId;

    /** Text prompt sent by the student. */
    @NotBlank
    public String prompt;

}
