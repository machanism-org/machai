package org.machanism.machai.core.bindex;

import java.io.File;
import java.io.FileNotFoundException;

public class BIndexBuilderFactory {

	public static BIndexBuilder builder(File projectDir, boolean create, boolean callLLM) throws FileNotFoundException {
		BIndexBuilder bindex = null;
		if (MavenBIndexBuilder.isMavenProject(projectDir)) {
			bindex = new MavenBIndexBuilder(callLLM).effectivePomRequired(create);
		} else if (JScriptBIndexBuilder.isPackageJsonPresent(projectDir)) {
			bindex = new JScriptBIndexBuilder(callLLM);
		} else if (PythonBIndexBuilder.isPythonProject(projectDir)) {
			bindex = new PythonBIndexBuilder(callLLM);
		} else if (projectDir.exists()) {
			bindex = new DefaultBIndexBuilder(callLLM);
		} else {
			throw new FileNotFoundException(projectDir.getAbsolutePath());
		}
		return bindex;
	}

}
