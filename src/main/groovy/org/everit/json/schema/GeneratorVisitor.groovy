package org.everit.json.schema

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


}
