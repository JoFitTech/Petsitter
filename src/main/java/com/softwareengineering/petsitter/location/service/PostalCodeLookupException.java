package com.softwareengineering.petsitter.location.service;

public class PostalCodeLookupException extends RuntimeException {

    public PostalCodeLookupException(String message) {
        super(message);
    }

    public PostalCodeLookupException(String message, Throwable cause) {
        super(message, cause);
    }
}
