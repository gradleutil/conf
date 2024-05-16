package net.gradleutil.conf.util


import com.typesafe.config.ConfigRenderOptions
import net.gradleutil.conf.AbstractTest
import net.gradleutil.conf.Loader
import net.gradleutil.conf.json.schema.SchemaUtil

import static net.gradleutil.conf.Loader.loaderOptions

class ConfUtilTest extends AbstractTest {

    def "test json print simple conf"() {
        setup:
        def conf = new File(base, 'config.conf').tap {
            text = """
			{
				"car": {
					"engine": {
						"type": "big",
						"type": "sterling"
						"size": "\${"car"."doors"."number"}"
						"moreDoors": "\${car.doors.number}"
						"inString": "in \${car.doors.number} ling"
					},
					"doors": {
						"number": 4
					}
					"doors": {
						"number": 4
					}
				}
			}
			""".stripIndent()
        }

        when:
        def config = Loader.load(conf)

        then:
        def car = Loader.load(ConfUtil.configToJson(config, 'car'))
        car.getConfig('engine').getString('type') == 'sterling'

        def engine = Loader.load(ConfUtil.configToJson(config, 'car.engine'))
        engine.getString('type') == 'sterling'
        engine.getString("moreDoors") == "4"
        engine.getString("inString") == "in 4 ling"

    }


    def "test substitutions in json to object strings"() {
        setup:
        def libraryJson = new File('src/test/resources/json/library.json')
        def conf = new File(base, 'config.conf').tap { text = libraryJson.text }
        println "file:///${libraryJson.absolutePath}"

        when:
        def config = Loader.load(conf)
        def library = Loader.create(config, Library, loaderOptions().silent(false))

        then:
        library.books.size() == 2

    }

    def "test order in json to object strings"() {
        setup:
        def libraryJsonFile = new File('src/test/resources/json/server.schema.json')
        def libraryJson = libraryJsonFile.text
        println "file:///${libraryJsonFile.absolutePath}"

        when:
        def config = Loader.load(libraryJson)
        def schema = SchemaUtil.getSchema(libraryJson,"")

        then:
        schema.getSchemaNode().size() == 3
        config.getConfig('definitions').entrySet().size() == 135

    }

    def "test object to schema"() {
        setup:
        def library = new Library()

        when:
        def schema = SchemaUtil.getSchema(library.class).schemaNode

        then:
        schema.size() == 4

    }

    def "test config to schema"() {
        setup:
        def libraryJsonFile = new File('src/test/resources/json/library.json')
        def libraryJson = libraryJsonFile.text
        println "file:///${libraryJsonFile.absolutePath}"

        when:
        def config = Loader.load(libraryJson)
        def schema = SchemaUtil.getSchema(config, 'libraries', "")
        def conf = new File(base, 'config.schema.json').tap { text = schema.schemaNode.toPrettyString() }
        println "file:///${conf.absolutePath}"

        then:
        schema.schemaNode.size() == 3

    }

    def "test referenced file schema"() {
        setup:
        def libraryJsonFile = new File('src/test/resources/json/people.schema.json')
        def libraryJson = libraryJsonFile.text
        println "file:///${libraryJsonFile.absolutePath}"

        when:
        def schema = SchemaUtil.getSchema(libraryJson, libraryJsonFile.parentFile.absolutePath)
        schema.walk(null, false);
        println schema.schemaNode.toPrettyString()

        then:
        schema.schemaNode.get('definitions').size() == 2

    }

    def "test beanToJson"() {
        setup:
        def libraryJson = new File('src/test/resources/json/library.json')
        def conf = new File(base, 'config.conf').tap { text = libraryJson.text }
        println "file:///${libraryJson.absolutePath}"

        when:
        def config = Loader.load(conf)
        def library = Loader.create(config, Library, loaderOptions().silent(false))

        then:
        library.books.size() == 2
        def json = ConfUtil.beanToJson(library)
        json.startsWith('{"books":[{"ISBN":')

    }

    def "test substitutions in json strings"() {
        setup:
        def libraryJson = new File('src/test/resources/json/library.json')

        def conf = new File(base, 'config.conf').tap { text = libraryJson.text }

        when:
        def config = Loader.load(conf)

        then:

        println config.root().render(ConfigRenderOptions.concise().tap { formatted = true })

        config.getConfig('library').getConfigList('books').size() == 2
        config.getConfig('library').getConfigList('books').get(1).getInt('pages') == 330
        (config.getConfig('library').getConfigList('books').get(1).
                getList('authors').get(0) as ConfigObject).get('lastName').toString() == 'Quoted("Jackson Suzy")'

    }


    def "test simple substitution"() {
        setup:
        def conf = new File(base, 'config.conf').tap {
            text = '''
			{
				"" : {  "" : { "" : 42 } },
				"42_a" : ${""."".""},
				"42_b" : ${  ""."".""  },
				"a" : { "b" : { "c" : 57 } },
				"57_a" : ${a.b.c},
				"57_b" : ${"a"."b"."c"},
				"a.b.c" : 103,
				"103_a" : ${"a.b.c"},
				"103_D" : { "e": ${"a.b.c"} },
				"103_E" : { "f": ${"103_D"} },
				"a-c" : 259,
				"a_c" : 260,
				"-" : 261
			}
			'''.stripIndent()
        }

        when:
        def config = Loader.load(conf)

        then:
        //println ConfUtil.configToJson(config).toString()
        config.getConfig("103_D").getInt('e') == 103
        config.getConfig("103_E").getConfig('f').getInt('e') == 103

    }

    def "test chained substitution"() {
        setup:
        def conf = new File(base, 'config.conf').tap {
            text = '''
			{
				"mainSuffix": "suffix",
				"sub": {
					"concat1": "${mainSuffix}More",
					"domain": "e-cycle.com"
				},
				"sub2": {
					"concat2": "${sub.concat1}"
				},
			}
			'''.stripIndent()
        }

        when:
        def config = Loader.load(conf)

        then:
        println config.root().render(ConfigRenderOptions.concise().tap { formatted = true })
        config.getConfig("sub2").getValue('concat2').unwrapped() == 'suffixMore'

    }

    def "test simpler substitution"() {
        setup:
        def conf = new File(base, 'config.conf').tap {
            text = '''
				{
					"a.b.c" : 103,
					"103_a" : ${"a.b.c"},
					"103_D" : { "e": ${"a.b.c"} },
					"103_E" : { "f": ${"103_D"} },
				}
				'''.stripIndent()
        }

        when:
        def config = Loader.load(conf)

        then:
        //println ConfUtil.configToJson(config).toString()
        config.getConfig("103_D").getInt('e') == 103
        config.getConfig("103_E").getConfig('f').getInt('e') == 103
    }

    def "test identifier replacement"() {
        setup:
        def words

        when:
        words = [
                '323'    : '_323',
                'default': '_default',
                'case'    : '_case'
        ]

        then:
        words.each {
            String converted = ConfUtil.ident(it.key, false, false, false)
            assert converted == it.value
        }
    }


    static class Library {
        String name
        List<Book> books
    }

    static class Book {
        String title
        String description
        Integer pages
        String ISBN
        List<Author> authors
    }

    static class Author {
        String firstName
        String lastName
    }

}
