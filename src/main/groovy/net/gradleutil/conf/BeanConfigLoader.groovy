package net.gradleutil.conf

import com.typesafe.config.Config
import com.typesafe.config.ConfigObject
import com.typesafe.config.ConfigValue
import com.typesafe.config.ConfigValueType

import java.beans.PropertyDescriptor

import static net.gradleutil.conf.BeanLoader.getBeanInfo
import static net.gradleutil.conf.BeanLoader.getPropertyDescriptors
import static net.gradleutil.conf.util.ConfUtil.ident

class BeanConfigLoader {

    static <T> T get(Config config, String className, ClassLoader classLoader, Boolean ignoreMissingProperties = false) {
        def bean = create(Loader.defaultOptions().config(config).className(className).classLoader(classLoader).allowUnresolved(ignoreMissingProperties))
        return bean as T
    }

    static <T> T create(Loader.LoaderOptions options) {
        Class clazz = getClassForKey(options.className, options)
        T bean = clazz.getDeclaredConstructor().newInstance() as T
        setBeanFromConfig(bean, options)
        return bean
    }

    static void setBeanFromConfig(Object bean, Loader.LoaderOptions options) {
        List<PropertyDescriptor> beanProps = getPropertyDescriptors(getBeanInfo(bean.class))
        if (!options.config) {
            options.config = Loader.load(options)
        }
        def packageName = bean.class.package.name
        List<Map<String, ConfigValue>> keyValueMap = toList(bean, options.config)
        options.classLoader(options.classLoader ?: bean.class.classLoader)
        keyValueMap*.each { key, configValue ->
            def id = ident(key, true, true, options.singularizeClasses)
            def setter = getSetter(beanProps, key, id)
            def value = getValue(configValue, key, packageName + '.' + id, beanProps, options.classLoader, options.allowUnresolved)
            if (setter && value != null) {
                def firstParam = setter.getWriteMethod().parameters.first()
                try {
                    if(firstParam.type?.superclass?.simpleName == 'Enum' && value instanceof String){
                        def consts = Class.forName(firstParam.type.name).getEnumConstants()
                        def eVal = consts.find{it.toString() == value }
                        setter.getWriteMethod().invoke(bean, eVal)
                    } else if (firstParam.type.simpleName == 'Long') {
                        setter.getWriteMethod().invoke(bean, value as Long)
                    } else {
                        setter.getWriteMethod().invoke(bean, value)
                    }
                } catch (Exception e) {
                    throw new Exception("Unable to set ${key}=${value}", e)
                }
            }
        }
    }

    static List<Map<String, ConfigValue>> toList(Object bean, Config config) {
        List<Map<String, ConfigValue>> keyValueMap = config.root().collect { [(it.key): it.value] }
        if (keyValueMap*.keySet().size() == 1) {
            def k = keyValueMap.first().keySet().first()
            if (k.equalsIgnoreCase bean.class.simpleName) {
                def key = config.root().get(k) as Map
                return key.collect { [(it.key): it.value] } as List<Map<String, ConfigValue>>
            }
        }
        return keyValueMap
    }

    static PropertyDescriptor getSetter(List<PropertyDescriptor> beanProps, String key, String id) {
        def setter = beanProps.find { it.getWriteMethod().name.toLowerCase() == 'set' + key.toLowerCase() }
        if (setter != null) {
            return setter
        }
        return beanProps.find { it.getWriteMethod().name.toLowerCase() == 'set' + id.toLowerCase() }
    }

    static Class getClassForKey(String name, Loader.LoaderOptions options) {
        def obClass
        def packageName = name.take(name.lastIndexOf('.'))
        def className = name.replace(packageName + '.', '')
        try {
            def ident = ident(className, true, true, options.singularizeClasses)
            obClass = options.classLoader.loadClass(packageName + '.' + ident)
        } catch (ClassNotFoundException ignore) {
            obClass = options.classLoader.loadClass(name)
        }
        obClass
    }

    private static Object getValue(ConfigValue configValue, String configPropName, String className, List<PropertyDescriptor> beanProps, ClassLoader classLoader, Boolean ignoreMissingProperties) {
        def value
        def identPropName = ident(configPropName, true, false, false).toLowerCase()
        def setter = beanProps.find { it.getWriteMethod().name.toLowerCase() == 'set' + identPropName }
        value = null
        if (!setter) {
            if (!ignoreMissingProperties) {
                def message = """Missing set${identPropName} for ${className}#${configPropName} (${beanProps*.name})\n${configValue}
"""
                throw new InstantiationException(message)
            }
        } else {
            if (configValue.valueType() == ConfigValueType.NULL) {
                value = null
            } else if (configValue.valueType() == ConfigValueType.OBJECT) {
                def con = configValue as ConfigObject
                value = get(con.toConfig(), className, classLoader, ignoreMissingProperties)
            } else if (configValue.valueType() == ConfigValueType.LIST) {
                value = configValue.collect {
                    if (it.valueType() == ConfigValueType.OBJECT) {
                        def configObject = it as ConfigObject
                        def packageName = className.take(className.lastIndexOf('.'))
                        def classSimpleName = className.replace(packageName + '.', '')
                        def firstKey = configObject.keySet().first()
                        if (configObject.keySet().size() == 1 && firstKey.toLowerCase() == classSimpleName.toLowerCase()) {
                            def conf = configObject.get(configObject.keySet().first()) as ConfigObject
                            return getValue(conf, configPropName, packageName + '.' + firstKey, beanProps, classLoader, ignoreMissingProperties)
                        }
                        def beanProp = beanProps.find{it.name == configPropName}
                        def beanReturnType = beanProp.readMethod.genericReturnType.typeName.replaceAll(/.*<(.*)>/,'$1')
                        return getValue(it, configPropName, beanReturnType, beanProps, classLoader, ignoreMissingProperties)

                    }
                    def beanProp = beanProps.find{it.name == configPropName}
                    def beanReturnType = beanProp.readMethod.genericReturnType.typeName.replaceAll(/.*<(.*)>/,'$1')
                    return getValue(it, configPropName, beanReturnType, beanProps, classLoader, ignoreMissingProperties)
                }
            } else if (configValue.valueType() == ConfigValueType.NUMBER) {
                def number = configValue.unwrapped()
                if (setter.propertyType == Long) {
                    value = number as Long
                } else {
                    value = number as Integer
                }
            } else {
                value = configValue.unwrapped()
            }

        }
        value
    }
}
