package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.machanism.machai.schema.Bindex;
import org.machanism.machai.schema.Classification;
import org.machanism.machai.schema.Language;

/**
 * Unit tests for {@link Picker} that avoid any real MongoDB/GenAI calls.
 */
class PickerTest {

	@Test
	void getNormalizedLanguageName_shouldLowercaseTrimAndStripParentheses() {
		// Arrange
		Language lang = new Language();
		lang.setName("  Java (JVM)  ");

		// Act
		String normalized = Picker.getNormalizedLanguageName(lang);

		// Assert
		assertEquals("java", normalized);
	}

	@Test
	void getNormalizedLanguageName_shouldHandleAlreadyNormalized() {
		// Arrange
		Language lang = new Language();
		lang.setName("python");

		// Act
		String normalized = Picker.getNormalizedLanguageName(lang);

		// Assert
		assertEquals("python", normalized);
	}

	@Test
	void setScoreAndGetScore_shouldStoreAndReturnScoresFromMap() throws Exception {
		// Arrange
		Picker picker = allocateWithoutConstructor(Picker.class);
		picker.setScore(0.42d);
		Map<String, Double> scoreMap = new HashMap<>();
		scoreMap.put("lib:1.0", 0.99d);
		setField(picker, "scoreMap", scoreMap);

		// Act
		Double score = picker.getScore("lib:1.0");

		// Assert
		assertEquals(0.99d, score);
	}

	@Test
	void addDependencies_shouldAddTransitiveDependenciesAndAvoidCycles() {
		// Arrange
		TestablePickerAccess access = new TestablePickerAccess();
		access.repo.put("A", bindexWithDependencies("A", "B", "C"));
		access.repo.put("B", bindexWithDependencies("B", "C"));
		access.repo.put("C", bindexWithDependencies("C", "A")); // cycle
		Set<String> deps = new HashSet<>();

		// Act
		access.addDependencies(deps, "A");

		// Assert
		assertEquals(new HashSet<>(java.util.Arrays.asList("A", "B", "C")), deps);
	}

	@Test
	void addDependencies_shouldIgnoreUnknownBindexId() {
		// Arrange
		TestablePickerAccess access = new TestablePickerAccess();
		Set<String> deps = new HashSet<>();

		// Act
		access.addDependencies(deps, "missing");

		// Assert
		assertTrue(deps.isEmpty());
	}

	@Test
	void getEmbeddingBson_shouldValidateArguments() throws Exception {
		// Arrange
		Picker picker = allocateWithoutConstructor(Picker.class);

		// Act + Assert
		assertThrows(IllegalArgumentException.class, () -> picker.getEmbeddingBson(null, 1));

		Classification classification = new Classification();
		assertThrows(IllegalArgumentException.class, () -> picker.getEmbeddingBson(classification, 0));
		assertThrows(IllegalArgumentException.class, () -> picker.getEmbeddingBson(classification, -1));
	}

	private static Bindex bindexWithDependencies(String id, String... deps) {
		Bindex b = new Bindex();
		b.setId(id);
		b.setDependencies(deps == null ? Collections.emptyList() : java.util.Arrays.asList(deps));
		return b;
	}

	private static void setField(Object target, String fieldName, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(target, value);
	}

	@SuppressWarnings("unchecked")
	private static <T> T allocateWithoutConstructor(Class<T> type) throws Exception {
		Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
		Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
		theUnsafe.setAccessible(true);
		Object unsafe = theUnsafe.get(null);
		return (T) unsafeClass.getMethod("allocateInstance", Class.class).invoke(unsafe, type);
	}

	private static final class TestablePickerAccess {
		private final Map<String, Bindex> repo = new HashMap<>();

		void addDependencies(Set<String> dependencies, String bindexId) {
			Bindex bindex = repo.get(bindexId);
			if (bindex != null) {
				String id = bindex.getId();
				if (!dependencies.contains(id)) {
					dependencies.add(id);
					for (String dependencyId : bindex.getDependencies()) {
						addDependencies(dependencies, dependencyId);
					}
				}
			}
		}
	}
}
