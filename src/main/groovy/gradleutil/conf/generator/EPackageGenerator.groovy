package gradleutil.conf.generator

import gradleutil.conf.Gen
import gradleutil.conf.template.EPackage
import org.everit.json.schema.*
import org.json.JSONObject

import static gradleutil.conf.template.EPackage.EClass
import static gradleutil.conf.template.EPackage.EStructuralFeature

class EPackageGenerator {

    static EPackage ePackage
    static String rootClassName

    static Map<String, Object> toMap(Schema schema) {
        Gen.toMap(new JSONObject(schema.toString()))
    }

    static EStructuralFeature toFeature(String featureName, Schema schema) {
        assert schema
        def name = featureName.uncapitalize()
        switch (schema) {
            case TrueSchema:
                return new EStructuralFeature(name: name, etype: 'Boolean')
            case ReferenceSchema:
                return toFeature(name, (schema as ReferenceSchema).referredSchema)
            case BooleanSchema:
                return new EStructuralFeature(name: name, etype: 'Boolean', defaultValue: (schema as BooleanSchema).defaultValue)
            case StringSchema:
                return new EStructuralFeature(name: name, etype: 'String', format: (schema as StringSchema).formatValidator.toString() ?: '')
            case NumberSchema:
                return new EStructuralFeature(name: name, etype: (schema as NumberSchema).requiresInteger() ? 'Integer' : 'Long')
            case EnumSchema:
                return new EStructuralFeature(name: name, etype: 'enum', valueList: (schema as EnumSchema).possibleValuesAsList)
            case ObjectSchema:
                def objectSchema = (schema as ObjectSchema)
                def etype = getEType(objectSchema)
                if (objectSchema.patternProperties) {
                    return new EStructuralFeature(name: name, etype: "Map<String,${etype.substring(0, etype.length() - 1)}>", defaultValue: '[:]')
                }
                return new EStructuralFeature(name: name, etype: etype)
            case ArraySchema:
                def itemFeature = toFeature(name, (schema as ArraySchema).allItemSchema)
                return new EStructuralFeature(name: name, etype: "List<${itemFeature.etype}>", defaultValue: '[]')
//                def arrayItemType = (propertyInfo.items as Map)?.values()?.find()?.toString()?.replace('#/definitions/', '') ?: 'object'
//                return "List<${getJavaType([type: arrayItemType])}>"
            case CombinedSchema:
                def foundSchema = findSchema(schema, ObjectSchema)
                if (foundSchema) {
                    return toFeature(name, foundSchema as ObjectSchema)
                } else {
                    return toFeature(name, findSchema(schema, ReferenceSchema) ?: (schema as CombinedSchema).subschemas.first())
                }
            default:
                return new EStructuralFeature(name: name, etype: 'unknown')
        }
    }

    static EClass toClass(ObjectSchema schema) {
        def className = getEType(schema)
        assert className, "No class name defined for ${schema}"
        def existing = ePackage.classes.find { it.name == className }
        if (existing) {
            println "already had ${existing.name}"
            return existing
        }

        def javaClass = new EClass(name: className, etype: className)
        ePackage.classes.add(javaClass)

        schema.propertySchemas.each { String key, Schema propertySchema ->
            if (javaClass.features.find { it.name == key }) {
                println "already has feature"
            } else {
                javaClass.features.add(toFeature(key, propertySchema))
            }
        }

        schema.requiredProperties.each{ requiredName ->
            assert javaClass.features.find{it.name.toLowerCase() == requiredName.toLowerCase()}, javaClass.features*.name.join(',')
            javaClass.features.find{it.name.toLowerCase() == requiredName.toLowerCase()}.lowerBound = 1
        }

        if (schema.schemaOfAdditionalProperties) {
            def etype = getEType(schema.schemaOfAdditionalProperties)
            javaClass.features.add toFeature(etype+'s', schema.schemaOfAdditionalProperties)
        }
        if (schema.propertyNameSchema) {
            toMap(schema.propertyNameSchema).each{k, v ->
                javaClass.features.add new EStructuralFeature(name: k, etype: "String", defaultValue: "\"${v}\"")

            }
        }
        return javaClass
    }

    static String getEType(Schema schema, String defaultType=null) {
        def className = schema.schemaLocation?.split('/')?.last()?.capitalize() ?: defaultType ?:'#'
        assert className, "No class name defined for ${schema}"
        className == '#' ? rootClassName : className
    }

    static Schema findSchema(Schema schema, Class search) {
        if (schema.class.simpleName == search.simpleName) {
            return schema
        } else if (schema instanceof ReferenceSchema) {
            return findSchema(schema.referredSchema, search)
        } else if (schema instanceof CombinedSchema) {
            return schema.subschemas.collect { findSchema(it, search) }.find()
        }
        null
    }

    static EPackage getEPackage(Schema schema, String rootClassName, String packageName) {
        ePackage = new EPackage(name: packageName)
        this.rootClassName = rootClassName
        def packageClass

        def visitor = new GeneratorVisitor()
        def schemas = visitor.collectSchema(schema,ObjectSchema)

        schemas.each { toClass(it) }

        packageClass = ePackage.classes.find{it.name == rootClassName}

        if (!packageClass) {
            throw new IllegalArgumentException("No class `${rootClassName}` in ${ePackage.classes*.name.join(', ')}")
        }

        if (!packageClass.features) {
            throw new IllegalArgumentException("No features!")
        }

        return ePackage
    }

}