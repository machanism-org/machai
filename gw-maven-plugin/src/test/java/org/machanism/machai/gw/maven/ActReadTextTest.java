package org.machanism.machai.gw.maven;

import static org.junit.Assert.assertEquals;

import java.util.ArrayDeque;
import java.util.Queue;

import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.junit.Test;

public class ActReadTextTest {

	static class QueuePrompter implements Prompter {
		private final Queue<String> lines;

		QueuePrompter(String... lines) {
			this.lines = new ArrayDeque<>();
			for (String l : lines) {
				if (l != null) {
					this.lines.add(l);
				}
			}
		}

		public String prompt(String message) throws PrompterException {
			return lines.poll();
		}

		// Sonar java:S1172 - defaultReply is part of Prompter API; avoid unused parameter by using it
		public String prompt(String message, String defaultReply) throws PrompterException {
			return (defaultReply != null) ? defaultReply : prompt(message);
		}

		public String prompt(String message, java.util.List<String> possibleValues) throws PrompterException {
			return prompt(message);
		}

		public String prompt(String message, java.util.List<String> possibleValues, String defaultReply)
				throws PrompterException {
			return prompt(message, defaultReply);
		}

		public String promptForPassword(String message) throws PrompterException {
			return prompt(message);
		}

		public String promptForPassword(String message, String defaultReply) throws PrompterException {
			return prompt(message, defaultReply);
		}

		public void showMessage(String message) throws PrompterException {
			// no-op
		}
	}

	static class TestableAct extends Act {
		void setPrompter(Prompter prompter) {
			this.prompter = prompter;
		}
	}

	@Test
	public void readText_singleLineWithoutBreaker_returnsLine() throws Exception {
		// Arrange
		TestableAct act = new TestableAct();
		act.setPrompter(new QueuePrompter("Hello world"));

		// Act
		String text = act.readText("Act");

		// Assert
		assertEquals("Hello world", text);
	}

	@Test
	public void readText_multipleLinesWithBreaker_concatenatesWithNewlines() throws Exception {
		// Arrange
		String breaker = org.machanism.machai.gw.processor.Ghostwriter.MULTIPLE_LINES_BREAKER;
		TestableAct act = new TestableAct();
		act.setPrompter(new QueuePrompter("Line1" + breaker, "Line2" + breaker, "Last"));

		// Act
		String text = act.readText("Act");

		// Assert
		assertEquals("Line1\nLine2\nLast", text);
	}

	@Test
	public void readText_breakerOnLastLine_dropsBreakerAndKeepsTrailingNewline() throws Exception {
		// Arrange
		String breaker = org.machanism.machai.gw.processor.Ghostwriter.MULTIPLE_LINES_BREAKER;
		TestableAct act = new TestableAct();
		act.setPrompter(new QueuePrompter("Only" + breaker));

		// Act
		String text = act.readText("Act");

		// Assert
		assertEquals("Only\n", text);
	}
}
