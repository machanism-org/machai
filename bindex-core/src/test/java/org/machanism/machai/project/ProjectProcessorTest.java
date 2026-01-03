package org.machanism.machai.project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Need to fix.")
class ProjectProcessorTest {

    private static class TestProjectProcessor extends ProjectProcessor {
        List<String> processed = new ArrayList<>();

        @Override
        public void processFolder(ProjectLayout processor) {
            processed.add(processor.getProjectDir().getAbsolutePath());
        }
    }

    private TestProjectProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new TestProjectProcessor();
    }

    @Test
    void scanFolderProcessesModules() throws Exception {
        File testDir = new File("src/test/resources/sample-default-project");
        processor.scanFolder(testDir);
        assertTrue(processor.processed.contains(testDir.getAbsolutePath()));
    }

    @Test
    void scanFolderCallsProcessFolderIfNoModules() throws Exception {
        File testDir = new File("src/test/resources/no-modules");
        processor.scanFolder(testDir);
        assertTrue(processor.processed.contains(testDir.getAbsolutePath()));
    }

    @Test
    void processModuleCallsScanFolderOnModule() throws Exception {
        File parentDir = new File("src/test/resources/sample-default-project");
        processor.processModule(parentDir, "module1");
        File expectedModuleDir = new File(parentDir, "module1");
        assertTrue(processor.processed.contains(expectedModuleDir.getAbsolutePath()));
    }

    @Test
    void getProjectLayoutReturnsDefaultProjectLayout() throws Exception {
        File dir = new File("src/test/resources/sample-default-project");
        ProjectLayout layout = processor.getProjectLayout(dir);
        assertTrue(layout instanceof DefaultProjectLayout);
    }
}
