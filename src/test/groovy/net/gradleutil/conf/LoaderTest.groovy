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
        def conf = new File(base, 'config.conf').tap { text = 'one=1\ntwo=2\nthree=3\n' }
        def confOverride = new File(base, 'config.override.conf').tap { text = 'two=dos\nthree=tres\n' }

        when:
        def config = Loader.loadWithOverride('config.conf', conf, confOverride)
        ConfUtil.configToJson(config)

        then:
        config.getString('two') == 'dos'
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
