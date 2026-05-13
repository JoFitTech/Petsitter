package com.softwareengineering.petsitter;

import com.softwareengineering.petsitter.config.DockerComposeStartupGuard;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PetsitterApplication {

    public static void main(String[] args) {
        String blockingMessage = DockerComposeStartupGuard.getBlockingMessage();
        if (blockingMessage != null) {
            System.err.println(blockingMessage);
            System.exit(1);
        }
        SpringApplication.run(PetsitterApplication.class, args);
    }

}
