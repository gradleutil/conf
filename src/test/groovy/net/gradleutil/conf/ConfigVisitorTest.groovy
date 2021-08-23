package net.gradleutil.conf

import com.typesafe.config.ConfigList
import com.typesafe.config.ConfigObject
import com.typesafe.config.ConfigValue
import net.gradleutil.conf.config.impl.ConfigVisitor

class ConfigVisitorTest extends AbstractTest {

    def "print all the things"() {
        setup:
        def configFile = new File('src/test/resources/conf/manytyped.conf')
        def config = Loader.resolveWithSystem(configFile)
        def visitor = new ConfigVisitor() {
            @Override
            void visitObject(String key, ConfigObject configValue) {
                println("visitObject ${key} : ${configValue.valueType()}")
                visit(configValue)
            }

            @Override
            void visitList(String key, ConfigList list) {
                println("visitList ${key} : ${list.valueType()}")
                visit(list)
            }

            @Override
            void visitString(String key, ConfigValue configValue) {
                println("visit ${key} : ${configValue.valueType()}")
                visit(configValue)
            }

            @Override
            void visitNumber(String key, ConfigValue configValue) {
                println("visit ${key} : ${configValue.valueType()}")
                visit(configValue)
            }

            @Override
            void visitNull(String key, ConfigValue configValue) {
                println("visit ${key} : ${configValue.valueType()}")
                visit(configValue)
            }

            @Override
            void visitBoolean(String key, ConfigValue configValue) {
                println("visit ${key} : ${configValue.valueType()}")
                visit(configValue)
            }

        }

        when:
        visitor.visit(config)

        then:
        true
    }

}
