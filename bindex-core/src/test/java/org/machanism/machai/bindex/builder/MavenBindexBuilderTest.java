package org.machanism.machai.bindex.builder;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.maven.model.Build;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Reporting;
import org.apache.maven.model.Repository;
import org.apache.maven.model.Scm;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.machanism.machai.project.layout.MavenProjectLayout;

class MavenBindexBuilderTest {

    @Test
    @Disabled
    void removeNotImportantData_clearsKnownSections() {
        // Arrange
        MavenBindexBuilder builder = new MavenBindexBuilder(new MavenProjectLayout());
        Model model = new Model();
        model.setDistributionManagement(new DistributionManagement());
        model.setBuild(new Build());
        model.setProperties(new java.util.Properties());
        model.setDependencyManagement(new DependencyManagement());
        model.setReporting(new Reporting());
        model.setScm(new Scm());
        model.setPluginRepositories(java.util.List.of(new Repository()));

        // Act
        builder.removeNotImportantData(model);

        // Assert
        assertNull(model.getDistributionManagement());
        assertNull(model.getBuild());
        assertNull(model.getProperties());
        assertNull(model.getDependencyManagement());
        assertNull(model.getReporting());
        assertNull(model.getScm());
        assertNull(model.getPluginRepositories());
    }
}
