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

import dev.orion.bella.domain.model.User;

/**
 * Driven port that resolves the authenticated web user from an Orion Users JWT token.
 *
 * @author Rodrigo Prestes Machado
 */
public interface AuthPort {

    /**
     * Extracts the {@code c_hash} claim (Orion Users identifier) from the given JWT.
     *
     * @param jwtToken the raw JWT token (without the {@code Bearer} prefix)
     * @return the Orion Users hash
     */
    String extractUserHash(String jwtToken);

    /**
     * Extracts the {@code email} claim from the given JWT.
     *
     * @param jwtToken the raw JWT token (without the {@code Bearer} prefix)
     * @return the user email
     */
    String extractEmail(String jwtToken);

    /**
     * Resolves the {@link User} identified by the given JWT token, decoding its claims.
     *
     * @param jwtToken the raw JWT token (without the {@code Bearer} prefix)
     * @return the resolved user, with {@code orionUserHash} and {@code email} populated
     */
    User resolveUser(String jwtToken);

}
