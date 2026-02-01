package org.machanism.machai.gw;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class GhostwriterTest {

	@Test
	void helpMethod_existsAndIsInvokableViaReflection() throws Exception {
		// Arrange
		var help = Ghostwriter.class.getDeclaredMethod("help", org.apache.commons.cli.Options.class,
				org.apache.commons.cli.HelpFormatter.class);
		help.setAccessible(true);

		// Act
		help.invoke(null, new org.apache.commons.cli.Options(), new org.apache.commons.cli.HelpFormatter());

		// Assert
		assertNotNull(help);
	}
}
