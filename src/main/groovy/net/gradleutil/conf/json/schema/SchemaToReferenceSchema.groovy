package net.gradleutil.conf.json.schema

import org.everit.json.schema.*
import org.json.JSONObject

class SchemaToReferenceSchema {

    static ReferenceSchema schemaToReferenceSchema(Schema schema, String ref, String schemaProperty = 'http://json-schema.org/draft-07/schema#') {
        ReferenceSchema.Builder referenceBuilder = ReferenceSchema.builder().tap { refValue("#/definitions/" + ref) }
        referenceBuilder.unprocessedProperties.put('$schema', schemaProperty)
        ReferenceSchema referenceSchema = toReferenceSchema(referenceBuilder, schema, ref)
        setUnprocessedPropertyDefinitions(referenceSchema)
        referenceSchema
    }


    static void setUnprocessedPropertyDefinitions(Schema referenceSchema){
        Map<String, Object> definitions =   [:] as Map<String, Object>
        GeneratorVisitor visitor = new GeneratorVisitor(){
            @Override
            void visit(Schema schema) {
                if(schema instanceof ReferenceSchema){
                    definitions.put(getRef(schema),new JSONObject(schema.referredSchema?.toString()))
                }
                super.visit(schema)
            }
        }
        visitor.visit(referenceSchema)
        referenceSchema.unprocessedProperties.definitions = definitions
    }


    @SuppressWarnings('GroovyAccessibility')
    static String getRef(ReferenceSchema schema) {
        String ref = schema.refValue.toString().split('/')?.last()
        return ref
    }

    static ReferenceSchema toReferenceSchema(Schema.Builder<ReferenceSchema> referenceBuilder, Schema schema, String ref) {
        getReferenceSchema(referenceBuilder, schema , ref)
    }

    static ReferenceSchema toReferenceSchema(Schema schema, String ref, String schemaProperty = null) {
        ReferenceSchema.Builder referenceBuilder = ReferenceSchema.builder().tap { refValue("#/definitions/" + ref) }
        getReferenceSchema(referenceBuilder, schema , ref)
    }

    static ReferenceSchema getReferenceSchema(Schema.Builder<ReferenceSchema> referenceBuilder, Schema schema, String ref) {
        ReferenceSchema referenceSchema
        if (schema instanceof ObjectSchema) {
            referenceSchema = buildReferenceSchema(ref, referenceBuilder, schema as ObjectSchema)
        } else if (schema instanceof CombinedSchema) {
            referenceSchema = buildReferenceSchema(referenceBuilder, schema as CombinedSchema)
        } else {
            throw new RuntimeException("Ahhhhhhhhhhhhhhhhhhhhhh" + schema.class)
        }
        referenceSchema
    }

    static ReferenceSchema buildReferenceSchema(String ref, Schema.Builder<ReferenceSchema> referenceBuilder, ObjectSchema sourceObjectSchema, Boolean toSingularNames = false) {
        def objectBuilder = ObjectSchema.builder()
        objectBuilder.title(ref)
        objectBuilder.schemaLocation(SchemaLocation.parseURI('#/definitions/' + ref))

        sourceObjectSchema.propertySchemas.each { String key, Schema propertySchema ->
            if (propertySchema instanceof ObjectSchema) {
                ReferenceSchema refSchema = toReferenceSchema(propertySchema as ObjectSchema, key)
                objectBuilder.addPropertySchema(key, refSchema)
            } else if (propertySchema instanceof ArraySchema) {
                Schema firstItem = propertySchema.allItemSchema ?: propertySchema.itemSchemas?.first()
                if(firstItem instanceof ObjectSchema){
                    Map.Entry<String, Schema> firstProp = firstItem.propertySchemas.entrySet().first()
                    if(firstItem.propertySchemas.size() == 1  && firstProp.value instanceof ObjectSchema){
                        // single keyed array item infers sub-object, so use the key for the field name and add the sub-object type
                        ReferenceSchema refSchema = toReferenceSchema(firstProp.value, firstProp.key)
                        ArraySchema arraySchema = ArraySchema.builder().allItemSchema(refSchema).build()
                        objectBuilder.addPropertySchema(key, arraySchema)
                    } else {
                        ReferenceSchema refSchema = toReferenceSchema(firstItem as ObjectSchema, key)
                        ArraySchema arraySchema = ArraySchema.builder().allItemSchema(refSchema).build()
                        objectBuilder.addPropertySchema(key, arraySchema)
                    }
                } else {
                    objectBuilder.addPropertySchema(key, propertySchema)
                }
            } else {
                objectBuilder.addPropertySchema(key, propertySchema)
            }
        }

        sourceObjectSchema.requiredProperties.each { requiredName ->
            objectBuilder.addRequiredProperty(requiredName)
        }
        if (sourceObjectSchema.schemaOfAdditionalProperties) {
            objectBuilder.schemaOfAdditionalProperties(sourceObjectSchema.schemaOfAdditionalProperties)
        }
        if (sourceObjectSchema.propertyNameSchema) {
            objectBuilder.propertyNameSchema(sourceObjectSchema.propertyNameSchema)
        }
        def objectSchema = objectBuilder.build()
        def referenceSchema = referenceBuilder.build()
        referenceSchema.setReferredSchema(objectSchema)
        referenceSchema
    }



    static ReferenceSchema buildReferenceSchema(Schema.Builder<ReferenceSchema> referenceBuilder, CombinedSchema combinedSchema, Boolean toSingularNames = false) {
        def referenceSchema = referenceBuilder.build()
        referenceSchema.setReferredSchema(combinedSchema)
        referenceSchema
    }



}
