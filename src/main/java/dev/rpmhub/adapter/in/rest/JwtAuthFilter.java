/*
 * Copyright (c) 2026 Rodrigo Prestes Machado
 * All rights reserved.
 *
 * This source code is proprietary and confidential.
 * Unauthorized copying, modification, distribution, or use
 * of this software, via any medium, is strictly prohibited
 * without the express prior written permission of the copyright holder.
 */
package dev.rpmhub.adapter.in.rest;

import io.quarkus.logging.Log;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;

/**
 * Extracts the raw Bearer JWT (already verified by SmallRye JWT, see
 * {@code mp.jwt.verify.*} in {@code application.properties}) and stashes it in the
 * request context so downstream resources can decode its claims (Orion Users hash,
 * email) without re-parsing the {@code Authorization} header themselves.
 *
 * @author Rodrigo Prestes Machado
 */
@Provider
public class JwtAuthFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwtToken = authHeader.substring(7);
            requestContext.setProperty("jwt.token", jwtToken);
            Log.debug("JWT token stored in request context");
        }
    }

}
