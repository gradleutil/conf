package net.gradleutil.conf.generator

import com.typesafe.config.*
import net.gradleutil.conf.Loader
import org.everit.json.schema.*
import org.everit.json.schema.regexp.JavaUtilRegexpFactory
import org.everit.json.schema.regexp.RegexpFactory
import org.json.JSONObject

class ConfigSchema {

    private static final RegexpFactory REGEXP_FACTORY = new JavaUtilRegexpFactory()
    static Set<Reference> definitions = []

    static def getReferenceSchema(ObjectSchema objectSchema) {

    }

    static void addProperty(objectBuilder, String name, ConfigValue configValue) {
        assert configValue != null
        switch (configValue.valueType()) {
            case ConfigValueType.STRING:
                objectBuilder.addPropertySchema(name, new StringSchema())
                break
            case ConfigValueType.LIST:
                def ref = getReference(null, (configValue as List).first() as ConfigObject).referenceSchema
                def arraySchema = ArraySchema.builder().addItemSchema(ref)
                objectBuilder.addPropertySchema(name, arraySchema.build())
                break
            case ConfigValueType.NUMBER:
                objectBuilder.addPropertySchema(name, new NumberSchema())
                break
            case ConfigValueType.BOOLEAN:
                objectBuilder.addPropertySchema(name, BooleanSchema.INSTANCE)
                break
            case ConfigValueType.OBJECT:
                def config = configValue as ConfigObject
                objectBuilder.addPropertySchema(name, getReference(name, config).referenceSchema)
                break
            default:
                throw new IllegalArgumentException("No ${configValue.valueType()}")

        }
    }

    static void toDefinitions(ConfigObject config) {
        config.keySet().each { key ->
            def vo = config.get(key)
            if (vo.valueType() == ConfigValueType.OBJECT) {
                getReference(key, vo as ConfigObject)
            }
        }
        definitions.each {
            println "DEFINITION: ${it.name}:${it.jsonObject}"
        }
    }

    static class Reference {
        String name
        ObjectSchema objectSchema
        Map<String, ConfigValueType> configValueTypeMap
        ReferenceSchema referenceSchema

        ReferenceSchema getReferenceSchema() {
            if (!referenceSchema) {
                referenceSchema = ReferenceSchema.builder().refValue("#/definitions/${name}").build()
                referenceSchema.setReferredSchema(objectSchema)
            }
            referenceSchema
        }

        JSONObject getJsonObject() {
            new JSONObject(objectSchema.toString())
        }
    }

    static Map<String, ConfigValueType> getDefinition(ConfigObject configObject) {
        configObject.collectEntries { [(it.key): it.value.valueType()] }.sort { it.key } as Map<String, ConfigValueType>
    }

    static Reference getReference(String name, ConfigObject config) {
        def definition = getDefinition(config)
        def existing = definitions.find { it.configValueTypeMap == definition }
        if (existing) {
            return existing
        }

        if (isPatternObject(definition)) {
            def reference = new Reference(name: name, configValueTypeMap: definition)
            definitions.add reference
            def configObject = config.get(definition.keySet().first()) as ConfigObject
            def patternReference = getReference(name.substring(0, name.length() - 1),configObject)
            def objectBuilder = ObjectSchema.builder()
            objectBuilder.patternProperty(REGEXP_FACTORY.createHandler('^.*'), patternReference.referenceSchema)
            reference.objectSchema = objectBuilder.build()
            return reference
        }

        def reference = new Reference(name: name, configValueTypeMap: definition)
        definitions.add reference

        reference.objectSchema = toObjectSchema(config)

        config.findAll { it.value.valueType() == ConfigValueType.LIST }.each {
            def list = config.get(it.key) as ConfigList
            if (list.first().valueType() == ConfigValueType.OBJECT) {
                return getReference(null, list.first() as ConfigObject)
            }
        }

        config.findAll { it.value.valueType() == ConfigValueType.OBJECT }.each {
            getReference(it.key, it.value as ConfigObject)
        }
        reference
    }


    static boolean isPatternObject(definition) {
        if (definition.every { it.value == ConfigValueType.OBJECT }) {
            def allObjects = definition.collect { it.value }
            if (allObjects.size() > 1 && allObjects.unique().size() == 1) {
                return true
            }
        }
        return false
    }

    static ObjectSchema toObjectSchema(ConfigObject config, String schemaProperty = null) {
        def objectBuilder = ObjectSchema.builder()
        if (schemaProperty) {
            objectBuilder.unprocessedProperties.put('$schema', schemaProperty)
        }

        config.keySet().each {
            addProperty(objectBuilder, it, config.get(it))
        }
        if (schemaProperty) {
            objectBuilder.unprocessedProperties.definitions = definitions.collectEntries { [(it.name): it.jsonObject] }
        }
        def objectSchema = objectBuilder.build()
        objectSchema
    }

    static Schema getSchema(ConfigObject config) {
        toDefinitions(config)
        toObjectSchema(config, "http://json-schema.org/draft-07/schema#")
    }

    static void toFile(ConfigObject config, File file) {
        file.text = new JSONObject(getSchema(config).toString()).toString(1)
    }

    static void configFileToSchemaFile(String configFilePath, String schemaFilePath) {
        configFileToSchemaFile(new File(configFilePath), new File(schemaFilePath))
    }

    static void configFileToSchemaFile(File configFile, File schemaFile) {
        def config = Loader.parse(configFile).root()
        schemaFile.text = new JSONObject(getSchema(config).toString()).toString(1)
    }

    static String configToJson(Config config) {
        return new JSONObject(config.root().render(ConfigRenderOptions.concise())).toString(1)
    }


}
