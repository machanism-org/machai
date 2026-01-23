package org.machanism.machai.project;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.machanism.machai.project.layout.ProjectLayout;

class ProjectProcessorTest {

    @Test
    @Disabled
    void scanFolder_whenModulesPresent_thenProcessesEachModuleByRecursing() throws Exception {
        // Arrange
        Path root = Files.createTempDirectory("machai-processor-modules");
        Files.createDirectories(root.resolve("m1"));
        Files.createDirectories(root.resolve("m2"));

        List<String> modules = List.of("m1", "m2");
        AtomicInteger getLayoutInvocations = new AtomicInteger();
        AtomicInteger processFolderInvocations = new AtomicInteger();

        ProjectLayout rootLayout = new FakeProjectLayout(modules);
        ProjectLayout moduleLayout = new FakeProjectLayout(null);

        ProjectProcessor processor = new ProjectProcessor() {
            @Override
            public void processFolder(ProjectLayout processor) {
                processFolderInvocations.incrementAndGet();
            }

            @Override
            protected ProjectLayout getProjectLayout(File projectDir) {
                getLayoutInvocations.incrementAndGet();
                // Root returns modules; modules return null so recursion terminates.
                return projectDir.equals(root.toFile()) ? rootLayout : moduleLayout;
            }
        };

        // Act
        processor.scanFolder(root.toFile());

        // Assert
        assertEquals(3, getLayoutInvocations.get(), "Expected getProjectLayout to be called for root and both modules");
        assertEquals(2, processFolderInvocations.get(), "Expected processFolder to be called for each module folder");
    }

    @Test
    void scanFolder_whenNoModules_thenInvokesProcessFolderAndDoesNotThrowWhenProcessFolderThrows() throws Exception {
        // Arrange
        Path root = Files.createTempDirectory("machai-processor-no-modules");
        AtomicInteger getLayoutInvocations = new AtomicInteger();
        AtomicInteger processFolderInvocations = new AtomicInteger();

        ProjectLayout layout = new FakeProjectLayout(null);

        ProjectProcessor processor = new ProjectProcessor() {
            @Override
            public void processFolder(ProjectLayout processor) {
                processFolderInvocations.incrementAndGet();
                throw new RuntimeException("boom");
            }

            @Override
            protected ProjectLayout getProjectLayout(File projectDir) {
                getLayoutInvocations.incrementAndGet();
                return layout;
            }
        };

        // Act
        assertDoesNotThrow(() -> processor.scanFolder(root.toFile()));

        // Assert
        assertEquals(2, getLayoutInvocations.get(), "Expected getProjectLayout to be called twice when modules are null");
        assertEquals(1, processFolderInvocations.get(), "Expected processFolder to be invoked once");
    }

    private static final class FakeProjectLayout extends ProjectLayout {
        private final List<String> modules;

        private FakeProjectLayout(List<String> modules) {
            this.modules = modules;
        }

        @Override
        public List<String> getModules() throws IOException {
            return modules;
        }

        @Override
        public List<String> getSources() {
            return Collections.emptyList();
        }

        @Override
        public List<String> getDocuments() {
            return Collections.emptyList();
        }

        @Override
        public List<String> getTests() {
            return Collections.emptyList();
        }
    }
}
