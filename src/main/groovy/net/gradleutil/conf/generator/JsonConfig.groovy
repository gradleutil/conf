package net.gradleutil.conf.generator

import com.typesafe.config.ConfigObject
import org.everit.json.schema.Schema
import org.everit.json.schema.ValidationException
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject
import org.json.JSONTokener

class JsonConfig {

    /**
     * Get JSON schema from JSON file.
     * @param json
     * @return
     */
    static JSONObject jsonObjectFromString(String json) {
        GroovyConfig.illegalIfNull(json, "json is empty")
        JSONObject jSONObject
        try {
            jSONObject = new JSONObject(new JSONTokener(json))
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not parse script text:\n${json}", e)
        }
        jSONObject
    }

    static sanitize(Map originalMap) {
        Map<String, Object> map = [:]
        def handle = { v, k ->
            if (['eaterObject', 'json'].contains(k))
                return null
            if (v instanceof Map || v instanceof ConfigObject) {
                if (v.size()) {
                    def res = sanitize(v?.eaterObject ?: v)
                    return res
                }
                return null
            } else if (v instanceof File) {
                return 'file://' + v.path
            }
            return v.hasProperty('eaterObject') ? sanitize(v.eaterObject as Map) : v
        }
        (originalMap as Map<String, Object>)?.each { k, v ->
            if (!['eaterObject', 'json'].contains(k)) {
                def val = handle(v, k)
                if (val != null) {
                    map.put(k, val)
                }
            }
        }
        map
    }


    /**
     * Pretty println JSON
     * @param config
     * @param namespace limit by dotted object path, e.g. `object.subObject' for only object.subObject keys/values
     */
    static void jsonPrint(Object config, String namespace = '') {
        System.out.println(new JSONObject(config).toString())
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
        def inputStream = JsonConfig.class.getResourceAsStream(schemaJsonName)
        getSchema(inputStream, schemaJsonName)
    }

    /**
     * Get an internal schema file from the classpath.
     * @param name of /schema file (.json is appended)
     * @return
     */
    static Schema getSchema(InputStream inputStream, String name = 'inputstream.schema.json', boolean useDefaults = true) {
        JSONObject rawSchema
        rawSchema = null
        GroovyConfig.illegalIfNull(inputStream, "Could not load resource: " + name)
        inputStream.withCloseable { is ->
            rawSchema = new JSONObject(new JSONTokener(is))
        }
        GroovyConfig.illegalIfNull(rawSchema, "Internal Schema was empty or could not be parsed: ${name}")
        SchemaLoader.builder().useDefaults(useDefaults).schemaJson(rawSchema).build().load().build()
    }

    /**
     * Get JSON schema from JSON string.
     * @param json
     * @return
     */
    static Schema getSchema(String json, boolean useDefaults = true) {
        JSONObject rawSchema = jsonObjectFromString(json)
        GroovyConfig.illegalIfNull(rawSchema, "Schema could not be parsed: ${json}")
        SchemaLoader.builder().useDefaults(useDefaults).schemaJson(rawSchema).build().load().build()
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
    static List<ValidationException> validate(Schema schema, JSONObject json) {
        def exceptions = []
        try {
            schema.validate(json)
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
        schemaForm = new File(JsonConfig.class.getResource('/editor/index.html').toURI()).text
        schemaForm.replace('@schema', schemaJson)
                .replace('@formData', dataJson)
    }

}
