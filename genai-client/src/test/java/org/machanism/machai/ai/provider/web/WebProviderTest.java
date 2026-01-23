package org.machanism.machai.ai.provider.web;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.provider.web.WebProvider;

import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link WebProvider}.
 *
 * <p>
 * Validates AE workspace integration, configuration handling, and core error conditions
 * of the {@code WebProvider} class.
 * </p>
 * <pre>
 * <code>
 * WebProvider provider = new WebProvider();
 * provider.model("CodeMie");
 * provider.setWorkingDir(new File("/tmp/test"));
 * String result = provider.perform();
 * </code>
 * </pre>
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
@Disabled
class WebProviderTest {
    /**
     * Verifies that setWorkingDir throws IllegalArgumentException
     * if configuration or AE setup node initialization fails.
     */
    @Test
    void testSetWorkingDir_exception() {
        WebProvider provider = new WebProvider();
        provider.model("CodeMie");
        File invalidDir = new File("/invalid/directory");
        assertThrows(IllegalArgumentException.class, () -> provider.setWorkingDir(invalidDir));
    }

    /**
     * Validates that calling model() with the same config name reports configuration change error.
     */
    @Test
    void testModel_duplicateConfigName_throws() {
        WebProvider provider = new WebProvider();
        provider.model("CodeMie");
        assertThrows(IllegalArgumentException.class, () -> provider.model("CodeMie"));
    }

    /**
     * Example test for perform(). Should only be enabled if AEWorkspace is configured.
     */
    @Test
    @Disabled("Perform logic requires live AEWorkspace and configuration.")
    void testPerform_runsRecipe() {
        WebProvider provider = new WebProvider();
        provider.model("CodeMie");
        provider.setWorkingDir(new File("/tmp/test"));
        String result = provider.perform();
        assertNotNull(result);
        // Further assertions based on actual result could be added here.
    }
}
