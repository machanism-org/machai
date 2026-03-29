package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;

/**
 * This test forces the jar/zip scanning branch of {@link ActFunctionTools#getBaseActList()}.
 */
class ActFunctionToolsJarParsingTest {

	@TempDir
	Path tempDir;

	@Test
	void getBaseActList_whenToolsAndGhostwriterLoadedFromJar_thenScansActsAndIncludesHelpAct() throws Exception {
		// Arrange
		Path jarPath = tempDir.resolve("gw.jar");
		createJarWithGhostwriterAndAct(jarPath);

		ClassLoader parent = getClass().getClassLoader();
		try (URLClassLoader cl = new URLClassLoader(new URL[] { jarPath.toUri().toURL() }, parent) {
			@Override
			protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
				// Child-first for our package so ActFunctionTools uses the jar-loaded Ghostwriter.
				if (name.startsWith("org.machanism.machai.gw.")) {
					Class<?> loaded = findLoadedClass(name);
					if (loaded == null) {
						try {
							loaded = findClass(name);
						} catch (ClassNotFoundException ignore) {
							// fallback to parent
						}
					}
					if (loaded != null) {
						if (resolve) {
							resolveClass(loaded);
						}
						return loaded;
					}
				}
				return super.loadClass(name, resolve);
			}
		}) {
			Class<?> toolsClass = cl.loadClass("org.machanism.machai.gw.tools.ActFunctionTools");
			Object tools = toolsClass.getDeclaredConstructor().newInstance();

			toolsClass.getMethod("setConfigurator", cl.loadClass("org.machanism.macha.core.commons.configurator.Configurator"))
					.invoke(tools, new PropertiesConfigurator());

			String ghostwriterCs = cl.loadClass("org.machanism.machai.gw.processor.Ghostwriter").getProtectionDomain()
					.getCodeSource().getLocation().toString();
			assertTrue(ghostwriterCs.contains("gw.jar"), "Expected Ghostwriter CodeSource to be jar but was: " + ghostwriterCs);

			// Act
			@SuppressWarnings("unchecked")
			Set<String> acts = (Set<String>) toolsClass.getMethod("getBaseActList").invoke(tools);

			// Assert
			assertNotNull(acts);
			assertEquals(1, acts.size());
			String only = acts.iterator().next();
			assertTrue(only.contains("`help`"), "Expected act name wrapped in backticks but was: " + only);
			assertTrue(only.contains("act"), "Expected description to be present but was: " + only);
		}
	}

	private void createJarWithGhostwriterAndAct(Path jarPath) throws IOException {
		try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(jarPath))) {
			addCompiledClassToJar(jos, "org/machanism/machai/gw/processor/Ghostwriter.class");
			addCompiledClassToJar(jos, "org/machanism/machai/gw/tools/ActFunctionTools.class");

			// The description actually returned comes from ActProcessor.tryLoadActFromClasspath(),
			// i.e., from the real classpath resources; this entry is primarily to trigger jar scanning.
			JarEntry actEntry = new JarEntry("acts/help.toml");
			jos.putNextEntry(actEntry);
			jos.write("description='Help act'\n".getBytes(StandardCharsets.UTF_8));
			jos.closeEntry();
		}
	}

	private void addCompiledClassToJar(JarOutputStream jos, String classPath) throws IOException {
		File compiled = new File("target/test-classes", classPath);
		if (!compiled.exists()) {
			compiled = new File("target/classes", classPath);
		}
		assertTrue(compiled.exists(), "Expected compiled class to exist: " + compiled.getAbsolutePath());

		JarEntry entry = new JarEntry(classPath);
		jos.putNextEntry(entry);
		jos.write(Files.readAllBytes(compiled.toPath()));
		jos.closeEntry();
	}
}
