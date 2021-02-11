package net.gradleutil.conf

import com.typesafe.config.*
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.everit.json.schema.Schema
import org.json.JSONObject

import static com.typesafe.config.ConfigFactory.parseFile
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
        String baseName = 'config.conf'
        File conf = null
        File confOverride = null
        File schemaFile = null
        String schemaName = 'schema.json'
    }

    static void validate(options = defaultOptions()) {
        if(options.schemaFile){
            Gen.getSchema(options.schemaFile.text)
        }
    }

    static Config load(LoaderOptions options = defaultOptions()) {
        List<Config> fallbacks = []
        Config config, confOverride

        if (options.invalidateCaches) {
            invalidateCaches()
        }
        if (options.useSystemProperties) {
            fallbacks.add ConfigFactory.systemProperties()
        }

        if (options.conf?.exists()) {
            config = parseFile(options.conf)
        } else {
            config = parseResourcesAnySyntax(Loader.classLoader, options.baseName)
        }

        if (options.confOverride?.exists()) {
            confOverride = parseFile(options.confOverride)
            config = confOverride.withFallback(config)
        }

        fallbacks.each { config = config.withFallback(it) }

        def resolver = ConfigResolveOptions.defaults().setAllowUnresolved(options.allowUnresolved).setUseSystemEnvironment(options.useSystemEnvironment)
        config.resolve(resolver)
    }

    static Config load(File conf, LoaderOptions options = defaultOptions()) {
        load(options.setConf(conf))
    }

    static LoaderOptions defaultOptions() {
        return new LoaderOptions()
    }

    static Config loadWithSchema(File schemaFile, File conf, LoaderOptions options = defaultOptions()) {
        return load(options.setConf(conf).setSchemaFile(schemaFile))
    }

    static Config loadWithOverride(File conf, File confOverride, LoaderOptions options = defaultOptions()) {
        load(options.setConf(conf).setConfOverride(confOverride))
    }


    static Config parse(File configFile) {
        if (!configFile.exists()) {
            throw new FileNotFoundException("config file '${configFile.absolutePath}' not found")
        }
        def options = ConfigResolveOptions.defaults().setAllowUnresolved(true).appendResolver(BLANK_RESOLVER)
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

    private static final ConfigResolver BLANK_RESOLVER = new ConfigResolver() {

        @Override
        ConfigValue lookup(String path) {
            return ConfigValueFactory.fromMap([(path): path]).get(path)
        }

        @Override
        ConfigResolver withFallback(ConfigResolver fallback) {
            return fallback
        }

    }


}
