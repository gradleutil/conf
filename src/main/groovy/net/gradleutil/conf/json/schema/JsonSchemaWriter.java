package net.gradleutil.conf.json.schema;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.SpecVersion;

public class JsonSchemaWriter {

    private final ObjectMapper OBJECT_MAPPER;
    private final ObjectNode definitions;

    JsonSchemaWriter() {
        OBJECT_MAPPER = new ObjectMapper();
        definitions = OBJECT_MAPPER.createObjectNode();

    }

    public String getJsonSchema(String jsonDocument, String refName) throws IllegalArgumentException, IOException {
        Map<String, Object> map = OBJECT_MAPPER.readValue(jsonDocument, new TypeReference<Map<String, Object>>() {
        });
        return getJsonSchema(map, refName);
    }

    public String getJsonSchema(Map<String, Object> jsonDocument, String refName) throws IllegalArgumentException, IOException {
        definition(refName, OBJECT_MAPPER.convertValue(jsonDocument, JsonNode.class));
        return getJsonSchema(refName);

    }

    private String getJsonSchema(String refName) throws JsonProcessingException {
        ObjectNode schema = OBJECT_MAPPER.createObjectNode();
        schema.put("$schema", SpecVersion.VersionFlag.V7.getId());
        schema.put("$ref", "#/definitions/" + refName);
        schema.set("definitions", definitions);
        ObjectMapper jacksonObjectMapper = new ObjectMapper();
        String schemaString = jacksonObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
        return schemaString;
    }

    private ObjectNode definition(String name, JsonNode node) throws IOException {
        if (definitions.has(name)) {
            OBJECT_MAPPER.createObjectNode().put("$ref", "#/definitions/" + name);
        }
        ObjectNode objectSchema = OBJECT_MAPPER.createObjectNode();
        objectSchema.put("type", "object");
        Iterator<Entry<String, JsonNode>> fieldsIterator = node.fields();
        ObjectNode properties = OBJECT_MAPPER.createObjectNode();
        objectSchema.set("properties", properties);
        while (fieldsIterator.hasNext()) {
            Entry<String, JsonNode> field = fieldsIterator.next();

            String fieldName = field.getKey();
            JsonNode fieldValue = field.getValue();
            JsonNodeType fieldType = fieldValue.getNodeType();

            ObjectNode property = processJsonField(fieldValue, fieldType, fieldName);
            if (property.isEmpty()) {
                property.put("type", "null");
            }
            properties.set(fieldName, property);
        }
        objectSchema.set("properties", properties);
        definitions.set(name, objectSchema);
        return OBJECT_MAPPER.createObjectNode().put("$ref", "#/definitions/" + name);
    }

    private ObjectNode processJsonField(JsonNode fieldValue, JsonNodeType fieldType, String fieldName) throws IOException {
        ObjectNode property = OBJECT_MAPPER.createObjectNode();

        switch (fieldType) {

            case ARRAY:
                property.put("type", "array");

                if (fieldValue.isEmpty()) {
                    break;
                }

                // Get first element of the array
                JsonNodeType typeOfArrayElements = fieldValue.get(0).getNodeType();
                if (typeOfArrayElements.equals(JsonNodeType.OBJECT)) {
                    property.set("items", definition(fieldName, fieldValue.get(0)));
                } else {
                    property.set("items", processJsonField(fieldValue.get(0), typeOfArrayElements, fieldName));
                }

                break;
            case BOOLEAN:
                property.put("type", "boolean");
                break;

            case NUMBER:
                property.put("type", "number");
                break;

            case OBJECT:
                return definition(fieldName, fieldValue);

            case STRING:
                property.put("type", "string");
                break;
            default:
                break;
        }
        return property;
    }


}
