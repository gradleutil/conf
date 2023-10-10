package net.gradleutil.conf.json;

import org.json.*;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

public class JsonObject extends JSONObject {
    public JsonObject() {
        super();
    }

    public JsonObject(JSONTokener jsonTokener) {
        super(jsonTokener);
    }

    public JsonObject(String source) {
        super(source);
    }

    public JsonObject(Object object) {
        super(object);
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
     * }}</pre>
     * <p><b>
     * Warning: This method assumes that the data structure is acyclical.
     * </b>
     *
     * @param writer       Writes the serialized JSON
     * @param indentFactor The number of spaces to add to each level of indentation.
     * @param indent       The indentation of the top level.
     * @return The writer.
     */
    public Writer write(Writer writer, int indentFactor, int indent)
            throws JSONException {
        try {
            boolean needsComma = false;
            final int length = this.length();
            writer.write('{');

            if (length == 1) {
                final Map.Entry<String, ?> entry = this.entrySet().iterator().next();
                writeEntry(writer, entry, indentFactor, indent);
            } else if (length != 0) {
                final int newIndent = indent + indentFactor;
                for (final Map.Entry<String, ?> entry : this.entrySet()) {
                    if (needsComma) {
                        writer.write(',');
                    }
                    if (indentFactor > 0) {
                        writer.write('\n');
                    }
                    indent(writer, newIndent);
                    writeEntry(writer, entry, indentFactor, newIndent);
                    needsComma = true;
                }
                if (indentFactor > 0) {
                    writer.write('\n');
                }
                indent(writer, indent);
            }
            writer.write('}');
            return writer;
        } catch (IOException exception) {
            throw new JSONException(exception);
        }
    }


    public static void writeEntry(Writer writer, Map.Entry<String, ?> entry, int indentFactor, int newIndent) throws IOException {
        final String key = entry.getKey();
        writer.write(quote(key));
        writer.write(':');
        if (indentFactor > 0) {
            writer.write(' ');
        }
        try {
            writeValue(writer, entry.getValue(), indentFactor, newIndent);
        } catch (Exception e) {
            throw new JSONException("Unable to write JSONObject value for key: " + key, e);
        }

    }

    static final Pattern NUMBER_PATTERN = Pattern.compile("-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?");

    @SuppressWarnings("resource")
    static final Writer writeValue(Writer writer, Object value,
                                   int indentFactor, int indent) throws JSONException, IOException {
        if (value == null || value.equals(null)) {
            writer.write("null");
        } else if (value instanceof JSONString) {
            Object o;
            try {
                o = ((JSONString) value).toJSONString();
            } catch (Exception e) {
                throw new JSONException(e);
            }
            writer.write(o != null ? o.toString() : quote(value.toString()));
        } else if (value instanceof Number) {
            // not all Numbers may match actual JSON Numbers. i.e. fractions or Imaginary
            final String numberAsString = numberToString((Number) value);
            if(NUMBER_PATTERN.matcher(numberAsString).matches()) {
                writer.write(numberAsString);
            } else {
                // The Number value is not a valid JSON number.
                // Instead we will quote it as a string
                quote(numberAsString, writer);
            }
        } else if (value instanceof Boolean) {
            writer.write(value.toString());
        } else if (value instanceof Enum<?>) {
            writer.write(quote(((Enum<?>)value).name()));
        } else if (value instanceof JSONObject) {
            ((JSONObject) value).write(writer, indentFactor, indent);
        } else if (value instanceof JSONArray) {
            ((JSONArray) value).write(writer, indentFactor, indent);
        } else if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            new JSONObject(map).write(writer, indentFactor, indent);
        } else if (value instanceof Collection) {
            Collection<?> coll = (Collection<?>) value;
            new JSONArray(coll).write(writer, indentFactor, indent);
        } else if (value.getClass().isArray()) {
            new JSONArray(value).write(writer, indentFactor, indent);
        } else {
            quote(value.toString(), writer);
        }
        return writer;
    }

    static final void indent(Writer writer, int indent) throws IOException {
        for (int i = 0; i < indent; i += 1) {
            writer.write(' ');
        }
    }
}
