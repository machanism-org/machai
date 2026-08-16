package org.machanism.machai.bindex.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.parser.InvalidSyntaxException;
import org.junit.jupiter.api.Test;

/** Unit tests for the package-private GraphQL JSON projection utility. */
class GraphqlJsonFilterTest {

    private final ObjectMapper mapper = new ObjectMapper();

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
    void filterJson_rejectsInvalidGraphql() {
        // Act and assert
        assertThrows(InvalidSyntaxException.class,
                () -> GraphqlJsonFilter.filterJson("value", "{ invalid"));
    }

}
