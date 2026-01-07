package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema. Bindex;

class BindexBuilderTest {

	private ProjectLayout projectLayout;
	private GenAIProvider genAIProvider;
	private  Bindex origin;
	private BindexBuilder bindexBuilder;

	@BeforeEach
	void setUp() {
		projectLayout = mock(ProjectLayout.class);
		genAIProvider = mock(GenAIProvider.class);
		origin = mock( Bindex.class);
		bindexBuilder = new BindexBuilder(projectLayout).genAIProvider(genAIProvider);
	}

	@Test
	void testOriginSetterAndGetter() {
		BindexBuilder builder = bindexBuilder.origin(origin);
		assertNotNull(builder);
		assertEquals(origin, builder.getOrigin());
	}

	@Test
	void testGenAIProviderSetterAndGetter() {
		assertEquals(genAIProvider, bindexBuilder.getGenAIProvider());
	}

	@Test
	void testProjectLayoutGetter() {
		assertEquals(projectLayout, bindexBuilder.getProjectLayout());
	}

	@Test
	void testBuildReturnsNullOnNullOutput() throws IOException {
		when(genAIProvider.perform()).thenReturn(null);
		assertNull(bindexBuilder.build());
	}

	@Test
	void testBindexSchemaPrompt() throws IOException {
		GenAIProvider provider = mock(GenAIProvider.class);
		BindexBuilder.bindexSchemaPrompt(provider);
		verify(provider, times(1)).prompt(anyString());
	}

	@Test
	void testProjectContextDoesNotThrow() throws IOException {
		assertDoesNotThrow(() -> bindexBuilder.projectContext());
	}
}
