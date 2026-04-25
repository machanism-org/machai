package org.machanism.machai.gw.maven;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collections;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.machanism.machai.gw.processor.ActProcessor;

public class ReactorActTest {

	private static MavenProject projectWithModules(int moduleCount) {
		MavenProject p = new MavenProject();
		for (int i = 0; i < moduleCount; i++) {
			p.getModel().addModule("m" + i);
		}
		return p;
	}

	static class RecordingActProcessor extends ActProcessor {
		boolean nonRecursive;
		boolean scanned;

		RecordingActProcessor() {
			super(new File("."), new org.machanism.macha.core.commons.configurator.PropertiesConfigurator(), null);
		}

		@Override
		public boolean isNonRecursive() {
			return nonRecursive;
		}

		@Override
		public void setNonRecursive(boolean nonRecursive) {
			this.nonRecursive = nonRecursive;
		}

		@Override
		public void scanDocuments(File basedir, String scanDir) {
			scanned = true;
		}
	}

	@Test
	public void scanDocuments_whenNotExecutionRootProject_butActProcessorAlreadyNonRecursive_logsSkipMessage() throws Exception {
		ActPerModuleMojo act = new ActPerModuleMojo();
		File basedir = new File(".").getAbsoluteFile();
		act.basedir = basedir;
		act.project = projectWithModules(0);
		act.session = newSession();

		RecordingActProcessor processor = new RecordingActProcessor();
		processor.nonRecursive = true;

		act.scanDocuments(processor);

		assertTrue(processor.nonRecursive);
		assertFalse(processor.scanned);
	}

	@SuppressWarnings("deprecation")
	private static MavenSession newSession() {
		DefaultMavenExecutionRequest request = new DefaultMavenExecutionRequest();
		return new MavenSession(null, null, request, null) {
			@Override
			public String getExecutionRootDirectory() {
				try {
					// Fix for Sonar java:S1130: handle IOException locally instead of declaring an unused checked exception.
					return new File("..").getCanonicalFile().getAbsolutePath();
				} catch (java.io.IOException e) {
					throw new IllegalStateException(e);
				}
			}

			@Override
			public java.util.List<MavenProject> getAllProjects() {
				return Collections.singletonList(new MavenProject());
			}
		};
	}
}
