package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.junit.jupiter.api.Test;

class PomReaderTest {

    @Test
    void getProjectModel_whenNoLicenses_thenAppliesDefaultLicensesFromPreviousPom() throws Exception {
        // Arrange
        Path dir = Files.createTempDirectory("machai-pom-reader-licenses");
        Path pom1 = dir.resolve("pom1.xml");
        Path pom2 = dir.resolve("pom2.xml");

        String pomWithLicense = "" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
                "  <modelVersion>4.0.0</modelVersion>\n" +
                "  <groupId>g</groupId>\n" +
                "  <artifactId>a</artifactId>\n" +
                "  <version>1</version>\n" +
                "  <licenses>\n" +
                "    <license><name>Apache-2.0</name></license>\n" +
                "  </licenses>\n" +
                "</project>\n";

        String pomWithoutLicense = "" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
                "  <modelVersion>4.0.0</modelVersion>\n" +
                "  <groupId>g</groupId>\n" +
                "  <artifactId>b</artifactId>\n" +
                "  <version>2</version>\n" +
                "</project>\n";

        Files.write(pom1, pomWithLicense.getBytes(StandardCharsets.UTF_8));
        Files.write(pom2, pomWithoutLicense.getBytes(StandardCharsets.UTF_8));

        PomReader reader = new PomReader();

        // Act
        Model model1 = reader.getProjectModel(pom1.toFile(), false);
        Model model2 = reader.getProjectModel(pom2.toFile(), false);

        // Assert
        assertFalse(model1.getLicenses().isEmpty());
        assertEquals(1, model2.getLicenses().size(), "Expected default license to be applied to subsequent models");
        License license = model2.getLicenses().get(0);
        assertEquals("Apache-2.0", license.getName());
    }

    @Test
    void getProjectModel_whenEffectiveFails_thenFallsBackToNonEffective() throws Exception {
        // Arrange
        Path dir = Files.createTempDirectory("machai-pom-reader-fallback");
        Path pom = dir.resolve("pom.xml");

        String pomXml = "" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
                "  <modelVersion>4.0.0</modelVersion>\n" +
                "  <groupId>g</groupId>\n" +
                "  <artifactId>a</artifactId>\n" +
                "  <version>1</version>\n" +
                "</project>\n";
        Files.write(pom, pomXml.getBytes(StandardCharsets.UTF_8));

        PomReader reader = new PomReader() {
            @Override
            public Model getProjectModel(java.io.File pomFile, boolean effective) {
                if (effective) {
                    throw new IllegalArgumentException("force-effective-failure");
                }
                return super.getProjectModel(pomFile, false);
            }
        };

        // Act
        Model model = reader.getProjectModel(pom.toFile());

        // Assert
        assertNotNull(model);
        assertEquals("a", model.getArtifactId());
        assertEquals("1", model.getVersion());
    }

    @Test
    void printModel_whenModelProvided_thenReturnsXmlContainingArtifactId() throws Exception {
        // Arrange
        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setGroupId("g");
        model.setArtifactId("a");
        model.setVersion("1");

        // Act
        String xml = PomReader.printModel(model);

        // Assert
        assertNotNull(xml);
        assertTrue(xml.contains("<artifactId>a</artifactId>"));
    }

    @Test
    void serviceLocator_whenCalled_thenReturnsNonNullLocator() {
        // Arrange
        PomReader reader = new PomReader();

        // Act
        Object locator = reader.serviceLocator();

        // Assert
        assertNotNull(locator);
    }
}
