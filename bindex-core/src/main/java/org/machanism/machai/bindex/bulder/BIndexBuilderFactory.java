package org.machanism.machai.bindex.bulder;

import java.io.File;
import java.io.FileNotFoundException;

public class BIndexBuilderFactory {

	public static BIndexBuilder builder(File projectDir) throws FileNotFoundException {
		BIndexBuilder bindexBuilder;
		if (MavenBIndexBuilder.isMavenProject(projectDir)) {
			bindexBuilder = new MavenBIndexBuilder();
		} else if (JScriptBIndexBuilder.isPackageJsonPresent(projectDir)) {
			bindexBuilder = new JScriptBIndexBuilder();
		} else if (PythonBIndexBuilder.isPythonProject(projectDir)) {
			bindexBuilder = new PythonBIndexBuilder();
		} else if (projectDir.exists()) {
			bindexBuilder = new DefaultBIndexBuilder();
		} else {
			throw new FileNotFoundException(projectDir.getAbsolutePath());
		}

		bindexBuilder.projectDir(projectDir);
		return bindexBuilder;
	}

}
