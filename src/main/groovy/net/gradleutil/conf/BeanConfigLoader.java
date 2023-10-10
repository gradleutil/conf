package net.gradleutil.conf;

import java.beans.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import com.typesafe.config.*;

import static net.gradleutil.conf.util.ConfUtil.ident;

public class BeanConfigLoader {

    public static <T> T get(Config config, String className, ClassLoader classLoader, Boolean ignoreMissingProperties) throws Exception {
        T bean = create(Loader.loaderOptions().config(config).className(className).classLoader(classLoader).allowUnresolved(ignoreMissingProperties));
        Class<?> expectedClass = classLoader.loadClass(className);
        if (bean.getClass().isAssignableFrom(expectedClass)) {
            return bean;
        } else {
            throw new ClassCastException("Expected " + expectedClass.getSimpleName() + ", but got " + bean.getClass().getSimpleName());
        }
    }

    public static <T> T get(Config config, String className, ClassLoader classLoader) throws Exception {
        return get(config, className, classLoader, false);
    }

    public static <T> T create(LoaderOptions options) throws RuntimeException {
        Class<?> clazz = getClassForKey(options.className, options);
        try {
            //noinspection unchecked
            T bean = (T) clazz.getDeclaredConstructor().newInstance();
            setBeanFromConfig(bean, options);
            return bean;
        } catch (Exception e) {
            throw new RuntimeException("Unable to create instance of " + clazz.getName(), e);
        }
    }

    public static void setBeanFromConfig(final Object bean, final LoaderOptions options) throws Exception {
        if (options.config == null) {
            options.config = Loader.load(options);
        }
        String packageName = bean.getClass().getPackage().getName();
        List<Map<String, ConfigValue>> keyValueMap = toList(bean, options.config);
        options.classLoader(options.classLoader != null ? options.classLoader : bean.getClass().getClassLoader());
        for (Map<String, ConfigValue> map : keyValueMap) {
            for (Map.Entry<String, ConfigValue> entry : map.entrySet()) {
                String key = entry.getKey();
                if(key.equals("$schema")) {
                    continue;
                }
                String id = ident(key, true, true, options.singularizeClasses);
                final List<Method> setters = getSetters(getBeanInfo(bean.getClass()));
                Method setter = getSetter(setters, key, id);
                if (setter == null) {
                    List<String> keys = setters.stream().map(Method::getName).collect(Collectors.toList());
                    throw new Exception("Unable to find setter for key " + key + " or ident " + id + " out of " + keys);
                }
                Parameter firstParam = setter.getParameters()[0];
                ConfigValue configValue = entry.getValue();
                Object value = getValue(configValue, key, packageName + '.' + id, setters, options.classLoader, options.allowUnresolved);
                if (value != null) {
                    try {
                        Class<?> superclass = firstParam.getType().getSuperclass();
                        if (superclass != null && superclass.getSimpleName().equals("Enum") && value instanceof String) {
                            Object[] consts = firstParam.getType().getEnumConstants();
                            Object eVal = Arrays.stream(consts).filter(c -> c.toString().equals(value)).findFirst().orElse(null);
                            setter.invoke(bean, eVal);
                        } else if (firstParam.getType().getSimpleName().equals("BigInteger")) {
                            setter.invoke(bean, BigInteger.valueOf(Long.parseLong(value.toString())));
                        } else if (firstParam.getType().getSimpleName().equals("Integer")) {
                            setter.invoke(bean, Integer.parseInt(value.toString()));
                        } else if (firstParam.getType().getSimpleName().equals("Long")) {
                            setter.invoke(bean, Long.parseLong(value.toString()));
                        } else if (firstParam.getType().getSimpleName().equals("String")) {
                            setter.invoke(bean, value.toString());
                        } else {
                            setter.invoke(bean, value);
                        }
                    } catch (Exception e) {
                        throw new Exception("Unable to set " + key + "=" + value, e);
                    }
                }
            }
        }
    }

    public static List<Map<String, ConfigValue>> toList(Object bean, Config config) {
        List<Map<String, ConfigValue>> keyValueMap = new ArrayList<>();
        for (Map.Entry<String, ConfigValue> entry : config.root().entrySet()) {
            Map<String, ConfigValue> map = new HashMap<>();
            map.put(entry.getKey(), entry.getValue());
            keyValueMap.add(map);
        }
        if (keyValueMap.stream().map(Map::keySet).distinct().count() == 1) {
            String k = keyValueMap.get(0).keySet().iterator().next();
            if (k.equalsIgnoreCase(bean.getClass().getSimpleName())) {
                Map<String, ConfigValue> key = (ConfigObject) config.root().get(k);
                List<Map<String, ConfigValue>> result = new ArrayList<>();
                for (Map.Entry<String, ConfigValue> entry : key.entrySet()) {
                    Map<String, ConfigValue> map = new HashMap<>();
                    map.put(entry.getKey(), entry.getValue());
                    result.add(map);
                }
                return result;
            }
        }
        return keyValueMap;
    }

    public static Method getSetter(List<Method> methods, String key, String id) {
        final String[] names = {key, id, "set" + key, "set" + id};
        return methods.stream()
                .filter(prop -> prop.getParameterCount() > 0)
                .filter(prop -> Arrays.stream(names).anyMatch(prop.getName()::equalsIgnoreCase))
                .findFirst()
                .orElse(null);
    }

    public static Class<?> getClassForKey(String name, LoaderOptions options) {
        Class<?> obClass;
        String packageName = name.substring(0, name.lastIndexOf('.'));
        String className = name.replace(packageName + ".", "");
        try {
            String ident = ident(className, true, true, options.singularizeClasses);
            obClass = options.classLoader.loadClass(packageName + "." + ident);
        } catch (ClassNotFoundException ignore) {
            try {
                obClass = options.classLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Unable to load class " + name, e);
            }
        }
        return obClass;
    }

    private static Object getValue(ConfigValue configValue, String configPropName, String className, List<Method> setters, ClassLoader classLoader, Boolean ignoreMissingProperties) throws Exception {
        Object value;
        String identPropName = ident(configPropName, true, false, false).toLowerCase();
        Method setter = getSetter(setters, configPropName, identPropName);
        value = null;
        if (setter == null) {
            if (!ignoreMissingProperties) {
                String message = String.format("Missing set%s for %s#%s (%s)\n%s", identPropName, className, configPropName, setters.stream().map(Method::getName).collect(Collectors.joining(", ")), configValue);
                throw new InstantiationException(message);
            }
        } else {
            if (configValue.valueType() == ConfigValueType.NULL) {
                return null;
            } else if (configValue.valueType() == ConfigValueType.LIST) {
                List<Object> list = new ArrayList<>();
                //noinspection unchecked
                for (ConfigValue item : (List<ConfigValue>) configValue) {
                    if (item.valueType() == ConfigValueType.OBJECT) {
                        ConfigObject configObject = (ConfigObject) item;
                        String packageName = className.substring(0, className.lastIndexOf('.'));
                        String classSimpleName = className.replace(packageName + ".", "");
                        String firstKey = configObject.keySet().iterator().next();
                        if (configObject.keySet().size() == 1 && firstKey.equalsIgnoreCase(classSimpleName)) {
                            ConfigObject conf = (ConfigObject) configObject.get(firstKey);
                            list.add(getValue(conf, configPropName, packageName + "." + firstKey, setters, classLoader, ignoreMissingProperties));
                        } else {
                            safeListAdd(configPropName, setters, classLoader, ignoreMissingProperties, list, item);
                        }
                    } else {
                        safeListAdd(configPropName, setters, classLoader, ignoreMissingProperties, list, item);
                    }
                }
                value = list;
            } else if (configValue.valueType() == ConfigValueType.OBJECT) {
                ConfigObject configObject = (ConfigObject) configValue;
                value = get(configObject.toConfig(), className, classLoader, ignoreMissingProperties);
            } else if (configValue.valueType() == ConfigValueType.NUMBER) {
                Number number = (Number) configValue.unwrapped();
                if (setter.getReturnType() == Long.class) {
                    value = number.longValue();
                } else {
                    value = number.intValue();
                }
            } else {
                value = configValue.unwrapped();
            }
        }
        return value;
    }

    private static void safeListAdd(String configPropName, List<Method> beanProps, ClassLoader classLoader, Boolean ignoreMissingProperties, List<Object> list, ConfigValue item) throws Exception {
        Method setter = getSetter(beanProps, configPropName, configPropName);
        assert setter != null;
//        String beanReturnType = setter.getGenericReturnType().getTypeName().replaceAll(".*<(.*)>", "$1");
        String beanReturnType = setter.getGenericParameterTypes()[0].getTypeName().replaceAll(".*<(.*)>", "$1");
        list.add(getValue(item, configPropName, beanReturnType, beanProps, classLoader, ignoreMissingProperties));
    }


    static BeanInfo getBeanInfo(Class<?> clazz) {
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException e) {
            throw new ConfigException.BadBean("Could not get bean information for class " + clazz.getName(), e);
        }
        return beanInfo;
    }

    static List<Method> getSetters(BeanInfo beanInfo) {
        List<Method> methods = new ArrayList<>();
        for (PropertyDescriptor beanProp : beanInfo.getPropertyDescriptors()) {
            if (beanProp.getReadMethod() == null || beanProp.getWriteMethod() == null || beanProp.getName().equals("metaClass")) {
                continue;
            }
            methods.add(beanProp.getWriteMethod());
        }
        Arrays.stream(beanInfo.getMethodDescriptors()).map(MethodDescriptor::getMethod).forEach(methods::add);
        return methods;
    }

}
