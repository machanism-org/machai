package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.maven.model.Model;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class PomReaderTest {
    @Test
    @Disabled("Need to fix.")
    void getProjectModel_withEffective_true_returnsModel() {
        // Arrange
        File pomFile = new File("src/test/resources/mock-pom.xml");
        assertTrue(pomFile.exists(), "POM file must exist for test");

        // Act
        Model model = PomReader.getProjectModel(pomFile, true);

        // Assert
        assertNotNull(model, "Model should not be null");
        assertNotNull(model.getGroupId(), "GroupId should not be null");
        assertNotNull(model.getArtifactId(), "ArtifactId should not be null");
        assertNotNull(model.getVersion(), "Version should not be null");
        assertFalse(model.getLicenses().isEmpty(), "Licenses should not be empty");
    }

    @Test
    @Disabled("Need to fix.")
    void getProjectModel_withEffective_false_returnsModel() {
        // Arrange
        File pomFile = new File("src/test/resources/mock-pom.xml");
        assertTrue(pomFile.exists(), "POM file must exist for test");

        // Act
        Model model = PomReader.getProjectModel(pomFile, false);

        // Assert
        assertNotNull(model, "Model should not be null");
        assertNotNull(model.getGroupId(), "GroupId should not be null");
        assertNotNull(model.getArtifactId(), "ArtifactId should not be null");
        assertNotNull(model.getVersion(), "Version should not be null");
    }

    @Test
    @Disabled("Need to fix.")
    void printModel_returnsPomString() throws IOException {
        // Arrange
        File pomFile = new File("src/test/resources/mock-pom.xml");
        Model model = PomReader.getProjectModel(pomFile, false);

        // Act
        String printed = PomReader.printModel(model);

        // Assert
        assertNotNull(printed);
        assertTrue(printed.contains("<project"), "Should contain <project tag");
        assertTrue(printed.contains(model.getArtifactId()));
    }

    @Test
    void replaceProperty_replacesAllPomProperties() {
        // Arrange
        PomReader.getPomProperties().clear();
        PomReader.getPomProperties().put("test.key", "test.value");
        String rawPom = "<project>\n    <name>${test.key}</name>\n</project>";
        // Act
        String result = invokeReplaceProperty(rawPom);
        // Assert
        assertTrue(result.contains("test.value"));
    }

    @Test
    void serviceLocator_returnsServiceLocator() {
        assertNotNull(PomReader.serviceLocator());
    }

    @Test
    void getPomProperties_returnsMap() {
        assertNotNull(PomReader.getPomProperties());
        assertTrue(PomReader.getPomProperties() instanceof Map);
    }

    // Helper method to call replaceProperty (reflection to reach private static method)
    private String invokeReplaceProperty(String pomString) {
        try {
            Method method = PomReader.class.getDeclaredMethod("replaceProperty", String.class);
            method.setAccessible(true);
            return (String) method.invoke(null, pomString);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
            return null;
        }
    }

    @Test
    void getProjectModel_handlesInvalidFileGracefully() {
        File badFile = new File("src/test/resources/nonexistent.xml");
        assertThrows(IllegalArgumentException.class, () -> PomReader.getProjectModel(badFile, true));
    }

    @Test
    @Disabled("Need to fix.")
    void getProjectModel_unifiedMethod() {
        File pomFile = new File("src/test/resources/mock-pom.xml");
        assertTrue(pomFile.exists(), "POM file must exist for test");
        Model model = PomReader.getProjectModel(pomFile);
        assertNotNull(model);
    }
}
