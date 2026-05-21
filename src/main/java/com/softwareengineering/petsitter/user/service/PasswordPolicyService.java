package com.softwareengineering.petsitter.user.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class PasswordPolicyService {

    public static final int MIN_PASSWORD_LENGTH = 14;

    private static final String LENGTH_MESSAGE =
            "Das Passwort muss mindestens 14 Zeichen lang sein.";
    private static final String UPPERCASE_MESSAGE =
            "Das Passwort muss mindestens einen Großbuchstaben enthalten.";
    private static final String LOWERCASE_MESSAGE =
            "Das Passwort muss mindestens einen Kleinbuchstaben enthalten.";
    private static final String DIGIT_MESSAGE =
            "Das Passwort muss mindestens eine Zahl enthalten.";
    private static final String SPECIAL_CHARACTER_MESSAGE =
            "Das Passwort muss mindestens ein Sonderzeichen enthalten.";
    private static final String FORBIDDEN_TERM_MESSAGE =
            "Das Passwort darf keine schwachen Wörter enthalten.";
    private static final String REPEATED_CHARACTER_MESSAGE =
            "Das Passwort darf nicht dreimal dasselbe Zeichen hintereinander enthalten.";
    private static final String FOUR_DIGIT_NUMBER_MESSAGE =
            "Das Passwort darf keine vierstellige Zahl enthalten.";
    private static final String NUMERIC_SEQUENCE_MESSAGE =
            "Das Passwort darf keine auf- oder absteigende Zahlenfolge enthalten.";

    private final Set<String> forbiddenTerms;

    public PasswordPolicyService(@Value("classpath:security/forbidden-password-terms.txt") Resource forbiddenTermsResource) {
        this.forbiddenTerms = loadForbiddenTerms(forbiddenTermsResource);
    }

    public PasswordPolicyResult evaluate(String password) {
        String value = password == null ? "" : password;
        String normalized = value.toLowerCase(Locale.ROOT);
        boolean hasValue = !normalized.isBlank();

        boolean minimumLength = value.length() >= MIN_PASSWORD_LENGTH;
        boolean uppercase = value.chars().anyMatch(Character::isUpperCase);
        boolean lowercase = value.chars().anyMatch(Character::isLowerCase);
        boolean digit = value.chars().anyMatch(Character::isDigit);
        boolean specialCharacter = value.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));
        boolean noForbiddenTerm = hasValue && forbiddenTerms.stream().noneMatch(normalized::contains);
        boolean noRepeatedCharacter = hasValue && !containsRepeatedCharacter(normalized);
        boolean noFourDigitNumber = hasValue && !containsFourDigitNumber(value);
        boolean noNumericSequence = hasValue && !containsNumericSequence(value);

        return new PasswordPolicyResult(
                minimumLength,
                uppercase,
                lowercase,
                digit,
                specialCharacter,
                noForbiddenTerm,
                noRepeatedCharacter,
                noFourDigitNumber,
                noNumericSequence,
                firstViolationMessage(
                        minimumLength,
                        uppercase,
                        lowercase,
                        digit,
                        specialCharacter,
                        noForbiddenTerm,
                        noRepeatedCharacter,
                        noFourDigitNumber,
                        noNumericSequence
                )
        );
    }

    int loadedForbiddenTermCount() {
        return forbiddenTerms.size();
    }

    private Set<String> loadForbiddenTerms(Resource resource) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                resource.getInputStream(),
                StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(term -> term.trim().toLowerCase(Locale.ROOT))
                    .filter(term -> !term.isBlank())
                    .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
        } catch (IOException ex) {
            throw new IllegalStateException("Forbidden password terms could not be loaded.", ex);
        }
    }

    private String firstViolationMessage(
            boolean minimumLength,
            boolean uppercase,
            boolean lowercase,
            boolean digit,
            boolean specialCharacter,
            boolean noForbiddenTerm,
            boolean noRepeatedCharacter,
            boolean noFourDigitNumber,
            boolean noNumericSequence
    ) {
        if (!minimumLength) {
            return LENGTH_MESSAGE;
        }
        if (!uppercase) {
            return UPPERCASE_MESSAGE;
        }
        if (!lowercase) {
            return LOWERCASE_MESSAGE;
        }
        if (!digit) {
            return DIGIT_MESSAGE;
        }
        if (!specialCharacter) {
            return SPECIAL_CHARACTER_MESSAGE;
        }
        if (!noForbiddenTerm) {
            return FORBIDDEN_TERM_MESSAGE;
        }
        if (!noRepeatedCharacter) {
            return REPEATED_CHARACTER_MESSAGE;
        }
        if (!noFourDigitNumber) {
            return FOUR_DIGIT_NUMBER_MESSAGE;
        }
        if (!noNumericSequence) {
            return NUMERIC_SEQUENCE_MESSAGE;
        }
        return null;
    }

    private boolean containsRepeatedCharacter(String value) {
        for (int i = 2; i < value.length(); i++) {
            if (value.charAt(i) == value.charAt(i - 1)
                    && value.charAt(i - 1) == value.charAt(i - 2)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsFourDigitNumber(String value) {
        int consecutiveDigits = 0;
        for (int i = 0; i < value.length(); i++) {
            if (Character.isDigit(value.charAt(i))) {
                consecutiveDigits++;
                if (consecutiveDigits >= 4) {
                    return true;
                }
            } else {
                consecutiveDigits = 0;
            }
        }
        return false;
    }

    private boolean containsNumericSequence(String value) {
        for (int i = 2; i < value.length(); i++) {
            char first = value.charAt(i - 2);
            char second = value.charAt(i - 1);
            char third = value.charAt(i);
            if (!Character.isDigit(first) || !Character.isDigit(second) || !Character.isDigit(third)) {
                continue;
            }

            int firstDigit = Character.digit(first, 10);
            int secondDigit = Character.digit(second, 10);
            int thirdDigit = Character.digit(third, 10);
            if ((secondDigit == firstDigit + 1 && thirdDigit == secondDigit + 1)
                    || (secondDigit == firstDigit - 1 && thirdDigit == secondDigit - 1)) {
                return true;
            }
        }
        return false;
    }

    public record PasswordPolicyResult(
            boolean minimumLength,
            boolean uppercase,
            boolean lowercase,
            boolean digit,
            boolean specialCharacter,
            boolean noForbiddenTerm,
            boolean noRepeatedCharacter,
            boolean noFourDigitNumber,
            boolean noNumericSequence,
            String errorMessage
    ) {
        public boolean valid() {
            return errorMessage == null;
        }
    }
}
