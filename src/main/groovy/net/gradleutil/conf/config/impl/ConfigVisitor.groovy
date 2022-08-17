package net.gradleutil.conf.config.impl

import com.typesafe.config.*
import groovy.transform.CompileStatic

@CompileStatic
abstract class ConfigVisitor {

    void visit(Config config) {
        config.entrySet().each {
            visit(it.key, it.value)
        }
    }

    void visit(String key, ConfigValue configValue) {
        def elementType = configValue.valueType()
        if (elementType == ConfigValueType.BOOLEAN) {
            callVisitBoolean(key,configValue)
        } else if (elementType == ConfigValueType.NUMBER) {
            callVisitNumber(key,configValue)
        } else if (elementType == ConfigValueType.STRING) {
            callVisitString(key,configValue)
        } else if (elementType == ConfigValueType.LIST) {
            callVisitList(key,configValue as ConfigList)
        } else if (elementType == ConfigValueType.OBJECT) {
            callVisitObject(key,configValue as ConfigObject)
        } else if (elementType == ConfigValueType.NULL) {
            callVisitNull(key,configValue)
        }
    }

    void callVisitObject(String key, ConfigObject configObject) {
        visitObject(key, configObject)
    }

    void callVisitList(String key, ConfigList configList) {
        visitList(key,configList)
        configList.each { visit(key, it) }
    }

    void callVisitString(String key, ConfigValue configValue) {
        visitString(key,configValue)
    }

    void callVisitNumber(String key, ConfigValue configValue) {
        visitNumber(key,configValue)
    }

    void callVisitNull(String key, ConfigValue configValue) {
        visitNull(key,configValue)
    }

    void callVisitBoolean(String key, ConfigValue configValue) {
        visitBoolean(key,configValue)
    }


    void visitObject(String key, ConfigObject configObject) {
    }

    void visitList(String key, ConfigList configList) {
    }

    void visitString(String key, ConfigValue configValue) {
    }

    void visitNumber(String key, ConfigValue configValue) {
    }

    void visitNull(String key, ConfigValue configValue) {
    }

    void visitBoolean(String key, ConfigValue configValue) {
    }

}
