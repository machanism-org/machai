package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
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
    void moveAndRepeatEpisodeShouldThrowExpectedExceptions() {
        ActFunctionTools tools = new ActFunctionTools();
        ObjectNode moveProps = MAPPER.createObjectNode();
        moveProps.put("id", "2");

        ObjectNode repeatProps = MAPPER.createObjectNode();
        repeatProps.put("message", "again");
        File projectDir = tempDir.toFile();

        MoveToEpisodeException moveException = assertThrows(MoveToEpisodeException.class,
                () -> tools.moveToEpisode(moveProps, projectDir));
        assertThrows(RepeatEpisodeException.class, () -> repeatEpisode(tools, repeatProps, projectDir));

        assertEquals(2, moveException.getEpisodeId());
    }

    private static void repeatEpisode(ActFunctionTools tools, ObjectNode repeatProps, File projectDir) {
        tools.repeateEpisode(repeatProps, projectDir);
    }

    @Test
    void getActDetailsShouldReturnMessageForUnknownAct() throws Exception {
        ActFunctionTools tools = new ActFunctionTools();
        tools.setConfigurator(new PropertiesConfigurator());
        ObjectNode props = MAPPER.createObjectNode();
        props.put("actName", "definitely-missing-act");
        props.put("custom", false);

        Object result = tools.getActDetails(props, tempDir.toFile());

        assertTrue(!result.toString().isEmpty());
    }
}
