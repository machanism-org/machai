package org.machanism.machai.gw.maven.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.common.reflect.ClassPath.ClassInfo;

class ClassFunctionalToolsTest {

    private ClassFunctionalTools tools;
    private ClassInfoHolder holder;
    private File projectDirectory;

    @BeforeEach
    void setUp() throws Exception {
        tools = new ClassFunctionalTools();
        holder = Mockito.mock(ClassInfoHolder.class);
        projectDirectory = new File("project");
        Field map = ClassFunctionalTools.class.getDeclaredField("classInfoProjectMap");
        map.setAccessible(true);
        Map<File, ClassInfoHolder> registrations = new HashMap<>();
        registrations.put(projectDirectory, holder);
        map.set(tools, registrations);
    }

    @Test
    void findClass_returnsMatchingFullyQualifiedNames() {
        ClassInfoHolder realHolder = new ClassInfoHolder(projectForMainSources());
        replaceHolder(realHolder);

        List<String> result = tools.findClass("ClassFunctionalTools", projectDirectory);

        assertTrue(result.contains("org.machanism.machai.gw.maven.tools.ClassFunctionalTools"));
    }

    @Test
    void findClass_rejectsUnknownProjectAndNoMatches() {
        IllegalArgumentException unknown = assertThrows(IllegalArgumentException.class,
                () -> tools.findClass("Anything", new File("other")));
        assertTrue(unknown.getMessage().contains("classInfoProjectMap"));
        when(holder.findClasses("Missing")).thenReturn(Collections.emptyList());

        IllegalArgumentException missing = assertThrows(IllegalArgumentException.class,
                () -> tools.findClass("Missing", projectDirectory));

        assertEquals("Class not found.", missing.getMessage());
    }

    @Test
    void findClass_rejectsMoreThanTenResults() {
        replaceHolder(new ClassInfoHolder(projectForMainSources()));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> tools.findClass(".*", projectDirectory));

        assertTrue(error.getMessage().contains("maximum allowed is 10") || error.getMessage().contains("Too many matches"));
    }

    @Test
    void getClassInfo_describesReflectionAndLocationMetadata() throws Exception {
        Mockito.doReturn(Sample.class).when(holder).loadClass("sample.Sample");
        when(holder.getClassPath("sample.Sample")).thenReturn("target/classes");
        when(holder.getArtifactId("sample.Sample")).thenReturn("group:sample:1");
        when(holder.getSourcePath("sample.Sample")).thenReturn("src/main/java/Sample.java");

        Map<String, Object> result = tools.getClassInfo("sample.Sample", projectDirectory);

        assertEquals(Sample.class.getName(), result.get("className"));
        assertEquals(Sample.class.getSuperclass().getName(), result.get("superclass"));
        assertEquals(Collections.singletonList(Runnable.class.getName()), result.get("interfaces"));
        assertEquals("target/classes", result.get("path"));
        assertEquals("group:sample:1", result.get("artifact"));
        assertEquals("src/main/java/Sample.java", result.get("sourcePath"));
        assertTrue(((List<?>) result.get("fields")).stream().anyMatch(f -> ((Map<?, ?>) f).get("name").equals("visible")));
        assertTrue(((List<?>) result.get("fields")).stream().noneMatch(f -> ((Map<?, ?>) f).get("name").equals("hidden")));
        assertTrue(((List<?>) result.get("constructors")).stream().anyMatch(c -> ((Map<?, ?>) c).get("parameterTypes").equals(Collections.singletonList("java.lang.String"))));
        assertTrue(((List<?>) result.get("methods")).stream().anyMatch(m -> ((Map<?, ?>) m).get("name").equals("run")));
        assertTrue(((List<?>) result.get("methods")).stream().noneMatch(m -> ((Map<?, ?>) m).get("name").equals("secret")));
    }

    @Test
    void getClassInfo_omitsOptionalLocationValuesAndRejectsUnknownProject() throws Exception {
        Mockito.doReturn(Sample.class).when(holder).loadClass("sample.Sample");
        when(holder.getClassPath("sample.Sample")).thenReturn("classes");
        when(holder.getArtifactId("sample.Sample")).thenReturn(null);
        when(holder.getSourcePath("sample.Sample")).thenReturn(null);

        Map<String, Object> result = tools.getClassInfo("sample.Sample", projectDirectory);

        assertTrue(!result.containsKey("artifact"));
        assertTrue(!result.containsKey("sourcePath"));
        assertThrows(IllegalArgumentException.class,
                () -> tools.getClassInfo("sample.Sample", new File("other")));
    }

    public static class Sample implements Runnable {
        public String visible;
        private String hidden;
        public Sample() { }
        public Sample(String value) { visible = value; }
        public void run() { }
        private void secret() { }
    }

    private void replaceHolder(ClassInfoHolder replacement) {
        try {
            Field map = ClassFunctionalTools.class.getDeclaredField("classInfoProjectMap");
            map.setAccessible(true);
            Map<File, ClassInfoHolder> registrations = new HashMap<>();
            registrations.put(projectDirectory, replacement);
            map.set(tools, registrations);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static org.apache.maven.project.MavenProject projectForMainSources() {
        org.apache.maven.model.Model model = new org.apache.maven.model.Model();
        org.apache.maven.model.Build build = new org.apache.maven.model.Build();
        build.setOutputDirectory(new File("target/classes").getAbsolutePath());
        build.setTestOutputDirectory(new File("target/test-classes").getAbsolutePath());
        model.setBuild(build);
        org.apache.maven.project.MavenProject project = new org.apache.maven.project.MavenProject(model) {
            @Override public File getBasedir() { return new File("project"); }
            @Override public List<String> getCompileClasspathElements() {
                return Collections.singletonList(new File("target/classes").getAbsolutePath());
            }
        };
        project.addCompileSourceRoot(new File("src/main/java").getAbsolutePath());
        project.setArtifacts(Collections.emptySet());
        return project;
    }
}
