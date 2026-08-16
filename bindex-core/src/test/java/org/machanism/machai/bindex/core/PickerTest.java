package org.machanism.machai.bindex.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.machanism.machai.schema.Bindex;
import org.machanism.machai.schema.Language;

/** Package-level tests for dependency traversal and language normalization. */
class PickerTest {

    @Test
    void normalizedLanguageNameIsLowercaseAndDropsParentheticalVersion() {
        // Arrange
        Language language = new Language();
        language.setName("  Java (JDK 21) ");

        // Act
        String normalized = Picker.getNormalizedLanguageName(language);

        // Assert
        assertEquals("java", normalized);
    }

    @Test
    void addDependenciesTraversesGraphOnlyOnceAndIgnoresMissingNodes() {
        // Arrange
        BindexRepository repository = mock(BindexRepository.class);
        Bindex root = bindex("root", List.of("child", "missing"));
        Bindex child = bindex("child", List.of("root"));
        when(repository.getBindex("root")).thenReturn(root);
        when(repository.getBindex("child")).thenReturn(child);
        when(repository.getBindex("missing")).thenReturn(null);
        Picker picker = new Picker(repository, null);
        Set<String> dependencies = new HashSet<>();

        // Act
        picker.addDependencies(dependencies, "root");

        // Assert
        assertEquals(Set.of("root", "child"), dependencies);
    }

    @Test
    void addDependenciesLeavesSetUnchangedForMissingRoot() {
        // Arrange
        BindexRepository repository = mock(BindexRepository.class);
        when(repository.getBindex("unknown")).thenReturn(null);
        Picker picker = new Picker(repository, null);
        Set<String> dependencies = new HashSet<>();

        // Act
        picker.addDependencies(dependencies, "unknown");

        // Assert
        assertTrue(dependencies.isEmpty());
    }

    private static Bindex bindex(String id, List<String> dependencies) {
        Bindex value = new Bindex();
        value.setId(id);
        value.setDependencies(dependencies);
        return value;
    }
}
