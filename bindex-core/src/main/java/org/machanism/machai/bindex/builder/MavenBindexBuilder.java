package org.machanism.machai.bindex.builder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Resource;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.PomReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link BindexBuilder} specialization for Maven projects.
 *
 * <p>
 * This builder reads the effective Maven model from {@code pom.xml} (via the
 * provided {@link MavenProjectLayout}), collects relevant source and resource
 * files, and prompts the configured GenAI provider with:
 * <ul>
 * <li>source and resource file contents, and</li>
 * <li>a sanitized POM representation with non-essential sections removed.</li>
 * </ul>
 *
 * <p>
 * Example:
 * 
 * <pre>{@code
 * Bindex bindex = new MavenBindexBuilder(layout).genAIProvider(provider).build();
 * }</pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 * @see org.machanism.machai.bindex.BindexBuilderFactory
 * @see MavenProjectLayout
 */
public class MavenBindexBuilder extends BindexBuilder {

	private static final Logger logger = LoggerFactory.getLogger(MavenBindexBuilder.class);
	private static final ResourceBundle promptBundle = ResourceBundle.getBundle("maven_project_prompts");

	/** Maven project layout (POM model and directory configuration). */
	private final MavenProjectLayout projectLayout;

	/**
	 * Creates a builder for a Maven project.
	 *
	 * @param projectLayout Maven project layout describing the POM and directory
	 *                      structure
	 */
	public MavenBindexBuilder(MavenProjectLayout projectLayout) {
		super(projectLayout);
		this.projectLayout = projectLayout;
	}

	/**
	 * Adds Maven-specific project context to the provider.
	 *
	 * <p>
	 * The implementation:
	 * <ol>
	 * <li>adds all files from source/resources/test directories configured in the
	 * POM build section,</li>
	 * <li>removes non-essential sections from the Maven {@link Model},</li>
	 * <li>prompts the cleaned POM text,</li>
	 * <li>adds any additional Maven-specific prompting rules.</li>
	 * </ol>
	 *
	 * @throws IOException if source/resource files cannot be read or prompting
	 *                     fails
	 */
	@Override
	public void projectContext() throws IOException {
		Build build = projectLayout.getModel().getBuild();
		if (build == null) {
			return;
		}

		addResources(build.getSourceDirectory());

		List<Resource> resourcesDirectory = build.getResources();
		if (resourcesDirectory != null) {
			for (Resource resource : resourcesDirectory) {
				addResources(resource.getDirectory());
			}
		}

		List<Resource> testResourcesDirectory = build.getTestResources();
		if (testResourcesDirectory != null) {
			for (Resource resource : testResourcesDirectory) {
				addResources(resource.getDirectory());
			}
		}

		addResources(build.getTestSourceDirectory());

		Model model = projectLayout.getModel();
		removeNotImportantData(model);

		String pom = PomReader.printModel(model);
		String prompt = MessageFormat.format(promptBundle.getString("pom_resource_section"), pom);
		getGenAIProvider().prompt(prompt);

		prompt = promptBundle.getString("additional_rules");
		getGenAIProvider().prompt(prompt);
	}

	/**
	 * Prompts the provider with all regular files in the provided directory.
	 *
	 * @param directory directory path (as configured in the POM) to scan; ignored
	 *                  if blank or missing
	 * @throws IOException if walking the directory fails
	 */
	private void addResources(String directory) throws IOException {
		if (StringUtils.isBlank(directory)) {
			return;
		}

		Path startPath = Paths.get(directory);
		if (!Files.exists(startPath)) {
			return;
		}

		Files.walk(startPath).filter(Files::isRegularFile).forEach(f -> {
			try {
				promptFile(f.toFile(), "source_resource_section");
			} catch (IOException e) {
				logger.warn("File: {} adding failed.", f);
			}
		});
	}

	/**
	 * Removes sections from the Maven model that are typically not useful for
	 * documentation-oriented indexing.
	 *
	 * @param model Maven model to mutate before serialization
	 */
	void removeNotImportantData(Model model) {
		model.setDistributionManagement(null);
		model.setBuild(null);
		model.setProperties(null);
		model.setDependencyManagement(null);
		model.setReporting(null);
		model.setScm(null);
		model.setPluginRepositories(null);
	}
}
