package org.machanism.machai.bindex;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import org.machanism.machai.bindex.bulder.BIndexBuilder;
import org.machanism.machai.core.ai.GenAIProvider;
import org.machanism.machai.schema.BIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindexRegister extends ScanProject implements Closeable {

	private static Logger logger = LoggerFactory.getLogger(BindexRegister.class);

	private Picker picker;

	private boolean update;

	public BindexRegister(GenAIProvider provider) {
		super();
		picker = new Picker(provider);
	}

	public String processProject(File projectDir, BIndexBuilder bindexBuilder) {
		BIndex bindex;
		try {
			bindex = getBindex(projectDir);

			String regId = null;
			if (bindex != null) {
				regId = picker.getRegistredId(bindex);
				if (regId == null || update) {
					regId = picker.create(bindex);
					logger.info("Registration id: {}", regId);
				}
			}

			return regId;
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public void close() throws IOException {
		picker.close();
	}

	public BindexRegister update(boolean overwrite) {
		this.update = overwrite;
		return this;
	}

	public void scanProjects(File basedir, boolean callLLM) throws IOException {
		scanProjects(basedir, false, callLLM);
	}

}
