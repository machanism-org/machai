package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for {@link JavaReviewer}.
 */
class JavaReviewerTest {

    @TempDir
    File tempDir;

    @Test
    void perform_returnsNull_whenNoGuidance() throws IOException {
        JavaReviewer reviewer = new JavaReviewer();
        File javaFile = new File(tempDir, "Example.java");
        Files.writeString(javaFile.toPath(), "public class Example {}");
        assertNull(reviewer.perform(tempDir, javaFile));
    }

    @Test
    @Disabled
    void perform_extractsGuidance_whenGuidanceTagPresent() throws IOException {
        JavaReviewer reviewer = new JavaReviewer();
        String source = "/* " + "@guidance: This is guidance */\npublic class Example {}";
        File javaFile = new File(tempDir, "Example.java");
        Files.writeString(javaFile.toPath(), source);

        String result = reviewer.perform(tempDir, javaFile);

        assertNotNull(result);
        assertTrue(result.contains("This is guidance"));
    }

    @Test
    @Disabled
    void perform_packageInfo_extraction() throws IOException {
        JavaReviewer reviewer = new JavaReviewer();
        String source = "/* " + "@guidance: Package-level guidance. */\npackage org.machanism.example;";
        File pkgInfo = new File(tempDir, "package-info.java");
        Files.writeString(pkgInfo.toPath(), source);

        String result = reviewer.perform(tempDir, pkgInfo);

        assertNotNull(result);
        assertTrue(result.contains("Package-level guidance."));
        assertTrue(result.contains("org.machanism.example"));
    }

    @Test
    void extractPackageName_findsPackageName() {
        String src = "package org.machanism.example;";
        String pkgName = JavaReviewer.extractPackageName(src);
        assertEquals("org.machanism.example", pkgName);
    }

    @Test
    void extractPackageName_returnsDefaultPackage_whenNotFound() {
        String src = "public class Example {}";
        assertEquals("<default package>", JavaReviewer.extractPackageName(src));
    }
}
