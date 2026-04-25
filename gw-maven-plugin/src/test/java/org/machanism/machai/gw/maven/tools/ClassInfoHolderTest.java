package org.machanism.machai.gw.maven.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath.ClassInfo;

public class ClassInfoHolderTest {

    @Test
    public void findClassesLoadClassAndMetadata_workForCompiledProjectClasses() throws Exception {
        MavenProject project = createProjectForMainSources();
        ClassInfoHolder holder = new ClassInfoHolder(project);

        List<ClassInfo> found = holder.findClasses("ClassInfoHolder");
        Class<?> loadedClass = holder.loadClass("org.machanism.machai.gw.maven.tools.ClassInfoHolder");
        String classPath = holder.getClassPath("org.machanism.machai.gw.maven.tools.ClassInfoHolder");
        String sourcePath = holder.getSourcePath("org.machanism.machai.gw.maven.tools.ClassInfoHolder");
        String nestedSourcePath = holder.getSourcePath("org.machanism.machai.gw.maven.tools.ClassFunctionalTools$1");
        String artifactId = holder.getArtifactId("org.machanism.machai.gw.maven.tools.ClassInfoHolder");

        assertFalse(found.isEmpty());
        assertEquals("org.machanism.machai.gw.maven.tools.ClassInfoHolder", found.get(0).getName());
        assertEquals(ClassInfoHolder.class, loadedClass);
        assertTrue(classPath.replace('\\', '/').endsWith("target/classes"));
        assertTrue(sourcePath.replace('\\', '/').endsWith("src/main/java/org/machanism/machai/gw/maven/tools/ClassInfoHolder.java"));
        assertTrue(nestedSourcePath.replace('\\', '/').endsWith("src/main/java/org/machanism/machai/gw/maven/tools/ClassFunctionalTools.java"));
        assertNull(artifactId);
    }

    @Test
    public void scanClassesByPath_recordsPublicClassesFromJarAndDirectoryAndIgnoresUnsupportedEntries() throws Exception {
        Path tempRoot = Files.createTempDirectory("class-info-holder");
        Path classesDir = tempRoot.resolve("classes");
        Path packageDir = classesDir.resolve(Paths.get("sample", "pkg"));
        Files.createDirectories(packageDir);
        Files.write(packageDir.resolve("Visible.class"), new byte[] { 1, 2, 3 });
        Files.write(packageDir.resolve("module-info.class"), new byte[] { 1 });
        Files.createDirectories(classesDir.resolve("META-INF"));
        Files.write(classesDir.resolve("META-INF").resolve("Ignored.class"), new byte[] { 1 });

        Path jarPath = tempRoot.resolve("sample.jar");
        try (JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(jarPath.toFile()))) {
            jarOutputStream.putNextEntry(new JarEntry("sample/pkg/Visible.class"));
            jarOutputStream.write(new byte[] { 1, 2, 3 });
            jarOutputStream.closeEntry();
            jarOutputStream.putNextEntry(new JarEntry("META-INF/Ignored.class"));
            jarOutputStream.write(new byte[] { 4 });
            jarOutputStream.closeEntry();
        }

        ClassInfoHolder holder = new ClassInfoHolder(createProjectForMainSources());
        setField(holder, "classes", ImmutableSet.of());
        setField(holder, "classLoader", new StubClassLoader());

        holder.scanClassesByPath(classesDir.toString(), "dir-artifact");
        assertEquals(classesDir.toString(), holder.getClassPath("sample.pkg.Visible"));
        assertEquals("dir-artifact", holder.getArtifactId("sample.pkg.Visible"));

        holder.scanClassesByPath(jarPath.toString(), "jar-artifact");
        holder.scanClassesByPath(tempRoot.resolve("missing-dir").toString(), "missing-artifact");

        assertEquals(jarPath.toString(), holder.getClassPath("sample.pkg.Visible"));
        assertEquals("jar-artifact", holder.getArtifactId("sample.pkg.Visible"));
        assertNull(holder.getClassPath("META-INF.Ignored"));
        assertNull(holder.getArtifactId("module-info"));
    }

    @Test
    public void scanClassesByPath_withArtifactsSet_scansEveryArtifact() throws Exception {
        Path tempRoot = Files.createTempDirectory("class-info-artifacts");
        Path jarPath = tempRoot.resolve("dependency.jar");
        try (JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(jarPath.toFile()))) {
            jarOutputStream.putNextEntry(new JarEntry("sample/pkg/Visible.class"));
            jarOutputStream.write(new byte[] { 1, 2, 3 });
            jarOutputStream.closeEntry();
        }

        DefaultArtifact artifact = new DefaultArtifact("g", "a", "1.0", null, "jar", null,
                new org.apache.maven.artifact.handler.DefaultArtifactHandler("jar"));
        artifact.setFile(jarPath.toFile());

        ClassInfoHolder holder = new ClassInfoHolder(createProjectForMainSources());
        setField(holder, "classes", ImmutableSet.of());
        setField(holder, "classLoader", new StubClassLoader());

        invokeScanArtifacts(holder, new LinkedHashSet<>(Collections.singletonList(artifact)));

        assertEquals(jarPath.toString(), holder.getClassPath("sample.pkg.Visible"));
        assertEquals("g:a:1.0", holder.getArtifactId("sample.pkg.Visible"));
    }

    @Test
    public void getSourcePath_returnsNullForUnknownClass() {
        ClassInfoHolder holder = new ClassInfoHolder(createProjectForMainSources());

        String sourcePath = holder.getSourcePath("missing.Type");

        assertNull(sourcePath);
    }

    @Test
    public void loadClass_throwsClassNotFoundForUnknownType() throws Exception {
        ClassInfoHolder holder = new ClassInfoHolder(createProjectForMainSources());

        try {
            holder.loadClass("missing.Type");
            fail("Expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void findClasses_returnsEmptyListWhenPatternDoesNotMatch() {
        ClassInfoHolder holder = new ClassInfoHolder(createProjectForMainSources());

        List<ClassInfo> found = holder.findClasses("DoesNotExist");

        assertTrue(found.isEmpty());
    }

    private static void invokeScanArtifacts(ClassInfoHolder holder, Set<org.apache.maven.artifact.Artifact> artifacts) throws Exception {
        java.lang.reflect.Method method = ClassInfoHolder.class.getDeclaredMethod("scanClassesByPath", Set.class);
        method.setAccessible(true);
        method.invoke(holder, artifacts);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = ClassInfoHolder.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
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
            public List<String> getCompileClasspathElements() {
                return Collections.singletonList(new File("target/classes").getAbsolutePath());
            }
        };
        project.getBuild().setOutputDirectory(new File("target/classes").getAbsolutePath());
        project.getBuild().setTestOutputDirectory(new File("target/test-classes").getAbsolutePath());
        project.addCompileSourceRoot(new File("src/main/java").getAbsolutePath());
        project.setArtifacts(Collections.emptySet());
        return project;
    }

    public static class StubClassLoader extends URLClassLoader {
        public StubClassLoader() {
            super(new java.net.URL[0], ClassInfoHolderTest.class.getClassLoader());
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if ("sample.pkg.Visible".equals(name)) {
                return Visible.class;
            }
            throw new ClassNotFoundException(name);
        }
    }

    public static class Visible {
        public String value = new String("ok".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }
}
