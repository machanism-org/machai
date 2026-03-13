package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

class ActProcessorTest {

	@Test
	void tryLoadActFromDirectory_whenNullActDir_thenNull() throws Exception {
		assertNull(ActProcessor.tryLoadActFromDirectory(new HashMap<>(), "x", null));
	}

	@Test
	void setActData_whenStringInheritance_thenFormats() {
		Map<String, Object> props = new HashMap<>();
		props.put("inputs", "Hello %s");

		TomlParseResult toml = Toml.parse("inputs='World'");
		ActProcessor.setActData(props, toml);
		assertEquals("Hello World", props.get("inputs"));
	}

}
