package com.softwareengineering.petsitter.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/VAADIN/**",
                                "/favicon.ico",
                                "/images/**",
                                "/icons/**",
                                "/manifest.webmanifest",
                                "/sw.js",
                                "/offline-page.html"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults())
                .logout(logout -> logout.logoutSuccessUrl("/login"))
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService() {
        // TODO: Lokalen In-Memory-User spaeter durch produktive Authentifizierung ersetzen.
        UserDetails localUser = User.withUsername("localuser")
                .password("{noop}localpass")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(localUser);
    }
}
