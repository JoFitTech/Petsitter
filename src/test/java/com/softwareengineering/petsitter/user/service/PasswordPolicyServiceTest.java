package com.softwareengineering.petsitter.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

class PasswordPolicyServiceTest {

    private final PasswordPolicyService passwordPolicyService = passwordPolicyService(
            "password",
            "passwort",
            "petsitter",
            "summer",
            ""
    );

    @Test
    void acceptsStrongPasswordWithoutForbiddenPatterns() {
        PasswordPolicyService.PasswordPolicyResult result = passwordPolicyService.evaluate("Axiom-River8!Q");

        assertThat(result.valid()).isTrue();
        assertThat(result.errorMessage()).isNull();
    }

    @Test
    void rejectsThreeIdenticalCharactersInARow() {
        assertThat(passwordPolicyService.evaluate("Axiom-Rivaaa8!Q").noRepeatedCharacter()).isFalse();
        assertThat(passwordPolicyService.evaluate("Axiom-Riv111!Q").noRepeatedCharacter()).isFalse();
        assertThat(passwordPolicyService.evaluate("Axiom-Riv!!!8Q").noRepeatedCharacter()).isFalse();
        assertThat(passwordPolicyService.evaluate("Axiom-Rivaa8!Q").valid()).isTrue();
    }

    @Test
    void rejectsAnyFourConsecutiveDigits() {
        assertThat(passwordPolicyService.evaluate("Axiom-River1975!Q").noFourDigitNumber()).isFalse();
        assertThat(passwordPolicyService.evaluate("Axiom-River2022!Q").noFourDigitNumber()).isFalse();
        assertThat(passwordPolicyService.evaluate("Axiom-River9999!Q").noFourDigitNumber()).isFalse();
        assertThat(passwordPolicyService.evaluate("Axiom-River9091!Q").noFourDigitNumber()).isFalse();
        assertThat(passwordPolicyService.evaluate("Axiom-River22!Q").valid()).isTrue();
        assertThat(passwordPolicyService.evaluate("Axiom-River975!Q").valid()).isTrue();
    }

    @Test
    void rejectsAscendingOrDescendingDigitSequencesOfThreeOrMore() {
        assertThat(passwordPolicyService.evaluate("Axiom-River123!Q").noNumericSequence()).isFalse();
        assertThat(passwordPolicyService.evaluate("Axiom-River789!Q").noNumericSequence()).isFalse();
        assertThat(passwordPolicyService.evaluate("Axiom-River321!Q").noNumericSequence()).isFalse();
        assertThat(passwordPolicyService.evaluate("Axiom-River987!Q").noNumericSequence()).isFalse();
        assertThat(passwordPolicyService.evaluate("Axiom-River890!Q").valid()).isTrue();
        assertThat(passwordPolicyService.evaluate("Axiom-River975!Q").valid()).isTrue();
    }

    @Test
    void rejectsCuratedWeakTermsAsCaseInsensitiveSubstrings() {
        assertThat(passwordPolicyService.evaluate("Axiom-password8!Q").noForbiddenTerm()).isFalse();
        assertThat(passwordPolicyService.evaluate("Axiom-Passwort8!Q").noForbiddenTerm()).isFalse();
        assertThat(passwordPolicyService.evaluate("Axiom-Petsitter8!Q").noForbiddenTerm()).isFalse();
        assertThat(passwordPolicyService.evaluate("Axiom-Summer8!Q").noForbiddenTerm()).isFalse();
    }

    @Test
    void ignoresBlankForbiddenTermsFromResource() {
        assertThat(passwordPolicyService.loadedForbiddenTermCount()).isEqualTo(4);
    }

    private PasswordPolicyService passwordPolicyService(String... forbiddenTerms) {
        return new PasswordPolicyService(new ByteArrayResource(
                String.join("\n", forbiddenTerms).getBytes(StandardCharsets.UTF_8)));
    }
}
