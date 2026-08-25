package org.machanism.machai.bindex.ai.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import graphql.language.Document;
import graphql.language.Field;
import graphql.language.SelectionSet;
import graphql.parser.Parser;

/**
 * Internal support component for projecting serialized tool results according
 * to a GraphQL selection document.
 * <p>
 * The filter supports fields in operation selection sets. Nested selection
 * syntax is parsed by GraphQL but is not recursively projected, so this
 * component is intended for reducing the top-level payload returned by Bindex
 * tools.
 * </p>
 */
class GraphqlJsonFilter {

	/**
	 * JSON mapper used to convert input values and construct filtered objects.
	 */
	private static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * Projects the top-level fields of an object onto the fields requested by a
	 * GraphQL document.
	 *
	 * @param data the value to serialize and filter
	 * @param graphqlQuery the GraphQL selection document; must be syntactically
	 *                     valid
	 * @return an object containing the selected fields, or the serialized value
	 *         unchanged when it is not a JSON object
	 */
	static JsonNode filterJson(Object data, String graphqlQuery) {
		Parser parser = new Parser();
		Document document = parser.parseDocument(graphqlQuery);

		JsonNode jsonNode = mapper.valueToTree(data);

		// Assuming we are filtering the root object for simplicity
		if (jsonNode.isObject()) {
			ObjectNode filteredNode = mapper.createObjectNode();

			document.getDefinitions().forEach(definition -> {
				if (definition instanceof graphql.language.OperationDefinition) {
					SelectionSet selectionSet = ((graphql.language.OperationDefinition) definition).getSelectionSet();
					processSelectionSet(jsonNode, filteredNode, selectionSet);
				}
			});
			return filteredNode;
		}
		return jsonNode;
	}

	/**
	 * Copies fields selected by one GraphQL selection set from a source object to
	 * a target object.
	 *
	 * @param sourceNode the JSON object from which values are read
	 * @param targetNode the JSON object receiving selected values
	 * @param selectionSet the GraphQL selection set to process; may be
	 *                     {@code null}
	 */
	private static void processSelectionSet(JsonNode sourceNode, ObjectNode targetNode, SelectionSet selectionSet) {
		if (selectionSet == null)
			return;

		selectionSet.getSelections().forEach(selection -> {
			if (selection instanceof Field) {
				Field field = (Field) selection;
				String fieldName = field.getName();

				if (sourceNode.has(fieldName)) {
					targetNode.set(fieldName, sourceNode.get(fieldName));
				}
			}
		});
	}
}
