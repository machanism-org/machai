package org.machanism.machai.gw.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.junit.Test;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.tools.CommandFunctionTools.ProcessTerminationException;
import org.machanism.machai.gw.processor.GuidanceProcessor;

public class GWPerModuleMojoTest {

    @Test
    public void execute_setsDefaultScanDirAndWrapsProcessTerminationException() throws Exception {
        TestGWPerModuleMojo mojo = new TestGWPerModuleMojo();
        File basedir = new File(".").getAbsoluteFile();
        MavenProject project = new MavenProject(new Model()) {
            @Override
            public File getBasedir() {
                return basedir;
            }
        };
        project.setFile(new File(basedir, "pom.xml"));

        MavenSession session = createSession(true, basedir.getAbsolutePath());
        setAbstractField(mojo, "project", project);
        setAbstractField(mojo, "basedir", basedir);
        setAbstractField(mojo, "session", session);
        setAbstractField(mojo, "settings", new Settings());

        try {
            mojo.execute();
        } catch (MojoExecutionException e) {
            assertTrue(e.getMessage().contains("Process terminated while scanning documents: boom (exit code: 7)"));
            assertNotNull(mojo.capturedProcessor);
            assertEquals(basedir.getAbsolutePath(), mojo.scanDir);
            assertTrue(mojo.capturedProjectPresent);
            return;
        }
        throw new AssertionError("Expected MojoExecutionException");
    }

    @Test
    public void execute_usesExistingScanDirAndSkipsToolRegistrationWhenProjectMissing() throws Exception {
        TestGWPerModuleMojo mojo = new TestGWPerModuleMojo();
        File basedir = new File(".").getAbsoluteFile();
        MavenProject project = new MavenProject(new Model()) {
            @Override
            public File getBasedir() {
                return basedir;
            }
        };
        project.setFile(new File(basedir, "pom.xml"));

        MavenSession session = createSession(false, basedir.getAbsolutePath());
        setAbstractField(mojo, "project", project);
        setAbstractField(mojo, "basedir", basedir);
        setAbstractField(mojo, "session", session);
        setAbstractField(mojo, "settings", new Settings());
        setAbstractField(mojo, "scanDir", "custom-scan");

        mojo.execute();

        assertEquals("custom-scan", mojo.scanDir);
        assertNotNull(mojo.capturedProcessor);
        assertFalse(mojo.capturedProjectPresent);
    }

    @SuppressWarnings("deprecation")
	private static MavenSession createSession(boolean projectPresent, String executionRootDirectory) {
        DefaultMavenExecutionRequest request = new DefaultMavenExecutionRequest();
        request.setProjectPresent(projectPresent);
        return new MavenSession(null, null, request, null) {
            @Override
            public String getExecutionRootDirectory() {
                return executionRootDirectory;
            }
        };
    }

    private static void setAbstractField(Object target, String fieldName, Object value) throws Exception {
        Field field = AbstractGWMojo.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    static class TestGWPerModuleMojo extends GWPerModuleMojo {
        GuidanceProcessor capturedProcessor;
        boolean capturedProjectPresent;

        @Override
        protected PropertiesConfigurator getConfiguration() {
            return new PropertiesConfigurator();
        }

        @Override
        protected void scanDocuments(GuidanceProcessor processor) throws MojoExecutionException {
            this.capturedProcessor = processor;
            this.capturedProjectPresent = session.getRequest().isProjectPresent();
            if ("custom-scan".equals(this.scanDir)) {
                return;
            }
            throw new ProcessTerminationException("boom", 7);
        }
    }
}
