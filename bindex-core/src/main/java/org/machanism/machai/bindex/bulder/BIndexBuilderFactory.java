package org.machanism.machai.bindex.bulder;

import java.io.File;
import java.io.FileNotFoundException;

public class BIndexBuilderFactory {

	public static BIndexBuilder builder(File projectDir, boolean create, boolean callLLM) throws FileNotFoundException {
		BIndexBuilder bindexBuilder;
		if (MavenBIndexBuilder.isMavenProject(projectDir)) {
			bindexBuilder = new MavenBIndexBuilder(callLLM).effectivePomRequired(create);
		} else if (JScriptBIndexBuilder.isPackageJsonPresent(projectDir)) {
			bindexBuilder = new JScriptBIndexBuilder(callLLM);
		} else if (PythonBIndexBuilder.isPythonProject(projectDir)) {
			bindexBuilder = new PythonBIndexBuilder(callLLM);
		} else if (projectDir.exists()) {
			bindexBuilder = new DefaultBIndexBuilder(callLLM);
		} else {
			throw new FileNotFoundException(projectDir.getAbsolutePath());
		}

		bindexBuilder.projectDir(projectDir);
		return bindexBuilder;
	}

}
