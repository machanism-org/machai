package org.machanism.machai.cli;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class MachaiCLIMainTest {

	@Test
	@Disabled("MachaiCLI.main calls System.exit; intercepting via SecurityManager is not supported on this runtime")
	void main_shouldInvokeSystemExit() {
		// no-op
	}
}
