package org.machanism.machai.bindex;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.machanism.machai.project.layout.ProjectLayout;

public final class TestLayouts {

    private TestLayouts() {
    }

    public static ProjectLayout projectLayout(File projectDir) {
        return new ProjectLayout() {
            @Override
            public File getProjectDir() {
                return projectDir;
            }

            @Override
            public List<String> getSources() {
                return Collections.emptyList();
            }

            @Override
            public List<String> getDocuments() {
                return Collections.emptyList();
            }

            @Override
            public List<String> getTests() {
                return Collections.emptyList();
            }
        };
    }
}
