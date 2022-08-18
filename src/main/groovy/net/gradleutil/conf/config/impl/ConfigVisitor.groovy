package net.gradleutil.conf.config.impl

import com.typesafe.config.*
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FirstParam

@CompileStatic
abstract class ConfigVisitor {

	Stack<String> entryStack = []

	String getStackPath() {
		entryStack.join('.')
	}

	def pushPop(Map.Entry<String, ConfigValue> entry, @ClosureParams(FirstParam) final Closure closure) {
		entryStack.push(entry.key)
		closure.delegate = entry
		closure.call(entry)
		entryStack.pop()
	}

	void visit(Config config) {
		config.entrySet().each {
			pushPop(it) {
				visit(it)
			}
		}
	}

	void visit(Map.Entry<String, ConfigValue> entry) {
		def elementType = entry.value.valueType()
		if (elementType == ConfigValueType.BOOLEAN) {
			callVisitBoolean(entry)
		} else if (elementType == ConfigValueType.NUMBER) {
			callVisitNumber(entry)
		} else if (elementType == ConfigValueType.STRING) {
			callVisitString(entry)
		} else if (elementType == ConfigValueType.LIST) {
			callVisitList(entry)
		} else if (elementType == ConfigValueType.OBJECT) {
			callVisitObject(entry)
		} else if (elementType == ConfigValueType.NULL) {
			callVisitNull(entry)
		} else {
			throw new Exception("Don't know how to handle ${elementType}")
		}
	}

	void callVisitObject(Map.Entry<String, ConfigValue> entry) {
		visitObject(entry.key, entry.value as ConfigObject)
	}

	void callVisitConfigList(ConfigList configList) {
		if (!configList) return
		configList.each {
			if (it instanceof ConfigList) {
				callVisitConfigList(it as ConfigList)
			} else if (it instanceof ConfigObject) {
				visit((it as ConfigObject).toConfig())
			} else {
				visitString(entryStack.peek(), it as ConfigValue)
			}
		}
	}

	void callVisitList(Map.Entry<String, ConfigValue> entry) {
		visitList(entry.key, entry.value as ConfigList)
		callVisitConfigList(entry.value as ConfigList)
	}

	void callVisitString(Map.Entry<String, ConfigValue> entry) {
		visitString(entry.key, entry.value)
	}

	void callVisitNumber(Map.Entry<String, ConfigValue> entry) {
		visitNumber(entry.key, entry.value)
	}

	void callVisitNull(Map.Entry<String, ConfigValue> entry) {
		visitNull(entry.key, entry.value)
	}

	void callVisitBoolean(Map.Entry<String, ConfigValue> entry) {
		visitBoolean(entry.key, entry.value)
	}


	void visitObject(String key, ConfigObject configObject) {}

	void visitList(String key, ConfigList configList) {}

	void visitString(String key, ConfigValue configValue) {}

	void visitNumber(String key, ConfigValue configValue) {}

	void visitNull(String key, ConfigValue configValue) {}

	void visitBoolean(String key, ConfigValue configValue) {}

}
