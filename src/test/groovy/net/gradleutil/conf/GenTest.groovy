package net.gradleutil.conf


import spock.lang.Specification

class GenTest extends Specification {

    def base = 'src/test/groovy/net/gradleutil/conf/temp/'
    def packageName = 'gradleutil.conf.temp'

    def setup() {
        new File(base).with {
            if (exists()) {
                deleteDir()
            }
            mkdirs()
        }
    }

    def "json schema"() {
        setup:
        def jsonSchema = new File('src/test/resources/json/json-schema.json')
        def gen = new Gen()
        gen.packageName = packageName

        when:
        if (!jsonSchema.exists()) {
            jsonSchema.text = new URL('http://json-schema.org/draft-07/schema#').text
        }
        def modelFile = new File(base + 'JsonSchema.groovy')
        def result = gen.groovyClassFromSchema(jsonSchema.text, 'JsonSchema', modelFile)
        println "file://${modelFile.absolutePath}"

        then:
        result
    }

    def "family schema"() {
        setup:
        def jsonSchema = new File('src/test/resources/json/family.schema.json')
        def gen = new Gen()
        gen.packageName = packageName

        when:
        def modelFile = new File(base + 'Family.groovy')
        def result = gen.groovyClassFromSchema(jsonSchema.text, 'Family', modelFile)
        println "file://${modelFile.absolutePath}"

        then:
        result
    }

    def "multiple schema"() {
        setup:
        def jsonSchemaDir = new File('src/test/resources/json/multiple')
        def gen = new Gen()
        gen.packageName = packageName

        when:
        def modelFile = new File(base + '/multiple')
        def result = gen.groovyClassFromSchema(jsonSchemaDir, modelFile, packageName)
        println "file://${modelFile.absolutePath}"

        then:
        result.size() > 0
    }

    def "ref schema"() {
        setup:
        def jsonSchema = new File('src/test/resources/json/ref.schema.json')
        def gen = new Gen()
        gen.packageName = packageName

        when:
        def modelFile = new File(base + 'Family.groovy')
        def result = gen.groovyClassFromSchema(jsonSchema.text, 'Ref', modelFile)
        println "file://${modelFile.absolutePath}"

        then:
        result == true
    }

    def "ecore schema"() {
        setup:
        def jsonSchema = new File('buildSrc/src/net/gradleutil/conf/schema/Ecore.schema.json')
        def gen = new Gen()
        gen.packageName = packageName

        when:
        def modelFile = new File(base + 'Ecore.groovy')
        def result = gen.groovyClassFromSchema(jsonSchema.text, 'EPackage', modelFile)
        println "file://${modelFile.absolutePath}"

        then:
        result == true
    }

    def "veggies schema"() {
        setup:
        def jsonSchema = new File('src/test/resources/json/veggies.schema.json').text
        def gen = new Gen()
        gen.packageName = packageName

        when:
        def modelFile = new File(base + 'Veggies.groovy')
        def result = gen.groovyClassFromSchema(jsonSchema, 'Veggies', modelFile)
        println "file://${modelFile.absolutePath}"

        then:
        result == true
    }

    def "minecraft schema"() {
        setup:
        def jsonSchema = new File('src/test/resources/json/minecraft.schema.json')
        def gen = new Gen()
        gen.packageName = packageName

        when:
        def modelFile = new File(base + 'Minecraft.groovy')
        def result = gen.groovyClassFromSchema(jsonSchema.text, 'MinecraftConfig', modelFile)
        println "file://${modelFile.absolutePath}"

        then:
        result == true
    }


/*
    def "test dsl"() {
        setup:
        def configModelFile = new File('src/test/groovy/net/gradleutil/generated/JavaClass.groovy')
        def gen = new Gen()

        when:
        def result = net.gradleutil.generated.DSL.javaClass{
            name = 'fart'
        }

        then:
        result == true
    }
*/
}
