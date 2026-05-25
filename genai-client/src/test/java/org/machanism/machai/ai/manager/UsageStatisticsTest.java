package org.machanism.machai.ai.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UsageStatisticsTest {

    @BeforeEach
    void clearStoredUsages() throws Exception {
        // Arrange
        Field usagesField = UsageStatistics.class.getDeclaredField("modelUsages");
        usagesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, List<Usage>> usages = (Map<String, List<Usage>>) usagesField.get(null);

        // Act
        usages.clear();

        // Assert
        assertTrue(usages.isEmpty());
    }

    @Test
    void addUsageCreatesANewModelEntryAndReturnsDefensiveCopiesForModelLookup() {
        // Arrange
        Usage usage = new Usage(10, 3, 5);

        // Act
        UsageStatistics.addUsage("model-a", usage);
        List<Usage> retrievedUsages = UsageStatistics.getUsageForModel("model-a");

        // Assert
        assertEquals(1, retrievedUsages.size());
        assertEquals(usage, retrievedUsages.get(0));

        retrievedUsages.clear();
        assertEquals(1, UsageStatistics.getUsageForModel("model-a").size());
    }

    @Test
    void addUsageAppendsMultipleEntriesForTheSameModel() {
        // Arrange
        Usage firstUsage = new Usage(1, 2, 3);
        Usage secondUsage = new Usage(4, 5, 6);

        // Act
        UsageStatistics.addUsage("model-b", firstUsage);
        UsageStatistics.addUsage("model-b", secondUsage);
        List<Usage> retrievedUsages = UsageStatistics.getUsageForModel("model-b");

        // Assert
        assertEquals(2, retrievedUsages.size());
        assertEquals(firstUsage, retrievedUsages.get(0));
        assertEquals(secondUsage, retrievedUsages.get(1));
    }

    @Test
    void getUsageForModelReturnsEmptyListWhenModelHasNoEntries() {
        // Arrange

        // Act
        List<Usage> usages = UsageStatistics.getUsageForModel("missing-model");

        // Assert
        assertTrue(usages.isEmpty());
    }

    @Test
    void getAllModelUsagesReturnsShallowCopyOfRegistryMap() {
        // Arrange
        Usage usage = new Usage(7, 8, 9);
        UsageStatistics.addUsage("model-c", usage);

        // Act
        Map<String, List<Usage>> allUsages = UsageStatistics.getAllModelUsages();

        // Assert
        assertEquals(1, allUsages.size());
        assertNotSame(allUsages, UsageStatistics.getAllModelUsages());
        assertEquals(usage, allUsages.get("model-c").get(0));

        allUsages.put("other-model", new ArrayList<>());
        assertTrue(UsageStatistics.getAllModelUsages().get("other-model") == null);
    }

    @Test
    void getAllModelUsagesSharesNestedUsageListsAsDocumented() {
        // Arrange
        UsageStatistics.addUsage("model-d", new Usage(2, 4, 6));

        // Act
        Map<String, List<Usage>> allUsages = UsageStatistics.getAllModelUsages();
        allUsages.get("model-d").add(new Usage(8, 10, 12));

        // Assert
        assertEquals(2, UsageStatistics.getUsageForModel("model-d").size());
    }

    @Test
    void logUsageForModelDoesNotThrowWhenModelHasEntries() {
        // Arrange
        UsageStatistics.addUsage("model-e", new Usage(3, 1, 4));
        UsageStatistics.addUsage("model-e", new Usage(5, 9, 2));

        // Act
        UsageStatistics.logUsageForModel("model-e");

        // Assert
        assertEquals(2, UsageStatistics.getUsageForModel("model-e").size());
    }

    @Test
    void logUsageForModelDoesNotThrowWhenModelIsMissing() {
        // Arrange

        // Act
        UsageStatistics.logUsageForModel("unknown-model");

        // Assert
        assertTrue(UsageStatistics.getUsageForModel("unknown-model").isEmpty());
    }

    @Test
    void logUsageInvokesLoggingForAllRegisteredModelsWithoutChangingStoredValues() {
        // Arrange
        UsageStatistics.addUsage("model-f", new Usage(1, 1, 1));
        UsageStatistics.addUsage("model-g", new Usage(2, 3, 5));

        // Act
        UsageStatistics.logUsage();

        // Assert
        assertEquals(1, UsageStatistics.getUsageForModel("model-f").size());
        assertEquals(1, UsageStatistics.getUsageForModel("model-g").size());
    }
}
