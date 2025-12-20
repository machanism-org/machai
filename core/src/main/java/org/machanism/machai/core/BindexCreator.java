package org.machanism.machai.core;

import java.io.File;
import java.io.IOException;

import org.machanism.machai.core.ai.GenAIProvider;
import org.machanism.machai.core.bindex.BIndexBuilder;
import org.machanism.machai.core.bindex.BIndexBuilderFactory;
import org.machanism.machai.schema.BIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class BindexCreator extends ScanProject {

	private static Logger logger = LoggerFactory.getLogger(BindexCreator.class);

	private GenAIProvider provider;
	private boolean update;

	public BindexCreator(GenAIProvider provider) {
		super();
		this.provider = provider;
	}

	public String processProject(File projectDir) {
		BIndex bindex;
		try {
			bindex = getBindex(projectDir);

			String result = null;
			File bindexFile = getBindexFile(projectDir);
			if (update || bindex == null) {
				if (bindexFile.getParentFile() != null) {
					bindexFile.getParentFile().mkdirs();
				}
				BIndexBuilder bindexBuilder = BIndexBuilderFactory.builder(projectDir);
				
				bindex = bindexBuilder
						.source(bindex)
						.projectDir(projectDir)
						.bindexDir(bindexFile.getParentFile())
						.provider(provider)
						.build();
			}
			if (bindex != null) {
				result = new ObjectMapper().writeValueAsString(bindex);
				new ObjectMapper().writeValue(bindexFile, bindex);
				logger.info("BIndex file: {}", bindexFile);
			}

			return result;
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public BindexCreator update(boolean update) {
		this.update = update;
		return this;
	}

}
