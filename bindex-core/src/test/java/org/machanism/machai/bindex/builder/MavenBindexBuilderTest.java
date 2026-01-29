package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.maven.model.Build;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Reporting;
import org.apache.maven.model.Scm;
import org.junit.jupiter.api.Test;
import org.machanism.machai.project.layout.MavenProjectLayout;

class MavenBindexBuilderTest {

    @Test
    void removeNotImportantData_clearsKnownSectionsButKeepsModelUsable() {
        // Arrange
        MavenBindexBuilder builder = new MavenBindexBuilder(new MavenProjectLayout());
        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setDistributionManagement(new DistributionManagement());
        model.setBuild(new Build());
        model.setProperties(new java.util.Properties());
        model.setDependencyManagement(new DependencyManagement());
        model.setReporting(new Reporting());
        model.setScm(new Scm());
        model.setPluginRepositories(java.util.List.of());

        // Act
        builder.removeNotImportantData(model);

        // Assert
        assertNull(model.getDistributionManagement());
        assertNull(model.getBuild());

        // Note: Maven Model may normalize properties to a non-null empty instance.
        assertNotNull(model.getProperties());
        org.junit.jupiter.api.Assertions.assertTrue(model.getProperties().isEmpty());

        assertNull(model.getDependencyManagement());
        assertNull(model.getReporting());
        assertNull(model.getScm());

        // Maven Model normalizes pluginRepositories to a non-null list in some cases; assert it is empty.
        assertNotNull(model.getPluginRepositories());
        org.junit.jupiter.api.Assertions.assertTrue(model.getPluginRepositories().isEmpty());
    }
}
