package org.machanism.machai.project;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectProcessorTest {

    static class TestProjectProcessor extends ProjectProcessor {
        List<ProjectLayout> processedFolders = new ArrayList<>();
        List<String> processedModules = new ArrayList<>();

        @Override
        public void processFolder(ProjectLayout processor) {
            processedFolders.add(processor);
        }

        @Override
        protected void processModule(File projectDir, String module) throws IOException {
            processedModules.add(module);
            super.processModule(projectDir, module);
        }
    }

    @Test
    @Disabled
    void scanFolderProcessesModules(@TempDir File tempDir) throws Exception {
        File mod1 = new File(tempDir, "modA");
        File mod2 = new File(tempDir, "modB");
        mod1.mkdir();
        mod2.mkdir();
        DefaultProjectLayout layout = new DefaultProjectLayout();
        layout.projectDir(tempDir);

        TestProjectProcessor proc = new TestProjectProcessor() {
            @Override
            protected ProjectLayout getProjectLayout(File projectDir) {
                DefaultProjectLayout l = new DefaultProjectLayout();
                l.projectDir(projectDir);
                return l;
            }
        };
        proc.scanFolder(tempDir);

        assertTrue(proc.processedModules.contains("modA"));
        assertTrue(proc.processedModules.contains("modB"));
        // Should also process folders if modules are not detected
        File customDir = new File(tempDir, "no-modules");
        customDir.mkdir();
        TestProjectProcessor proc2 = new TestProjectProcessor() {
            @Override
            protected ProjectLayout getProjectLayout(File dir) {
                DefaultProjectLayout layout = new DefaultProjectLayout();
                layout.projectDir(dir);
                return layout;
            }
        };
        proc2.scanFolder(customDir);
        assertFalse(proc2.processedModules.contains("no-modules"));
        assertFalse(proc2.processedFolders.isEmpty());
    }

    @Test
    void scanFolderHandlesExceptionsInProcessFolder(@TempDir File tempDir) {
        TestProjectProcessor proc = new TestProjectProcessor() {
            @Override
            public void processFolder(ProjectLayout processor) {
                throw new RuntimeException("fail intentionally");
            }
            @Override
            protected ProjectLayout getProjectLayout(File dir) {
                DefaultProjectLayout l = new DefaultProjectLayout();
                l.projectDir(dir);
                return l;
            }
        };
        assertDoesNotThrow(() -> proc.scanFolder(tempDir));
    }

    @Test
    void getProjectLayoutReturnsDefaultLayout(@TempDir File tempDir) throws Exception {
        TestProjectProcessor proc = new TestProjectProcessor();
        ProjectLayout layout = proc.getProjectLayout(tempDir);
        assertNotNull(layout);
        assertTrue(layout instanceof DefaultProjectLayout);
    }
}
