package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.ProjectLayout;

/**
 * This module no longer contains the real MavenBindexBuilder implementation.
 * Keep a minimal sanity test for layout creation.
 */
class MavenBindexBuilderAdditionalTest {

	@Test
	void projectLayout_helper_createsLayout(@TempDir File tempDir) {
		ProjectLayout layout = TestProjectLayouts.projectLayout(tempDir);
		assertNotNull(layout);
	}
}
