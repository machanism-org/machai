package org.machanism.machai.bindex;

import java.io.File;
import java.io.IOException;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema.Bindex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers a project's {@link Bindex} document in the backing Bindex registry.
 *
 * <p>
 * This type reads {@code bindex.json} from a project directory and uses
 * {@link Picker} to insert or update the corresponding document in the
 * registry.
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * Configurator config = ...;
 * ProjectLayout layout = ...;
 *
 * BindexRegister register = new BindexRegister("openai", null, config);
 * register.update(true).processFolder(layout);
 * }</pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 * @see BindexProjectProcessor
 */
public class BindexRegister extends BindexProjectProcessor {

	/** Logger instance for this class. */
	private static final Logger LOGGER = LoggerFactory.getLogger(BindexRegister.class);

	/** Picker instance used to register and look up Bindex documents. */
	private final Picker picker;

	/** Whether to force re-registration even when an entry already exists. */
	private boolean update;

	/**
	 * Creates a register instance.
	 *
	 * @param genai GenAI provider identifier used by {@link Picker}
	 * @param registerUrl   MongoDB connection URI to use; when {@code null}, {@link Picker}
	 *              chooses a default
	 * @param conf  configurator used to initialize the provider
	 * @throws IllegalArgumentException if {@code genai} or {@code conf} is
	 *                                  {@code null}
	 */
	public BindexRegister(String genai, String registerUrl, Configurator conf) {
		if (genai == null) {
			throw new IllegalArgumentException("genai must not be null");
		}
		if (conf == null) {
			throw new IllegalArgumentException("conf must not be null");
		}

		this.picker = new Picker(genai, registerUrl, conf);
	}

	/**
	 * Registers the Bindex found in the given project directory.
	 *
	 * <p>
	 * If no {@code bindex.json} exists, this method performs no action.
	 *
	 * @param projectLayout layout describing the project directory
	 * @throws IllegalArgumentException if {@code projectLayout} is {@code null} or
	 *                                  registration fails
	 */
	public void processFolder(ProjectLayout projectLayout) {
		if (projectLayout == null) {
			throw new IllegalArgumentException("projectLayout must not be null");
		}

		try {
			File projectDir = projectLayout.getProjectDir();
			Bindex bindex = getBindex(projectDir);
			if (bindex == null) {
				return;
			}

			String registeredId = picker.getRegistredId(bindex);
			if (registeredId == null || update) {
				registeredId = picker.create(bindex);
				LOGGER.info("Registration id: {}", registeredId);
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Enables or disables update mode.
	 *
	 * @param overwrite {@code true} to overwrite existing registrations;
	 *                  {@code false} to only register when missing
	 * @return this instance for chaining
	 */
	public BindexRegister update(boolean overwrite) {
		this.update = overwrite;
		return this;
	}

}
