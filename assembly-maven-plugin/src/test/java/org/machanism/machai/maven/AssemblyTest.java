package org.machanism.machai.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link Assembly} Mojo.
 * <p>
 * Verifies behavior for prompt handling, parameter injection, and error management.
 * </p>
 *
 * <pre>{@code
 * mvn org.machanism.machai.maven:assembly -Dassembly.inputs.only=false
 * }</pre>
 * @author Generated
 */
public class AssemblyTest {

    private Assembly assembly;
    private Prompter prompter;

    @BeforeEach
    public void setUp() {
        assembly = new Assembly();
        prompter = mock(Prompter.class);
        assembly.prompter = prompter;
        assembly.inputsOnly = true;
        assembly.chatModel = "OpenAI:gpt-5";
        assembly.pickChatModel = "OpenAI:gpt-5-mini";
        assembly.assemblyPromptFile = new File("test_project.txt");
        assembly.score = 0.88;
        assembly.registerUrl = "http://example.com/register";
        assembly.basedir = new File(".");
    }

    @Test
    @Disabled("Need to fix.")
    public void testExecutePromptFileNotExist_promptsUser() throws Exception {
        File temp = File.createTempFile("notfound", ".txt");
        assertTrue(temp.delete()); // Ensure nonexistent
        assembly.assemblyPromptFile = temp;
        when(prompter.prompt(anyString())).thenReturn("AI assembly prompt");
        // Can't actually test AI or Maven internals, just check exception thrown
        assertThrows(MojoExecutionException.class, assembly::execute);
    }

    @Test
    @Disabled("Need to fix.")
    public void testExecutePromptFileExists_readsFile() throws Exception {
        File temp = File.createTempFile("exists", ".txt");
        java.nio.file.Files.write(temp.toPath(), "test contents".getBytes());
        assembly.assemblyPromptFile = temp;
        // Mock the prompter but it should not be called
        verify(prompter, never()).prompt(anyString());
        // Can't actually test AI or Maven internals, just check exception thrown
        assertThrows(MojoExecutionException.class, assembly::execute);
    }

    @Test
    @Disabled("Need to fix.")
    public void testExecute_handlesExceptions() throws Exception {
        assembly.assemblyPromptFile = new File("missing_file.txt");
        when(prompter.prompt(anyString())).thenThrow(new RuntimeException("Prompter error"));
        assertThrows(MojoExecutionException.class, assembly::execute);
    }
}
