package org.machanism.machai.cli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Machai CLI application.
 * <p>
 * Boots and runs the Spring Boot CLI shell, enabling user access to GenAI-powered
 * commands such as assembly, bindex, and document processing.
 * Usage Example:
 * <pre>
 * {@code
 * MachaiCLI.main(new String[]{ });
 * }
 * </pre>
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
@SpringBootApplication
public class MachaiCLI {

    /**
     * Starts the Machai CLI application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(MachaiCLI.class, args)));
    }
}
