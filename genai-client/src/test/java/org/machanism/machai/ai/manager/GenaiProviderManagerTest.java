package org.machanism.machai.ai.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;

class GenaiProviderManagerTest {

    @BeforeEach
    void resetUsages() throws Exception {
        Field usagesField = GenaiProviderManager.class.getDeclaredField("usages");
        usagesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Usage> usages = (List<Usage>) usagesField.get(null);
        usages.clear();
    }

    @Test
    void getProviderShouldRejectUnknownProvider() {
        Configurator configurator = mock(Configurator.class);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> GenaiProviderManager.getProvider("MissingProvider:model", configurator));

        assertEquals(
                "Failed to initialize GenAI provider 'MissingProvider': provider is not supported or an error occurred during initialization.",
                ex.getMessage());
    }

    @Test
    void getProviderWithoutColonShouldTreatWholeValueAsProviderName() {
        Configurator configurator = mock(Configurator.class);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> GenaiProviderManager.getProvider("standalone-model", configurator));

        assertEquals(
                "Failed to initialize GenAI provider 'standalone-model': provider is not supported or an error occurred during initialization.",
                ex.getMessage());
    }

    @Test
    void addUsageShouldAcceptEntriesIncludingNull() throws Exception {
        Usage usage = new Usage(1, 2, 3);

        GenaiProviderManager.addUsage(usage);
        GenaiProviderManager.addUsage(null);

        Field usagesField = GenaiProviderManager.class.getDeclaredField("usages");
        usagesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Usage> usages = (List<Usage>) usagesField.get(null);
        assertEquals(2, usages.size());
        assertSame(usage, usages.get(0));
        assertSame(null, usages.get(1));
    }
}
