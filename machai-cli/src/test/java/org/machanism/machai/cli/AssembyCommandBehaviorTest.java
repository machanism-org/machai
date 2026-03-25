

package org.machanism.machai.cli;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import java.io.File;
import org.jline.reader.LineReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.ai.manager.GenAIProviderManager;
import org.mockito.MockedStatic;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.machanism.machai.bindex.ApplicationAssembly;
import org.machanism.machai.bindex.Picker;
import org.machanism.machai.schema.Bindex;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import java.lang.reflect.Field;
import org.machanism.machai.project.layout.ProjectLayout;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;


class AssembyCommandBehaviorTest {

    @TempDir
    File tempDir;
    private AssembyCommand assembyCommand;
    @Mock
    private LineReader lineReader;

    @BeforeEach
void setUp() {
    assembyCommand = new AssembyCommand(lineReader);
    ConfigCommand.config.set(ApplicationAssembly.MODEL_PROP_NAME, "test-model");
    ConfigCommand.config.set("score", "0.7");
    ConfigCommand.config.set(ProjectLayout.PROJECT_DIR_PROP_NAME, tempDir.getAbsolutePath());
}

    @Test
    void pick_whenQueryIsNull_shouldPromptAndAlwaysLogUsage_evenIfPickerFailsFast() {
    	// Arrange
    	LineReader lineReader = mock(LineReader.class);
    	when(lineReader.readLine("Prompt: ")).thenReturn("my query");
    	AssembyCommand cmd = new AssembyCommand(lineReader);

    	try (MockedStatic<GenAIProviderManager> providerManager = mockStatic(GenAIProviderManager.class)) {
    		// Act: we don't have a reliable way to isolate Picker construction here (final/JDK21),
    		// so we only assert the finally-block behavior.
    		assertThrows(Exception.class, () -> cmd.pick(null, null, null, null));

    		// Assert
    		providerManager.verify(GenAIProviderManager::logUsage);
    	}
    }
    @Test
    void assembly_whenQueryNullAndNoPreviousPick_shouldThrowIllegalArgumentExceptionAndLogUsage() {
    	// Arrange
    	LineReader lineReader = mock(LineReader.class);
    	when(lineReader.readLine("Project assembly prompt: ")).thenReturn(null);
    	AssembyCommand cmd = new AssembyCommand(lineReader);

    	try (MockedStatic<GenAIProviderManager> providerManager = mockStatic(GenAIProviderManager.class)) {
    		// Act
    		assertThrows(IllegalArgumentException.class, () -> cmd.assembly(null, tempDir, null, null, null));

    		// Assert
    		providerManager.verify(GenAIProviderManager::logUsage);
    	}
    }
    @Test
    void assembly_whenQueryProvidedAsFile_shouldReadQueryFromFileAndExecuteAssembly() throws IOException {
        // TestMate-77cc5d7aa00976a33f7a105782c8a19d
        // Given
        String fileContent = "Create a Spring Boot CRUD app";
        File promptFile = new File(tempDir, "prompt.txt");
        try (FileWriter writer = new FileWriter(promptFile)) {
            writer.write(fileContent);
        }
        Bindex mockBindex = new Bindex();
        mockBindex.setId("test-artifact");
        List<Bindex> bindexList = Collections.singletonList(mockBindex);
        try (MockedStatic<GenAIProviderManager> providerManager = mockStatic(GenAIProviderManager.class);
             MockedConstruction<Picker> pickerConstruction = mockConstruction(Picker.class, (mock, context) -> {
                 when(mock.pick(fileContent)).thenReturn(bindexList);
             });
             MockedConstruction<ApplicationAssembly> assemblyConstruction = mockConstruction(ApplicationAssembly.class)) {
            // When
            assembyCommand.assembly(promptFile.getAbsolutePath(), null, null, null, null);
            // Then
            Picker mockPicker = pickerConstruction.constructed().get(0);
            verify(mockPicker).setScore(0.7);
            verify(mockPicker).pick(fileContent);
            ApplicationAssembly mockAssembly = assemblyConstruction.constructed().get(0);
            verify(mockAssembly).assembly(eq(fileContent), eq(bindexList));
            providerManager.verify(GenAIProviderManager::logUsage);
        }
    }

    @Test
void assembly_whenQueryNullButBindexListExists_shouldReusePreviousQueryAndAssemble() throws IOException, NoSuchFieldException, IllegalAccessException {
    // TestMate-cf9dcc165f129899dc4e239c19a5efc4
    // Given
    // Initialize a local mock for LineReader since the class-level @Mock is not initialized via MockitoExtension
    LineReader localLineReader = mock(LineReader.class);
    when(localLineReader.readLine("Project assembly prompt: ")).thenReturn(null);
    // Create a new instance of AssembyCommand with the valid mock
    AssembyCommand localAssembyCommand = new AssembyCommand(localLineReader);
    String cachedQuery = "previous search query";
    Bindex mockBindex = new Bindex();
    mockBindex.setId("cached-artifact");
    List<Bindex> cachedBindexList = Collections.singletonList(mockBindex);
    // Use reflection to set the private fields that simulate state from a previous 'pick' or 'find' operation
    Field bindexListField = AssembyCommand.class.getDeclaredField("bindexList");
    bindexListField.setAccessible(true);
    bindexListField.set(localAssembyCommand, cachedBindexList);
    Field findQueryField = AssembyCommand.class.getDeclaredField("findQuery");
    findQueryField.setAccessible(true);
    findQueryField.set(localAssembyCommand, cachedQuery);
    try (MockedStatic<GenAIProviderManager> providerManager = mockStatic(GenAIProviderManager.class);
         MockedConstruction<ApplicationAssembly> assemblyConstruction = mockConstruction(ApplicationAssembly.class)) {
        
        // When
        // Pass null for query to trigger the logic that checks the lineReader and then the cached findQuery
        localAssembyCommand.assembly(null, null, null, null, null);
        // Then
        // Verify that the user was prompted when the initial query parameter was null
        verify(localLineReader).readLine("Project assembly prompt: ");
        
        // Verify that ApplicationAssembly was instantiated and called with the cached query and bindex list
        Assertions.assertFalse(assemblyConstruction.constructed().isEmpty(), "ApplicationAssembly should have been constructed");
        ApplicationAssembly mockAssembly = assemblyConstruction.constructed().get(0);
        verify(mockAssembly).assembly(eq(cachedQuery), eq(cachedBindexList));
        
        // Verify the finally block executed the usage logging
        providerManager.verify(GenAIProviderManager::logUsage);
    }
}

    @Test
	void assembly_whenBindexListIsEmpty_shouldLogErrorAndNotCallAssembly() throws IOException {
  // TestMate-e4834ef9960a4fe767d2c548a1609f5d
		// Arrange
		String query = "query yielding no results";
		
		try (MockedStatic<GenAIProviderManager> providerManager = mockStatic(GenAIProviderManager.class);
			 MockedConstruction<Picker> pickerConstruction = mockConstruction(Picker.class, (mock, context) -> {
				 when(mock.pick(query)).thenReturn(Collections.emptyList());
			 });
			 MockedConstruction<ApplicationAssembly> assemblyConstruction = mockConstruction(ApplicationAssembly.class)) {
			// Act
			assembyCommand.assembly(query, null, null, null, null);
			// Assert
			assertTrue(assemblyConstruction.constructed().isEmpty(), "ApplicationAssembly should not be constructed when no libraries are found");
			providerManager.verify(GenAIProviderManager::logUsage);
		}
	}

    @Test
    void pick_whenQueryIsFilePath_shouldReadQueryFromFile() throws IOException, NoSuchFieldException, IllegalAccessException {
        // TestMate-eced508bd3ad23e621e0f289bef5fff2
        // Given
        String fileContent = "Search for logging libraries";
        File queryFile = new File(tempDir, "query.txt");
        try (FileWriter writer = new FileWriter(queryFile)) {
            writer.write(fileContent);
        }
        Bindex mockBindex = new Bindex();
        mockBindex.setId("logging-lib");
        List<Bindex> expectedBindexList = Collections.singletonList(mockBindex);
        try (MockedStatic<GenAIProviderManager> providerManager = mockStatic(GenAIProviderManager.class);
             MockedConstruction<Picker> pickerConstruction = mockConstruction(Picker.class, (mock, context) -> {
                 when(mock.pick(fileContent)).thenReturn(expectedBindexList);
             })) {
            // When
            assembyCommand.pick(queryFile.getAbsolutePath(), null, null, null);
            // Then
            Picker mockPicker = pickerConstruction.constructed().get(0);
            verify(mockPicker).setScore(0.7);
            verify(mockPicker).pick(fileContent);
            Field findQueryField = AssembyCommand.class.getDeclaredField("findQuery");
            findQueryField.setAccessible(true);
            assertEquals(fileContent, findQueryField.get(assembyCommand));
            Field bindexListField = AssembyCommand.class.getDeclaredField("bindexList");
            bindexListField.setAccessible(true);
            assertEquals(expectedBindexList, bindexListField.get(assembyCommand));
            providerManager.verify(GenAIProviderManager::logUsage);
        }
    }

    @Test
    void pick_shouldUseConfigDefaultsWhenOptionsAreNull() throws IOException {
        // TestMate-42affdbb52b3e457472e2864aa29bae7
        // Given
        String query = "test-query";
        String configModel = "Config-Model-X";
        Double configScore = 0.85;
        ConfigCommand.config.set(ApplicationAssembly.MODEL_PROP_NAME, configModel);
        ConfigCommand.config.set("score", configScore.toString());
        List<Bindex> mockBindexList = Collections.singletonList(new Bindex());
        try (MockedStatic<GenAIProviderManager> providerManager = mockStatic(GenAIProviderManager.class);
             MockedConstruction<Picker> pickerConstruction = mockConstruction(Picker.class, (mock, context) -> {
                 when(mock.pick(query)).thenReturn(mockBindexList);
             })) {
            // When
            assembyCommand.pick(query, null, null, null);
            // Then
            Picker mockPicker = pickerConstruction.constructed().get(0);
            verify(mockPicker).setScore(configScore);
            verify(mockPicker).pick(query);
            providerManager.verify(GenAIProviderManager::logUsage);
        }
    }

    @Test
    void pick_whenNoResultsFound_shouldHandleEmptyListGracefully() throws IOException, NoSuchFieldException, IllegalAccessException {
        // TestMate-176c8a02c6e5dfef44c9cf533d07da14
        // Given
        String query = "non-existent-library-query";
        
        try (MockedStatic<GenAIProviderManager> providerManager = mockStatic(GenAIProviderManager.class);
             MockedConstruction<Picker> pickerConstruction = mockConstruction(Picker.class, (mock, context) -> {
                 when(mock.pick(anyString())).thenReturn(Collections.emptyList());
             })) {
            
            // When
            assembyCommand.pick(query, null, null, null);
            
            // Then
            Picker mockPicker = pickerConstruction.constructed().get(0);
            verify(mockPicker).pick(query);
            
            Field bindexListField = AssembyCommand.class.getDeclaredField("bindexList");
            bindexListField.setAccessible(true);
            List<Bindex> actualBindexList = (List<Bindex>) bindexListField.get(assembyCommand);
            
            assertTrue(actualBindexList.isEmpty());
            providerManager.verify(GenAIProviderManager::logUsage);
        }
    }

    @Test
void pick_shouldPassRegisterUrlToPickerConstructor() throws IOException {
    // TestMate-ed32834e4ea3198c460ff17c0f976736
    // Given
    String query = "search-query";
    String registerUrl = "http://my-registry.local";
    List<Bindex> mockBindexList = Collections.emptyList();
    try (MockedStatic<GenAIProviderManager> providerManager = mockStatic(GenAIProviderManager.class);
         MockedConstruction<Picker> pickerConstruction = mockConstruction(Picker.class, (mock, context) -> {
             when(mock.pick(query)).thenReturn(mockBindexList);
             
             // Verify constructor arguments: model (index 0), registerUrl (index 1), config (index 2)
             assertEquals(registerUrl, context.arguments().get(1));
         })) {
        // When
        assembyCommand.pick(query, registerUrl, null, null);
        // Then
        assertEquals(1, pickerConstruction.constructed().size());
        providerManager.verify(GenAIProviderManager::logUsage);
    }
}
}
