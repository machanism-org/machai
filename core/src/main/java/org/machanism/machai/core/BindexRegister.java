package org.machanism.machai.core;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import org.machanism.machai.core.ai.GenAIProvider;
import org.machanism.machai.core.bindex.BIndexBuilder;
import org.machanism.machai.schema.BIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindexRegister extends ScanProject implements Closeable {

	private static Logger logger = LoggerFactory.getLogger(BindexRegister.class);

	private Picker embeddingProvider;

	private boolean update;

	public BindexRegister(GenAIProvider provider) {
		super();
		embeddingProvider = new Picker(provider);
	}

	public String processProject(File projectDir, BIndexBuilder bindexBuilder) {
		BIndex bindex;
		try {
			bindex = getBindex(projectDir);

			String regId = null;
			if (bindex != null) {
				regId = embeddingProvider.getRegistredId(bindex);
				if (regId == null || update) {
					regId = embeddingProvider.create(bindex);
					logger.info("embeddingId: {}", regId);
				}
			}

			return regId;
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public void close() throws IOException {
		embeddingProvider.close();
	}

	public BindexRegister update(boolean overwrite) {
		this.update = overwrite;
		return this;
	}

	public void scanProjects(File basedir) throws IOException {
		scanProjects(basedir, false);
	}

}
