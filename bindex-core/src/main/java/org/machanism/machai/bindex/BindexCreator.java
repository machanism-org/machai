package org.machanism.machai.bindex;

import java.io.File;
import java.io.IOException;

import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.bindex.builder.BindexBuilder;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema.Bindex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Creates or updates a project's {@code bindex.json} file using a {@link GenAIProvider}.
 *
 * <p>The actual Bindex content is produced by a {@link BindexBuilder} selected by * {@link BindexBuilderFactory} based on the provided {@link ProjectLayout}.
 *
 * <h2>Example</h2>
 *
 * <pre>
 * GenAIProvider provider = ...;
 * ProjectLayout layout = ...;
 *
 * new BindexCreator(provider)
 *     .update(true)
 *     .processFolder(layout);
 * </pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 * @see BindexProjectProcessor
 */
public class BindexCreator extends BindexProjectProcessor {

	/** Logger instance for the BindexCreator class. */
	private static final Logger LOGGER = LoggerFactory.getLogger(BindexCreator.class);

	/** Provider used to generate or enrich the Bindex. */
	private final GenAIProvider provider;

	/** Whether an existing Bindex should be overwritten. */
	private boolean update;

	/**
	 * Creates a {@link BindexCreator}.
	 *
	 * @param provider provider used for AI-assisted Bindex creation
	 */
	public BindexCreator(GenAIProvider provider) {
		this.provider = provider;
	}

	/**
	 * Creates or updates {@code bindex.json} in the project directory described by the given layout.
	 *
	 * <p>If {@link #update(boolean)} is enabled, any existing Bindex file will be regenerated.
	 * If update is disabled, an existing file is left unchanged.
	 *
	 * @param projectLayout project layout to inspect
	 * @throws IllegalArgumentException if an I/O error occurs while reading or writing the Bindex
	 */
	public void processFolder(ProjectLayout projectLayout) {
		try {
			BindexBuilder bindexBuilder = BindexBuilderFactory.create(projectLayout);

			File projectDir = bindexBuilder.getProjectLayout().getProjectDir();
			Bindex origin = getBindex(projectDir);

			File bindexFile = getBindexFile(projectDir);
			if (!update && origin != null) {
				return;
			}

			File parent = bindexFile.getParentFile();
			if (parent != null) {
				parent.mkdirs();
			}

			Bindex bindex = bindexBuilder.origin(origin).genAIProvider(provider).build();
			if (bindex != null) {
				new ObjectMapper().writeValue(bindexFile, bindex);
				LOGGER.info("Bindex file: {}", bindexFile);
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Enables or disables update mode.
	 *
	 * @param update {@code true} to overwrite an existing {@code bindex.json}; {@code false} to only create when absent
	 * @return this instance for chaining
	 */
	public BindexCreator update(boolean update) {
		this.update = update;
		return this;
	}

}
