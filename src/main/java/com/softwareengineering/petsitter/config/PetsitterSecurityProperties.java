package com.softwareengineering.petsitter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Zentrale Security-Properties fuer Authentifizierung und Demo-Login.
 */
@ConfigurationProperties(prefix = "petsitter.security")
public class PetsitterSecurityProperties {

    private final Demo demo = new Demo();

    public Demo getDemo() {
        return demo;
    }

    public static class Demo {

        private boolean enabled = true;
        private String username = "localuser";
        private String password = "localpass";
        private String role = "USER";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}

