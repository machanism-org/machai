package org.machanism.machai.gw.tools;

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
