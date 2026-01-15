package org.machanism.machai.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Machai CLI application.
 * <p>
 * Boots and runs the Spring Boot CLI shell, enabling user access to
 * GenAI-powered commands such as assembly, bindex, and file processing. Usage
 * Example:
 * 
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
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		loadSystemProperties();
		System.exit(SpringApplication.exit(SpringApplication.run(MachaiCLI.class, args)));
	}

	private static void loadSystemProperties() throws FileNotFoundException, IOException {
		String configFIle = System.getProperty("config");
		File conf;
		if (configFIle != null) {
			conf = new File(configFIle);
		} else {
			conf = new File("machai.properties");
		}

		if (conf.exists()) {
			FileInputStream propFile = new FileInputStream(conf);
			Properties p = new Properties(System.getProperties());
			p.load(propFile);
			propFile.close();
			System.setProperties(p);
		}
	}
}
