package org.machanism.machai.bindex;

import java.io.File;
import java.io.IOException;

import org.machanism.machai.bindex.bulder.BIndexBuilder;
import org.machanism.machai.core.ai.GenAIProvider;
import org.machanism.machai.schema.BIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class BindexCreator extends BIndexProjectProcessor {

	private static Logger logger = LoggerFactory.getLogger(BindexCreator.class);

	private GenAIProvider provider;
	private boolean update;

	public BindexCreator(GenAIProvider provider) {
		super();
		this.provider = provider;
	}

	public void processProject(BIndexBuilder bindexBuilder) {
		BIndex bindex;
		try {
			File projectDir = bindexBuilder.getProjectDir();
			bindex = getBindex(projectDir);

			File bindexFile = getBindexFile(projectDir);
			if (update || bindex == null) {
				if (bindexFile.getParentFile() != null) {
					bindexFile.getParentFile().mkdirs();
				}

				bindex = bindexBuilder
						.origin(bindex)
						.projectDir(projectDir)
						.bindexDir(bindexFile.getParentFile())
						.provider(provider)
						.build();

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
