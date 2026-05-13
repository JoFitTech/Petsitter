package com.softwareengineering.petsitter.config;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class DockerComposeStartupGuard {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BOLD = "\u001B[1m";
    private static final String ANSI_DOCKER_BLUE = "\u001B[38;2;36;150;237m";
    private static final String SEPARATOR = "============================================================";

    private DockerComposeStartupGuard() {
    }

    public static String getBlockingMessage() {
        if (!isDockerComposeEnabled()) {
            return null;
        }
        return isDockerReachable() ? null : createBlockingMessage(supportsAnsiColors());
    }

    static String createBlockingMessage(boolean useAnsiColors) {
        String plainMessage = "\n"
                + SEPARATOR + "\n"
                + " DOCKER NICHT ERREICHBAR\n"
                + " Pruefe bitte, ob Docker bzw. Docker Desktop gestartet ist.\n"
                + " Starte die App danach erneut.\n"
                + SEPARATOR + "\n";

        if (!useAnsiColors) {
            return plainMessage;
        }

        return "\n"
                + ANSI_BOLD
                + ANSI_DOCKER_BLUE
                + SEPARATOR + "\n"
                + " DOCKER NICHT ERREICHBAR\n"
                + " Pruefe bitte, ob Docker bzw. Docker Desktop gestartet ist.\n"
                + " Starte die App danach erneut.\n"
                + SEPARATOR
                + ANSI_RESET
                + "\n";
    }

    static boolean isDockerComposeEnabled() {
        String systemProperty = System.getProperty("spring.docker.compose.enabled");
        if (systemProperty != null) {
            return !"false".equalsIgnoreCase(systemProperty);
        }

        String environmentProperty = System.getenv("SPRING_DOCKER_COMPOSE_ENABLED");
        if (environmentProperty != null) {
            return !"false".equalsIgnoreCase(environmentProperty);
        }

        return true;
    }

    static boolean supportsAnsiColors() {
        return System.console() != null && System.getenv("NO_COLOR") == null;
    }

    static boolean isDockerReachable() {
        try {
            Process process = new ProcessBuilder("docker", "version", "--format", "{{.Client.Version}}").start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return false;
            }
            return process.exitValue() == 0;
        } catch (IOException ex) {
            return false;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

}
