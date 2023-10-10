package net.gradleutil.conf.json.schema;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.everit.json.schema.*;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONTokener;

import net.gradleutil.conf.json.JsonObject;

public class SchemaUtil extends SchemaLoader {

    public SchemaUtil(SchemaLoaderBuilder builder) {
        super(builder);
    }

    /**
     * Get JSON schema from JSON file.
     *
     * @param json file
     * @return Schema
     * @throws IOException if file cannot be read
     */
    public static Schema getSchema(File json) throws IOException {
        return getSchema(new String(Files.readAllBytes(json.toPath())));
    }

    /**
     * Get an internal schema file from the classpath.
     *
     * @param name      of /schema file (.json is appended)
     * @param basePath  of /schema file
     * @param extension of /schema file
     * @return Schema
     */
    public static Schema getInternalSchema(String name, String basePath, String extension) {
        String schemaJsonName = basePath + name + extension;
        InputStream inputStream = SchemaUtil.class.getResourceAsStream(schemaJsonName);
        return getSchema(inputStream, schemaJsonName);
    }

    /**
     * Get an internal schema file from the classpath.
     *
     * @param name     of /schema file (.json is appended)
     * @param basePath of /schema file
     * @return Schema
     */
    public static Schema getInternalSchema(String name, String basePath) {
        return SchemaUtil.getInternalSchema(name, basePath, ".json");
    }

    /**
     * Get an internal schema file from the classpath.
     *
     * @param name of /schema file (.json is appended)
     * @return Schema
     */
    public static Schema getInternalSchema(String name) {
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
    public static Schema getSchema(InputStream inputStream, String name, boolean useDefaults) {
        JsonObject rawSchema;
        illegalIfNull(inputStream, "Could not load resource: " + name);
        rawSchema = new JsonObject(new JSONTokener(inputStream));
        illegalIfNull(rawSchema, "Internal Schema was empty or could not be parsed: " + name);
        return SchemaLoader.builder().useDefaults(useDefaults).schemaJson(rawSchema).build().load().build();
    }

    /**
     * Get an internal schema file from the classpath.
     *
     * @param inputStream of /schema file (.json is appended)
     * @param name        of /schema file (.json is appended)
     * @return Schema
     */
    public static Schema getSchema(InputStream inputStream, String name) {
        return SchemaUtil.getSchema(inputStream, name, true);
    }

    /**
     * Get an internal schema file from the classpath.
     *
     * @param inputStream of /schema file (.json is appended)
     * @return Schema
     */
    public static Schema getSchema(InputStream inputStream) {
        return SchemaUtil.getSchema(inputStream, "inputstream.schema.json", true);
    }

    /**
     * Get JSON schema from JSON string.
     *
     * @param json        JSON string
     * @param useDefaults use defaults
     * @param refName     reference name
     * @return Schema
     */
    public static Schema getSchema(String json, boolean useDefaults, String refName) {
        JsonObject rawSchema = JsonObjectFromString(json);
        illegalIfNull(rawSchema, "Schema could not be parsed: " + json);
        Schema schema = SchemaLoader.builder().useDefaults(useDefaults).schemaJson(rawSchema).build().load().build();
        if (!refName.isEmpty() && !(schema instanceof ReferenceSchema || schema instanceof CombinedSchema)) {
            return SchemaToReferenceSchema.toReferenceSchema(schema, refName);
        }

        return schema;
    }

    /**
     * Get JSON schema from JSON string.
     *
     * @param json        JSON string
     * @param useDefaults use defaults
     * @return Schema
     */
    public static Schema getSchema(String json, boolean useDefaults) {
        return SchemaUtil.getSchema(json, useDefaults, "");
    }

    /**
     * Get JSON schema from JSON string.
     *
     * @param json JSON string
     * @return Schema
     */
    public static Schema getSchema(String json) {
        return SchemaUtil.getSchema(json, true, "");
    }

    /**
     * Get JSON schema from JSON file.
     *
     * @param json file
     * @return Schema
     */
    public static JsonObject JsonObjectFromString(final String json) {
        illegalIfNull(json, "json is empty");
        JsonObject JsonObject;
        try {
            JsonObject = new JsonObject(json);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not parse script text:\n" + json, e);
        }

        return JsonObject;
    }

    /**
     * Get a list of ValidationExceptions.
     *
     * @param e ValidationException
     * @return List
     */
    public static List<ValidationException> getExceptions(ValidationException e) {
        final ArrayList<ValidationException> exceptions = new ArrayList<>();
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
    public static List<ValidationException> validate(Schema schema, JsonObject json) {
        ArrayList<ValidationException> exceptions = new ArrayList<>();
        try {
            Validator validator = Validator.builder().primitiveValidationStrategy(PrimitiveValidationStrategy.LENIENT).build();
            validator.performValidation(schema, json);
        } catch (ValidationException e) {
            exceptions.addAll(getExceptions(e));
        }

        return exceptions;
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
