package org.machanism.machai.bindex;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema.BIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindexRegister extends BIndexProjectProcessor implements Closeable {

	private static Logger logger = LoggerFactory.getLogger(BindexRegister.class);

	private Picker picker;
	private boolean update;

	public BindexRegister(GenAIProvider provider) {
		super();
		picker = new Picker(provider);
	}

	public void processProject(ProjectLayout projectLayout) {
		BIndex bindex;
		try {
			File projectDir = projectLayout.getProjectDir();
			bindex = getBindex(projectDir);

			String regId = null;
			if (bindex != null) {
				regId = picker.getRegistredId(bindex);
				if (regId == null || update) {
					regId = picker.create(bindex);
					logger.info("Registration id: {}", regId);
				}
			}

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

}
