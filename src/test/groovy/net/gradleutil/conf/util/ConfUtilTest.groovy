package net.gradleutil.conf.util

import com.typesafe.config.Config
import net.gradleutil.conf.AbstractTest
import net.gradleutil.conf.Loader

import static net.gradleutil.conf.Loader.defaultOptions

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

		when:
		def config = Loader.load(conf)
		def library = Loader.create(config, Library, defaultOptions().silent(false))

		then:
		library.books.size() == 2

	}

	def "test substitutions in json strings"() {
		setup:
		def libraryJson = new File('src/test/resources/json/library.json')

		def conf = new File(base, 'config.conf').tap { text = libraryJson.text }

		when:
		def config = Loader.load(conf)

		then:

		println ConfUtil.configToJson(config).toString()

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


	static class Library {
		String name
		List<Book> books
	}

	static class Book {
		String title
		Integer pages
		String ISBN
		List<Author> authors
	}

	static class Author {
		String firstName
		String lastName
	}

}
