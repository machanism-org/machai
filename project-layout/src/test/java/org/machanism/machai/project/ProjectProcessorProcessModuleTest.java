package org.machanism.machai.project;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.ProjectLayout;

class ProjectProcessorProcessModuleTest {

	static class NoOpProcessor extends ProjectProcessor {
		@Override
		public void processFolder(ProjectLayout processor) {
			// no-op
		}
	}

	@TempDir
	File tempDir;

	@Test
	void processModule_whenCalled_scansNestedModuleFolderAndDoesNotInvokeProcessFolderDirectly() throws Exception {
		// Arrange
		File moduleDir = new File(tempDir, "module-a");
		moduleDir.mkdirs();

		ProjectLayout moduleLayout = org.mockito.Mockito.mock(ProjectLayout.class);
		doReturn(null).when(moduleLayout).getModules();

		NoOpProcessor processor = spy(new NoOpProcessor());

		ProjectLayout rootLayout = org.mockito.Mockito.mock(ProjectLayout.class);
		doReturn(Arrays.asList("module-a")).when(rootLayout).getModules();

		doReturn(rootLayout).when(processor).getProjectLayout(tempDir);
		doReturn(moduleLayout).when(processor).getProjectLayout(moduleDir);
		doNothing().when(processor).processFolder(any(ProjectLayout.class));

		// Act
		processor.scanFolder(tempDir);

		// Assert
		verify(processor, times(1)).processModule(tempDir, "module-a");
		verify(processor, times(1)).scanFolder(moduleDir);
		verify(processor, times(1)).processFolder(moduleLayout);
		verify(processor, times(0)).processFolder(rootLayout);
	}

	@Test
	void processModule_whenRecursiveScanThrowsIOException_propagatesException() throws Exception {
		// Arrange
		File moduleDir = new File(tempDir, "module-b");
		moduleDir.mkdirs();

		NoOpProcessor processor = spy(new NoOpProcessor());
		doThrow(new IOException("boom")).when(processor).scanFolder(moduleDir);

		// Act + Assert
		org.junit.jupiter.api.Assertions.assertThrows(IOException.class,
				() -> processor.processModule(tempDir, "module-b"));
	}

	@Test
	void getProjectLayout_whenDirectoryMissing_throwsFileNotFoundException() {
		// Arrange
		NoOpProcessor processor = new NoOpProcessor();
		File missing = new File(tempDir, "does-not-exist");

		// Act + Assert
		org.junit.jupiter.api.Assertions.assertThrows(FileNotFoundException.class, () -> processor.getProjectLayout(missing));
	}

	@Test
	void scanFolder_whenModulesPresent_processesEachModule() throws Exception {
		// Arrange
		File module1 = new File(tempDir, "m1");
		File module2 = new File(tempDir, "m2");
		module1.mkdirs();
		module2.mkdirs();

		ProjectLayout rootLayout = org.mockito.Mockito.mock(ProjectLayout.class);
		doReturn(Arrays.asList("m1", "m2")).when(rootLayout).getModules();

		ProjectLayout moduleLayout = org.mockito.Mockito.mock(ProjectLayout.class);
		doReturn(null).when(moduleLayout).getModules();

		NoOpProcessor processor = spy(new NoOpProcessor());
		doReturn(rootLayout).when(processor).getProjectLayout(tempDir);
		doReturn(moduleLayout).when(processor).getProjectLayout(module1);
		doReturn(moduleLayout).when(processor).getProjectLayout(module2);
		doNothing().when(processor).processFolder(any(ProjectLayout.class));

		// Act
		assertDoesNotThrow(() -> processor.scanFolder(tempDir));

		// Assert
		verify(processor, times(1)).processModule(tempDir, "m1");
		verify(processor, times(1)).processModule(tempDir, "m2");
	}
}
