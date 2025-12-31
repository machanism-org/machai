package org.machanism.machai.bindex;

import java.io.File;
import java.io.IOException;

import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.bindex.builder.BindexBuilder;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema.BIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class BindexCreator extends BIndexProjectProcessor {

	private static Logger logger = LoggerFactory.getLogger(BindexCreator.class);

	private GenAIProvider provider;
	private boolean update;

	private boolean callLLM;

	public BindexCreator(GenAIProvider provider, boolean callLLM) {
		super();
		this.provider = provider;
		this.callLLM = callLLM;
	}

	public void processProject(ProjectLayout projectLayout) {
		BIndex bindex;
		try {
			BindexBuilder bindexBuilder = BindexBuilderFactory.create(projectLayout);
			
			File projectDir = bindexBuilder.getProjectLayout().getProjectDir();
			bindex = getBindex(projectDir);

			File bindexFile = getBindexFile(projectDir);
			if (update || bindex == null) {
				if (bindexFile.getParentFile() != null) {
					bindexFile.getParentFile().mkdirs();
				}

				bindex = bindexBuilder
						.origin(bindex)
						.genAIProvider(provider)
						.build(callLLM);

				if (bindex != null) {
					new ObjectMapper().writeValue(bindexFile, bindex);
					logger.info("BIndex file: {}", bindexFile);
				}
			}

		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public BindexCreator update(boolean update) {
		this.update = update;
		return this;
	}

}
