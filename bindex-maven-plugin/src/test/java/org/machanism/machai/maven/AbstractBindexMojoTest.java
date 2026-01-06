package org.machanism.machai.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AbstractBindexMojo}.
 * <p>
 * Covers the determination of Bindex applicability based on project packaging.
 */
class AbstractBindexMojoTest {
    private AbstractBindexMojo mojo;
    private MavenProject project;
    private File basedir;

    @BeforeEach
    void setUp() {
        // Anonymous subclass since AbstractBindexMojo is abstract
        mojo = new AbstractBindexMojo() {
            @Override
            public void execute() throws MojoExecutionException, MojoFailureException {
                // Not required for these tests
            }
        };
        project = new MavenProject();
        basedir = new File(".");
        mojo.project = project;
        mojo.basedir = basedir;
    }

    /**
     * Test that isBindexed returns true when packaging is not "pom".
     */
    @Test
    void testIsBindexedTrue() {
        project.setPackaging("jar");
        assertTrue(mojo.isBindexed());
    }

    /**
     * Test that isBindexed returns false when packaging is "pom".
     */
    @Test
    void testIsBindexedFalse() {
        project.setPackaging("pom");
        assertFalse(mojo.isBindexed());
    }
}
