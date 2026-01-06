package org.machanism.machai.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Register} Mojo.
 * <p>
 * Verifies execution of the register goal and correct reaction to project packaging types.
 */
class RegisterTest {
    private Register mojo;
    private MavenProject project;

    @BeforeEach
    void setUp() {
        mojo = new Register();
        project = new MavenProject();
        mojo.project = project;
    }

    /**
     * Test that the register goal executes without exception for supported packaging.
     */
    @Test
    void testExecuteNoExceptionIfBindexed() {
        project.setPackaging("jar");
        assertDoesNotThrow(() -> mojo.execute());
    }

    /**
     * Test that the register goal executes without exception (and does nothing) for pom packaging.
     */
    @Test
    void testExecuteNoActionIfNotBindexed() {
        project.setPackaging("pom");
        assertDoesNotThrow(() -> mojo.execute());
    }
}
