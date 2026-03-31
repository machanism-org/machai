package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The original BindexRegister class is not part of this module anymore.
 * Keep a minimal sanity test around the test utilities.
 */
class BindexRegisterTest {

	@Test
	void tempDir_isProvided(@TempDir File tempDir) {
		assertNotNull(tempDir);
	}
}
