package net.gradleutil.conf

import com.typesafe.config.*
import org.json.JSONObject

import static com.typesafe.config.ConfigFactory.parseFile
import static com.typesafe.config.ConfigFactory.parseFile
import static com.typesafe.config.ConfigFactory.parseFileAnySyntax
import static com.typesafe.config.ConfigFactory.parseResourcesAnySyntax

class Loader {

    static Config load(File conf) {
//        ConfigFactory.invalidateCaches()
        return ConfigFactory.load(parseFileAnySyntax(conf))
    }

    static Config load(File schemaFile, File conf) {
        def schema = Gen.getSchema(schemaFile.text)
        return ConfigFactory.load(parseFileAnySyntax(conf))
    }

    static Config loadWithOverride(String baseName, File conf, File confOverride) {
        ConfigFactory.invalidateCaches()
        Config defaultConfig, userConfig, returnConfig
        def resolver = ConfigResolveOptions.defaults().setAllowUnresolved( false )
        def parseOptions = ConfigParseOptions.defaults()
        if( conf.exists() ) {
            defaultConfig = parseFile( conf, parseOptions )
        } else {
            defaultConfig = parseResourcesAnySyntax( Loader.classLoader, baseName, parseOptions )
        }
        if( confOverride.exists() ) {
            userConfig = parseFile( confOverride, parseOptions ).withFallback( defaultConfig )
            returnConfig = ConfigFactory.load( userConfig ).withFallback( defaultConfig )
        } else {
            returnConfig = ConfigFactory.load(defaultConfig)
        }
        returnConfig.resolve( resolver )
    }


    static Config parse(File configFile) {
        if(!configFile.exists()){
            throw new FileNotFoundException("config file '${configFile.absolutePath}' not found")
        }
        def options = ConfigResolveOptions.defaults().setAllowUnresolved(true).appendResolver(BLANK_RESOLVER)
        return parseFileAnySyntax(configFile, ConfigParseOptions.defaults()).resolve(options)
    }

    static <T> T create(String json, Class<T> clazz) {
        def config = ConfigFactory.load(ConfigFactory.parseString(json))
        return create(config, clazz)
    }

    static <T> T create(URL json, Class<T> clazz) {
        def config = ConfigFactory.load(ConfigFactory.parseString(json))
        return create(config, clazz)
    }

    static <T> T create(Config config, Class<T> clazz) {
        return BeanLoader.create(config, clazz)
    }

    static <T> T create(Map map, Class<T> clazz) {
        def json = new JSONObject(map).toString()
        def config = ConfigFactory.load(ConfigFactory.parseString(json))
        return BeanLoader.create(config, clazz)
    }

    static invalidateCaches(){
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