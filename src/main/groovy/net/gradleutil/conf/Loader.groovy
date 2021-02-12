package net.gradleutil.conf

import com.typesafe.config.*
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.json.JSONObject

import static com.typesafe.config.ConfigFactory.parseFileAnySyntax
import static com.typesafe.config.ConfigFactory.parseResourcesAnySyntax
import static com.typesafe.config.ConfigFactory.parseString

class Loader {


    @Builder(builderStrategy = SimpleStrategy)
    static class LoaderOptions {
        Boolean useSystemEnvironment = false
        Boolean useSystemProperties = false
        Boolean allowUnresolved = false
        Boolean invalidateCaches = false
        Boolean silent = true
        String baseName = 'config.conf'
        File conf = null
        File reference = null
        File confOverride = null
        File schemaFile = null
        String schemaName = 'schema.json'
        ClassLoader classLoader = Loader.classLoader
    }

    static void validate(options = defaultOptions()) {
        if(options.schemaFile){
            Gen.getSchema(options.schemaFile.text)
        }
    }

    static Config load(File conf, LoaderOptions options = defaultOptions()) {
        load(options.setConf(conf))
    }

    static LoaderOptions defaultOptions() {
        return new LoaderOptions()
    }

    static Config load(LoaderOptions options = defaultOptions()) {
        List<Config> fallbacks = []
        Config config, confOverride
        def log = new Log(options)

        def ifExists = { String message, File file ->
            if(file?.exists()){
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

        def resolver = ConfigResolveOptions.defaults().setAllowUnresolved(options.allowUnresolved).setUseSystemEnvironment(options.useSystemEnvironment).appendResolver(SYSTEM_PROPERTY)

        if(ifExists('load reference', options.reference)){
            fallbacks.add ConfigFactory.parseFile(options.reference)
        } else {
            if (!options.useSystemProperties) {
                log.info("Loading reference from classloader, classLoader=${options.classLoader}")
                fallbacks.add ConfigFactory.defaultReferenceUnresolved()
            }
        }

        if (ifExists('load config', options.conf)) {
            config = ConfigFactory.parseFile(options.conf)
        } else {
            log.info("Loading config from classloader, baseName=${options.baseName}, classLoader=${options.classLoader}")
            config = parseResourcesAnySyntax(options.classLoader, options.baseName)
        }

        if (ifExists('load override', options.confOverride)) {
            config = ConfigFactory.parseFile(options.confOverride).withFallback(config)
        }

        fallbacks.each { config = config.withFallback(it) }

        log.info('Resolving config, existing keys:' + config.root().keySet().join(', '))
        if (options.useSystemProperties) {
            log.info('Using System Properties')
            return ConfigFactory.load(config)
        } else {
            return config.resolve(resolver)
        }

    }

    static Config loadWithSchema(File schemaFile, File conf, LoaderOptions options = defaultOptions()) {
        return load(options.setConf(conf).setSchemaFile(schemaFile))
    }

    static Config loadWithOverride(File conf, File confOverride, LoaderOptions options = defaultOptions()) {
        load(options.setConf(conf).setConfOverride(confOverride))
    }


    static Config resolveWithSystem(String baseName) {
        def options = ConfigResolveOptions.defaults().setAllowUnresolved(true).appendResolver(SYSTEM_PROPERTY)
        return parseResourcesAnySyntax(baseName, ConfigParseOptions.defaults()).resolve(options)
    }

    static Config resolveWithSystem(File configFile) {
        if (!configFile.exists()) {
            throw new FileNotFoundException("config file '${configFile.absolutePath}' not found")
        }
        def options = ConfigResolveOptions.defaults().setAllowUnresolved(true).appendResolver(SYSTEM_PROPERTY)
        return parseFileAnySyntax(configFile, ConfigParseOptions.defaults()).resolve(options)
    }

    static <T> T create(String json, Class<T> clazz) {
        def config = ConfigFactory.load(parseString(json))
        return create(config, clazz)
    }

    static <T> T create(URL json, Class<T> clazz) {
        def config = ConfigFactory.load(parseString(json.getFile()))
        return create(config, clazz)
    }

    static <T> T create(Config config, Class<T> clazz) {
        return BeanLoader.create(config, clazz)
    }

    static <T> T create(Map map, Class<T> clazz) {
        def json = new JSONObject(map).toString()
        def config = ConfigFactory.load(parseString(json))
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

    static class Log {
        LoaderOptions options
        Log(LoaderOptions options){
            this.options = options
        }

        def info(String string){
            if(!options.silent){
                println('conf-info: ' + string)
            }
        }
    }


}
