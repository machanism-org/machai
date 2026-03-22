package org.machanism.machai.project.layout;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.transport.wagon.WagonTransporterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for reading and processing Maven <code>pom.xml</code> files into
 * Maven models.
 * <p>
 * Provides model parsing, effective POM calculation, property replacement,
 * license detection, and model serialization.
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
public class PomReader {

	private Map<String, String> pomProperties = new HashMap<>();
	private List<License> defaultLicenses;

	/**
	 * Loads and returns the Maven model from a <code>pom.xml</code> file.
	 * Optionally calculates the effective model with plugin resolution.
	 *
	 * @param pomFile   pom.xml file to parse
	 * @param effective true for effective model calculation, false for raw parsing
	 * @return Parsed Maven Model
	 * @throws IllegalArgumentException if <code>pom.xml</code> cannot be processed
	 * @see <a href="https://maven.apache.org/pom.html">Maven POM Reference</a>
	 */
	public Model getProjectModel(File pomFile) {
		ModelBuildingRequest request = new DefaultModelBuildingRequest();
		request.setPomFile(pomFile);

		Model model = null;
		try {
				MavenXpp3Reader reader = new MavenXpp3Reader();
				FileReader fileReader = new FileReader(pomFile);
				String pomStr = IOUtils.toString(fileReader);
				pomStr = replaceProperty(pomStr);
				if (pomStr == null) {
					throw new IllegalArgumentException("POM content could not be read: " + pomFile);
				}
				model = reader.read(new ByteArrayInputStream(pomStr.getBytes()), false);
		} catch (Exception e) {
			throw new IllegalArgumentException("POM file: " + pomFile, e);
		}

		Set<Entry<Object, Object>> propertiesEntries = model.getProperties().entrySet();
		for (Entry<Object, Object> entry : propertiesEntries) {
			pomProperties.put((String) entry.getKey(), (String) entry.getValue());
		}

			String version = model.getVersion();
			if (version != null) {
				pomProperties.put("project.version", version);
			}

		List<License> licenses = model.getLicenses();
		if (licenses.isEmpty()) {
			if (defaultLicenses != null) {
				model.setLicenses(defaultLicenses);
			}
		} else if (defaultLicenses == null) {
			defaultLicenses = licenses;
		}

		return model;
	}

	/**
	 * Replaces placeholders in a POM string using collected properties.
	 * 
	 * @param pomStr Raw POM file as string
	 * @return POM string with placeholders replaced
	 */
	private String replaceProperty(String pomStr) {
		if (pomStr != null) {
			Set<Entry<String, String>> propertiesEntries = pomProperties.entrySet();
			for (Entry<String, String> entry : propertiesEntries) {
				String placeholder = "${" + entry.getKey() + "}";
				String value = entry.getValue();
				if (value != null) {
					pomStr = pomStr.replace(placeholder, value);
				}
			}
		}
		return pomStr;
	}

	/**
	 * Serializes a Maven Model to its string (XML) representation.
	 * 
	 * @param model Maven Model
	 * @return XML string of the model
	 * @throws IOException if serialization fails
	 */
	public static String printModel(Model model) throws IOException {
		MavenXpp3Writer writer = new MavenXpp3Writer();
		Writer stringWriter = new StringWriter();
		writer.write(stringWriter, model);
		return stringWriter.toString();
	}

	/**
	 * Configures a Maven ServiceLocator with repository and transporter services.
	 * 
	 * @return Initialized DefaultServiceLocator
	 */
	public DefaultServiceLocator serviceLocator() {
		DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
		locator.addService(RepositoryConnectorFactory.class,
				BasicRepositoryConnectorFactory.class);
		locator.addService(
				TransporterFactory.class, FileTransporterFactory.class);
		locator.addService(TransporterFactory.class,
				HttpTransporterFactory.class);
		locator.addService(TransporterFactory.class,
				WagonTransporterFactory.class);
		return locator;
	}

	/**
	 * Returns properties parsed from pom.xml files.
	 * 
	 * @return Map of POM properties
	 */
	public Map<String, String> getPomProperties() {
		return pomProperties;
	}

}
