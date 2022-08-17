package net.gradleutil.conf.util

import net.gradleutil.conf.AbstractTest
import net.gradleutil.conf.Loader

class ConfUtilTest extends AbstractTest {

    def "test json print simple conf"() {
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
            """.stripIndent()
        }

        when:
        def config = Loader.load(conf)

        then:
        def car = Loader.load(ConfUtil.configToJson(config, 'car'))
        car.getConfig('engine').getString('brand') == 'sterling'

        def engine = Loader.load(ConfUtil.configToJson(config, 'car.engine'))
        engine.getString('brand') == 'sterling'
    }


    def "test json print json"() {
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
            """.stripIndent()
        }

        when:
        def config = Loader.load(conf)
        def carConfig = Loader.create(config, Car, Loader.defaultOptions().silent(false))
        def carJsonString = Loader.create(conf.text, Car, Loader.defaultOptions().silent(false))

        then:
        carConfig.engine.brand == 'sterling'

        carJsonString.engine.brand == 'sterling'

    }

    def "test replace json refs"() {
        setup:
        def conf = new File(base, 'config.conf').tap {
            text = '''
            {
                "car": {
                    "engine": {
                        "type": "big",
                        "brand": "sterling"
                        "size": "${"car"."doors"."number"}"
                        "moreDoors": "${car.doors.number}"
                        "inString": "in ${car.doors.number} ling"
                    },
                    "doors": {
                        "number": 4
                    }
                }
            }
            '''.stripIndent()
        }

        when:
        def config = Loader.load(conf)

        then:
        println config.toString()
        println ConfUtil.configToJson(config).toString()
        config.getConfig("car").getConfig('engine').getInt('size') == 4
        config.getConfig("car").getConfig('engine').getInt('moreDoors') == 4

    }

    def "test simple substitution"() {
        setup:
        def conf = new File(base, 'config.conf').tap {
            text = 
            '''
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
            text = 
            '''
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


    static class House {
        Garage garage
        Car car
    }

    static class Garage {
        String carEngineType
        String color
        String carColor
    }

    static class Car {
        Engine engine
        Doors doors
    }

    static class Engine {
        String type
        String brand
    }

    static class Doors {
        Integer number
    }

}
