package org.machanism.machai.maven;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Create} Mojo.
 * <p>
 * Ensures proper invocation of the create goal and correct behavior based on project packaging.
 */
class CreateTest {
    private Create mojo;
    private MavenProject project;

    @BeforeEach
    void setUp() {
        mojo = new Create();
        project = new MavenProject();
        mojo.project = project;
    }

    /**
     * Test that the create goal executes without exception for supported packaging.
     */
    @Test
    @Disabled("Need to fix.")
    void testExecuteNoExceptionIfBindexed() {
        project.setPackaging("jar");
        assertDoesNotThrow(() -> mojo.execute());
    }

    /**
     * Test that the create goal executes without exception (and does nothing) for unsupported packaging (pom).
     */
    @Test
    void testExecuteNoActionIfNotBindexed() {
        project.setPackaging("pom");
        assertDoesNotThrow(() -> mojo.execute());
    }
}
