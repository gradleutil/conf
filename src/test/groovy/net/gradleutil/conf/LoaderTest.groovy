package net.gradleutil.conf

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


}
