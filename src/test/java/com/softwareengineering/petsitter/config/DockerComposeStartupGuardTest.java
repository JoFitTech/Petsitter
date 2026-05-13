package com.softwareengineering.petsitter.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DockerComposeStartupGuardTest {

    private static final String DOCKER_COMPOSE_ENABLED = "spring.docker.compose.enabled";
    private static final String ANSI_ESCAPE = "\u001B[";

    @AfterEach
    void clearProperty() {
        System.clearProperty(DOCKER_COMPOSE_ENABLED);
    }

    @Test
    void dockerComposeCheckIsEnabledByDefault() {
        assertThat(DockerComposeStartupGuard.isDockerComposeEnabled()).isTrue();
    }

    @Test
    void dockerComposeCheckCanBeDisabledViaSystemProperty() {
        System.setProperty(DOCKER_COMPOSE_ENABLED, "false");

        assertThat(DockerComposeStartupGuard.isDockerComposeEnabled()).isFalse();
    }

    @Test
    void blockingMessageFallsBackToPlainTextWithoutAnsi() {
        String message = DockerComposeStartupGuard.createBlockingMessage(false);

        assertThat(message).contains("DOCKER NICHT ERREICHBAR");
        assertThat(message).contains("Docker Desktop gestartet ist.");
        assertThat(message).doesNotContain(ANSI_ESCAPE);
    }

    @Test
    void blockingMessageCanBeRenderedWithDockerBlueAnsiColor() {
        String message = DockerComposeStartupGuard.createBlockingMessage(true);

        assertThat(message).contains("DOCKER NICHT ERREICHBAR");
        assertThat(message).contains("\u001B[38;2;36;150;237m");
        assertThat(message).contains("\u001B[1m");
        assertThat(message).contains("\u001B[0m");
    }

}
