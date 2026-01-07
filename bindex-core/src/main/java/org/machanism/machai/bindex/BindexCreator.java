package org.machanism.machai.bindex;

import java.io.File;
import java.io.IOException;

import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.bindex.builder.BindexBuilder;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema. Bindex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * BindexCreator is responsible for generating and updating  Bindex
 * representation files for the supplied project layout using a GenAIProvider.
 * <p>
 * Usage example:
 * 
 * <pre>
 * BindexCreator creator = new BindexCreator(provider, true);
 * creator.processFolder(layout);
 * </pre>
 *
 * This class can update or create  Bindex documents based on the model provided
 * by the builder and the current contents in the project directory.
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 * @see  BindexProjectProcessor
 */
public class BindexCreator extends  BindexProjectProcessor {

	/** Logger instance for the BindexCreator class. */
	private static Logger logger = LoggerFactory.getLogger(BindexCreator.class);

	/** GenAIProvider instance to be used for AI-based indexing. */
	private GenAIProvider provider;

	/** Flag indicating whether to update existing  Bindex, if available. */
	private boolean update;

	/**
	 * Constructs a BindexCreator with the specified GenAIProvider and LLM
	 * invocation setting.
	 *
	 * @param provider GenAIProvider used for generating  Bindex data
	 * @param callLLM  Indicates if LLM should be involved in the process
	 */
	public BindexCreator(GenAIProvider provider) {
		super();
		this.provider = provider;
	}

	/**
	 * Processes the specified project layout folder to create/update the  Bindex
	 * file.
	 *
	 * @param projectLayout The project layout to be processed for indexing
	 * @throws IllegalArgumentException If any IO errors occur during processing
	 * @see BindexBuilderFactory
	 */
	public void processFolder(ProjectLayout projectLayout) {
		 Bindex bindex;
		try {
			BindexBuilder bindexBuilder = BindexBuilderFactory.create(projectLayout);

			File projectDir = bindexBuilder.getProjectLayout().getProjectDir();
			bindex = getBindex(projectDir);

			File bindexFile = getBindexFile(projectDir);
			if (update || bindex == null) {
				if (bindexFile.getParentFile() != null) {
					bindexFile.getParentFile().mkdirs();
				}

				bindex = bindexBuilder.origin(bindex).genAIProvider(provider).build();

				if (bindex != null) {
					new ObjectMapper().writeValue(bindexFile, bindex);
					logger.info(" Bindex file: {}", bindexFile);
				}
			}

		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Toggles the update mode for  Bindex creation, enabling file updates if set to
	 * true.
	 *
	 * @param update true to update existing  Bindex files, false otherwise
	 * @return This BindexCreator instance for chained calls
	 */
	public BindexCreator update(boolean update) {
		this.update = update;
		return this;
	}

}
