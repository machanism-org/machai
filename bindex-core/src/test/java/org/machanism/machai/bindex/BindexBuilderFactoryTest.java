package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.ProjectLayout;

class BindexBuilderFactoryTest {

    @TempDir
    File tempDir;

    @Test
    void create_whenProjectDirDoesNotExist_throwsFileNotFoundException() {
        // Arrange
        File missing = new File(tempDir, "missing");
        ProjectLayout layout = TestLayouts.projectLayout(missing);

        // Act / Assert
        assertThrows(FileNotFoundException.class, () -> BindexBuilderFactory.create(layout));
    }
}
