/*
 * Copyright (c) 2026 Rodrigo Prestes Machado
 * All rights reserved.
 *
 * This source code is proprietary and confidential.
 * Unauthorized copying, modification, distribution, or use
 * of this software, via any medium, is strictly prohibited
 * without the express prior written permission of the copyright holder.
 */
package dev.rpmhub.domain.model;

/**
 * Domain model that represents a chat user.
 *
 * @author Rodrigo Prestes Machado
 */
public class User {

    /**
     * Phone number that identifies the user (WhatsApp channel).
     */
    private String phoneNumber;

    /**
     * Stable hash that identifies the user in the Orion Users service (web channel,
     * extracted from the {@code c_hash} JWT claim).
     */
    private String orionUserHash;

    /**
     * Email address of the user, as reported by the Orion Users JWT ({@code email} claim).
     */
    private String email;

    /**
     * Returns the user phone number.
     *
     * @return the phone number
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the user phone number.
     *
     * @param phoneNumber the phone number to set
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Returns the Orion Users hash that identifies this user on the web channel.
     *
     * @return the Orion Users hash
     */
    public String getOrionUserHash() {
        return orionUserHash;
    }

    /**
     * Sets the Orion Users hash that identifies this user on the web channel.
     *
     * @param orionUserHash the Orion Users hash to set
     */
    public void setOrionUserHash(String orionUserHash) {
        this.orionUserHash = orionUserHash;
    }

    /**
     * Returns the user email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user email address.
     *
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

}
