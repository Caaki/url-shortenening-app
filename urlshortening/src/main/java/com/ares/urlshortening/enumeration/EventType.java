package com.ares.urlshortening.enumeration;

import lombok.Getter;

@Getter
public enum EventType {
    LOGIN_ATTEMPT("Login attempted"),
    LOGIN_ATTEMPT_FAILURE("Login failed"),
    LOGIN_ATTEMPT_SUCCESS("Login Successfully"),
    PROFILE_UPDATE("Profile information updated"),
    PROFILE_PICTURE_UPDATE("Profile picture updated"),
    ROLE_UPDATE("User role was updated"),
    ACCOUNT_SETTINGS_UPDATE("Account settings were updated"),
    PASSWORD_UPDATE("User password was updated"),
    MFA_UPDATE("MFA settings were changed"),
    URL_VISITED("Your link was visited");
    private final String description;

    EventType(String description) {
        this.description = description;
    }
}
