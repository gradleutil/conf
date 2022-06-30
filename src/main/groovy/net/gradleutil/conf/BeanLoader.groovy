package net.gradleutil.conf

import com.typesafe.config.*
import com.typesafe.config.impl.SimpleConfig
import groovy.util.logging.Log
import net.gradleutil.conf.util.ConfUtil

import java.beans.BeanInfo
import java.beans.IntrospectionException
import java.beans.Introspector
import java.beans.PropertyDescriptor
import java.lang.reflect.*
import java.time.Duration

@Log(value = "log")
class BeanLoader {

    /**
     * Create instance of class set with config values
     * @param config
     * @param clazz
     * @param ignoreMissingProperties
     * @return
     */
    static <T> T create(Config config, Class<T> clazz, Loader.LoaderOptions options = Loader.defaultOptions()) {
        T bean = clazz.getDeclaredConstructor().newInstance()
        return create(bean,config,clazz,options)
    }

    /**
     * Create instance of class set with config values
     * @param config
     * @param clazz
     * @param ignoreMissingProperties
     * @return
     */
    static <T> T create(T bean, Config config, Class<T> clazz, Loader.LoaderOptions options = Loader.defaultOptions()) {
        try {
            List<PropertyDescriptor> beanProps = getPropertyDescriptors(getBeanInfo(clazz))
            for (PropertyDescriptor beanProp : beanProps) {
                Method setter = beanProp.getWriteMethod()
                Type parameterType = setter.getGenericParameterTypes()[0]
                Class<?> parameterClass = setter.getParameterTypes()[0]
                String configPropName = beanProp.getName()
                if (parameterClass.getName().contains("groovy.lang")) {
                    continue
                }
                // Is the property key or value missing in the config?
                Object unwrapped
                unwrapped = getValue(clazz, parameterType, parameterClass, config, configPropName, options)
                options.logger.info '🟢' + clazz.simpleName + '🔸' + beanProp.getName() + " = ${unwrapped}"
                if (configPropName == null || unwrapped == null) {
                    // If so, continue if the field is marked as @{link Optional}
                    if (isOptionalProperty(clazz, beanProp) || options.allowUnresolved) {
                        continue
                    }
                    // Otherwise, raise a {@link Missing} exception right here
                    throw new ConfigException.Missing(clazz.simpleName + '#' + beanProp.getName() + " = ${unwrapped} (keys: ${config.root().keySet()})")
                }
                setter.invoke(bean, unwrapped)
            }
            return bean
        } catch (InstantiationException e) {
            throw new ConfigException.BadBean(clazz.getName() + " needs a public no-args constructor to be used as a bean", e)
        } catch (IllegalAccessException e) {
            throw new ConfigException.BadBean(clazz.getName() + " getters and setters are not accessible, they must be for use as a bean", e)
        } catch (InvocationTargetException e) {
            throw new ConfigException.BadBean("Calling bean method on " + clazz.getName() + " caused an exception", e)
        }
    }

    static BeanInfo getBeanInfo(Class clazz) {
        BeanInfo beanInfo
        try {
            beanInfo = Introspector.getBeanInfo(clazz)
        } catch (IntrospectionException e) {
            throw new ConfigException.BadBean("Could not get bean information for class " + clazz.getName(), e)
        }
        beanInfo
    }

    static List<PropertyDescriptor> getPropertyDescriptors(BeanInfo beanInfo) {
        List<PropertyDescriptor> beanProps = new ArrayList<PropertyDescriptor>()
        for (PropertyDescriptor beanProp : beanInfo.getPropertyDescriptors()) {
            if (beanProp.getReadMethod() == null || beanProp.getWriteMethod() == null) {
                continue
            }
            beanProps.add(beanProp)
        }
        beanProps
    }

    private static Object getValue(Class<?> beanClass, Type parameterType, Class<?> parameterClass, Config config,
                                   String configPropName, Loader.LoaderOptions options) {
        if (!config.hasPath(configPropName)) {
            def identPropName = ConfUtil.ident(configPropName, true, false)
            if (config.hasPath(configPropName)) {
                return getValue(beanClass, parameterType, parameterClass, config, identPropName, options)
            }
            return null
        }
//        System.out.println("getting value:" + configPropName + "(" + parameterClass.getSimpleName() + ")");
//        System.out.println("config:" + config.getAnyRef(configPropName).toString());
        if (parameterClass == Boolean.class || parameterClass == boolean.class) {
            return config.getBoolean(configPropName)
        } else if (parameterClass == Integer.class || parameterClass == int.class) {
            return config.getInt(configPropName)
        } else if (parameterClass == Double.class || parameterClass == double.class) {
            return config.getDouble(configPropName)
        } else if (parameterClass == Long.class || parameterClass == long.class) {
            return config.getLong(configPropName)
        } else if (parameterClass == String.class) {
            return config.getString(configPropName)
        } else if (parameterClass == Duration.class) {
            return config.getDuration(configPropName)
        } else if (parameterClass == ConfigMemorySize.class) {
            return config.getMemorySize(configPropName)
        } else if (parameterClass == Object.class) {
            return config.getAnyRef(configPropName)
        } else if (parameterClass == List.class) {
            return getListValue(beanClass, parameterType, parameterClass, config, configPropName, options)
        } else if (parameterClass == Set.class) {
            return getSetValue(beanClass, parameterType, parameterClass, config, configPropName, options)
        } else if (parameterClass == Map.class) {
            // we could do better here, but right now we don't.
            Type[] typeArgs = ((ParameterizedType) parameterType).getActualTypeArguments()
            if (typeArgs[0] != String.class || typeArgs[1] != Object.class) {
                throw new ConfigException.BadBean("Bean property '" + configPropName + "' of class " + beanClass.getName() + " has unsupported Map<" + typeArgs[0] + "," + typeArgs[1] + ">, only Map<String,Object> is supported right now")
            }
            return config.getObject(configPropName).unwrapped()
        } else if (parameterClass == Config.class) {
            return config.getConfig(configPropName)
        } else if (parameterClass == ConfigObject.class) {
            return config.getObject(configPropName)
        } else if (parameterClass == ConfigValue.class) {
            return config.getValue(configPropName)
        } else if (parameterClass == ConfigList.class) {
            return config.getList(configPropName)
        } else if (parameterClass.isEnum()) {
            @SuppressWarnings("unchecked")
            Enum enumValue = getEnum(config, (Class<Enum>) parameterClass, configPropName)
            return enumValue
        } else if (hasAtLeastOneBeanProperty(parameterClass)) {
            return create(config.getConfig(configPropName), parameterClass as Class<Object>, options)
        } else {
            throw new ConfigException.BadBean("Bean property " + configPropName + " of class " + beanClass.getName() + " has unsupported type " + parameterType)
        }
    }

    private static Object getSetValue(Class<?> beanClass, Type parameterType, Class<?> parameterClass, Config config, String configPropName, Loader.LoaderOptions options) {
        return new HashSet((List) getListValue(beanClass, parameterType, parameterClass, config, configPropName, options))
    }

    private static Object getListValue(Class<?> beanClass, Type parameterType, Class<?> parameterClass, Config config, String configPropName, Loader.LoaderOptions options) {
        Type elementType = ((ParameterizedType) parameterType).getActualTypeArguments()[0]

        if (elementType == Boolean.class) {
            return config.getBooleanList(configPropName)
        } else if (elementType == Integer.class) {
            return config.getIntList(configPropName)
        } else if (elementType == Double.class) {
            return config.getDoubleList(configPropName)
        } else if (elementType == Long.class) {
            return config.getLongList(configPropName)
        } else if (elementType == String.class) {
            return config.getStringList(configPropName)
        } else if (elementType == Duration.class) {
            return config.getDurationList(configPropName)
        } else if (elementType == ConfigMemorySize.class) {
            return config.getMemorySizeList(configPropName)
        } else if (elementType == Object.class) {
            return config.getAnyRefList(configPropName)
        } else if (elementType == Config.class) {
            return config.getConfigList(configPropName)
        } else if (elementType == ConfigObject.class) {
            return config.getObjectList(configPropName)
        } else if (elementType == ConfigValue.class) {
            return config.getList(configPropName)
        } else if (((Class<?>) elementType).isEnum()) {
            @SuppressWarnings("unchecked")
            List<Enum> enumValues = config.getEnumList((Class<Enum>) elementType, configPropName)
            return enumValues
        } else if (hasAtLeastOneBeanProperty((Class<?>) elementType)) {
            List<Object> beanList = new ArrayList<Object>()
            if (config.hasPath(configPropName)) {
                List<? extends Config> configList = config.getConfigList(configPropName)
                for (Config listMember : configList) {
                    if (listMember.root().keySet().size() == 1 && listMember.root().keySet().first() == configPropName) {
                        beanList.add(create(listMember.getConfig(configPropName), elementType.class as Class<Object>, options))
                    } else {
                        beanList.add(create(listMember, elementType as Class<Object>, options))
                    }
                }
            }
            return beanList
        } else {
            throw new ConfigException.BadBean("Bean property '" + configPropName + "' of class " + beanClass.getName() + " has unsupported list element type " + elementType)
        }
    }

    // null if we can't easily say; this is heuristic/best-effort
    private static ConfigValueType getValueTypeOrNull(Class<?> parameterClass) {
        if (parameterClass == Boolean.class || parameterClass == boolean.class) {
            return ConfigValueType.BOOLEAN
        } else if (parameterClass == Integer.class || parameterClass == int.class) {
            return ConfigValueType.NUMBER
        } else if (parameterClass == Double.class || parameterClass == double.class) {
            return ConfigValueType.NUMBER
        } else if (parameterClass == Long.class || parameterClass == long.class) {
            return ConfigValueType.NUMBER
        } else if (parameterClass == String.class) {
            return ConfigValueType.STRING
        } else if (parameterClass == Duration.class) {
            return null
        } else if (parameterClass == ConfigMemorySize.class) {
            return null
        } else if (parameterClass == List.class) {
            return ConfigValueType.LIST
        } else if (parameterClass == Map.class) {
            return ConfigValueType.OBJECT
        } else if (parameterClass == Config.class) {
            return ConfigValueType.OBJECT
        } else if (parameterClass == ConfigObject.class) {
            return ConfigValueType.OBJECT
        } else if (parameterClass == ConfigList.class) {
            return ConfigValueType.LIST
        } else {
            return null
        }
    }

    private static boolean hasAtLeastOneBeanProperty(Class<?> clazz) {
        BeanInfo beanInfo
        try {
            beanInfo = Introspector.getBeanInfo(clazz)
        } catch (IntrospectionException ignored) {
            return false
        }

        for (PropertyDescriptor beanProp : beanInfo.getPropertyDescriptors()) {
            if (beanProp.getReadMethod() != null && beanProp.getWriteMethod() != null) {
                return true
            }
        }

        return false
    }

    private static boolean isOptionalProperty(Class beanClass, PropertyDescriptor beanProp) {
        Boolean optional
        Field field = getField(beanClass, beanProp.getName())
        optional = field.getAnnotations().any { it.annotationType().simpleName.endsWith('Optional') }
        if (optional || beanProp.name == 'hash') {
            return true
        }
        return field != null ? field.getAnnotationsByType(Optional.class).length > 0 : beanProp.getReadMethod().getAnnotationsByType(Optional.class).length > 0
    }

    private static Field getField(Class beanClass, String fieldName) {
        try {
            Field field = beanClass.getDeclaredField(fieldName)
            field.setAccessible(true)
            return field
        } catch (NoSuchFieldException e) {
            // Don't give up yet. Try to look for field in super class, if any.
        }
        beanClass = beanClass.getSuperclass()
        if (beanClass == null) {
            return null
        }
        return getField(beanClass, fieldName)
    }

    static <T extends Enum<T>> T getEnum(SimpleConfig config, Class<T> enumClass, String path) {
        ConfigValue v = config.find(path, ConfigValueType.STRING);
        return getEnumValue(path, enumClass, v);
    }

    private static <T extends Enum<T>> T getEnumValue(String path, Class<T> enumClass, ConfigValue enumConfigValue) {
        // escape strings for enums to avoid invalid java naming conventions
        String enumName = toEnumValue(enumConfigValue.unwrapped() as String)
        try {
            return Enum.valueOf(enumClass, enumName);
        } catch (IllegalArgumentException e) {
            List<String> enumNames = new ArrayList<String>();
            Enum[] enumConstants = enumClass.getEnumConstants();
            if (enumConstants != null) {
                for (Enum enumConstant : enumConstants) {
                    enumNames.add(enumConstant.name());
                }
            }
            throw new ConfigException.BadValue(
                    enumConfigValue.origin(), path,
                    String.format("The enum class %s has no constants of the name '%s' (should be one of %s.)",
                            enumClass.getSimpleName(), enumName, enumNames));
        }
    }

    static String toEnumValue(String string) {
        string.replaceAll("[^A-Za-z0-9]+", '_').toUpperCase().with {
            if (Character.isDigit(string.charAt(0))) {
                'V' + it
            } else {
                it
            }
        }
    }

}
