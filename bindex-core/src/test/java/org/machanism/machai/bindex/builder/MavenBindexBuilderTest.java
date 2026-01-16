package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.junit.jupiter.api.Test;
import org.machanism.machai.project.layout.MavenProjectLayout;

class MavenBindexBuilderTest {

	@Test
	void removeNotImportantData_nullsOutExpectedModelFields() {
		// Arrange
		MavenProjectLayout layout = mock(MavenProjectLayout.class);
		Model model = new Model();
		Build build = new Build();
		model.setBuild(build);
		when(layout.getModel()).thenReturn(model);
		MavenBindexBuilder builder = new MavenBindexBuilder(layout);

		// Act
		builder.removeNotImportantData(model);

		// Assert
		assertNull(model.getDistributionManagement());
		assertNull(model.getBuild());
		assertNull(model.getDependencyManagement());
		assertNull(model.getReporting());
		assertNull(model.getScm());
	}

	@Test
	void projectContext_doesNotThrowWithEmptyBuildAndNoResources() {
		// Arrange
		MavenProjectLayout layout = mock(MavenProjectLayout.class);
		Model model = mock(Model.class);
		Build build = mock(Build.class);
		when(layout.getModel()).thenReturn(model);
		when(model.getBuild()).thenReturn(build);
		when(build.getSourceDirectory()).thenReturn(null);
		when(build.getResources()).thenReturn(null);
		when(build.getTestResources()).thenReturn(null);
		when(build.getTestSourceDirectory()).thenReturn(null);

		MavenBindexBuilder builder = new MavenBindexBuilder(layout);
		builder.genAIProvider(mock(org.machanism.machai.ai.manager.GenAIProvider.class));
		when(model.getBuild()).thenReturn(build);

		// Act + Assert
		assertDoesNotThrow(builder::projectContext);
		verify(model).setDistributionManagement(null);
		verify(model).setBuild(null);
		verify(model).setProperties(null);
		verify(model).setDependencyManagement(null);
		verify(model).setReporting(null);
		verify(model).setScm(null);
		verify(model).setPluginRepositories(null);
	}
}
