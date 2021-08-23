package net.gradleutil.conf.config.impl

import com.typesafe.config.Config
import com.typesafe.config.ConfigList
import com.typesafe.config.ConfigObject
import com.typesafe.config.ConfigValue
import groovy.transform.CompileStatic

@CompileStatic
abstract class ConfigVisitor {

    Stack<Map.Entry<String,ConfigValue>> entryStack = []
    
    void visit(Config config) {
        config.root().entrySet().each {
            entryStack.push(it)
            visit(it.key, it.value)
            entryStack.pop()
        }
    }

    void visit(String key, ConfigValue configValue){
        def objectClass = configValue.valueType().toString().toLowerCase().capitalize()
        invokeMethod("visit${objectClass}",[key,configValue])
    }

    void visit(ConfigValue configValue) {
        if(configValue instanceof ConfigObject){
            configValue.entrySet().each {
                visit(it.key, it.value)
/*
                def parentKey = entryStack.peek().key
                entryStack.push(it)
                visit(parentKey + '.' + it.key, it.value)
                entryStack.pop()
*/
            }
        }
        
    }

    void visitObject(String key, ConfigObject configObject) {
        visit(configObject)
    }

    void visitList(String key, ConfigList configList) {
        configList.each{visit(it) }
    }

    void visitString(String key, ConfigValue configValue) {
        visit(configValue)
    }

    void visitNumber(String key, ConfigValue configValue) {
        visit(configValue)
    }

    void visitNull(String key, ConfigValue configValue) {
        visit(configValue)
    }

    void visitBoolean(String key, ConfigValue configValue) {
        visit(configValue)
    }


}
