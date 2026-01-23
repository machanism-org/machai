package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link JavaReviewer}.
 */
class JavaReviewerTest {

    @Test
    @Disabled
    void perform_returnsNull_whenNoGuidance() throws IOException {
        JavaReviewer reviewer = new JavaReviewer();

        assertNull(reviewer.perform(null, null));
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
