package com.softwareengineering.petsitter.config;

import com.mongodb.UuidRepresentation;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Erzwingt eine konsistente UUID-Repräsentation für MongoDB.
 */
@Configuration
public class MongoUuidConfig {

    @Bean
    MongoClientSettingsBuilderCustomizer mongoUuidRepresentationCustomizer() {
        return builder -> builder.uuidRepresentation(UuidRepresentation.STANDARD);
    }
}

