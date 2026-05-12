package com.softwareengineering.petsitter.user.domain;

public enum AccountRole {
    ADMIN,
    SIGNED_IN_USER;

    // String constants for use in @RolesAllowed annotations (which require
    // compile-time constants)
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_SIGNED_IN_USER = "SIGNED_IN_USER";
}
