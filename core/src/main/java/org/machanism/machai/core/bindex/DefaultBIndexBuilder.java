package org.machanism.machai.core.bindex;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class DefaultBIndexBuilder extends BIndexBuilder {

	@Override
	protected void projectContext() throws IOException {
	}

	@Override
	public List<String> getModules() throws IOException {
		List<String> modules = new ArrayList<>();
		File[] listFiles = getProjectDir().listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory()
						&& !StringUtils.startsWithAny(pathname.getName(), STARTS_WITH_EXCLUDE_DIRS);
			}
		});

		for (File file : listFiles) {
			modules.add(file.getName());
		}

		return modules;
	}

}
