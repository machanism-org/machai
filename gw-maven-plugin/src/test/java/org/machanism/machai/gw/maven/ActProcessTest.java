package org.machanism.machai.gw.maven;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.processor.ActProcessor;
import org.machanism.machai.gw.processor.Ghostwriter;

public class ActProcessTest {

	static class TestableAct extends ActMojo {
		boolean configureAndScanCalled;

		@Override
		public void configureAndScan(ActProcessor actProcessor) throws MojoExecutionException, IOException {
			configureAndScanCalled = true;
		}
	}

	static class RecordingActProcessor extends ActProcessor {
		String actsLocation;
		String[] excludes;
		boolean logInputs;
		private final PropertiesConfigurator configurator = new PropertiesConfigurator();

		RecordingActProcessor() {
			super(new File("."), new PropertiesConfigurator(), null);
		}

		@Override
		public PropertiesConfigurator getConfigurator() {
			return configurator;
		}

		@Override
		public void setActsLocation(String actsLocation) {
			this.actsLocation = actsLocation;
		}

		@Override
		public void setExcludes(String... excludes) {
			this.excludes = excludes;
		}

		@Override
		public void setLogInputs(boolean logInputs) {
			this.logInputs = logInputs;
		}
	}

	@Test
	public void process_whenActsLocationProvided_setsActsLocation() throws Exception {
		TestableAct act = new TestableAct();
		RecordingActProcessor processor = new RecordingActProcessor();
		processor.getConfigurator().set(Ghostwriter.ACTS_LOCATION_PROP_NAME, "c:/acts");

		act.process(processor);

		assertTrue(act.configureAndScanCalled);
		assertFalse(processor.logInputs);
		assertEquals("c:/acts", processor.actsLocation);
	}

	@Test
	public void process_whenNoExcludesConfigured_setsNullExcludes() throws Exception {
		TestableAct act = new TestableAct();
		RecordingActProcessor processor = new RecordingActProcessor();

		act.process(processor);

		assertTrue(act.configureAndScanCalled);
		assertNull(processor.excludes);
		assertNull(act.excludes);
	}

	@Test
	public void process_whenExcludesConfigured_splitsAndUsesConfiguredValues() throws Exception {
		TestableAct act = new TestableAct();
		RecordingActProcessor processor = new RecordingActProcessor();
		processor.getConfigurator().set(Ghostwriter.EXCLUDES_PROP_NAME, "one,two");

		act.process(processor);

		assertArrayEquals(new String[] { "one", "two" }, processor.excludes);
	}

	@Test
	public void process_whenConfigureAndScanThrowsIOException_wrapsIntoMojoExecutionException() throws Exception {
		ActMojo act = new ActMojo() {
			@Override
			public void configureAndScan(ActProcessor actProcessor) throws MojoExecutionException, IOException {
				throw new IOException("boom");
			}
		};
		RecordingActProcessor processor = new RecordingActProcessor();

		try {
			act.process(processor);
			fail("Expected MojoExecutionException");
		} catch (MojoExecutionException e) {
			assertTrue(e.getMessage().contains("I/O error occurred during file processing"));
			assertTrue(e.getCause() instanceof IOException);
		}
	}
}
