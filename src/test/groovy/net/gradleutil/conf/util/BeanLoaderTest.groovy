package net.gradleutil.conf.util

import com.typesafe.config.Config
import net.gradleutil.conf.AbstractTest
import net.gradleutil.conf.BeanConfigLoader
import net.gradleutil.conf.Loader
import net.gradleutil.conf.bean.Manytyped
import net.gradleutil.conf.bean.MinecraftConfig
import net.gradleutil.conf.bean.Royalty
import net.gradleutil.conf.bean.server.Server

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
        println "loading file://${configFile.absolutePath}"
        //Config conf = Load.load(configFile);
        BeanConfigLoader.setBeanFromConfig(manytyped, Loader.loaderOptions().conf(configFile))
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

    def "test mc java bean"() {
        setup:
        def configFile = new File('src/test/resources/json/MinecraftConfig.json')

        when:
        println "loading file://${configFile.absolutePath}"
        Config conf = Loader.load(configFile)

        MinecraftConfig serverOptions = Loader.create(conf, MinecraftConfig.class)

        then:
        serverOptions.minecrafts.size() > 0

    }

    def "test royal java bean"() {
        setup:
        def configFile = new File('src/test/resources/json/royalty.json')

        when:
        println "loading file://${configFile.absolutePath}"
        Config conf = Loader.load(configFile)
        Royalty royalty = new Royalty();

        Royalty serverOptions = Loader.create(conf, Royalty.class)

        then:
        serverOptions.children.size() > 0

    }

    def "test soyal java bean"() {
        setup:
        def configFile = new File('src/test/resources/json/server.json')

        when:
        println "loading file://${configFile.absolutePath}"
        Server royalty = new Server();
        Config conf = Loader.load(new File("src/test/resources/json/server.json"), new File("src/test/resources/json/server.schema.json"));
        Server server = new Server();
        Server serverOptions = Loader.create(conf, Server.class, Loader.loaderOptions().config(conf).silent(false));

        then:
        serverOptions.trayOptions.size() > 0

    }


}
