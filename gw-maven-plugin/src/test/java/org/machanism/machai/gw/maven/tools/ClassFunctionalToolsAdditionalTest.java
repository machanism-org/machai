package org.machanism.machai.gw.maven.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ClassFunctionalToolsAdditionalTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void findClass_acceptsJsonNodeParameters() throws Exception {
        MavenProject project = createProjectForMainSources();
        ClassFunctionalTools tools = new ClassFunctionalTools(project);

        String classes = tools.findClass(OBJECT_MAPPER.readTree("{\"className\":\"ClassFunctionalTools\"}"), project.getBasedir());

        assertTrue(classes.contains("org.machanism.machai.gw.maven.tools.ClassFunctionalTools"));
    }

    @Test
    public void findClass_returnsNotFoundWhenClassNameParameterIsMissing() throws Exception {
        ClassFunctionalTools tools = new ClassFunctionalTools();
        File basedir = new File(".");
        setProjectMap(tools, Collections.singletonMap(basedir, new StubClassInfoHolder(createProjectForMainSources())));

        String classes = tools.findClass(new HashMap<String, Object>(), basedir);

        assertEquals("Class not found.", classes);
    }

    @Test
    public void getClassInfo_acceptsJsonNodeAndIncludesArtifactMetadata() throws Exception {
        MavenProject project = createProjectForMainSources();
        ClassFunctionalTools tools = new ClassFunctionalTools();
        StubClassInfoHolder holder = new StubClassInfoHolder(project);
        holder.loadedClass = SampleType.class;
        holder.classPath = "target/classes";
        holder.artifactId = "g:a:1";
        holder.sourcePath = "src/main/java/sample/SampleType.java";
        setProjectMap(tools, Collections.singletonMap(project.getBasedir(), holder));

        Map<String, Object> info = tools.getClassInfo(
                OBJECT_MAPPER.readTree("{\"className\":\"sample.SampleType\"}"), project.getBasedir());

        assertEquals(SampleType.class.getName(), info.get("className"));
        assertEquals("target/classes", info.get("path"));
        assertEquals("g:a:1", info.get("artifact"));
        assertEquals("src/main/java/sample/SampleType.java", info.get("sourcePath"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> fields = (List<Map<String, Object>>) info.get("fields");
        assertEquals(2, fields.size());
        assertTrue(fields.stream().anyMatch(field -> "visibleField".equals(field.get("name"))));
        assertFalse(fields.stream().anyMatch(field -> "hiddenField".equals(field.get("name"))));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> methods = (List<Map<String, Object>>) info.get("methods");
        assertTrue(methods.stream().anyMatch(method -> "visibleMethod".equals(method.get("name"))));
        assertFalse(methods.stream().anyMatch(method -> "hiddenMethod".equals(method.get("name"))));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> constructors = (List<Map<String, Object>>) info.get("constructors");
        assertEquals(2, constructors.size());
        assertTrue(constructors.stream().anyMatch(ctor -> ((List<?>) ctor.get("parameterTypes")).isEmpty()));
        assertTrue(constructors.stream().anyMatch(ctor -> Arrays.asList(String.class.getName()).equals(ctor.get("parameterTypes"))));

        @SuppressWarnings("unchecked")
        List<String> interfaces = (List<String>) info.get("interfaces");
        assertEquals(Collections.singletonList(Runnable.class.getName()), interfaces);

        @SuppressWarnings("unchecked")
        List<String> annotations = (List<String>) info.get("annotations");
        assertEquals(1, annotations.size());
        assertTrue(annotations.get(0).contains(Marker.class.getSimpleName()));
    }

    @Test
    public void getClassInfo_handlesMissingClassNameParameter() throws Exception {
        MavenProject project = createProjectForMainSources();
        ClassFunctionalTools tools = new ClassFunctionalTools();
        StubClassInfoHolder holder = new StubClassInfoHolder(project);
        setProjectMap(tools, Collections.singletonMap(project.getBasedir(), holder));

        Map<String, Object> info = tools.getClassInfo(new Object(), project.getBasedir());

        assertEquals("Class not found: null", info.get("error"));
        assertNull(info.get("artifact"));
    }

    private static void setProjectMap(ClassFunctionalTools tools, Map<File, ClassInfoHolder> value) throws Exception {
        Field field = ClassFunctionalTools.class.getDeclaredField("classInfoProjectMap");
        field.setAccessible(true);
        field.set(tools, value);
    }

    private static MavenProject createProjectForMainSources() {
        Model model = new Model();
        model.setBuild(new Build());
        MavenProject project = new MavenProject(model) {
            @Override
            public File getBasedir() {
                return new File(".");
            }

            @Override
            public java.util.List<String> getCompileClasspathElements() {
                return Collections.singletonList(new File("target/classes").getAbsolutePath());
            }
        };
        project.getBuild().setOutputDirectory(new File("target/classes").getAbsolutePath());
        project.getBuild().setTestOutputDirectory(new File("target/test-classes").getAbsolutePath());
        project.addCompileSourceRoot(new File("src/main/java").getAbsolutePath());
        return project;
    }

    private static class StubClassInfoHolder extends ClassInfoHolder {
        private Class<?> loadedClass;
        private String classPath;
        private String artifactId;
        private String sourcePath;

        StubClassInfoHolder(MavenProject project) {
            super(project);
        }

        @Override
        public List<com.google.common.reflect.ClassPath.ClassInfo> findClasses(String className) {
            if (className == null) {
                return Collections.emptyList();
            }
            try {
                return new ClassInfoHolder(createProjectForMainSources()).findClasses(className);
            } catch (RuntimeException e) {
                return Collections.emptyList();
            }
        }

        @Override
        public Class<?> loadClass(String className) throws ClassNotFoundException {
            if (loadedClass == null || className == null) {
                throw new ClassNotFoundException(String.valueOf(className));
            }
            return loadedClass;
        }

        @Override
        public String getClassPath(String className) {
            return classPath;
        }

        @Override
        public String getArtifactId(String className) {
            return artifactId;
        }

        @Override
        public String getSourcePath(String className) {
            return sourcePath;
        }
    }

    @Marker
    public static class SampleType implements Runnable {
        public String visibleField;
        protected int protectedField;

        public SampleType() {
        }

        protected SampleType(String value) {
            this.visibleField = value;
        }

        public String visibleMethod(String input) {
            return input;
        }

        protected void protectedMethod() { // Empty by design for Sonar java:S1186: validates protected method metadata.
        }

        @Override
        public void run() { // Empty by design for Sonar java:S1186: sample Runnable has no runtime action.
        }
    }

    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    private @interface Marker {
    }
}


