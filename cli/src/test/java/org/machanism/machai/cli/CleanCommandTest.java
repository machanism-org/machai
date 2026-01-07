package org.machanism.machai.cli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CleanCommand}.
 * <p>
 * Tests removal of ".machai" directories from a given root.
 */
public class CleanCommandTest {

    private Path testRoot;

    @BeforeEach
    public void setUp() throws IOException {
        testRoot = Files.createTempDirectory("machai-test-root");
        // Create a ".machai" folder inside the test root
        Files.createDirectory(testRoot.resolve(".machai"));
        // Create a file inside the .machai folder
        Files.createFile(testRoot.resolve(".machai").resolve("test.txt"));
    }

    @AfterEach
    public void tearDown() throws IOException {
        if (Files.exists(testRoot)) {
            Files.walk(testRoot)
                 .map(Path::toFile)
                 .forEach(File::delete);
        }
    }

    /**
     * Tests that the clean command removes all ".machai" folders.
     */
    @Test
    public void testCleanRemovesMachaiFolders() throws IOException {
        CleanCommand command = new CleanCommand();
        command.clean(testRoot.toFile());
        assertFalse(Files.exists(testRoot.resolve(".machai")), ".machai folder should be deleted");
    }

    /**
     * Tests static utility for removing directories by name.
     */
    @Test
    public void testRemoveAllDirectoriesByName() throws IOException {
        // Add another ".machai" folder in a subdirectory
        Path subDir = Files.createDirectory(testRoot.resolve("subdir"));
        Files.createDirectory(subDir.resolve(".machai"));
        CleanCommand.removeAllDirectoriesByName(testRoot, CleanCommand.MACHAI_TEMP_DIR);
        assertFalse(Files.exists(testRoot.resolve(".machai")), "Root .machai folder should be deleted");
        assertFalse(Files.exists(subDir.resolve(".machai")), "Subdir .machai folder should be deleted");
    }
}
