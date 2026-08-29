package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;

class LogBuilderTest {

	@TempDir
	Path tempDir;

	@Test
	void appendShouldRetainTailTrackTotalAndReportTruncation() {
		// Arrange
		LogBuilder builder = new LogBuilder("unused", 4, null, null);

		// Act
		assertSame(builder, builder.append("abc"));
		builder.append("def");
		Map<String, Object> report = builder.getReport();

		// Assert
		assertEquals("cdef", builder.getTail());
		assertEquals(4, builder.length());
		assertEquals(6, builder.getTotalLength());
		assertEquals("cdef", report.get("tail"));
		assertEquals(6, report.get("total_length"));
		assertEquals(Boolean.TRUE, report.get("truncated"));
		assertTrue((Long) report.get("processTime_ms") >= 0L);
	}

	@Test
	void appendNullAndClearShouldNotChangeTotalButShouldResetRetainedState() {
		// Arrange
		LogBuilder builder = new LogBuilder("unused", 3, null, null);
		builder.append("abcd");

		// Act
		builder.append(null);
		builder.clear();

		// Assert
		assertEquals("", builder.getTail());
		assertEquals(0, builder.length());
		assertEquals(4, builder.getTotalLength());
		assertFalse((Boolean) builder.getReport().get("truncated"));
	}

	@Test
	void appendWithLogIdentityShouldPersistUtf8ContentToCommandLog() throws Exception {
		// Arrange
		String folder = "gw-logbuilder-test-" + System.nanoTime();
		String logId = "command";
		LogBuilder builder = new LogBuilder(folder, 20, logId, tempDir.toFile());
		Path logPath = LogBuilder.getCommandLogPath(folder, logId);

		// Act
		builder.append("first-").append("second");

		// Assert
		assertEquals("first-second", new String(Files.readAllBytes(logPath), StandardCharsets.UTF_8));
		assertEquals("first-second", builder.getTail());
		Files.deleteIfExists(logPath);
		Files.deleteIfExists(logPath.getParent());
	}

	@Test
	void constructorShouldRejectNonPositiveMaximumSize() {
		// Arrange / Act / Assert
		Executable executable = () -> new LogBuilder("logs", 0, null, new File("."));
		org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
				executable);
	}
}
