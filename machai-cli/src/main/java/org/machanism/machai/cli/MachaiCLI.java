package org.machanism.machai.cli;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Machai CLI application.
 *
 * <p>Bootstraps Spring Boot and starts the Spring Shell runtime.
 *
 * <p>At startup, the application attempts to load additional system properties
 * from {@code machai.properties}. A different configuration file can be provided
 * via {@code -Dconfig=/path/to/file}.
 *
 * <h2>Example</h2>
 * <pre>
 * {@code
 * MachaiCLI.main(new String[] {});
 * }
 * </pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
@SpringBootApplication
public class MachaiCLI {

	/**
	 * Starts the Machai CLI application.
	 *
	 * @param args command-line arguments
	 * @throws IOException if loading the optional configuration file fails
	 */
	public static void main(String[] args) throws IOException {
		System.exit(SpringApplication.exit(SpringApplication.run(MachaiCLI.class, args)));
	}

}
