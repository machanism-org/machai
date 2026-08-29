package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.tools.MoveToEpisodeException;
import org.machanism.machai.gw.tools.RepeatEpisodeException;

class EpisodesTest {

	@Test
	void selectionValidationAndMetadataUseOneBasedIdsAndHeadings() {
		Episodes episodes = episodes("# First\nbody", "---\nx: y\n---\n# Second\nbody", "no heading");

		episodes.setSelectedEpisodes(Arrays.asList(3, 1));

		assertFalse(episodes.isRegularOrder());
		assertEquals(3, episodes.size());
		assertEquals("First", ((java.util.List<?>) episodes.getActInformation(2).get("EPISODES")).isEmpty() ? null
				: ((java.util.Map<?, ?>) ((java.util.List<?>) episodes.getActInformation(2).get("EPISODES")).get(0)).get("EPISODE_NAME"));
		assertEquals("Second", ((java.util.Map<?, ?>) ((java.util.List<?>) episodes.getActInformation(2).get("EPISODES")).get(1)).get("EPISODE_NAME"));
		assertEquals(2, episodes.getActInformation(2).get("CURRENT_EPISODE_ID"));
		Executable executable = () -> episodes.setSelectedEpisodes(Collections.singletonList(0));
		assertThrows(IllegalArgumentException.class, executable);
		Executable executable2 = () -> episodes.setSelectedEpisodes(Collections.singletonList(4));
		assertThrows(IllegalArgumentException.class, executable2);
	}

	@Test
	void requestedOrderRepeatsRequestedEpisodeAndRecordsResults() {
		ActProcessor processor = processor();
		Episodes episodes = new Episodes(processor);
		episodes.setEpisodes(Arrays.asList("one", "two"));
		episodes.setSelectedEpisodes(Arrays.asList(2, 1));
		AtomicInteger calls = new AtomicInteger();

		int last = episodes.requestedOrder((id, prompt) -> {
			if (id == 2 && calls.getAndIncrement() == 0) {
				throw new RepeatEpisodeException();
			}
			return id + ":" + prompt;
		});

		assertEquals(1, last);
		assertEquals(Arrays.asList("2:two", "1:one"), processor.getResults());
	}

	@Test
	void regularOrderHandlesRepeatAndMoveByName() {
		ActProcessor processor = processor();
		Episodes episodes = new Episodes(processor);
		episodes.setEpisodes(Arrays.asList("# First\na", "# Second\nb"));
		AtomicInteger firstCalls = new AtomicInteger();
		AtomicInteger secondCalls = new AtomicInteger();

		episodes.regularOrder(1, (id, prompt) -> {
			if (id == 1 && firstCalls.getAndIncrement() == 0) {
				throw new RepeatEpisodeException();
			}
			if (id == 2 && secondCalls.getAndIncrement() == 0) {
				throw new MoveToEpisodeException(null, "First");
			}
			return "done-" + id;
		});

		assertEquals(3, firstCalls.get());
		assertEquals(2, secondCalls.get());
		assertEquals(Arrays.asList("done-1", "done-1", "done-2"), processor.getResults());
	}

	@Test
	void getEpisodeIdUsesExplicitIdOrNameAndFailsForUnknownName() {
		Episodes episodes = episodes("# Alpha\na");

		assertEquals(7, episodes.getEpisodeId(1, new MoveToEpisodeException(7, "ignored")));
		assertEquals(1, episodes.getEpisodeId(null, new MoveToEpisodeException(null, "Alpha")));
		Executable executable = () -> episodes.getEpisodeId(null, new MoveToEpisodeException(null, "missing"));
		assertThrows(EpisodeNotFoundException.class, executable);
		assertTrue(episodes.isRegularOrder());
	}

	private Episodes episodes(String... values) {
		Episodes episodes = new Episodes(processor());
		episodes.setEpisodes(Arrays.asList(values));
		return episodes;
	}

	private ActProcessor processor() {
		return new ActProcessor(new File("."), "Any:Model", new PropertiesConfigurator());
	}
}
