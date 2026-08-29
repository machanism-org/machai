package org.machanism.machai.mcp.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.mcp.server.HttpStreamableMcpServer;

class HttpStreamableMcpServerMojoTest {

    @Test
    void executeConfiguresAndStartsStreamableServer() throws Exception {
        TestMojo mojo = new TestMojo();
        HttpStreamableMcpServer server = mock(HttpStreamableMcpServer.class);
        PropertiesConfigurator config = mock(PropertiesConfigurator.class);
        mojo.server = server;
        mojo.config = config;
        mojo.project = project("stream-demo", "2.0");
        mojo.basedir = new File("target/stream-project");
        mojo.port = 9191;

        mojo.execute();

        assertEquals(1, mojo.applyCalls);
        verify(server).setProjectDir(mojo.basedir);
        verify(server).tools(config);
        verify(server).setPort(9191);
        verify(server).start();
        assertEquals("stream-demo", mojo.createdName);
        assertEquals("2.0", mojo.createdVersion);
    }

    @Test
    void executeWrapsStreamableServerStartupFailure() throws Exception {
        TestMojo mojo = new TestMojo();
        HttpStreamableMcpServer server = mock(HttpStreamableMcpServer.class);
        RuntimeException failure = new RuntimeException("startup failure");
        doThrow(failure).when(server).start();
        mojo.server = server;
        mojo.config = mock(PropertiesConfigurator.class);
        mojo.project = project("stream-demo", "2.0");
        mojo.basedir = new File(".");

        MojoExecutionException exception = assertThrows(MojoExecutionException.class, mojo::execute);

        assertEquals("HttpStateless MCP server failed.", exception.getMessage());
        assertSame(failure, exception.getCause());
    }

    @Test
    void createServerBuildsStreamableServerForProjectIdentity() {
        HttpStreamableMcpServer server = new HttpStreamableMcpServerMojo().createServer("stream", "1.0");

        org.junit.jupiter.api.Assertions.assertNotNull(server);
    }

    private static MavenProject project(String name, String version) {
        MavenProject project = mock(MavenProject.class);
        org.mockito.Mockito.when(project.getName()).thenReturn(name);
        org.mockito.Mockito.when(project.getVersion()).thenReturn(version);
        return project;
    }

    private static final class TestMojo extends HttpStreamableMcpServerMojo {
        private HttpStreamableMcpServer server;
        private PropertiesConfigurator config;
        private int applyCalls;
        private String createdName;
        private String createdVersion;

        @Override public void applyParameters() { applyCalls++; }
        @Override public PropertiesConfigurator getConfigurator() { return config; }
        @Override protected HttpStreamableMcpServer createServer(String name, String version) {
            createdName = name;
            createdVersion = version;
            return server;
        }
    }
}
