package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.bindex.builder.BindexBuilder;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema.Bindex;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for {@link BindexCreator}.
 * <p>
 * Test Bindex creation/updating logic, chaining setter, and error handling.
 * </p>
 *
 * @author Viktor Tovstyi
 */
class BindexCreatorTest {
    private GenAIProvider provider;
    private ProjectLayout projectLayout;
    private BindexCreator creator;
    private BindexBuilder builder;
    private Bindex bindex;
    private ObjectMapper objectMapper;

    /**
     * Set up mock objects and test fixture for each test.
     */
    @BeforeEach
    void setUp() {
        provider = mock(GenAIProvider.class);
        projectLayout = mock(ProjectLayout.class);
        builder = mock(BindexBuilder.class);
        bindex = mock(Bindex.class);
        objectMapper = mock(ObjectMapper.class);
        creator = new BindexCreator(provider);
    }

    /**
     * Tests that {@link BindexCreator#update(boolean)} returns this instance for chaining.
     */
    @Test
    void testUpdateSetterReturnsSelf() {
        BindexCreator self = creator.update(true);
        assertSame(creator, self);
    }

    /**
     * Tests the folder processing creates the Bindex file when update is true.
     * @throws IOException if mocked components fail
     */
    @Test
    @Disabled("Need to fix.")
    void testProcessFolderCreatesBindexFileAndLogs() throws IOException {
        File projectDir = new File("/tmp/project");
        File bindexFile = new File(projectDir, "bindex.json");
        when(projectLayout.getProjectDir()).thenReturn(projectDir);
        // Simulate bindex.json missing
        doReturn(builder).when(BindexBuilderFactory.class);
        BindexCreator creatorSpy = spy(creator);
        doReturn(null).when(creatorSpy).getBindex(projectDir);
        doReturn(bindexFile).when(creatorSpy).getBindexFile(projectDir);
        doReturn(bindex).when(builder).build();
        doNothing().when(objectMapper).writeValue(bindexFile, bindex);
        creatorSpy.update(true);
        // Should not throw any exceptions
        assertDoesNotThrow(() -> creatorSpy.processFolder(projectLayout));
    }

    /**
     * Tests that folder processing throws IllegalArgumentException on I/O errors.
     * @throws IOException if mocked builder fails
     */
    @Test
    @Disabled("Need to fix.")
    void testProcessFolderThrowsOnIOException() throws IOException {
        ProjectLayout layout = mock(ProjectLayout.class);
        BindexCreator creatorSpy = spy(new BindexCreator(provider));
        File projectDir = new File("/tmp/project2");
        when(layout.getProjectDir()).thenReturn(projectDir);
        doReturn(null).when(creatorSpy).getBindex(projectDir);
        doReturn(new File(projectDir, "bindex.json")).when(creatorSpy).getBindexFile(projectDir);
        doThrow(new IOException("fail")).when(builder).build();
        doReturn(builder).when(BindexBuilderFactory.class);
        creatorSpy.update(true);
        assertThrows(IllegalArgumentException.class, () -> creatorSpy.processFolder(layout));
    }
}
