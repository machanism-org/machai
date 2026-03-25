package org.machanism.machai.gw.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Targets the private static helper methods in {@link GW} via reflection.
 */
public class GWPrivateMethodsTest {

	private static Method getResolveMethod() throws Exception {
		Method m = GW.class.getDeclaredMethod("resolveProjectByArtifactId", java.util.List.class, Model.class);
		m.setAccessible(true);
		return m;
	}

	private static Method getToCoordMethod() throws Exception {
		Method m = GW.class.getDeclaredMethod("toCoord", MavenProject.class);
		m.setAccessible(true);
		return m;
	}

	private static MavenProject newProject(String groupId, String artifactId, String version, File basedir) {
		MavenProject p = new MavenProject();
		p.setGroupId(groupId);
		p.setArtifactId(artifactId);
		p.setVersion(version);
		p.setFile(basedir == null ? null : new File(basedir, "pom.xml"));
		return p;
	}

	@Test
	public void resolveProjectByArtifactId_nullOrEmptyInputs_returnsNull() throws Exception {
		// Arrange
		Method resolve = getResolveMethod();
		Model model = new Model();
		model.setArtifactId("a");

		// Act + Assert
		assertNull(resolve.invoke(null, null, model));
		assertNull(resolve.invoke(null, Collections.emptyList(), model));
		assertNull(resolve.invoke(null, Collections.singletonList(newProject("g", "a", "1", new File("."))), null));
	}

	@Test
	public void resolveProjectByArtifactId_blankArtifactId_returnsNull() throws Exception {
		// Arrange
		Method resolve = getResolveMethod();
		Model model = new Model();
		model.setArtifactId("   ");

		// Act
		Object result = resolve.invoke(null,
				Collections.singletonList(newProject("g", "a", "1", new File("."))), model);

		// Assert
		assertNull(result);
	}

	@Test
	public void resolveProjectByArtifactId_singleMatch_returnsMatchingProject() throws Exception {
		// Arrange
		Method resolve = getResolveMethod();
		Model model = new Model();
		model.setArtifactId("target");
		MavenProject p1 = newProject("g", "other", "1", new File("module1"));
		MavenProject p2 = newProject("g", "target", "1", new File("module2"));

		// Act
		Object result = resolve.invoke(null, Arrays.asList(p1, p2), model);

		// Assert
		assertEquals(p2, result);
	}

	@Test
	public void resolveProjectByArtifactId_multipleMatches_throwsIllegalStateException() throws Exception {
		// Arrange
		Method resolve = getResolveMethod();
		Model model = new Model();
		model.setArtifactId("dup");
		MavenProject p1 = newProject("g", "dup", "1", new File("m1"));
		MavenProject p2 = newProject("g", "dup", "2", new File("m2"));

		// Act
		try {
			resolve.invoke(null, Arrays.asList(p1, p2), model);
			Assert.fail("Expected IllegalStateException due to multiple matching projects"); // Sonar java:S2699
		} catch (InvocationTargetException e) {
			// Assert
			if (!(e.getCause() instanceof IllegalStateException)) {
				throw e;
			}
		}
	}

	@Test
	public void toCoord_nullProject_returnsNullToken() throws Exception {
		// Arrange
		Method toCoord = getToCoordMethod();

		// Act
		Object coord = toCoord.invoke(null, new Object[] { null });

		// Assert
		assertEquals("<null>", coord);
	}

	@Test
	public void toCoord_missingFields_doesNotReturnLiteralNull() throws Exception {
		// Arrange
		Method toCoord = getToCoordMethod();
		MavenProject p = new MavenProject();

		// Act
		String coord = (String) toCoord.invoke(null, p);

		// Assert
		Assert.assertEquals(-1, coord.indexOf("null"));
		Assert.assertTrue(coord.contains(":"));
		Assert.assertTrue(coord.contains("@"));
	}
}
