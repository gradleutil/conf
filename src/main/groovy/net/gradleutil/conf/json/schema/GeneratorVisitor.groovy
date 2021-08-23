package net.gradleutil.conf.json.schema

import org.everit.json.schema.ArraySchema
import org.everit.json.schema.BooleanSchema
import org.everit.json.schema.CombinedSchema
import org.everit.json.schema.EnumSchema
import org.everit.json.schema.NullSchema
import org.everit.json.schema.NumberSchema
import org.everit.json.schema.ObjectSchema
import org.everit.json.schema.ReferenceSchema
import org.everit.json.schema.Schema
import org.everit.json.schema.StringSchema
import org.everit.json.schema.Visitor

class GeneratorVisitor extends Visitor {

    synchronized List<Schema> visitedSchemas = []

    def <T> List<T> collectSchema(Schema schema, Class<T> clazz) {
        visit(schema)
        return visitedSchemas.findAll { it.class == clazz } as List<T>
    }

    void visitSchema(Schema schema) {
        if (!visitedSchemas.contains(schema)) {
            visitedSchemas.add(schema)
            visit(schema)
        }
    }

    @Override
    void visit(Schema schema) {
        super.visit(schema)
    }

    @Override
    void visitCombinedSchema(CombinedSchema schema) {
        super.visitCombinedSchema(schema)
        schema.subschemas.each {
            visit(it)
        }
    }

    @Override
    void visitReferenceSchema(ReferenceSchema schema) {
        super.visitReferenceSchema(schema)
        visit(schema.referredSchema)
    }

    @Override
    void visitObjectSchema(ObjectSchema objectSchema) {
        super.visitObjectSchema(objectSchema)
    }

    @Override
    void visitBooleanSchema(BooleanSchema schema) {
        super.visitBooleanSchema(schema)
    }

    @Override
    void visitStringSchema(StringSchema stringSchema) {
        super.visitStringSchema(stringSchema)
    }

    @Override
    void visitEnumSchema(EnumSchema enumSchema) {
        super.visitEnumSchema(enumSchema)
    }

    @Override
    void visitNullSchema(NullSchema nullSchema) {
        super.visitNullSchema(nullSchema)
    }

    @Override
    void visitNumberSchema(NumberSchema numberSchema) {
        super.visitNumberSchema(numberSchema)
    }

    @Override
    void visitArraySchema(ArraySchema arraySchema) {
        super.visitArraySchema(arraySchema)
    }

    @Override
    void visitPropertySchema(String properyName, Schema schema) {
        super.visitPropertySchema(properyName, schema)
    }

    @Override
    void visitPropertySchemas(Map<String, Schema> propertySchemas) {
        super.visitPropertySchemas(propertySchemas)
    }
}
