package org.machanism.machai.project.layout;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class DefaultProjectLayout extends ProjectLayout {

	@Override
	public List<String> getModules() throws IOException {
		List<String> modules = new ArrayList<>();
		File[] listFiles = getProjectDir().listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory()
						&& !StringUtils.startsWithAny(pathname.getName(), EXCLUDE_DIRS);
			}
		});

		if (listFiles != null) {
			for (File file : listFiles) {
				modules.add(file.getName());
			}
		}

		return modules;
	}

	@Override
	public List<String> getSources() {
		return null;
	}

	@Override
	public List<String> getDocuments() {
		return null;
	}

	@Override
	public List<String> getTests() {
		return null;
	}

}
