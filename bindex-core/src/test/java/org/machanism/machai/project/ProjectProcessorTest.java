package org.machanism.machai.project;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.machanism.machai.project.layout.ProjectLayout;

/**
 * Unit tests for {@link ProjectProcessor} functionality.
 * <p>
 * Verifies folder scanning, module processing, and subclass API.
 * <p>
 * Usage Example:
 * <pre>{@code
 *     DummyProjectProcessor processor = new DummyProjectProcessor();
 *     processor.scanFolder(new File("my-project"));
 * }</pre>
 *
 * @author machanism
 */
class DummyProjectProcessor extends ProjectProcessor {
    /** Indicates whether processFolder() was called. */
    public boolean folderProcessed = false;

    /**
     * Dummy implementation; sets folderProcessed to true for test signaling.
     * @param processor {@link ProjectLayout} passed by main API
     */
    @Override
    public void processFolder(ProjectLayout processor) {
        folderProcessed = true;
    }
}

@DisplayName("ProjectProcessor Tests")
public class ProjectProcessorTest {
    private DummyProjectProcessor processor;
    private File tempProjectDir;
    private ProjectLayout mockLayout;

    /**
     * Initializes test resources and mocks.
     */
    @BeforeEach
    void setUp() {
        processor = new DummyProjectProcessor();
        tempProjectDir = mock(File.class);
        mockLayout = mock(ProjectLayout.class);
    }

    /**
     * Verifies scanFolder() delegates to processModule() when modules are present.
     * @throws IOException if IO error occurs
     */
    @Test
    @DisplayName("Scan Folder with Modules")
    void testScanFolderWithModules() throws IOException {
        List<String> modules = Arrays.asList("module1", "module2");
        when(mockLayout.getModules()).thenReturn(modules);
        ProjectProcessor spyProcessor = spy(processor);
        doReturn(mockLayout).when(spyProcessor).getProjectLayout(tempProjectDir);
        doNothing().when(spyProcessor).processModule(any(File.class), any(String.class));
        spyProcessor.scanFolder(tempProjectDir);
        verify(spyProcessor, times(2)).processModule(any(File.class), any(String.class));
        assertFalse(processor.folderProcessed);
    }

    /**
     * Verifies scanFolder() delegates to processFolder() if no modules are present.
     * @throws IOException if IO error occurs
     */
    @Test
    @DisplayName("Scan Folder without Modules")
    @Disabled("Need to fix.")
    void testScanFolderWithoutModules() throws IOException {
        when(mockLayout.getModules()).thenReturn(null);
        ProjectProcessor spyProcessor = spy(processor);
        doReturn(mockLayout).when(spyProcessor).getProjectLayout(tempProjectDir);
        spyProcessor.scanFolder(tempProjectDir);
        assertTrue(processor.folderProcessed);
    }

    /**
     * Verifies processModule() calls scanFolder() on the correct subdirectory.
     * @throws IOException if IO error occurs
     */
    @Test
    @DisplayName("ProcessModule calls scanFolder on subdirectory")
    @Disabled("Need to fix.")
    void testProcessModuleCallsScanFolder() throws IOException {
        ProjectProcessor spyProcessor = spy(processor);
        File projectDir = mock(File.class);
        String module = "moduleX";
        File subDir = mock(File.class);
        when(projectDir.getPath()).thenReturn("/tmp/test");
        doNothing().when(spyProcessor).scanFolder(subDir);
        spyProcessor.processModule(projectDir, module);
        verify(spyProcessor).scanFolder(subDir);
    }
}
