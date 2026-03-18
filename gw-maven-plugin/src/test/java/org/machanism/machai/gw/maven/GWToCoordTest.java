package org.machanism.machai.gw.maven;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.lang.reflect.Method;

import org.apache.maven.project.MavenProject;
import org.junit.Test;

public class GWToCoordTest {

	@Test
	public void toCoord_allFieldsPresent_formatsAsGavAndBasedir() throws Exception {
		// Arrange
		Method toCoord = GW.class.getDeclaredMethod("toCoord", MavenProject.class);
		toCoord.setAccessible(true);

		MavenProject p = new MavenProject();
		p.setGroupId("g");
		p.setArtifactId("a");
		p.setVersion("1");
		File basedir = new File("some-dir");
		p.setFile(new File(basedir, "pom.xml"));

		// Act
		String coord = (String) toCoord.invoke(null, p);

		// Assert
		assertEquals("g:a:1@" + basedir, coord);
	}
}
