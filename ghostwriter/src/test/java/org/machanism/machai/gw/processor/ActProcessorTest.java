package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;

import org.junit.jupiter.api.Test;

class ActProcessorTest {

	@Test
	void tryLoadActFromDirectory_whenNullActDir_thenNull() throws Exception {
		assertNull(ActProcessor.tryLoadActFromDirectory(new HashMap<>(), "x", null));
	}

}
