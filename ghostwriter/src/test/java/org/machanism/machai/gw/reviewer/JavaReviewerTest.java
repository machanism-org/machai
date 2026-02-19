package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link JavaReviewer}.
 */
class JavaReviewerTest {

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
