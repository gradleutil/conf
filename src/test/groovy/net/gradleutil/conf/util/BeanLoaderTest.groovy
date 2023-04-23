package net.gradleutil.conf.util

import net.gradleutil.conf.AbstractTest
import net.gradleutil.conf.bean.Manytyped
import net.gradleutil.conf.bean.MinecraftConfig

class BeanLoaderTest extends AbstractTest {

    def "test bean"() {
        setup:
        def configFile = new File('src/test/resources/conf/manytyped.conf')

        when:
        Manytyped manytyped = new Manytyped()
/*
        def options = Loader.defaultOptions()
                .allowUnresolved(false)
                .silent(false)
                .confString(configFile.text)
                .useSystemProperties(false)

        BeanConfigLoader.setBeanFromConfig(manytyped, options)
*/

        ConfUtil.setBeanFromConf(manytyped, configFile.text )
//        ConfUtil.setBeanFromConfigFile(manytyped, configFile, configFile )

        then:
        manytyped.get_1funkyProperty() == 'funky1'
        manytyped.getTasks().size() == 1
        manytyped.aSimpleStringList.size() == 2
    }

    def "test mc bean"() {
        setup:
        def configFile = new File('src/test/resources/json/MinecraftConfig.json')

        when:
        MinecraftConfig minecraftConfig = new MinecraftConfig(configFile.text)


        then:
        minecraftConfig.minecrafts.size() > 0

    }


}
