package com.softwareengineering.petsitter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PetsitterApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetsitterApplication.class, args);
    }

}
