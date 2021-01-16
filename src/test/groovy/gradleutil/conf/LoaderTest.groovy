package gradleutil.conf

import spock.lang.Specification

class LoaderTest extends Specification {

    def "test loader"() {
        setup:
        def funk = Loader.create(new File('src/test/resources/json/veggies.json').text, gradleutil.conf.genned.Veggies)
        println funk.vegetables
    }

/*
    def "test dsl"() {
        setup:
        def configModelFile = new File('src/test/groovy/gradleutil/generated/JavaClass.groovy')
        def gen = new Gen()

        when:
        def result = gradleutil.generated.DSL.javaClass{
            name = 'fart'
        }

        then:
        result == true
    }
*/
}
