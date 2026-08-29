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
import org.machanism.machai.mcp.server.HttpStatelessMcpServer;

class HttpStatelessServerMojoTest {

    @Test
    void executeConfiguresAndStartsStatelessServer() throws Exception {
        TestMojo mojo = new TestMojo();
        HttpStatelessMcpServer server = mock(HttpStatelessMcpServer.class);
        PropertiesConfigurator config = mock(PropertiesConfigurator.class);
        MavenProject project = project("demo", "1.2.3");
        File basedir = new File("target/test-project");
        mojo.server = server;
        mojo.config = config;
        mojo.project = project;
        mojo.basedir = basedir;
        mojo.port = 8181;

        mojo.execute();

        assertEquals(1, mojo.applyCalls);
        verify(server).setProjectDir(basedir);
        verify(server).tools(config);
        verify(server).setPort(8181);
        verify(server).start();
        assertEquals("demo", mojo.createdName);
        assertEquals("1.2.3", mojo.createdVersion);
    }

    @Test
    void executeWrapsServerStartupFailure() throws Exception {
        TestMojo mojo = new TestMojo();
        HttpStatelessMcpServer server = mock(HttpStatelessMcpServer.class);
        RuntimeException failure = new RuntimeException("port unavailable");
        doThrow(failure).when(server).start();
        mojo.server = server;
        mojo.config = mock(PropertiesConfigurator.class);
        mojo.project = project("demo", "1");
        mojo.basedir = new File(".");

        MojoExecutionException exception = assertThrows(MojoExecutionException.class, mojo::execute);

        assertEquals("HttpStateless MCP server failed.", exception.getMessage());
        assertSame(failure, exception.getCause());
    }

    @Test
    void createServerBuildsStatelessServerForProjectIdentity() {
        HttpStatelessMcpServer server = new HttpStatelessServerMojo().createServer("demo", "1.0");

        org.junit.jupiter.api.Assertions.assertNotNull(server);
    }

    private static MavenProject project(String name, String version) {
        MavenProject project = mock(MavenProject.class);
        org.mockito.Mockito.when(project.getName()).thenReturn(name);
        org.mockito.Mockito.when(project.getVersion()).thenReturn(version);
        return project;
    }

    private static final class TestMojo extends HttpStatelessServerMojo {
        private HttpStatelessMcpServer server;
        private PropertiesConfigurator config;
        private int applyCalls;
        private String createdName;
        private String createdVersion;

        @Override public void applyParameters() { applyCalls++; }
        @Override public PropertiesConfigurator getConfigurator() { return config; }
        @Override protected HttpStatelessMcpServer createServer(String name, String version) {
            createdName = name;
            createdVersion = version;
            return server;
        }
    }
}
