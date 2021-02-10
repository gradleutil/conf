package net.gradleutil.conf


import net.gradleutil.conf.generator.ConfigSchema
import spock.lang.Specification

class ConfigSchemaTest extends Specification {

    def base = 'src/test/groovy/net/gradleutil/conf/temp/'

    def setup() {
        new File(base).with {
            if (exists()) {
                deleteDir()
            }
            mkdirs()
        }
    }

    def "config to schema"() {
        setup:
        def configFile = new File('src/test/resources/json/family.json')
        def config = Loader.parse(configFile)
        assert config

        when:
        def modelFile = new File(base + 'Family.schema.json')
        def dataFile = new File(base + 'Family.json')
        dataFile.text = ConfigSchema.configToJson(config)
        //  "$schema": "./TsCfg.schema.json",
        ConfigSchema.configFileToSchemaFile(configFile, modelFile)
        println "file://${modelFile.absolutePath}"
        println "file://${dataFile.absolutePath}"

        then:
        modelFile.exists()
    }

}
