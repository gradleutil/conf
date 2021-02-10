package net.gradleutil.conf

import com.typesafe.config.*
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
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
    }

    static Config load(Config config, LoaderOptions options = defaultOptions()) {
        def resolver = ConfigResolveOptions.defaults().setAllowUnresolved(options.allowUnresolved).setUseSystemEnvironment(options.useSystemEnvironment)
        List<Config> fallbacks = []
        if (options.invalidateCaches) {
            invalidateCaches()
        }
        if (options.useSystemProperties) {
            fallbacks.add ConfigFactory.systemProperties()
        }
        fallbacks.each { config = config.withFallback(it) }
        config.resolve(resolver)
    }

    static Config load(File conf, LoaderOptions options = defaultOptions()) {
        load(parseFileAnySyntax(conf), options)
    }

    static LoaderOptions defaultOptions() {
        return new LoaderOptions()
    }

    static Config load(File schemaFile, File conf, LoaderOptions options = defaultOptions()) {
        def schema = Gen.getSchema(schemaFile.text)
        return load(conf, options)
    }

    static Config loadWithOverride(String baseName, File conf, File confOverride, LoaderOptions options = defaultOptions()) {
        ConfigFactory.invalidateCaches()
        Config defaultConfig, userConfig, returnConfig
        def parseOptions = ConfigParseOptions.defaults()
        if (conf.exists()) {
            defaultConfig = parseFile(conf, parseOptions)
        } else {
            defaultConfig = parseResourcesAnySyntax(Loader.classLoader, baseName, parseOptions)
        }
        if (confOverride.exists()) {
            userConfig = parseFile(confOverride, parseOptions).withFallback(defaultConfig)
            returnConfig = load(userConfig, options).withFallback(defaultConfig)
        } else {
            returnConfig = load(defaultConfig, options)
        }
        returnConfig
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
