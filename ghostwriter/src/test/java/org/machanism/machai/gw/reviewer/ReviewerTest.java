package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

class ReviewerTest {

	private static final class TestReviewer implements Reviewer {
		@Override
		public String perform(java.io.File projectDir, java.io.File file) {
			throw new UnsupportedOperationException("not used");
		}

		@Override
		public String[] getSupportedFileExtensions() {
			return new String[] { "a", "b" };
		}
	}

	@Test
	void getSupportedFileExtensions_returnsExpectedArray() {
		Reviewer reviewer = new TestReviewer();
		assertArrayEquals(new String[] { "a", "b" }, reviewer.getSupportedFileExtensions());
	}
}
