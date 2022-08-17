package net.gradleutil.conf.config.impl

import com.typesafe.config.*
import groovy.transform.CompileStatic

@CompileStatic
abstract class ConfigObjectVisitor {

    Stack<Map.Entry<String, ConfigValue>> entryStack = []

    void visit(Config config) {
        config.root().entrySet().each {
            entryStack.push(it)
            visit(it.value)
            entryStack.pop()
        }
    }

    void visit(ConfigValue configValue) {
        def elementType = configValue.valueType()
        if (elementType == ConfigValueType.BOOLEAN) {
            callVisitBoolean(configValue)
        } else if (elementType == ConfigValueType.NUMBER) {
            callVisitNumber(configValue)
        } else if (elementType == ConfigValueType.STRING) {
            callVisitString(configValue)
        } else if (elementType == ConfigValueType.LIST) {
            callVisitList(configValue as ConfigList)
        } else if (elementType == ConfigValueType.OBJECT) {
            callVisitObject(configValue as ConfigObject)
        } else if (elementType == ConfigValueType.NULL) {
            callVisitNull(configValue)
        }
    }

    void callVisitObject(ConfigObject configObject) {
        visitObject(configObject)
        visit(configObject.toConfig())
    }

    void callVisitList(ConfigList configList) {
        visitList(configList)
        configList.each { visit(it) }
    }

    void callVisitString(ConfigValue configValue) {
        visitString(configValue)
    }

    void callVisitNumber(ConfigValue configValue) {
        visitNumber(configValue)
    }

    void callVisitNull(ConfigValue configValue) {
        visitNull(configValue)
    }

    void callVisitBoolean(ConfigValue configValue) {
        visitBoolean(configValue)
    }


    void visitObject(ConfigObject configObject) {
    }

    void visitList(ConfigList configList) {
    }

    void visitString(ConfigValue configValue) {
    }

    void visitNumber(ConfigValue configValue) {
    }

    void visitNull(ConfigValue configValue) {
    }

    void visitBoolean(ConfigValue configValue) {
    }


}
