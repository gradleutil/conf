package net.gradleutil.conf

import com.typesafe.config.ConfigList
import com.typesafe.config.ConfigObject
import com.typesafe.config.ConfigRenderOptions
import com.typesafe.config.ConfigValue
import net.gradleutil.conf.config.impl.ConfigVisitor

class ConfigVisitorTest extends AbstractTest {

    def "visit list"() {
        setup:
        def aSimpleStringList = ["alice", "bob", "lucy"]
        def listStr = "aSimpleStringList = ${aSimpleStringList.toString()}"
        def config = Loader.resolveStringWithSystem(listStr)
        def values = []
        def visitor = new ConfigVisitor() {
            @Override
            void visitString(String path, ConfigValue configValue) {
                println("visit  : ${configValue.valueType()} = ${configValue.render(ConfigRenderOptions.concise())}")
                values.add(configValue.render())
            }
        }

        when:
        visitor.visit(config)

        then:
        values.size() == 3
    }


    def "print all the things"() {
        setup:
        def configFile = new File('src/test/resources/conf/manytyped.conf')
        def config = Loader.resolveWithSystem(configFile)
        def visitor = new ConfigVisitor() {
            @Override
            void visitObject(String path, ConfigObject configValue) {
                println("visitObject  : ${path} ${configValue.valueType()} = ${configValue.render(ConfigRenderOptions.concise())}")
            }

            @Override
            void visitList(String path, ConfigList list) {
                println("visitList  : ${path} ${list.valueType()} = ${list.render(ConfigRenderOptions.concise())}")
            }

            @Override
            void visitString(String path, ConfigValue configValue) {
                println("visit  : ${path} ${configValue.valueType()} = ${configValue.render(ConfigRenderOptions.concise())}")
            }

            @Override
            void visitNumber(String path, ConfigValue configValue) {
                println("visit  : ${path} ${configValue.valueType()} = ${configValue.render()}")
            }

            @Override
            void visitNull(String path, ConfigValue configValue) {
                println("visit  : ${path} ${configValue.valueType()} = ${configValue.render()}")
            }

            @Override
            void visitBoolean(String path, ConfigValue configValue) {
                println("visit : ${path} ${configValue.valueType()} = ${configValue.render()}")
            }

        }

        when:
        visitor.visit(config)

        then: true
    }

}
