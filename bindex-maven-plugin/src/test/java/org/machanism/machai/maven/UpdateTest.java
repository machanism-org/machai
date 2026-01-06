package org.machanism.machai.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Update} Mojo.
 * <p>
 * Ensures the update goal is executed correctly depending on Maven project packaging.
 */
class UpdateTest {
    private Update mojo;
    private MavenProject project;

    @BeforeEach
    void setUp() {
        mojo = new Update();
        project = new MavenProject();
        mojo.project = project;
    }

    /**
     * Test that the update goal executes without exception for non-pom packaging.
     */
    @Test
    @Disabled("Need to fix.")
    void testExecuteNoExceptionIfBindexed() {
        project.setPackaging("jar");
        assertDoesNotThrow(() -> mojo.execute());
    }

    /**
     * Test that the update goal executes without exception (and does nothing) for pom packaging.
     */
    @Test
    void testExecuteNoActionIfNotBindexed() {
        project.setPackaging("pom");
        assertDoesNotThrow(() -> mojo.execute());
    }
}
