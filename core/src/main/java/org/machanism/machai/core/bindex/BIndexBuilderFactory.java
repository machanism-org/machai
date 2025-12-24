package org.machanism.machai.core.bindex;

import java.io.File;
import java.io.FileNotFoundException;

public class BIndexBuilderFactory {

	public static BIndexBuilder builder(File projectDir, boolean create) throws FileNotFoundException {
		BIndexBuilder bindex = null;
		if (MavenBIndexBuilder.isMavenProject(projectDir)) {
			bindex = new MavenBIndexBuilder().effectivePomRequired(create);
		} else if (JScriptBIndexBuilder.isPackageJsonPresent(projectDir)) {
			bindex = new JScriptBIndexBuilder();
		} else if (PythonBIndexBuilder.isPythonProject(projectDir)) {
			bindex = new PythonBIndexBuilder();
		} else if (projectDir.exists()) {
			bindex = new DefaultBIndexBuilder();
		} else {
			throw new FileNotFoundException(projectDir.getAbsolutePath());
		}
		return bindex;
	}

}
