package org.machanism.machai.gw.maven;

import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.gw.processor.ActProcessor;
import org.machanism.machai.gw.processor.Ghostwriter;
import org.mockito.Mockito;

public class ActProcessTest {

	static class TestableAct extends Act {
		boolean configureAndScanCalled;

		@Override
		public void configureAndScan(ActProcessor actProcessor) throws MojoExecutionException, IOException {
			configureAndScanCalled = true;
		}
	}

	@Test
	public void process_whenActsLocationProvided_setsActsLocation() throws Exception {
		// Arrange
		TestableAct act = new TestableAct();
		ActProcessor processor = Mockito.mock(ActProcessor.class);
		Configurator conf = Mockito.mock(Configurator.class);
		Mockito.when(processor.getConfigurator()).thenReturn(conf);
		Mockito.when(conf.get(Mockito.eq(Ghostwriter.ACTS_LOCATION_PROP_NAME), Mockito.any())).thenReturn("c:/acts");
		Mockito.when(conf.get(Ghostwriter.EXCLUDES_PROP_NAME, null)).thenReturn(null);

		// Act
		act.process(processor);

		// Assert
		Mockito.verify(processor).setActsLocation("c:/acts");
		Mockito.verify(processor).setLogInputs(false);
	}

	@Test
	public void process_whenNoExcludesConfigured_setsNullExcludes() throws Exception {
		// Arrange
		TestableAct act = new TestableAct();
		ActProcessor processor = Mockito.mock(ActProcessor.class);
		Configurator conf = Mockito.mock(Configurator.class);
		Mockito.when(processor.getConfigurator()).thenReturn(conf);
		Mockito.when(conf.get(Mockito.eq(Ghostwriter.ACTS_LOCATION_PROP_NAME), Mockito.any())).thenReturn(null);
		Mockito.when(conf.get(Ghostwriter.EXCLUDES_PROP_NAME, null)).thenReturn(null);

		// Act
		act.process(processor);

		// Assert
		Mockito.verify(processor).setExcludes((String[]) Mockito.isNull());
		assertNull(act.excludes);
	}

	@Test(expected = MojoExecutionException.class)
	public void process_whenConfigureAndScanThrowsIOException_wrapsIntoMojoExecutionException() throws Exception {
		// Arrange
		Act act = new Act() {
			@Override
			public void configureAndScan(ActProcessor actProcessor) throws MojoExecutionException, IOException {
				throw new IOException("boom");
			}
		};
		ActProcessor processor = Mockito.mock(ActProcessor.class);
		Configurator conf = Mockito.mock(Configurator.class);
		Mockito.when(processor.getConfigurator()).thenReturn(conf);
		Mockito.when(conf.get(Mockito.eq(Ghostwriter.ACTS_LOCATION_PROP_NAME), Mockito.any())).thenReturn(null);
		Mockito.when(conf.get(Ghostwriter.EXCLUDES_PROP_NAME, null)).thenReturn(null);

		// Act
		act.process(processor);
	}
}
