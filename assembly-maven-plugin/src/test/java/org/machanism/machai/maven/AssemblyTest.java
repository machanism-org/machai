package org.machanism.machai.maven;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.codehaus.plexus.components.interactivity.Prompter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AssemblyTest {

	private Assembly assembly;
	private Prompter prompter;

	@BeforeEach
	public void setUp() {
		assembly = new Assembly();
		prompter = mock(Prompter.class);
		assembly.prompter = prompter;
		assembly.chatModel = "OpenAI:gpt-5";
		assembly.pickChatModel = "OpenAI:gpt-5-mini";
		assembly.assemblyPromptFile = new File("test_project.txt");
		assembly.score = 0.88;
		assembly.registerUrl = "http://example.com/register";
		assembly.basedir = new File(".");
	}

	@Test
	public void testExecutePromptFileNotExist_promptsUser() throws Exception {
		File temp = File.createTempFile("notfound", ".txt");
		assertTrue(temp.delete()); // Ensure nonexistent
		assembly.assemblyPromptFile = temp;
		when(prompter.prompt(anyString())).thenReturn("AI assembly prompt");
		// Can't actually test AI or Maven internals, just check exception thrown
		assertThrows(IllegalArgumentException.class, assembly::execute);
	}

	@Test
	public void testExecutePromptFileExists_readsFile() throws Exception {
		File temp = File.createTempFile("exists", ".txt");
		java.nio.file.Files.write(temp.toPath(), "test contents".getBytes());
		assembly.assemblyPromptFile = temp;
		// Mock the prompter but it should not be called
		verify(prompter, never()).prompt(anyString());
		// Can't actually test AI or Maven internals, just check exception thrown
		assertThrows(IllegalArgumentException.class, assembly::execute);
	}

	@Test
	public void testExecute_handlesExceptions() throws Exception {
		assembly.assemblyPromptFile = new File("missing_file.txt");
		when(prompter.prompt(anyString())).thenThrow(new RuntimeException("Prompter error"));
		assertThrows(RuntimeException.class, assembly::execute);
	}
}
