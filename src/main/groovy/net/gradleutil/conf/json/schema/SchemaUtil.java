package net.gradleutil.conf.json.schema;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.victools.jsonschema.generator.*;
import com.networknt.schema.*;
import com.typesafe.config.Config;

import net.gradleutil.conf.util.ConfUtil;


public class SchemaUtil {


    /**
     * Get JSON schema from JSON file.
     *
     * @param json file
     * @return Schema
     * @throws IOException if file cannot be read
     */
    public static JsonSchema getSchema(File json, String basePath) throws IOException {
        return getSchema(new String(Files.readAllBytes(json.toPath())), true, basePath);
    }


    private static JsonSchemaFactory getFactory() {
        return JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012, builder ->
                builder.schemaMappers(schemaMappers ->
                        schemaMappers.mapPrefix("https://www.example.org/", "classpath:json/")
                )
        );
    }

    private static SchemaValidatorsConfig getSchemaValidatorsConfig(Boolean applyDefaults, String basePath) {
        ApplyDefaultsStrategy applyDefaultsStrategy = new ApplyDefaultsStrategy(applyDefaults, true, true);
        SchemaValidatorsConfig schemaValidatorsConfig = new SchemaValidatorsConfig();
        schemaValidatorsConfig.setApplyDefaultsStrategy(applyDefaultsStrategy);
        schemaValidatorsConfig.addKeywordWalkListener("$ref", new RefWalker(basePath));
        return schemaValidatorsConfig;
    }


    /**
     * Get JSON schema from JSON string.
     *
     * @param json        JSON string
     * @param useDefaults use defaults
     * @return Schema
     */
    public static JsonSchema getSchema(String json, boolean useDefaults, String basePath) {
        illegalIfNull(json, "Schema could not be parsed: " + json);
        try {
            return getSchema(new ObjectMapper().readTree(json), useDefaults, basePath);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static JsonSchema getSchema(JsonNode json, Boolean applyDefaults, String basePath) {
        illegalIfNull(json, "Schema could not be parsed: " + json);
        JsonSchema schema = getFactory().getSchema(json, getSchemaValidatorsConfig(applyDefaults, basePath));
        schema.walk(null, false);
        return schema;
    }

    /**
     * Get an internal schema file from the classpath.
     *
     * @param name      of /schema file (.json is appended)
     * @param basePath  of /schema file
     * @param extension of /schema file
     * @return Schema
     */
    public static JsonSchema getInternalSchema(String name, String basePath, String extension) {
        String schemaJsonName = basePath + name + extension;
        InputStream inputStream = SchemaUtil.class.getResourceAsStream(schemaJsonName);
        return getSchema(inputStream, schemaJsonName, basePath);
    }

    /**
     * Get an internal schema file from the classpath.
     *
     * @param name     of /schema file (.json is appended)
     * @param basePath of /schema file
     * @return Schema
     */
    public static JsonSchema getInternalSchema(String name, String basePath) {
        return SchemaUtil.getInternalSchema(name, basePath, ".json");
    }

    /**
     * Get an internal schema file from the classpath.
     *
     * @param name of /schema file (.json is appended)
     * @return Schema
     */
    public static JsonSchema getInternalSchema(String name) {
        return SchemaUtil.getInternalSchema(name, "/schema/", ".json");
    }

    /**
     * Get an internal schema file from the classpath.
     *
     * @param inputStream of /schema file (.json is appended)
     * @param name        of /schema file (.json is appended)
     * @param useDefaults use defaults
     * @return Schema
     */
    public static JsonSchema getSchema(InputStream inputStream, String name, boolean useDefaults, String basePath) {
        illegalIfNull(inputStream, "Could not load resource: " + name);
        JsonSchema jsonSchema;
        try {
            jsonSchema = getSchema(readFromInputStream(inputStream), useDefaults, basePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        illegalIfNull(jsonSchema, "Internal Schema was empty or could not be parsed: " + name);
        return jsonSchema;
    }

    /**
     * Get an internal schema file from the classpath.
     *
     * @param inputStream of /schema file (.json is appended)
     * @param name        of /schema file (.json is appended)
     * @return Schema
     */
    public static JsonSchema getSchema(InputStream inputStream, String name, String basePath) {
        return getSchema(inputStream, name, true, basePath);
    }

    /**
     * Get an internal schema file from the classpath.
     *
     * @param inputStream of /schema file (.json is appended)
     * @return Schema
     */
    public static JsonSchema getSchema(InputStream inputStream) {
        return getSchema(inputStream, "inputstream.schema.json", true, "");
    }

    public static JsonSchema getSchema(String json, String basePath) {
        illegalIfNull(json, "Schema could not be parsed: " + json);
        return getSchema(json, true, basePath);
    }

    /**
     * Get JSON schema from Object.
     *
     * @param objectClass JSON string
     * @return Schema
     */
    public static JsonSchema getSchema(Type objectClass) {
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_7, OptionPreset.PLAIN_JSON);
        configBuilder.with(Option.DEFINITIONS_FOR_ALL_OBJECTS);
        SchemaGeneratorConfig config = configBuilder.build();
        SchemaGenerator generator = new com.github.victools.jsonschema.generator.SchemaGenerator(config);
        return getSchema(generator.generateSchema(objectClass), true,"");
    }


    public static JsonSchema getSchema(Config configObject, String refName, String basePath) {
        try {
            String schema = new JsonSchemaWriter().getJsonSchema(ConfUtil.configToJson(configObject), refName);
            return getSchema(schema, true, basePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get JSON schema from JSON file.
     *
     * @param json
     * @return JsonNode
     */
    public static JsonNode jsonObjectFromString(final String json) {
        illegalIfNull(json, "json is empty");
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return jsonNode;
    }

    /**
     * Get a list of ValidationExceptions.
     *
     * @param e ValidationException
     * @return List
     */
    public static List<ValidationMessage> getExceptions(ValidationMessage e) {
        final ArrayList<ValidationMessage> exceptions = new ArrayList<>();
        exceptions.add(e);
        return exceptions;
    }

    /**
     * Validate using provided schema.
     *
     * @param schema to validate against
     * @param json   to validate
     * @return List
     */
    public static Set<ValidationMessage> validate(JsonSchema schema, JsonNode json) {
        return schema.validate(json);
    }

    /**
     * Generate an editor HTML file for creating/editing JSON configs.
     *
     * @param schemaJson JSON schema
     * @param dataJson   JSON data
     * @param uiSchema   JSON uiSchema
     * @return HTML
     */
    public static String editor(String schemaJson, String dataJson, String uiSchema) {
        String schemaForm;
        try {
            schemaForm = readFromInputStream(SchemaUtil.class.getResourceAsStream("/editor/index.html"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return schemaForm.replace("@schema", schemaJson).replace("@uiSchema", uiSchema).replace("@formData", dataJson);
    }

    /**
     * Generate an editor HTML file for creating/editing JSON configs.
     *
     * @param schemaJson JSON schema
     * @param dataJson   JSON data
     * @return HTML
     */
    public static String editor(String schemaJson, String dataJson) {
        return SchemaUtil.editor(schemaJson, dataJson, "{}");
    }

    public static void illegalIfNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }

    }

    private static String readFromInputStream(InputStream inputStream)
            throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

}
