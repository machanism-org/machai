package org.machanism.machai.bindex.fixtures;

import java.io.File;
import java.util.List;

import org.machanism.machai.project.layout.ProjectLayout;

/** Test fixtures for {@link ProjectLayout}. */
public final class ProjectLayouts {

    private ProjectLayouts() {
    }

    public static ProjectLayout layoutWithDir(File dir) {
        return new ProjectLayout() {
            @Override
            public File getProjectDir() {
                return dir;
            }

            @Override
            public List<String> getSources() {
                return null;
            }

            @Override
            public List<String> getDocuments() {
                return null;
            }

            @Override
            public List<String> getTests() {
                return null;
            }
        };
    }
}
