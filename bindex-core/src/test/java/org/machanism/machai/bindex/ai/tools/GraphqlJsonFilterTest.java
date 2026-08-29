package org.machanism.machai.bindex.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import graphql.parser.InvalidSyntaxException;
import graphql.language.SelectionSet;

/** Unit tests for the package-private GraphQL JSON projection utility. */
class GraphqlJsonFilterTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void constructor_canBeInstantiatedWithinPackage() {
        // Arrange and Act
        GraphqlJsonFilter filter = new GraphqlJsonFilter();

        // Assert
        assertTrue(filter instanceof GraphqlJsonFilter);
    }

    @Test
    void filterJson_copiesOnlyExistingRootFields() throws Exception {
        // Arrange
        JsonNode source = mapper.readTree("{\"name\":\"library\",\"version\":\"1\",\"ignored\":true}");

        // Act
        JsonNode filtered = GraphqlJsonFilter.filterJson(source, "{ name missing }");

        // Assert
        assertEquals("library", filtered.get("name").asText());
        assertFalse(filtered.has("version"));
        assertFalse(filtered.has("missing"));
    }

    @Test
    void filterJson_supportsMultipleOperationsByCombiningSelectedFields() {
        // Arrange
        JsonNode source = mapper.createObjectNode().put("name", "library").put("version", "1");

        // Act
        JsonNode filtered = GraphqlJsonFilter.filterJson(source,
                "query First { name } query Second { version }");

        // Assert
        assertEquals("library", filtered.get("name").asText());
        assertEquals("1", filtered.get("version").asText());
    }

    @Test
    void filterJson_supportsNestedSelectionsWithoutDroppingNestedValue() throws Exception {
        // Arrange
        JsonNode source = mapper.readTree("{\"name\":\"library\",\"classification\":{\"languages\":[\"Java\"],\"level\":\"runtime\"}}");

        // Act
        JsonNode filtered = GraphqlJsonFilter.filterJson(source, "{ classification { languages } }");

        // Assert
        assertEquals("Java", filtered.at("/classification/languages/0").asText());
        assertEquals("runtime", filtered.at("/classification/level").asText());
    }

    @Test
    void filterJson_returnsScalarInputUnchanged() {
        // Arrange
        String value = "plain value";

        // Act
        JsonNode filtered = GraphqlJsonFilter.filterJson(value, "{ value }");

        // Assert
        assertEquals(value, filtered.asText());
    }

    @Test
    void filterJson_returnsNullNodeForNullInput() {
        // Arrange
        String query = "{ value }";

        // Act
        JsonNode filtered = GraphqlJsonFilter.filterJson(null, query);

        // Assert
        assertTrue(filtered.isNull());
    }

    @Test
    void filterJson_rejectsInvalidGraphql() {
        // Act and assert
        assertThrows(InvalidSyntaxException.class,
                () -> GraphqlJsonFilter.filterJson("value", "{ invalid"));
    }

    @Test
    void filterJson_ignoresFragmentsAndNonOperationDefinitions() {
        // Arrange
        JsonNode source = mapper.createObjectNode().put("name", "library").put("version", "1");

        // Act
        JsonNode filtered = GraphqlJsonFilter.filterJson(source,
                "fragment Details on Library { version } query Find { name }");

        // Assert
        assertEquals("library", filtered.get("name").asText());
        assertFalse(filtered.has("version"));
    }

    @Test
    void filterJson_handlesEmptySelectionSet() {
        // Arrange
        JsonNode source = mapper.createObjectNode().put("name", "library");

        // Act
        JsonNode filtered = GraphqlJsonFilter.filterJson(source, "query Empty { __typename }");

        // Assert
        assertTrue(filtered.isObject());
        assertTrue(filtered.isEmpty());
    }

    @Test
    void processSelectionSet_ignoresNullSelectionSet() throws Exception {
        // Arrange
        JsonNode source = mapper.createObjectNode().put("name", "library");
        var target = mapper.createObjectNode();
        Method method = GraphqlJsonFilter.class.getDeclaredMethod("processSelectionSet", JsonNode.class,
                com.fasterxml.jackson.databind.node.ObjectNode.class, SelectionSet.class);
        method.setAccessible(true);

        // Act
        method.invoke(null, source, target, null);

        // Assert
        assertTrue(target.isEmpty());
    }

}
