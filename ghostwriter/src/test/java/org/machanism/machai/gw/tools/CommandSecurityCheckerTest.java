package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;

class CommandSecurityCheckerTest {

    @Test
    void denyCheckShouldRejectConfiguredKeywordIgnoringCase() throws Exception {
        Configurator configurator = mock(Configurator.class);
        when(configurator.get("ft.command.denylist", null)).thenReturn("KEYWORD:DangerCommand");
        CommandSecurityChecker checker = new CommandSecurityChecker(configurator);

        DenyException exception = assertThrows(DenyException.class,
                () -> checker.denyCheck("cmd /c dangercommand now"));

        assertTrue(exception.getMessage().contains("Keyword: DangerCommand"));
    }

    @Test
    void denyCheckShouldRejectConfiguredRegexMatch() throws Exception {
        Configurator configurator = mock(Configurator.class);
        when(configurator.get("ft.command.denylist", null)).thenReturn("REGEX:.*secret-[0-9]+.*");
        CommandSecurityChecker checker = new CommandSecurityChecker(configurator);

        DenyException exception = assertThrows(DenyException.class,
                () -> checker.denyCheck("echo secret-123"));

        assertTrue(exception.getMessage().contains("Pattern: .*secret-[0-9]+.*"));
    }

    @Test
    void denyCheckShouldAllowCommandWhenRulesDoNotMatch() throws Exception {
        Configurator configurator = mock(Configurator.class);
        when(configurator.get("ft.command.denylist", null)).thenReturn("KEYWORD:blocked\nREGEX:^never$");
        CommandSecurityChecker checker = new CommandSecurityChecker(configurator);

        assertDoesNotThrow(() -> checker.denyCheck("echo hello"));
    }

    @Test
    void loadRulesShouldIgnoreNullEmptyCommentsAndUnknownRules() throws Exception {
        Configurator configurator = mock(Configurator.class);
        when(configurator.get("ft.command.denylist", null)).thenReturn("");
        CommandSecurityChecker checker = new CommandSecurityChecker(configurator);
        Method loadRules = CommandSecurityChecker.class.getDeclaredMethod("loadRules", String.class);
        loadRules.setAccessible(true);

        loadRules.invoke(checker, (String) null);
        loadRules.invoke(checker, "\n# comment\nUNKNOWN:value\n  KEYWORD:badword  \n  REGEX:bad[0-9]+  ");

        assertThrows(DenyException.class, () -> checker.denyCheck("contains BADWORD"));
        assertThrows(DenyException.class, () -> checker.denyCheck("bad42"));
    }
}
