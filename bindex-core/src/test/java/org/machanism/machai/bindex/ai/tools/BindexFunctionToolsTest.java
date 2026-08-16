package org.machanism.machai.bindex.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.bindex.core.BindexInfo;
import org.machanism.machai.bindex.core.BindexRepository;
import org.machanism.machai.bindex.core.Picker;
import org.machanism.machai.schema.Bindex;
import org.mockito.MockedConstruction;

import com.sun.net.httpserver.HttpServer;

/** Unit tests for the AI-facing Bindex tool facade. */
class BindexFunctionToolsTest {

    @Test
    void getBindex_readsLocalFileAndAppliesSelection(@TempDir File projectDir) throws Exception {
        // Arrange
        Files.writeString(new File(projectDir, "bindex.json").toPath(),
                "{\"id\":\"demo\",\"name\":\"Demo\",\"version\":\"1\"}");
        BindexFunctionTools tools = new BindexFunctionTools();

        // Act
        Bindex result = tools.getBindex("file://bindex.json", "{ name }", projectDir, mock(Configurator.class));

        // Assert
        assertEquals("Demo", result.getName());
        assertNull(result.getId());
    }

    @Test
    void getBindex_readsAbsoluteLocalFile(@TempDir File projectDir) throws Exception {
        // Arrange
        File descriptor = new File(projectDir, "absolute.json");
        Files.writeString(descriptor.toPath(), "{\"id\":\"absolute\",\"name\":\"Absolute\"}");
        BindexFunctionTools tools = new BindexFunctionTools();

        // Act
        Bindex result = tools.getBindex("file://" + descriptor.getAbsolutePath(), null, projectDir,
                mock(Configurator.class));

        // Assert
        assertEquals("absolute", result.getId());
        assertEquals("Absolute", result.getName());
    }

    @Test
    void registerBindex_acceptsAbsolutePathInsideProject(@TempDir File projectDir) throws Exception {
        // Arrange
        File descriptor = new File(projectDir, "absolute.json");
        Files.writeString(descriptor.toPath(), "{\"id\":\"absolute-id\"}");
        BindexFunctionTools tools = new BindexFunctionTools();
        setRepository(tools, mock(BindexRepository.class));
        try (MockedConstruction<Picker> ignored = org.mockito.Mockito.mockConstruction(Picker.class,
                (picker, context) -> when(picker.save(any(Bindex.class))).thenReturn("saved"))) {
            // Act
            String result = tools.registerBindex(descriptor.getAbsolutePath(), projectDir,
                    mock(Configurator.class));

            // Assert
            assertEquals("saved", result);
        }
    }

    @Test
    void registerBindex_readsHttpJsonDescriptor() throws Exception {
        // Arrange
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/bindex.json", exchange -> {
            byte[] body = "{\"id\":\"remote-id\"}".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            try (var output = exchange.getResponseBody()) {
                output.write(body);
            }
        });
        server.start();
        BindexFunctionTools tools = new BindexFunctionTools();
        setRepository(tools, mock(BindexRepository.class));
        try (MockedConstruction<Picker> ignored = org.mockito.Mockito.mockConstruction(Picker.class,
                (picker, context) -> when(picker.save(any(Bindex.class))).thenReturn("remote-saved"))) {
            try {
                // Act
                String result = tools.registerBindex("http://localhost:" + server.getAddress().getPort()
                        + "/bindex.json", null, mock(Configurator.class));

                // Assert
                assertEquals("remote-saved", result);
            } finally {
                server.stop(0);
            }
        }
    }

    @Test
    void getBindex_readsHttpJsonDescriptor() throws Exception {
        // Arrange
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/bindex.json", exchange -> {
            byte[] body = "{\"id\":\"remote\",\"name\":\"Remote\"}".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            try (var output = exchange.getResponseBody()) {
                output.write(body);
            }
        });
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
        try {
            BindexFunctionTools tools = new BindexFunctionTools();

            // Act
            Bindex result = tools.getBindex("http://localhost:" + server.getAddress().getPort()
                    + "/bindex.json", null, null, mock(Configurator.class));

            // Assert
            assertEquals("remote", result.getId());
            assertEquals("Remote", result.getName());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void getBindex_returnsRepositoryValueAndRejectsMissingRecord() throws Exception {
        // Arrange
        BindexRepository repository = mock(BindexRepository.class);
        Bindex expected = new Bindex();
        expected.setId("demo");
        BindexFunctionTools tools = new BindexFunctionTools();
        var field = BindexFunctionTools.class.getDeclaredField("bindexRepository");
        field.setAccessible(true);
        field.set(tools, repository);
        when(repository.getBindex("demo")).thenReturn(expected);

        // Act and assert
        assertEquals(expected, tools.getBindex("demo", null, null, mock(Configurator.class)));
        when(repository.getBindex("missing")).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> tools.getBindex("missing", null, null, mock(Configurator.class)));
    }

    @Test
    void getRecommendedLibraries_delegatesToPicker(@TempDir File projectDir) throws Exception {
        // Arrange
        Configurator configurator = mock(Configurator.class);
        BindexRepository repository = mock(BindexRepository.class);
        BindexFunctionTools tools = new BindexFunctionTools();
        var field = BindexFunctionTools.class.getDeclaredField("bindexRepository");
        field.setAccessible(true);
        field.set(tools, repository);
        List<BindexInfo> expected = List.of(mock(BindexInfo.class));
        try (MockedConstruction<Picker> ignored =
                     org.mockito.Mockito.mockConstruction(Picker.class,
                             (picker, context) -> when(picker.pick(anyString(), anyLong(), anyDouble(), any(Configurator.class)))
                                     .thenReturn(expected))) {
            // Act
            var actual = tools.getRecommendedLibraries("java client", .8, 3, configurator);

            // Assert
            assertEquals(expected, actual);
        }
    }

    @Test
    void registerBindexJson_setsSchemaAndReturnsPickerId() {
        // Arrange
        Bindex bindex = new Bindex();
        BindexFunctionTools tools = new BindexFunctionTools();
        setRepository(tools, mock(BindexRepository.class));
        try (MockedConstruction<Picker> ignored =
                     org.mockito.Mockito.mockConstruction(Picker.class,
                             (picker, context) -> when(picker.save(any(Bindex.class))).thenReturn("ignored"))) {
            // Act
            String result = tools.registerBindexJson(bindex, mock(Configurator.class));

            // Assert
            assertEquals("ignored", result);
            assertNotNull(bindex.get$schema());
        }
    }

    @Test
    void registerBindex_readsFileSetsSchemaAndReturnsId(@TempDir File projectDir) throws Exception {
        // Arrange
        Files.writeString(new File(projectDir, "bindex.json").toPath(), "{\"id\":\"file-id\"}");
        BindexFunctionTools tools = new BindexFunctionTools();
        setRepository(tools, mock(BindexRepository.class));
        try (MockedConstruction<Picker> ignored =
                     org.mockito.Mockito.mockConstruction(Picker.class,
                             (picker, context) -> when(picker.save(any(Bindex.class))).thenReturn("file-id"))) {
            // Act
            String result = tools.registerBindex("bindex.json", projectDir, mock(Configurator.class));

            // Assert
            assertEquals("file-id", result);
        }
    }

    @Test
    void registerBindex_rejectsMissingProjectDirectoryForLocalPath() {
        // Act and assert
        assertThrows(IllegalArgumentException.class,
                () -> new BindexFunctionTools().registerBindex("bindex.json", null, mock(Configurator.class)));
    }

    @Test
    void registerBindex_rejectsAbsolutePathOutsideProject(@TempDir File projectDir,
            @TempDir File outsideDirectory) throws Exception {
        // Arrange
        File descriptor = new File(outsideDirectory, "outside.json");
        Files.writeString(descriptor.toPath(), "{\"id\":\"outside\"}");

        // Act and assert
        assertThrows(IllegalArgumentException.class,
                () -> new BindexFunctionTools().registerBindex(descriptor.getAbsolutePath(), projectDir,
                        mock(Configurator.class)));
    }

    @Test
    void registerBindex_reportsMissingLocalDescriptor(@TempDir File projectDir) {
        // Act and assert
        assertThrows(java.io.FileNotFoundException.class,
                () -> new BindexFunctionTools().registerBindex("missing.json", projectDir,
                        mock(Configurator.class)));
    }

    @Test
    void resources_areLoadedFromClasspath() throws Exception {
        // Arrange
        BindexFunctionTools tools = new BindexFunctionTools();

        // Act
        String schema = tools.getBindexSchema(URI.create("file:///schema/bindex-schema-v2.json"));
        String prompt = tools.bindexGenerationPrompts();

        // Assert
        assertNotNull(schema);
        assertNotNull(prompt);
        org.junit.jupiter.api.Assertions.assertTrue(schema.contains("$schema"));
        org.junit.jupiter.api.Assertions.assertTrue(prompt.contains("Bindex"));
    }

    private static void setRepository(BindexFunctionTools tools, BindexRepository repository) {
        try {
            var field = BindexFunctionTools.class.getDeclaredField("bindexRepository");
            field.setAccessible(true);
            field.set(tools, repository);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
