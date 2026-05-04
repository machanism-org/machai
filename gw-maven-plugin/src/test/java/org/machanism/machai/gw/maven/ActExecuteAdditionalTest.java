package org.machanism.machai.gw.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.junit.Test;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.processor.ActProcessor;
import org.machanism.machai.gw.processor.GWConstants;
import org.machanism.machai.project.layout.ProjectLayout;

public class ActExecuteAdditionalTest {

    @Test
    public void execute_configuresProcessorForInteractiveParallelAndNonRecursiveModes() throws Exception {
        CapturingActMojo mojo = new CapturingActMojo();
        mojo.basedir = new File(".").getCanonicalFile();
        mojo.model = "fallback-model";

        MavenProject currentProject = new MavenProject();
        currentProject.setFile(new File(mojo.basedir, "pom.xml"));
        Model currentModel = new Model();
        currentModel.setArtifactId("current");
        currentModel.setModules(Arrays.asList("module-a", "module-b"));
        currentProject.setModel(currentModel);
        currentProject.setArtifactId("current");
        mojo.project = currentProject;

        Properties userProperties = new Properties();
        MavenSession session = newSession(userProperties, true, mojo.basedir.getAbsolutePath(), true, 3,
                Collections.singletonList(currentProject));
        mojo.session = session;
        setSettings(mojo);

        PropertiesConfigurator configuration = new PropertiesConfigurator();
        configuration.set(GWConstants.INTERACTIVE_MODE_PROP_NAME, "true");
        configuration.set(GWConstants.MODEL_PROP_NAME, "configured-model");
        mojo.configuration = configuration;

        mojo.execute();

        assertNotNull(mojo.capturedProcessor);
        assertTrue(mojo.capturedProcessor.isNonRecursive());
        assertTrue(mojo.capturedProcessor.isInteractive());
        assertEquals(3, readIntField(mojo.capturedProcessor, "degreeOfConcurrency"));

        ProjectLayout layout = mojo.capturedProcessor.getProjectLayout(mojo.basedir);
        assertNotNull(layout);
        assertSame(mojo.basedir, layout.getProjectDir());
    }

    @SuppressWarnings("java:S1874")
    private static MavenSession newSession(Properties userProperties, boolean projectPresent, String executionRoot,
            boolean parallel, int degreeOfConcurrency, java.util.List<MavenProject> allProjects) {
        DefaultMavenExecutionRequest request = new DefaultMavenExecutionRequest();
        request.setUserProperties(userProperties);
        request.setProjectPresent(projectPresent);
        request.setDegreeOfConcurrency(degreeOfConcurrency);
        return new MavenSession(null, null, request, null) {
            @Override
            public String getExecutionRootDirectory() {
                return executionRoot;
            }

            @Override
            public java.util.List<MavenProject> getAllProjects() {
                return allProjects;
            }

            @Override
            public boolean isParallel() {
                return parallel;
            }
        };
    }

    private static int readIntField(Object target, String fieldName) throws Exception {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.getInt(target);
            } catch (NoSuchFieldException e) {
                type = type.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private static void setSettings(AbstractGWMojo mojo) throws Exception {
        Field field = AbstractGWMojo.class.getDeclaredField("settings");
        field.setAccessible(true);
        field.set(mojo, new Settings());
    }

    static class CapturingActMojo extends ActMojo {
        private PropertiesConfigurator configuration;
        private ActProcessor capturedProcessor;

        @Override
        protected PropertiesConfigurator getConfiguration() {
            return configuration;
        }

        @Override
        protected void process(ActProcessor actProcessor) {
            this.capturedProcessor = actProcessor;
        }
    }
}
