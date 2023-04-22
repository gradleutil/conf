package net.gradleutil.conf

import com.typesafe.config.ConfigException
import com.typesafe.config.ConfigValue
import net.gradleutil.conf.bean.PersonList

import static net.gradleutil.conf.Loader.load

class LoaderTest extends AbstractTest {

	def "test override"() {
		setup:
		def conf = new File(base, 'config.conf').tap {
			text = """
        {
            "car": {
                "engine": {
                    "type": "big",
                    "brand": "sterling"
                },
                "doors": {
                    "number": 4
                }
            }
        }
        """
		}
		def confOverride = new File(base, 'config.override.conf').tap {
			text = """
        {
            "car": {
                "engine": {
                    "type": "small",
                }
            }
        }
        """
		}

		when:
		def config = Loader.loadWithOverride(conf, confOverride)

		then:
		config.root().unwrapped().car.engine.type == 'small'
		config.root().unwrapped().car.doors.number == 4
	}

	def "people loads"() {
		setup:
		def configFile = new File('src/test/resources/json/list/people.json')
		def schemaFile = new File('src/test/resources/json/people.schema.json')

		when:
		println 'file:///' + configFile.absolutePath
		println 'file:///' + schemaFile.absolutePath
		Loader.LoaderOptions opts = Loader.defaultOptions().silent(true)
				.confString(configFile.text)
				.schemaFile(schemaFile)
				.allowUnresolved(false)
				.useSystemProperties(false)
		def per = Loader.create(new PersonList(), load(opts as Loader.LoaderOptions), PersonList, opts)
		def config = load(opts)

		then:
		per
		config.hasPath('people')
		def list = config.getList('people')
		def cv = list.get(1).atKey('person')
		def age = cv.getValue('person')['age']
		def firstName = cv.getValue('person')['firstName']
		age.class.simpleName == 'ConfigNull'
		(firstName as ConfigValue).unwrapped() == 'Jane'

	}

	def "test bad json"() {
		setup:
		def conf = new File(base, 'config.conf').tap {
			text = """
				{
					"car": {
						"engine": {
							"type": "big",
							"brand": "sterling"
						},
						"doors": {
							"number": 4
						}
					}

				"""
		}

		when:
		def config = load(conf)

		then:
		final ConfigException.Parse exception = thrown()
	}

}
