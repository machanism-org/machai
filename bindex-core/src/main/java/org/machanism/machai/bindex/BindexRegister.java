package org.machanism.machai.bindex;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema.Bindex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers a project's {@link Bindex} document in the backing Bindex registry.
 *
 * <p>This type reads {@code bindex.json} from a project directory and uses {@link Picker} to * insert or update the corresponding document in the registry.
 *
 * <h2>Example</h2>
 *
 * <pre>
 * GenAIProvider provider = ...;
 * ProjectLayout layout = ...;
 *
 * try (BindexRegister register = new BindexRegister(provider, null)) {
 *     register.update(true).processFolder(layout);
 * }
 * </pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 * @see BindexProjectProcessor
 */
public class BindexRegister extends BindexProjectProcessor implements Closeable {

	/** Logger instance for this class. */
	private static final Logger LOGGER = LoggerFactory.getLogger(BindexRegister.class);

	/** Picker instance used to register and look up Bindex documents. */
	private final Picker picker;

	/** Whether to force re-registration even when an entry already exists. */
	private boolean update;

	/**
	 * Creates a register instance.
	 *
	 * @param provider GenAI provider used by {@link Picker}
	 * @param url      MongoDB connection URI to use; when {@code null}, {@link Picker} chooses a default
	 */
	public BindexRegister(GenAIProvider provider, String url) {
		this.picker = new Picker(provider, url);
	}

	/**
	 * Registers the Bindex found in the given project directory.
	 *
	 * <p>If no {@code bindex.json} exists, this method performs no action.
	 *
	 * @param projectLayout layout describing the project directory
	 * @throws IllegalArgumentException if the registration fails due to an I/O error
	 */
	public void processFolder(ProjectLayout projectLayout) {
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
	 * Closes the underlying {@link Picker} (and its database resources).
	 *
	 * @throws IOException if closing fails
	 */
	@Override
	public void close() throws IOException {
		picker.close();
	}

	/**
	 * Enables or disables update mode.
	 *
	 * @param overwrite {@code true} to overwrite existing registrations; {@code false} to only register when missing
	 * @return this instance for chaining
	 */
	public BindexRegister update(boolean overwrite) {
		this.update = overwrite;
		return this;
	}

}
