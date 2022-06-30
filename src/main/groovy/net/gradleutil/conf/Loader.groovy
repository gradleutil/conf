package net.gradleutil.conf

import com.typesafe.config.*
import groovy.transform.AnnotationCollector
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import net.gradleutil.conf.annotation.ToStringIncludeNames
import net.gradleutil.conf.json.schema.SchemaUtil
import net.gradleutil.conf.util.ConfUtil
import org.everit.json.schema.Schema
import org.everit.json.schema.ValidationException
import org.json.JSONObject

import static com.typesafe.config.ConfigFactory.load as factoryLoad
import static com.typesafe.config.ConfigFactory.parseFile as factoryParseFile
import static com.typesafe.config.ConfigFactory.parseResources as factoryParseResources
import static com.typesafe.config.ConfigFactory.parseString as factoryParseString

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

class Loader {


    @Builder(builderStrategy = SimpleStrategy, prefix = '')
    @ToStringIncludeNames
    static class LoaderOptions {
        Boolean useSystemEnvironment = false
        Boolean useSystemProperties = false
        Boolean useReferences = false
        Boolean allowUnresolved = false
        Boolean invalidateCaches = false
        Boolean singularizeClasses = true
        Boolean silent = true
        String baseName = 'config'
        String confString = null
        File conf = null
        File reference = null
        File confOverride = null
        File schemaFile = null
        Schema schema = null
        String schemaString = null
        Boolean schemaValidation = true
        Closure<List<ValidationException>> onSchemaValidationFailure = null
        String schemaName = 'schema.json'
        String className = 'Config'
        String packageName = 'conf.configuration'
        Config config
        Log logger = new Log(this)
        ClassLoader classLoader = Loader.classLoader
    }

    static LoaderOptions defaultOptions() {
        return new LoaderOptions()
    }

    static Config load(LoaderOptions options = defaultOptions()) {
        List<Config> fallbacks = []
        def log = new Log(options)

        def logIfFileExists = { String message, File file ->
            if (file?.exists()) {
                log.info(message + ': ' + file.absolutePath)
                return true
            }
            if (file && !file.exists()) {
                log.info("${message}: ${file.absolutePath} does not exist")
                return false
            }
            return false
        }

        if (options.invalidateCaches) {
            invalidateCaches()
            log.info('Invalidating caches')
        }


        if (logIfFileExists('load reference', options.reference)) {
            fallbacks.add factoryParseFile(options.reference)
        } else {
            if (options.useReferences) {
                log.info("Loading reference from classloader, classLoader=${options.classLoader}")
                fallbacks.add ConfigFactory.defaultReferenceUnresolved()
            }
        }

        def config
        if (options.config) {
            config = options.config
        } else if (logIfFileExists('load config', options.conf)) {
            config = factoryParseFile(options.conf)
        } else if (options.confString) {
            log.info("Loading config from string")
            config = ConfigFactory.parseString(options.confString)
        } else {
            log.info("Loading config from classloader, baseName=${options.baseName}, classLoader=${options.classLoader}")
            config = factoryParseResources(options.classLoader, options.baseName)
        }

        if (logIfFileExists('load override', options.confOverride)) {
            config = factoryParseFile(options.confOverride).withFallback(config)
        }

        if (options.schemaString) {
            log.info("Loading schema from string")
            options.schema = SchemaUtil.getSchema(options.schemaString)
        } else if (options.schemaFile) {
            log.info("Loading schema from ${options.schemaFile}")
            options.schema = SchemaUtil.getSchema(options.schemaFile)
        }

        fallbacks.each { config = config.withFallback(it) }

        log.info('Resolving config, existing keys:' + config.root().keySet().join(', '))

        if (options.schema && options.schemaValidation) {
            log.info('Validating config against schema')
            def errors = SchemaUtil.validate(options.schema, ConfUtil.configToJsonObject(config))
            if (errors) {
                if(options.onSchemaValidationFailure != null){
                    options.onSchemaValidationFailure.call(errors)
                } else {
                    errors.each {
                        System.err.println(it)
                    }
                    throw new IllegalArgumentException("Failed validation")
                }
            }
            log.info('Finished validating')
        }


        Config conf
        if (options.useSystemProperties) {
            log.info('Using System Properties')
            conf = factoryLoad(config)
        } else {
            def resolver = ConfigResolveOptions.defaults().setAllowUnresolved(options.allowUnresolved).
                    setUseSystemEnvironment(options.useSystemEnvironment).appendResolver(SYSTEM_PROPERTY)
            conf = config.resolve(resolver)
        }

        return conf

    }

    static Config load(String config, LoaderOptions options = defaultOptions()) {
        load(options.confString(config))
    }

    static Config load(Config config, LoaderOptions options = defaultOptions()) {
        load(options.config(config))
    }

    static Config load(File conf, LoaderOptions options = defaultOptions()) {
        load(options.conf(conf))
    }

    static <T> T load(Class<T> clazz, LoaderOptions options = defaultOptions()) {
        def config = factoryLoad(options.classLoader, options.baseName)
        return create(config, clazz, options)
    }


    static Config loadWithSchemaFile(File schemaFile, File conf, LoaderOptions options = defaultOptions()) {
        return load(options.conf(conf).setSchemaFile(schemaFile))
    }

    static Config loadWithSchema(String schemaString, File conf, LoaderOptions options = defaultOptions()) {
        return load(options.conf(conf).setSchemaString(schemaString))
    }

    static Config loadWithOverride(File conf, File confOverride, LoaderOptions options = defaultOptions()) {
        load(options.conf(conf).confOverride(confOverride))
    }


    static Config resolveWithSystem(String baseName) {
        def options = ConfigResolveOptions.defaults().setAllowUnresolved(true).appendResolver(SYSTEM_PROPERTY)
        return factoryParseResources(baseName, ConfigParseOptions.defaults()).resolve(options)
    }

    static Config resolveWithSystem(File configFile) {
        if (!configFile.exists()) {
            throw new FileNotFoundException("config file '${configFile.absolutePath}' not found")
        }
        def options = ConfigResolveOptions.defaults().setAllowUnresolved(true).appendResolver(SYSTEM_PROPERTY)
        def parseOpts = ConfigParseOptions.defaults()
        if (configFile.name.toLowerCase().endsWith('.mhf')) {
            parseOpts.setSyntax(ConfigSyntax.CONF)
        }
        return ConfigFactory.parseFile(configFile, parseOpts).resolve(options)
    }

    static Config resolveStringWithSystem(String conf) {
        def options = ConfigResolveOptions.defaults().setAllowUnresolved(true).appendResolver(SYSTEM_PROPERTY)
        def parseOpts = ConfigParseOptions.defaults()
        parseOpts.setSyntax(ConfigSyntax.CONF)
        return ConfigFactory.parseString(conf, parseOpts).resolve(options)
    }

    static <T> T create(String json, Class<T> clazz, LoaderOptions options = null) {
        def config = load(factoryParseString(json), options)
        return create(config, clazz, options ?: defaultOptions())
    }

    static <T> T create(URL json, Class<T> clazz, LoaderOptions options = null) {
        def config = factoryLoad(factoryParseString(json.getFile()))
        return create(config, clazz, options ?: defaultOptions())
    }

    static <T> T create(Config config, Class<T> clazz, LoaderOptions options = defaultOptions()) {
        try {
            return BeanLoader.create(config, clazz, options)
        } catch (ConfigException.Missing e) {
            if (!options.silent) {
                def message = ConfUtil.configToJson(config)
                throw new Exception(e.message + ' from:\n' + message, e)
            } else {
                throw e
            }
        }
    }

    static <T> T create(T bean, Config config, Class<T> clazz, LoaderOptions options = defaultOptions()) {
        try {
            return BeanLoader.create(bean, config, clazz, options)
        } catch (ConfigException.Missing e) {
            if (!options.silent) {
                def message = ConfUtil.configToJson(config)
                throw new Exception(e.message + ' from:\n' + message, e)
            } else {
                throw e
            }
        }
    }

    static <T> T get(Config config, LoaderOptions options) {
        try {
            return BeanConfigLoader.get(config, options.packageName + '.' + options.className, options.classLoader, options.allowUnresolved)
        } catch (ConfigException.Missing e) {
            if (!options.silent) {
                def message = ConfUtil.configToJson(config)
                throw new Exception(e.message + ' from:\n' + message, e)
            } else {
                throw e
            }
        }
    }

    static <T> T create(Map map, Class<T> clazz) {
        def json = new JSONObject(map).toString()
        def config = factoryLoad(factoryParseString(json))
        return BeanLoader.create(config, clazz)
    }

    static invalidateCaches() {
        ConfigFactory.invalidateCaches()
    }

    private static final ConfigResolver SYSTEM_PROPERTY = new ConfigResolver() {

        @Override
        ConfigValue lookup(String path) {
            return ConfigValueFactory.fromMap([(path): System.properties.get(path) ?: path]).get(path)
        }

        @Override
        ConfigResolver withFallback(ConfigResolver fallback) {
            return fallback
        }

    }

    /**
     * Pretty JSON
     * @param config
     * @param namespace limit by dotted object path, e.g. `object.subObject' for only object.subObject keys/values
     */
    static String jsonPrint(Config config, String namespace = '') {
        ConfUtil.configToJson(config, namespace);
    }

    static class Log {
        LoaderOptions options

        Log(LoaderOptions options) {
            this.options = options
        }

        def info(String string) {
            if (!options.silent) {
                println('conf-info: ' + string)
            }
        }

        def error(String string) {
            if (!options.silent) {
                if (options.config) {
                    System.err.println(jsonPrint(options.config))
                }
                System.err.println('conf-info: ' + string)
            }
        }
    }


}
