package net.gradleutil.conf

import com.typesafe.config.*

import java.beans.BeanInfo
import java.beans.IntrospectionException
import java.beans.Introspector
import java.beans.PropertyDescriptor
import java.lang.reflect.*
import java.time.Duration

class BeanLoader {

    /**
     * @param < T >     type of the bean
     * @param config config to use
     * @param clazz class of the bean
     * @return the bean instance
     */
    static <T> T create(Config config, Class<T> clazz) {
        BeanInfo beanInfo
        try {
            beanInfo = Introspector.getBeanInfo(clazz)
        } catch (IntrospectionException e) {
            throw new ConfigException.BadBean("Could not get bean information for class " + clazz.getName(), e)
        }

        try {
            List<PropertyDescriptor> beanProps = new ArrayList<PropertyDescriptor>()
            for (PropertyDescriptor beanProp : beanInfo.getPropertyDescriptors()) {
                if (beanProp.getReadMethod() == null || beanProp.getWriteMethod() == null) {
                    continue
                }
                beanProps.add(beanProp)
            }

            T bean = clazz.newInstance()
            for (PropertyDescriptor beanProp : beanProps) {
                Method setter = beanProp.getWriteMethod()
                Type parameterType = setter.getGenericParameterTypes()[0]
                Class<?> parameterClass = setter.getParameterTypes()[0]
                String configPropName = beanProp.getName()
                if (parameterClass.getName().contains("groovy.lang")) {
                    continue
                }
                // Is the property key or value missing in the config?
                Object unwrapped = getValue(clazz, parameterType, parameterClass, config, configPropName)
                if (configPropName == null || unwrapped == null) {
                    // If so, continue if the field is marked as @{link Optional}
                    if (isOptionalProperty(clazz, beanProp)) {
                        continue
                    }
                    // Otherwise, raise a {@link Missing} exception right here
                    throw new ConfigException.Missing(beanProp.getName())
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

    private static Object getValue(Class<?> beanClass, Type parameterType, Class<?> parameterClass, Config config,
                                   String configPropName) {
        if (!config.hasPath(configPropName)) {
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
            return getListValue(beanClass, parameterType, parameterClass, config, configPropName)
        } else if (parameterClass == Set.class) {
            return getSetValue(beanClass, parameterType, parameterClass, config, configPropName)
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
            Enum enumValue = config.getEnum((Class<Enum>) parameterClass, configPropName)
            return enumValue
        } else if (hasAtLeastOneBeanProperty(parameterClass)) {
            return create(config.getConfig(configPropName), parameterClass as Class<Object>)
        } else {
            throw new ConfigException.BadBean("Bean property " + configPropName + " of class " + beanClass.getName() + " has unsupported type " + parameterType)
        }
    }

    private static Object getSetValue(Class<?> beanClass, Type parameterType, Class<?> parameterClass, Config config, String configPropName) {
        return new HashSet((List) getListValue(beanClass, parameterType, parameterClass, config, configPropName))
    }

    private static Object getListValue(Class<?> beanClass, Type parameterType, Class<?> parameterClass, Config config, String configPropName) {
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
                    beanList.add(create(listMember, (Class<?>) elementType))
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
        } catch (IntrospectionException e) {
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
        Field field = getField(beanClass, beanProp.getName())
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
}
