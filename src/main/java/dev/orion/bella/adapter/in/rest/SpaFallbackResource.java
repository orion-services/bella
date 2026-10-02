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

import java.io.IOException;
import java.io.InputStream;

import io.quarkus.logging.Log;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Serves {@code index.html} for any client-side (Vue Router) route that has no matching
 * static asset or REST resource, so that deep links and page refreshes on routes like
 * {@code /login} or {@code /conversations} work instead of returning a raw 404.
 *
 * <p>Registered as a plain {@code {path:.*}} catch-all: JAX-RS resolves the more
 * specific, literal API routes ({@code /bella/*}, {@code /webhook/*}, {@code /q/*}) and the
 * static {@code /assets/*} handler ahead of this generic template regardless, so this
 * fallback only ever kicks in for genuinely unmatched paths (i.e. Vue Router routes like
 * {@code /login} or {@code /conversations}).
 *
 * @author Rodrigo Prestes Machado
 */
@Path("/{path:.*}")
public class SpaFallbackResource {

    /**
     * Returns the built SPA entry point.
     *
     * @return the {@code index.html} content, or a 404 if the frontend has not been built
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response index() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("META-INF/resources/index.html")) {
            if (in == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(in.readAllBytes()).build();
        } catch (IOException e) {
            Log.error("Failed to read META-INF/resources/index.html for SPA fallback", e);
            return Response.serverError().build();
        }
    }

}
