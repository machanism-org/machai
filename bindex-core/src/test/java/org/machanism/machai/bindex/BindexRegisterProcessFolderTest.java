package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The original BindexRegister class is not part of this module anymore.
 * Keep a minimal test ensuring JUnit TempDir works.
 */
class BindexRegisterProcessFolderTest {

	@Test
	void tempDir_isProvided(@TempDir File tempDir) {
		assertNotNull(tempDir);
	}
}
