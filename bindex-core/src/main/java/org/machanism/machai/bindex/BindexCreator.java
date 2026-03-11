package org.machanism.machai.bindex;

import java.io.File;
import java.io.IOException;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.bindex.builder.BindexBuilder;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema.Bindex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Creates or updates a project's {@code bindex.json} file using an AI-assisted {@link BindexBuilder}.
 *
 * <p>The Bindex content is produced by a {@link BindexBuilder} selected by
 * {@link BindexBuilderFactory} based on the supplied {@link ProjectLayout}.
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * Configurator config = ...;
 * ProjectLayout layout = ...;
 *
 * new BindexCreator("openai", config)
 *     .update(true)
 *     .processFolder(layout);
 * }</pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 * @see BindexProjectProcessor
 */
public class BindexCreator extends BindexProjectProcessor {

	/** Logger instance for the BindexCreator class. */
	private static final Logger LOGGER = LoggerFactory.getLogger(BindexCreator.class);

	/** Provider identifier used for AI-assisted Bindex creation. */
	private final String genai;

	/** Configurator used by builders and providers. */
	private final Configurator config;

	/** Whether an existing Bindex should be overwritten. */
	private boolean update;

	/**
	 * Creates a {@link BindexCreator}.
	 *
	 * @param genai  GenAI provider identifier used for AI-assisted Bindex creation
	 * @param config configurator used to initialize the provider and builders
	 * @throws IllegalArgumentException if {@code genai} or {@code config} is {@code null}
	 */
	public BindexCreator(String genai, Configurator config) {
		if (genai == null) {
			throw new IllegalArgumentException("genai must not be null");
		}
		if (config == null) {
			throw new IllegalArgumentException("config must not be null");
		}
		this.genai = genai;
		this.config = config;
	}

	/**
	 * Creates or updates {@code bindex.json} in the project directory described by the given layout.
	 *
	 * <p>If {@link #update(boolean)} is enabled, any existing Bindex file will be regenerated. If
	 * update is disabled, an existing file is left unchanged.
	 *
	 * @param projectLayout project layout to inspect
	 * @throws IllegalArgumentException if {@code projectLayout} is {@code null} or an I/O error occurs
	 */
	public void processFolder(ProjectLayout projectLayout) {
		if (projectLayout == null) {
			throw new IllegalArgumentException("projectLayout must not be null");
		}

		try {
			BindexBuilder bindexBuilder = BindexBuilderFactory.create(projectLayout, genai, config);

			File projectDir = bindexBuilder.getProjectLayout().getProjectDir();
			Bindex origin = getBindex(projectDir);

			File bindexFile = getBindexFile(projectDir);
			if (!update && origin != null) {
				LOGGER.info("Bindex file found at {}. Skipping generation.", bindexFile);
				return;
			}

			File parent = bindexFile.getParentFile();
			if (parent != null) {
				parent.mkdirs();
			}

			Bindex bindex = bindexBuilder.origin(origin).build();
			if (bindex != null) {
				new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(bindexFile, bindex);
				LOGGER.info("Bindex file: {}", bindexFile);
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Enables or disables update mode.
	 *
	 * @param update {@code true} to overwrite an existing {@code bindex.json}; {@code false} to only
	 *               create when absent
	 * @return this instance for chaining
	 */
	public BindexCreator update(boolean update) {
		this.update = update;
		return this;
	}

}
