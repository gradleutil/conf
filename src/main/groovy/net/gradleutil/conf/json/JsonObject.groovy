package net.gradleutil.conf.json

import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

import java.lang.reflect.Field
import java.lang.reflect.Method

class JsonObject extends JSONObject {

    JsonObject() {
        super()
    }

    JsonObject(JSONTokener jsonTokener) {
        super(jsonTokener)
    }

    JsonObject(String source) {
        super(source)
    }

    JsonObject(Object object) {
        super(object)
    }

    /**
     * Write the contents of the JSONObject as JSON text to a writer.
     *
     * <p>If <pre>{@code indentFactor > 0}</pre> and the {@link JsonObject}
     * has only one key, then the object will be output on a single line:
     * <pre>{@code {"key":1}}</pre>
     *
     * <p>If an object has 2 or more keys, then it will be output across
     * multiple lines: <pre>{@code {* "key1": 1,
     *  "key2": "value 2",
     *  "key3": 3
     *}}</pre>
     * <p><b>
     * Warning: This method assumes that the data structure is acyclical.
     * </b>
     *
     * @param writer
     *            Writes the serialized JSON
     * @param indentFactor
     *            The number of spaces to add to each level of indentation.
     * @param indent
     *            The indentation of the top level.
     * @return The writer.
     * @throws JSONException if a called function has an error or a write error
     * occurs
     */
    Writer write(Writer writer, int indentFactor, int indent)
            throws JSONException {
        try {
            boolean needsComma = false
            final int length = this.length()
            writer.write('{')

            if (length == 1) {
                final Map.Entry<String, ?> entry = this.entrySet().iterator().next()
                writeEntry(writer, entry, indentFactor, indent)
            } else if (length != 0) {
                final int newIndent = indent + indentFactor
                for (final Map.Entry<String, ?> entry : this.entrySet()) {
                    if (needsComma) {
                        writer.write(',')
                    }
                    if (indentFactor > 0) {
                        writer.write('\n')
                    }
                    indent(writer, newIndent)
                    writeEntry(writer, entry, indentFactor, newIndent)
                    needsComma = true
                }
                if (indentFactor > 0) {
                    writer.write('\n')
                }
                indent(writer, indent)
            }
            writer.write('}')
            return writer
        } catch (IOException exception) {
            throw new JSONException(exception)
        }
    }

    static void writeEntry(writer, entry, indentFactor, newIndent) {
        final String key = entry.getKey()
        writer.write(quote(key))
        writer.write(':')
        if (indentFactor > 0) {
            writer.write(' ')
        }
        try {
            //noinspection GroovyAccessibility
            writeValue(writer, entry.getValue(), indentFactor, newIndent)
        } catch (Exception e) {
            throw new JSONException("Unable to write JSONObject value for key: " + key, e)
        }

    }

}
