package net.gradleutil.conf

import com.typesafe.config.ConfigList
import com.typesafe.config.ConfigObject
import com.typesafe.config.ConfigRenderOptions
import com.typesafe.config.ConfigValue
import net.gradleutil.conf.config.impl.ConfigObjectVisitor

class ConfigObjectVisitorTest extends AbstractTest {

    def "visit list"() {
        setup:
        def aSimpleStringList = ["alice", "bob", "lucy"]
        def listStr = "aSimpleStringList = ${aSimpleStringList.toString()}"
        def config = Loader.resolveStringWithSystem(listStr)
        def values = []
        def visitor = new ConfigObjectVisitor() {
            @Override
            void visitString(ConfigValue configValue) {
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
        def visitor = new ConfigObjectVisitor() {

            @Override
            void visitObject(ConfigObject configValue) {
                println("visitObject  : ${configValue.valueType()} = ${configValue.render(ConfigRenderOptions.concise())}")
            }

            @Override
            void visitList(ConfigList list) {
                println("visitList  : ${list.valueType()} = ${list.render(ConfigRenderOptions.concise())}")
            }

            @Override
            void visitString(ConfigValue configValue) {
                println("visit  : ${configValue.valueType()} = ${configValue.render(ConfigRenderOptions.concise())}")
            }

            @Override
            void visitNumber(ConfigValue configValue) {
                println("visit  : ${configValue.valueType()} = ${configValue.render()}")
            }

            @Override
            void visitNull(ConfigValue configValue) {
                println("visit  : ${configValue.valueType()} = ${configValue.render()}")
            }

            @Override
            void visitBoolean(ConfigValue configValue) {
                println("visit : ${configValue.valueType()} = ${configValue.render()}")
            }

        }

        when:
        visitor.visit(config)

        then:
        true
    }

}
