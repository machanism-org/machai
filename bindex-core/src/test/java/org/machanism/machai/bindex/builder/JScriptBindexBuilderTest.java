package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.ProjectLayout;

/**
 * This module no longer contains the real JScriptBindexBuilder implementation.
 * Keep a minimal smoke test around the project layout helper.
 */
class JScriptBindexBuilderTest {

	@Test
	void projectLayout_helper_createsLayout(@TempDir File tempDir) {
		ProjectLayout layout = TestProjectLayouts.projectLayout(tempDir);
		assertNotNull(layout);
		assertNotNull(layout.getProjectDir());
	}
}
