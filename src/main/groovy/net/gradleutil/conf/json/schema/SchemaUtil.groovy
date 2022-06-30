package net.gradleutil.conf.json.schema

import net.gradleutil.conf.json.JsonObject
import org.everit.json.schema.*
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONTokener

class SchemaUtil extends SchemaLoader {
    /**
     * Constructor.
     *
     * @param builder
     *         the builder containing the properties. Only {@link SchemaLoaderBuilder#id} is
     *         nullable.
     * @throws NullPointerException*         if any of the builder properties except {@link SchemaLoaderBuilder#id id} is
     * {@code null}.
     */
    SchemaUtil(SchemaLoaderBuilder builder) {
        super(builder)
    }

    /**
     * Get JSON schema from JSON file.
     * @param json
     * @return
     */
    static Schema getSchema(File json) {
        getSchema(json.text)
    }

    /**
     * Get an internal schema file from the classpath.
     * @param name of /schema file (.json is appended)
     * @return
     */
    static Schema getInternalSchema(String name, String basePath = '/schema/', String extension = '.json') {
        def schemaJsonName = basePath + name + extension
        def inputStream = SchemaUtil.class.getResourceAsStream(schemaJsonName)
        getSchema(inputStream, schemaJsonName)
    }

    /**
     * Get an internal schema file from the classpath.
     * @param name of /schema file (.json is appended)
     * @return
     */
    static Schema getSchema(InputStream inputStream, String name = 'inputstream.schema.json', boolean useDefaults = true) {
        JsonObject rawSchema
        rawSchema = null
        illegalIfNull(inputStream, "Could not load resource: " + name)
        inputStream.withCloseable { is ->
            rawSchema = new JsonObject(new JSONTokener(is))
        }
        illegalIfNull(rawSchema, "Internal Schema was empty or could not be parsed: ${name}")
        builder().useDefaults(useDefaults).schemaJson(rawSchema).build().load().build()
    }

    /**
     * Get JSON schema from JSON string.
     * @param json
     * @return
     */
    static Schema getSchema(String json, boolean useDefaults = true, String refName = '') {
        JsonObject rawSchema = JsonObjectFromString(json)
        illegalIfNull(rawSchema, "Schema could not be parsed: ${json}")
        def schema = builder().useDefaults(useDefaults).schemaJson(rawSchema).build().load().build()
        if (refName && !(schema instanceof ReferenceSchema || schema instanceof CombinedSchema)) {
            return SchemaToReferenceSchema.toReferenceSchema(schema, refName)
        }
        schema
    }

    /**
     * Get JSON schema from JSON file.
     * @param json
     * @return
     */
    static JsonObject JsonObjectFromString(String json) {
        illegalIfNull(json, "json is empty")
        JsonObject JsonObject
        try {
            JsonObject = new JsonObject(json)
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not parse script text:\n${json}", e)
        }
        JsonObject
    }

    /**
     * Get a list of ValidationExceptions.
     * @param e
     * @return
     */
    static List<ValidationException> getExceptions(ValidationException e) {
        def exceptions = []
        exceptions.add(e)
        e.getCausingExceptions().each {
            exceptions.addAll(getExceptions(it))
        }
        return exceptions
    }

    /**
     * Validate using provided schema.
     * @param schema
     * @param json
     * @return
     */
    static List<ValidationException> validate(Schema schema, JsonObject json) {
        def exceptions = []
        try {
            Validator validator = Validator.builder()
                    .primitiveValidationStrategy(PrimitiveValidationStrategy.LENIENT)
                    .build()
            validator.performValidation(schema, json)
        } catch (ValidationException e) {
            exceptions.addAll(getExceptions(e))
        }
        return exceptions
    }

    /**
     * Generate an editor HTML file for creating/editing JSON configs.
     * @param schemaJson
     * @param dataJson
     * @return
     */
    static String editor(String schemaJson, String dataJson) {
        def schemaForm
        schemaForm = new File(SchemaUtil.class.getResource('/editor/index.html').toURI()).text
        schemaForm.replace('@schema', schemaJson)
                .replace('@formData', dataJson)
    }

    static illegalIfNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message)
        }
    }


}
