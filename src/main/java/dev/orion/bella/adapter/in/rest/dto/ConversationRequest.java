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
 * Request body used to create or rename a conversation.
 *
 * @author Rodrigo Prestes Machado
 */
public class ConversationRequest {

    /** Human-readable title for the conversation. */
    @NotBlank
    public String title;

}
