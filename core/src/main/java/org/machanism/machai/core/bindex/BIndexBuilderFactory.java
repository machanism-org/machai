package org.machanism.machai.core.bindex;

import java.io.File;

public class BIndexBuilderFactory {

	public static BIndexBuilder builder(File projectDir) {
		BIndexBuilder bindex = null;
		if (MavenBIndexBuilder.isMavenProject(projectDir)) {
			bindex = new MavenBIndexBuilder();
		} else if (JScriptBIndexBuilder.isPackageJsonPresent(projectDir)) {
			bindex = new JScriptBIndexBuilder();
		} else if (PythonBIndexBuilder.isPythonProject(projectDir)) {
			bindex = new PythonBIndexBuilder();
		} else {
			bindex = new DefaultBIndexBuilder();
		}
		return bindex;
	}

}
