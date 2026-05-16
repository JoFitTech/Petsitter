package com.softwareengineering.petsitter.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.UuidRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * MongoDB-Konfiguration für UUID-Kodierung.
 *
 * Registriert den UUID-Codec mit der Standard-Repräsentation (JAVA_UUID).
 * Dies behebt den Fehler: "The uuidRepresentation has not been specified, so the UUID cannot be encoded."
 *
 * Das Problem: Die MongoDB-URI enthält zwar ?uuidRepresentation=standard, aber der
 * Java-Client muss dies explizit über MongoClientSettings.Builder.uuidRepresentation() setzen.
 */
@Configuration
public class MongoDbUuidConfig {

    @Value("${spring.data.mongodb.uri:}")
    private String mongoUri;

    /**
     * Erstellt einen Primary MongoClient mit expliziter UUID-Repräsentationskonfiguration.
     *
     * Dies stellt sicher, dass der Client mit der korrekten uuidRepresentation.STANDARD
     * initialisiert wird, um UUIDs in MongoDB Dokumenten korrekt zu kodieren.
     *
     * Diese Bean wird nur erstellt, wenn eine MongoDB-URI konfiguriert ist.
     * In Tests ohne MongoDB kann diese Bean leer bleiben.
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.data.mongodb.uri")
    public MongoClient mongoClient() {
        if (mongoUri == null || mongoUri.trim().isEmpty()) {
            throw new IllegalStateException(
                "spring.data.mongodb.uri muss konfiguriert sein für MongoDB-Support"
            );
        }

        try {
            ConnectionString connectionString = new ConnectionString(mongoUri);

            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .uuidRepresentation(UuidRepresentation.STANDARD)
                    .build();

            return MongoClients.create(settings);
        } catch (Exception e) {
            throw new IllegalStateException(
                "Fehler beim Erstellen des MongoClient mit URI: " + mongoUri, e
            );
        }
    }
}







