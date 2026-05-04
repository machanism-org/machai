package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class ActFunctionToolsAdditionalTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @TempDir
    Path tempDir;

    @Test
    void putAndGetProjectContextVariableShouldHandleSuccessMissingAndFailure() {
        ActFunctionTools tools = new ActFunctionTools();
        ObjectNode putProps = MAPPER.createObjectNode();
        putProps.put("name", "episode");
        putProps.put("value", "42");

        ObjectNode getProps = MAPPER.createObjectNode();
        getProps.put("name", "episode");

        ObjectNode missingProps = MAPPER.createObjectNode();
        missingProps.put("name", "missing");

        Object putResult = tools.putProjectContextVariable(putProps, tempDir.toFile());
        Object getResult = tools.getProjectContextVariable(getProps, tempDir.toFile());
        Object missingResult = tools.getProjectContextVariable(missingProps, tempDir.toFile());
        Object failedPutResult = tools.putProjectContextVariable(MAPPER.createObjectNode(), tempDir.toFile());

        assertTrue(putResult.toString().contains("Context variable 'episode' set to '42'"));
        assertEquals("42", getResult);
        assertTrue(missingResult.toString().contains("not found"));
        assertTrue(failedPutResult.toString().contains("Failed to set context variable"));
    }

    @Test
    void moveAndRepeatEpisodeShouldThrowExpectedExceptions() {
        ActFunctionTools tools = new ActFunctionTools();
        ObjectNode moveProps = MAPPER.createObjectNode();
        moveProps.put("id", "ep-2");

        ObjectNode repeatProps = MAPPER.createObjectNode();
        repeatProps.put("message", "again");

        MoveToEpisodeException moveException = assertThrows(MoveToEpisodeException.class,
                () -> tools.moveToEpisode(moveProps, tempDir.toFile()));
        assertThrows(RepeatEpisodeException.class, () -> tools.repeateEpisode(repeatProps, tempDir.toFile()));

        assertEquals("ep-2", moveException.getEpisodeId());
    }

    @Test
    void getActDetailsShouldReturnMessageForUnknownAct() throws Exception {
        ActFunctionTools tools = new ActFunctionTools();
        tools.setConfigurator(new PropertiesConfigurator());
        ObjectNode props = MAPPER.createObjectNode();
        props.put("actName", "definitely-missing-act");
        props.put("custom", false);

        Object result = tools.getActDetails(props, tempDir.toFile());

        assertTrue(result.toString().length() > 0);
    }
}
