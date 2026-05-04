package org.machanism.machai.gw.maven.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

public class ClassInfoHolderAdditionalTest {

    @Test
    public void scanClassesByPath_propagatesZipExceptionForUnreadableJar() throws Exception {
        Path tempFile = Files.createTempFile("class-info-holder-invalid", ".bin");
        Files.write(tempFile, new byte[] { 1, 2, 3, 4 });
        ClassInfoHolder holder = new ClassInfoHolder(createProjectForMainSources());

        try {
            holder.scanClassesByPath(tempFile.toString(), "artifact");
            fail("Expected ZipException");
        } catch (java.util.zip.ZipException e) {
            assertEquals("zip END header not found", e.getMessage());
        }
    }

    @Test
    public void privateToClassName_convertsTopLevelAndDefaultPackageNames() throws Exception {
        ClassInfoHolder holder = new ClassInfoHolder(createProjectForMainSources());
        Method method = ClassInfoHolder.class.getDeclaredMethod("toClassName", String.class);
        method.setAccessible(true);

        assertEquals("a.b.Sample", method.invoke(holder, "a/b/Sample.class"));
        assertEquals("Root.", method.invoke(holder, "Root.class"));
    }

    @Test
    public void privateSupportedEntryCheck_filtersMetaInfAndModuleInfo() throws Exception {
        ClassInfoHolder holder = new ClassInfoHolder(createProjectForMainSources());
        Method method = ClassInfoHolder.class.getDeclaredMethod("isSupportedClassEntry", String.class);
        method.setAccessible(true);

        assertEquals(Boolean.TRUE, method.invoke(holder, "a/b/Sample.class"));
        assertEquals(Boolean.FALSE, method.invoke(holder, "META-INF/services/X.class"));
        assertEquals(Boolean.FALSE, method.invoke(holder, "module-info.class"));
        assertEquals(Boolean.FALSE, method.invoke(holder, "a/b/Sample.txt"));
    }

    @Test
    public void getSourcePath_returnsNullWhenSourceRootsDoNotContainFile() {
        MavenProject project = createProjectForMainSources();
        project.getCompileSourceRoots().clear();
        project.getCompileSourceRoots().addAll(Arrays.asList(new File("missing-src").getAbsolutePath()));
        ClassInfoHolder holder = new ClassInfoHolder(project);

        assertNull(holder.getSourcePath("org.example.Missing"));
    }

    @Test
    public void findClasses_throwsPatternSyntaxExceptionForInvalidPattern() {
        ClassInfoHolder holder = new ClassInfoHolder(createProjectForMainSources());

        try {
            holder.findClasses("[");
            fail("Expected PatternSyntaxException");
        } catch (java.util.regex.PatternSyntaxException e) {
            assertEquals("[", e.getPattern());
        }
    }

    @Test
    public void privateLoadClassList_wrapsClasspathFailures() throws Exception {
        MavenProject brokenProject = new MavenProject(new Model()) {
            @Override
            public java.util.List<String> getCompileClasspathElements() throws org.apache.maven.artifact.DependencyResolutionRequiredException {
                throw new org.apache.maven.artifact.DependencyResolutionRequiredException(new org.apache.maven.artifact.DefaultArtifact("g", "a", "1", null, "jar", null, new org.apache.maven.artifact.handler.DefaultArtifactHandler("jar")));
            }
        };
        brokenProject.setBuild(new Build());
        brokenProject.getBuild().setOutputDirectory(new File("target/classes").getAbsolutePath());
        brokenProject.getBuild().setTestOutputDirectory(new File("target/test-classes").getAbsolutePath());

        ClassInfoHolder holder = new ClassInfoHolder(brokenProject);
        Method method = ClassInfoHolder.class.getDeclaredMethod("loadClassList", MavenProject.class);
        method.setAccessible(true);

        try {
            method.invoke(holder, brokenProject);
            fail("Expected InvocationTargetException");
        } catch (InvocationTargetException e) {
            assertEquals(IllegalArgumentException.class, e.getCause().getClass());
            assertEquals(org.apache.maven.artifact.DependencyResolutionRequiredException.class,
                    e.getCause().getCause().getClass());
        }
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
}
