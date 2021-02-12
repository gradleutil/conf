package net.gradleutil.conf

import net.gradleutil.conf.util.ConfUtil
import spock.lang.Specification

class LoaderTest extends Specification {

    def base = 'src/test/groovy/net/gradleutil/conf/temp/'

    def setup() {
        new File(base).with {
            if (exists()) {
                deleteDir()
            }
            mkdirs()
        }
    }

    def "test loader"() {
        setup:
        def funk = Loader.create(new File('src/test/resources/json/veggies.json').text, net.gradleutil.conf.genned.Veggies)
        println funk.vegetables
    }

    def "test override"() {
        setup:
        def conf = new File(base, 'config.conf').tap { text = """
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
        """ }
        def confOverride = new File(base, 'config.override.conf').tap { text = """
        {
            "car": {
                "engine": {
                    "type": "small",
                }
            }
        }
        """ }

        when:
        def config = Loader.loadWithOverride(conf, confOverride)

        then:
        config.root().unwrapped().car.engine.type == 'small'
        config.root().unwrapped().car.doors.number == 4
    }

    def "test system props or nots"() {
        setup:
        def config
        def conf = new File(base, 'config.conf').tap { text = 'one=1\ntwo=2\nthree=3\n' }

        when:
        config = Loader.load(conf, Loader.defaultOptions().setUseSystemProperties(false).setSilent(false))

        then:
        config.root().unwrapped().java == null

        when:
        config = Loader.load(conf, Loader.defaultOptions().setUseSystemProperties(true))
        println ConfUtil.configToJson(config)

        then:
        config.root().unwrapped().java != null

    }

    def "test reference"() {
        setup:
        def config
        def ref = new File(base, 'reference.conf').tap { text = """
        {
            "car": {
                "engine": {
                    "type": "big",
                    "brand": "sterling",
                    "dir": \${user.dir}
                },
                "doors": {
                    "number": 4
                }
            }
        }
        """ }
        def conf = new File(base, 'config.conf').tap { text = """
        {
            "car": {
                "engine": {
                    "type": "small"
                }
            }
        }
        """ }
        def confOverride = new File(base, 'config.override.conf').tap { text = """
        {
            "car": {
                "engine": {
                    "brand": "wellbuilt"
                }
            }
        }
        """ }

        when:
        System.setProperty('car.doors.number','2')
        Loader.invalidateCaches()
        config = Loader.load(conf, Loader.defaultOptions().setUseSystemProperties(true).setReference(ref).setConfOverride(confOverride).setSilent(false))
        println ConfUtil.configToJson(config)


        then:
        config.root().unwrapped().car.engine.dir != 'user.dir'
        config.root().unwrapped().car.engine.brand == 'wellbuilt'
        config.root().unwrapped().car.engine.type == 'small'
        System.getProperty('car.doors.number') == '2'
        config.root().unwrapped().car.doors.number == '2'

        then:
        config.root().unwrapped().java != null
        System.clearProperty('car.doors.number')

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
