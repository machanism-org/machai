

package org.machanism.machai.ai.tools;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import org.mockito.InjectMocks;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import org.mockito.MockedStatic;
import java.net.URL;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import java.nio.charset.StandardCharsets;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


class CommandSecurityCheckerTest {

    @Mock
    private Configurator configurator;

    @BeforeEach
void setUp() {
    MockitoAnnotations.openMocks(this);
}

    @Test
    void testDenyCheckWhenCommandMatchesRegexShouldThrowDenyException() throws Exception {
        // TestMate-66da950a6919fb355f255caa76d994b9
        // Given
        // The regex rule we want to test
        String regexRule = ".*\\.py";

        /* 
         * The constructor logic is:
         * 1. Load denylist from classpath file (windows.txt or unix.txt).
         * 2. Get 'ft.command.denylist' from configurator.
         * 3. If present, use it as a format string: String.format(configValue, fileContent).
         * 
         * By returning "REGEX:.*\\.py\n%s", we ensure that our rule is prepended 
         * to whatever content is actually in the classpath file, making the test 
         * independent of the physical file content without needing static mocks.
         */
        String rulesFormat = "REGEX:" + regexRule + "\n%s";
        when(configurator.get(eq("ft.command.denylist"), nullable(String.class))).thenReturn(rulesFormat);

        // Instantiate the checker. This will trigger the loading logic in the constructor.
        CommandSecurityChecker checker = new CommandSecurityChecker(configurator);

        // A command that matches the regex ".*\\.py"
        String dangerousCommand = "python script.py";

        // When
        DenyException exception = assertThrows(DenyException.class, () -> checker.denyCheck(dangerousCommand));

        // Then
        // The message should reflect the pattern that triggered the exception
        assertEquals("Pattern: " + regexRule, exception.getMessage());
    }
    @Test
    void testDenyCheckWhenCommandContainsSpecialCharactersShouldMatchCorrectily() throws Exception {
        // TestMate-ae8ddd38341c9a5df9f1aba025c2cf7f
        // Given
        // We use a keyword that is unlikely to trigger existing Regex rules in the default denylist (like 'passwd').
        // The previous failure showed that 'passwd' in '/etc/passwd' triggered a default REGEX rule before the KEYWORD check.
        String keywordRule = ">> /tmp/special_secret_file";
        String rulesFormat = "KEYWORD:" + keywordRule + "\n%s";
        when(configurator.get(eq("ft.command.denylist"), nullable(String.class))).thenReturn(rulesFormat);

        CommandSecurityChecker checker = new CommandSecurityChecker(configurator);
        String dangerousCommand = "cat data >> /tmp/special_secret_file";
        String safeCommand = "echo test > /tmp/file";

        // When
        DenyException exception = assertThrows(DenyException.class, () -> checker.denyCheck(dangerousCommand));

        // Then
        assertEquals("Keyword: " + keywordRule, exception.getMessage());
        assertDoesNotThrow(() -> checker.denyCheck(safeCommand));
    }

    @Test
void testDenyCheckWhenCommandIsEmptyShouldPass() throws Exception {
    // TestMate-cf28b805a067a2296b565bc05216cfaf
    // Given
    String rulesFormat = "REGEX:rm -rf\nKEYWORD:sudo\n%s";
    when(configurator.get(eq("ft.command.denylist"), nullable(String.class))).thenReturn(rulesFormat);
    CommandSecurityChecker checker = new CommandSecurityChecker(configurator);
    String emptyCommand = "";
    // When / Then
    assertDoesNotThrow(() -> checker.denyCheck(emptyCommand));
}
}
