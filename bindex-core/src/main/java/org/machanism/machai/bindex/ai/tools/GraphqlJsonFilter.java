package org.machanism.machai.bindex.ai.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import graphql.language.Document;
import graphql.language.Field;
import graphql.language.SelectionSet;
import graphql.parser.Parser;

class GraphqlJsonFilter {

	private static final ObjectMapper mapper = new ObjectMapper();

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