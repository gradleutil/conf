package net.gradleutil.conf


import com.typesafe.config.ConfigException
import com.typesafe.config.ConfigValue


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
		LoaderOptions opts = Loader.loaderOptions().silent(true)
				.confString(configFile.text)
				.schemaFile(schemaFile)
				.allowUnresolved(false)
				.useSystemProperties(false)
		def config = load(opts)

		then:
		config.hasPath('people')
		def list = config.getList('people')
		def cv = list.get(1).atKey('person')
		def age = cv.getValue('person')['age']
		def firstName = cv.getValue('person')['firstName']
		age.class.simpleName == 'ConfigNull'
		(firstName as ConfigValue).unwrapped() == 'Jane'

	}

	def "royals loads"() {
		setup:
		def configFile = new File('src/test/resources/json/royalty.json')
		def schemaFile = new File('src/test/resources/json/royalty.schema.json')

		when:
		println 'file:///' + configFile.absolutePath
		println 'file:///' + schemaFile.absolutePath
		LoaderOptions opts = Loader.loaderOptions().silent(true)
				.confString(configFile.text)
				.schemaFile(schemaFile)
				.allowUnresolved(false)
				.useSystemProperties(false)
		def config = load(opts)

		then:
		config.hasPath('royalty')
		def royalty = config.getConfig('royalty')
		def list = royalty.getList('children')
		def cv = list.get(0).atKey('child')
		def firstName = cv.getValue('child')['name']
		(firstName as ConfigValue).unwrapped() == 'Charles'
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
