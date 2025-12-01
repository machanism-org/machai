package org.machanism.machai.cli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the CLI import application.
 * <p>
 * This class bootstraps the Spring Boot application and loads additional configuration
 * from the specified XML resource.
 */
@SpringBootApplication
public class Application {

    /**
     * Main method to start the CLI import application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(Application.class, args)));
    }
}
