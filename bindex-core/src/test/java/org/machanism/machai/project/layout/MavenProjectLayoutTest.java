package org.machanism.machai.project.layout;

import org.apache.maven.model.Model;
import org.apache.maven.model.Build;
import org.apache.maven.model.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MavenProjectLayoutTest {
    @Test
    void isMavenProjectTrueIfPomExists(@TempDir java.nio.file.Path tempDir) {
        File dir = tempDir.toFile();
        assertDoesNotThrow(() -> new java.io.PrintWriter(new File(dir, "pom.xml")).close());
        assertTrue(MavenProjectLayout.isMavenProject(dir));
    }
    
    @Test
    void isMavenProjectFalseIfPomMissing(@TempDir java.nio.file.Path tempDir) {
        File dir = tempDir.toFile();
        assertFalse(MavenProjectLayout.isMavenProject(dir));
    }

    @Test
    void modelChainingWorks() {
        MavenProjectLayout layout = new MavenProjectLayout();
        Model model = new Model();
        assertSame(layout, layout.model(model));
        assertSame(model, layout.getModel());
    }
    
    @Test
    void effectivePomRequiredChainingWorks() {
        MavenProjectLayout layout = new MavenProjectLayout();
        assertSame(layout, layout.effectivePomRequired(true));
    }

    @Test
    void getDocumentsAlwaysReturnsSrcSite() {
        MavenProjectLayout layout = new MavenProjectLayout();
        List<String> docs = layout.getDocuments();
        assertNotNull(docs);
        assertTrue(docs.contains("src/site"));
    }

    @Test
    void getSourcesUsesBuildSourceDirectoryAndResources() {
        Model model = new Model();
        Build build = new Build();
        build.setSourceDirectory("/absolute/sourceDir");
        Resource r = new Resource();
        r.setDirectory("/absolute/resourceDir");
        build.addResource(r);
        model.setBuild(build);

        MavenProjectLayout layout = new MavenProjectLayout();
        layout.model(model);
        layout.projectDir(new File("/absolute"));
        List<String> sources = layout.getSources();
        assertNotNull(sources);
        assertTrue(sources.contains("sourceDir"));
        assertTrue(sources.contains("resourceDir"));
    }

    @Test
    void getTestsUsesBuildTestSourceDirectoryAndTestResources() {
        Model model = new Model();
        Build build = new Build();
        build.setTestSourceDirectory("/absolute/testSourceDir");
        Resource r = new Resource();
        r.setDirectory("/absolute/testResourceDir");
        build.addTestResource(r);
        model.setBuild(build);

        MavenProjectLayout layout = new MavenProjectLayout();
        layout.model(model);
        layout.projectDir(new File("/absolute"));
        List<String> tests = layout.getTests();
        assertNotNull(tests);
        assertTrue(tests.contains("testSourceDir"));
        assertTrue(tests.contains("testResourceDir"));
    }
}
