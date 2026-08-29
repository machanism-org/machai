package org.machanism.machai.mcp.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.mcp.server.McpServer;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class AbstractMCPServerMojoTest {

    private static final String NEW_PROPERTY = "mcp.mojo.test.new";
    private static final String EXISTING_PROPERTY = "mcp.mojo.test.existing";

    @AfterEach
    void clearProperties() {
        System.clearProperty(NEW_PROPERTY);
        System.clearProperty(EXISTING_PROPERTY);
    }

    @Test
    void applyParametersSetsOnlyPropertiesThatAreAbsent() throws Exception {
        TestMojo mojo = new TestMojo();
        mojo.params = Map.of(NEW_PROPERTY, "new-value", EXISTING_PROPERTY, "replacement");
        System.setProperty(EXISTING_PROPERTY, "original");

        mojo.applyParameters();

        assertEquals("new-value", System.getProperty(NEW_PROPERTY));
        assertEquals("original", System.getProperty(EXISTING_PROPERTY));
    }

    @Test
    void getConfiguratorDelegatesUsingAbsoluteConfigurationPath() throws Exception {
        TestMojo mojo = new TestMojo();
        File config = new File("configuration.properties");
        mojo.setConfigFile(config);
        PropertiesConfigurator expected = org.mockito.Mockito.mock(PropertiesConfigurator.class);
        mojo.configurator = expected;

        PropertiesConfigurator actual = mojo.getConfigurator();

        assertSame(expected, actual);
        assertEquals(config.getAbsolutePath(), mojo.requestedPath);
    }

    @Test
    void getConfiguratorWrapsLoaderFailureInMojoExecutionException() {
        TestMojo mojo = new TestMojo();
        File config = new File("broken.properties");
        mojo.setConfigFile(config);
        mojo.failure = new IllegalStateException("invalid configuration");

        MojoExecutionException exception = assertThrows(MojoExecutionException.class, mojo::getConfigurator);

        assertEquals("Failed to load configuration from: " + config, exception.getMessage());
        assertSame(mojo.failure, exception.getCause());
    }

    @Test
    void configurationLoadingExceptionRetainsMessageAndCause() {
        IllegalArgumentException cause = new IllegalArgumentException("unreadable file");

        AbstractMCPServerMojo.ConfigurationLoadingException exception =
                new AbstractMCPServerMojo.ConfigurationLoadingException("Unable to load MCP server configuration", cause);

        assertEquals("Unable to load MCP server configuration", exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void loadConfiguratorReturnsValueProvidedByServerBootstrap() throws Exception {
        RawLoadingMojo mojo = new RawLoadingMojo();
        PropertiesConfigurator expected = Mockito.mock(PropertiesConfigurator.class);

        try (MockedStatic<McpServer> server = Mockito.mockStatic(McpServer.class)) {
            server.when(() -> McpServer.getConfigurator("/tmp/mcp.properties")).thenReturn(expected);

            PropertiesConfigurator actual = mojo.loadConfigurator("/tmp/mcp.properties");

            assertSame(expected, actual);
        }
    }

    @Test
    void loadConfiguratorWrapsServerBootstrapFailure() {
        RawLoadingMojo mojo = new RawLoadingMojo();
        IllegalArgumentException cause = new IllegalArgumentException("malformed configuration");

        try (MockedStatic<McpServer> server = Mockito.mockStatic(McpServer.class)) {
            server.when(() -> McpServer.getConfigurator("/tmp/broken.properties")).thenThrow(cause);

            AbstractMCPServerMojo.ConfigurationLoadingException exception = assertThrows(
                    AbstractMCPServerMojo.ConfigurationLoadingException.class,
                    () -> mojo.loadConfigurator("/tmp/broken.properties"));

            assertEquals("Unable to load MCP server configuration", exception.getMessage());
            assertSame(cause, exception.getCause());
        }
    }

    private static final class TestMojo extends AbstractMCPServerMojo {
        private PropertiesConfigurator configurator;
        private RuntimeException failure;
        private String requestedPath;

        void setConfigFile(File value) {
            try {
                var field = AbstractMCPServerMojo.class.getDeclaredField("configFile");
                field.setAccessible(true);
                field.set(this, value);
            } catch (ReflectiveOperationException exception) {
                throw new AssertionError(exception);
            }
        }

        @Override
        protected PropertiesConfigurator loadConfigurator(String configurationPath) {
            requestedPath = configurationPath;
            if (failure != null) {
                throw failure;
            }
            return configurator;
        }

        @Override
        public void execute() {
            // Not relevant to these base-class tests.
        }
    }

    private static final class RawLoadingMojo extends AbstractMCPServerMojo {
        @Override
        public void execute() {
            // Not relevant to this loader test.
        }
    }
}
