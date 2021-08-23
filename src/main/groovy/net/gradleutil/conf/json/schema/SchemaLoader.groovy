package net.gradleutil.conf.json.schema

import org.everit.json.schema.loader.SchemaLoader

class SchemaLoader extends org.everit.json.schema.loader.SchemaLoader {
    /**
     * Constructor.
     *
     * @param builder
     *         the builder containing the properties. Only {@link SchemaLoaderBuilder#id} is
     *         nullable.
     * @throws NullPointerException*         if any of the builder properties except {@link SchemaLoaderBuilder#id id} is
     * {@code null}.
     */
    SchemaLoader(SchemaLoaderBuilder builder) {
        super(builder)
    }
}
