package org.machanism.machai.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class SchemaPojoTest {

	@Test
	void language_getSetAndAdditionalPropertiesAndEqualsHashCode() {
		// Arrange
		Language l1 = new Language();
		l1.setName("Java");
		l1.setVersion("21");
		l1.setAdditionalProperty("x", 1);

		Language l2 = new Language();
		l2.setName("Java");
		l2.setVersion("21");
		l2.setAdditionalProperty("x", 1);

		// Act
		Map<String, Object> props = l1.getAdditionalProperties();

		// Assert
		assertEquals("Java", l1.getName());
		assertEquals("21", l1.getVersion());
		assertEquals(1, props.get("x"));
		assertEquals(l1, l2);
		assertEquals(l1.hashCode(), l2.hashCode());
		assertTrue(l1.toString().contains("Java"));
		assertNotEquals(l1, new Object());
	}

	@Test
	void bindex_aggregatesNestedObjectsAndCollections() throws Exception {
		// Arrange
		Bindex bindex = new Bindex();
		bindex.setId("id-1");
		bindex.setName("lib");
		bindex.setVersion("1.0.0");
		bindex.setDescription("desc");

		Author author = new Author();
		author.setName("Alice");
		author.setEmail("alice@example.org");
		author.setWebsite(new URI("https://example.org"));

		Coordinates coords = new Coordinates();
		coords.setGroup("g");
		coords.setArtifactId("a");
		coords.setVersion("1");
		coords.setClassifier("tests");
		coords.setExtension("jar");

		Location loc = new Location();
		loc.setRepositoryType("git");
		loc.setRepositoryUrl(new URI("https://repo.example"));
		loc.setCoordinates(coords);

		Classification classification = new Classification();
		classification.setType("library");
		classification.setDomains(Arrays.asList("ai"));
		classification.setLanguages(Arrays.asList(language("Java")));
		classification.setLayers(Arrays.asList(Layer.INTERACTORS));
		classification.setUsageContext(Arrays.asList("server"));
		classification.setTargetEnvironment(Arrays.asList("jvm"));
		classification.setIntegrations(Arrays.asList("Spring"));

		Feature feature = new Feature();
		feature.setName("logging");
		feature.setPackage("org.example");
		feature.setSignature("log(String)");
		feature.setDescription("... ");
		feature.setExamples(Arrays.asList(example__1("System.out.println(1);", "prints")));
		feature.setAdditionalProperty("extra", true);

		Constructor ctor = new Constructor();
		ctor.setPackage("org.example");
		ctor.setSignature("Example()") ;
		ctor.setDescription("ctor");
		ctor.setExamples(Arrays.asList(example("new Example()", "create")));

		Customization customization = new Customization();
		customization.setName("custom");
		customization.setPackage("org.example");
		customization.setType("config");
		customization.setDescription("customize");
		customization.setExamples(Arrays.asList(example__2("x", "y")));

		Stud stud = new Stud();
		stud.setName("stud");
		stud.setPackage("org.example");
		stud.setType("iface");
		stud.setDescription("desc");
		stud.setExamples(Arrays.asList(example__3("s", mapOf("d", 1))));
		stud.setImplement("impl");

		Example__4 genericExample = new Example__4();
		genericExample.setAdditionalProperty("snippet", "snip");
		genericExample.setAdditionalProperty("description", mapOf("k", "v"));

		bindex.setLocation(loc);
		bindex.setAuthors(Arrays.asList(author));
		bindex.setLicense("MIT");
		bindex.setClassification(classification);
		bindex.setConstructors(Arrays.asList(ctor));
		bindex.setFeatures(Arrays.asList(feature));
		bindex.setCustomizations(Arrays.asList(customization));
		bindex.setStuds(Arrays.asList(stud));
		bindex.setExamples(Arrays.asList(genericExample));
		bindex.setDependencies(Arrays.asList("dep:1"));
		bindex.setAdditionalProperty("p", 123);

		Bindex copy = new Bindex();
		copy.setId("id-1");
		copy.setName("lib");
		copy.setVersion("1.0.0");
		copy.setDescription("desc");
		copy.setLocation(loc);
		copy.setAuthors(Arrays.asList(author));
		copy.setLicense("MIT");
		copy.setClassification(classification);
		copy.setConstructors(Arrays.asList(ctor));
		copy.setFeatures(Arrays.asList(feature));
		copy.setCustomizations(Arrays.asList(customization));
		copy.setStuds(Arrays.asList(stud));
		copy.setExamples(Arrays.asList(genericExample));
		copy.setDependencies(Arrays.asList("dep:1"));
		copy.setAdditionalProperty("p", 123);

		// Act
		String s = bindex.toString();
		int hc = bindex.hashCode();
		boolean eq = bindex.equals(copy);

		// Assert
		assertTrue(s.contains("id-1"));
		assertNotEquals(0, hc);
		assertTrue(eq);
		assertEquals("lib", bindex.getName());
		assertEquals("jar", bindex.getLocation().getCoordinates().getExtension());
		assertEquals("Alice", bindex.getAuthors().get(0).getName());
		assertEquals("Java", bindex.getClassification().getLanguages().get(0).getName());
		assertEquals(Arrays.asList("dep:1"), bindex.getDependencies());
		assertEquals(123, bindex.getAdditionalProperties().get("p"));
		assertNotEquals(bindex, null);
		assertNotEquals(bindex, new Object());
	}

	@Test
	void layer_fromValue_parsesValidAndRejectsInvalid() {
		// Arrange
		String value = Layer.INTERACTORS.value();

		// Act
		Layer parsed = Layer.fromValue(value);

		// Assert
		assertSame(Layer.INTERACTORS, parsed);
		assertThrows(IllegalArgumentException.class, () -> Layer.fromValue("does-not-exist"));
		assertEquals("Interactors", Layer.INTERACTORS.toString());
	}

	@Test
	void example_variants_storeAdditionalPropertiesAndParticipateInEquality() {
		// Arrange
		Example__1 e1 = example__1("code", "desc");
		e1.setAdditionalProperty("x", 1);

		Example__1 e2 = example__1("code", "desc");
		e2.setAdditionalProperty("x", 1);

		Example__2 e3 = example__2("code", "desc");
		e3.setAdditionalProperty("x", 1);

		Example__3 e4 = example__3("code", mapOf("d", 1));
		e4.setAdditionalProperty("x", 1);

		Example__4 e5 = new Example__4();
		e5.setAdditionalProperty("x", 1);

		// Act + Assert
		assertEquals(e1, e2);
		assertEquals(e1.hashCode(), e2.hashCode());
		assertTrue(e1.toString().contains("code"));

		assertEquals("code", e3.getSnippet());
		assertEquals("desc", e3.getDescription());

		assertEquals("code", e4.getSnippet());
		assertEquals(mapOf("d", 1), e4.getDescription());
		assertEquals(1, e4.getAdditionalProperties().get("x"));

		assertEquals(1, e5.getAdditionalProperties().get("x"));
		assertTrue(e5.toString().contains("x"));
	}

	private static Map<String, Object> mapOf(String k, Object v) {
		Map<String, Object> m = new HashMap<>();
		m.put(k, v);
		return m;
	}

	private static Language language(String name) {
		Language l = new Language();
		l.setName(name);
		l.setVersion("n/a");
		return l;
	}

	private static Example example(String snippet, String description) {
		Example e = new Example();
		e.setSnippet(snippet);
		e.setDescription(description);
		return e;
	}

	private static Example__1 example__1(String snippet, String description) {
		Example__1 e = new Example__1();
		e.setSnippet(snippet);
		e.setDescription(description);
		return e;
	}

	private static Example__2 example__2(String snippet, String description) {
		Example__2 e = new Example__2();
		e.setSnippet(snippet);
		e.setDescription(description);
		return e;
	}

	private static Example__3 example__3(String snippet, Object description) {
		Example__3 e = new Example__3();
		e.setSnippet(snippet);
		e.setDescription(description);
		return e;
	}
}
