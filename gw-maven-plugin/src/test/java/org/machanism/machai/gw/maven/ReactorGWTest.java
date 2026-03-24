package org.machanism.machai.gw.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;
import org.machanism.machai.ai.tools.CommandFunctionTools.ProcessTerminationException;
import org.machanism.machai.gw.processor.GuidanceProcessor;
import org.mockito.Mockito;

public class ReactorGWTest {

	@Test
	public void scanDocumentsOrFail_whenProcessTerminationException_wrapsWithMessageAndCause() throws Exception {
		// Arrange
		ReactorGW spy = Mockito.spy(new ReactorGW());
		GuidanceProcessor processor = Mockito.mock(GuidanceProcessor.class);
		ProcessTerminationException termination = new ProcessTerminationException("terminated", 123);
		Mockito.doThrow(termination).when(spy).scanDocuments(Mockito.any(GuidanceProcessor.class));

		Method m = ReactorGW.class.getDeclaredMethod("scanDocumentsOrFail", GuidanceProcessor.class);
		m.setAccessible(true);

		// Act
		try {
			m.invoke(spy, processor);
			fail("Expected MojoExecutionException");
		} catch (java.lang.reflect.InvocationTargetException e) {
			// Assert
			Throwable cause = e.getCause();
			if (!(cause instanceof MojoExecutionException)) {
				throw e;
			}
			MojoExecutionException mee = (MojoExecutionException) cause;
			assertEquals(
					"Process terminated while scanning documents: terminated (exit code: 123)",
					mee.getMessage());
			assertEquals(termination, mee.getCause());
		}
	}
}
