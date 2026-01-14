package org.machanism.machai.maven;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GW} Maven Mojo.
 * <p>
 * Verifies initialization of Process Mojo and non-exceptional execution with default settings.
 * Does not invoke downstream AI or file processors.
 * </p>
 */
public class ProcessTest {

    private GW process;

    @TempDir
    File tempDir;
    private MavenProject testProject;

    @BeforeEach
    void setup() {
        process = new GW();
        process.basedir = tempDir;
        Model model = new Model();
        testProject = new MavenProject(model);
        process.project = testProject;
        process.chatModel = null; // default
    }

    /**
     * Tests that the execute method completes without exception.
     */
    @Test
    @Disabled
    void testExecute() {
        assertDoesNotThrow(() -> process.execute());
    }
}
