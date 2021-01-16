package gradleutil.conf

import com.fizzed.rocker.Rocker
import gradleutil.conf.generator.EPackageGenerator
import gradleutil.conf.generator.GroovyConfig
import gradleutil.conf.generator.JsonConfig
import gradleutil.conf.template.EPackage
import org.everit.json.schema.Schema
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONArray
import org.json.JSONObject

import static gradleutil.conf.generator.JsonConfig.jsonObjectFromString

class Gen {

    private Schema schema
    private String packageName
    public static final String GEN_GROOVY = "gradleutil/conf/generator/groovy/GroovyGen.rocker.raw"

    Gen() {
    }


    boolean groovyClassFromSchema(String jsonSchema, String rootClassName, File outputFile) throws IOException {

        EPackage ePackage = EPackageGenerator.getEPackage(getSchema(jsonSchema), rootClassName, packageName)
        String source = Rocker.template(GEN_GROOVY, ePackage).render().toString()

        FileWriter fileWriter = new FileWriter(outputFile)
        fileWriter.write(source)
        fileWriter.close()
        return true
    }

    static List<File> groovyClassFromSchema(File schemaDirectory,  File outputDirectory, String packageName) throws IOException {
        List<File> generatedFiles = []
        if(!outputDirectory.exists()){
            outputDirectory.mkdirs()
        }
        schemaDirectory.listFiles().each{
            String jsonSchema = it.text
            String rootClassName = it.name.replace('.schema','').replace('.json','')
            EPackage ePackage = EPackageGenerator.getEPackage(getSchema(jsonSchema), rootClassName, packageName + '.' + rootClassName.toLowerCase())
            String source = Rocker.template(GEN_GROOVY, ePackage).render().toString()
            def outputPackageDir = new File(outputDirectory.path + '/' + rootClassName.toLowerCase()).tap{it.mkdir() }
            def outputFile = new File(outputPackageDir,rootClassName+'.groovy')
            generatedFiles.add(outputFile)
            FileWriter fileWriter = new FileWriter(outputFile)
            fileWriter.write(source)
            fileWriter.close()
        }
        return generatedFiles
    }

    Schema getSchemaFile() {
        return schema
    }

    void setSchemaFile(Schema schemaFile) {
        this.schema = schemaFile
    }

    void setSchema(String schemaJson) {
        this.schema = getSchema(schemaJson)
    }

    String getPackageName() {
        return packageName
    }

    void setPackageName(String packageName) {
        this.packageName = packageName
    }

    void editor(File path, String json) {
        path.text = JsonConfig.editor(schema.toString(), json)
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


    static Map<String, Object> toMap(JSONObject jsonObject, List<String> definitionTypes = []) {
        Map<String, Object> map = new HashMap<String, Object>()
        Iterator<String> keys = jsonObject.keys()
        def key, value
        while (keys.hasNext()) {
            key = keys.next()
            value = jsonObject.get(key)
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value, definitionTypes)
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value, definitionTypes)
            } else if (value instanceof String && value.startsWith('#') && value.length() > 1) {
                def definition = value.toString().replace('#/definitions/', '')
                if (!definitionTypes.find { it == definition }) {
                    def closeMatch = definitionTypes.findAll { it.contains(definition) }.sort { it.length() }.find()
                    if (closeMatch) {
                        value = "#/definitions/" + closeMatch
                    } else {
                        key = 'type'
                        value = 'object'
                    }
                }
            }
            map.put(key, value)
        }
        return map
    }

    static List<Object> toList(JSONArray array, List<String> definitionTypes = []) {
        List<Object> list = new ArrayList<Object>()
        def value
        for (int i = 0; i < array.length(); i++) {
            value = array.get(i)
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value, definitionTypes)
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value, definitionTypes)
            }
            list.add(value)
        }
        return list
    }


}
