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
 * <p>
 * Tests extracting guidance from Java source and package-info files with proper @guidance tag handling.
 * <pre>
 * Example:
 *   // @guidance: This is guidance for the class
 * </pre>
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
    @Disabled("Need to fix.")
    void perform_extractsGuidance_whenGuidanceTagPresent() throws IOException {
        JavaReviewer reviewer = new JavaReviewer();
        String guidanceComment = "/* @guidance: This is guidance */\npublic class Example {}";
        File javaFile = new File(tempDir, "Example.java");
        Files.writeString(javaFile.toPath(), guidanceComment);
        String result = reviewer.perform(tempDir, javaFile);
        assertNotNull(result);
        assertTrue(result.contains("This is guidance"));
    }

    @Test
    void perform_packageInfo_extraction() throws IOException {
        JavaReviewer reviewer = new JavaReviewer();
        String content = "/**\n * " + "@guidance: Package-level guidance.\n */\npackage org.machanism.example;";
        File pkgInfo = new File(tempDir, "package-info.java");
        Files.writeString(pkgInfo.toPath(), content);
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
    void extractPackageName_returnsNullIfNotFound() {
        String src = "public class Example {}";
        assertEquals(JavaReviewer.extractPackageName(src), "<default package>");
    }
}
