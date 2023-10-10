package net.gradleutil.conf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.everit.json.schema.ValidationException;
import org.json.JSONObject;

import com.typesafe.config.*;

import net.gradleutil.conf.json.schema.SchemaUtil;
import net.gradleutil.conf.util.ConfUtil;

import static com.typesafe.config.ConfigFactory.parseFile;
import static com.typesafe.config.ConfigFactory.parseResources;
import static com.typesafe.config.ConfigFactory.parseString;


public class Loader {

    private static final ConfigResolver SYSTEM_PROPERTY = new ConfigResolver() {

        @Override
        public ConfigValue lookup(String path) {
            HashMap<String, Object> map = new HashMap<>();
            String systemPath = System.getProperties().getProperty(path);
            map.put(path, systemPath != null ? systemPath : path);
            return ConfigValueFactory.fromMap(map).get(path);
        }

        @Override
        public ConfigResolver withFallback(ConfigResolver fallback) {
            return fallback;
        }

    };

    public static LoaderOptions loaderOptions() {
        return new LoaderOptions();
    }

    public static <T> T create(Config config, Class<T> clazz, LoaderOptions options) throws Exception {
        if (!config.root().toConfig().isResolved())
            throw new ConfigException.NotResolved("need to Config#resolve() a config before using it to initialize a bean, see the API docs for Config#resolve()");
        T bean = clazz.newInstance();
        if(options.config == null){
            options.config(config);
        }
        BeanConfigLoader.setBeanFromConfig(bean, options);
        return bean;
    }

    public static <T> T create(Config config, Class<T> clazz) throws Exception {
        return create(config, clazz, loaderOptions());
    }


    static <T> T create(Map<String, Object> map, Class<T> clazz) throws Exception {
        String json = new JSONObject(map).toString();
        Config config = load(json);
        return create(config, clazz);
    }

    public static <T> T create(String json, Class<T> clazz, LoaderOptions options) throws Exception {
        return create(load(json), clazz, options);
    }

    public static <T> T create(String json, Class<T> clazz) throws Exception {
        return create(load(json), clazz, loaderOptions());
    }

    public static Config load(String json) throws IOException {
        return load(new LoaderOptions().confString(json));
    }

    public static Config load(File jsonFile) throws IOException {
        return load(new LoaderOptions().conf(jsonFile));
    }

    public static Config load(File jsonFile, File schemaFile) throws IOException {
        return load(new LoaderOptions().conf(jsonFile).schemaFile(schemaFile));
    }

    public static Config load(LoaderOptions options) throws IOException {
        List<Config> fallbacks = new ArrayList<>();
        Log log = new Log(options);

        BiFunction<String, File, Boolean> logIfFileExists = (message, file) -> {
            if (file != null && file.exists()) {
                log.info(message + ": " + file.getAbsolutePath());
                return true;
            } else if (file != null) {
                log.info(message + ": " + file.getAbsolutePath() + " does not exist");
            }
            return false;
        };

        if (options.invalidateCaches) {
            invalidateCaches();
            log.info("Invalidating caches");
        }

        if (logIfFileExists.apply("load reference", options.reference)) {
            fallbacks.add(parseFile(options.reference));
        } else {
            if (options.useReferences) {
                log.info("Loading reference from classloader, classLoader=" + options.classLoader);
                fallbacks.add(ConfigFactory.defaultReferenceUnresolved());
            }
        }

        Config config;
        if (options.config != null) {
            config = options.config;
        } else if (logIfFileExists.apply("load config", options.conf)) {
            config = parseFile(options.conf);
        } else if (options.confString != null) {
            log.info("Loading config from string");
            config = ConfigFactory.parseString(options.confString);
        } else {
            log.info("Loading config from classloader, baseName=" + options.baseName + ", classLoader=" + options.classLoader);
            config = parseResources(options.classLoader, options.baseName);
        }

        if (logIfFileExists.apply("load override", options.confOverride)) {
            config = parseFile(options.confOverride).withFallback(config);
        }

        if (options.schemaString != null) {
            log.info("Loading schema from string");
            options.schema(SchemaUtil.getSchema(options.schemaString));
        } else if (options.schemaFile != null) {
            log.info("Loading schema from " + options.schemaFile);
            if (!options.schemaFile.exists()) {
                log.info("schema file " + options.schemaFile + ", nonexistent, generating from conf");
            }
            options.schema(SchemaUtil.getSchema(options.schemaFile));
        }

        Config finalConfig = config;
        fallbacks.forEach(finalConfig::withFallback);

        log.info("Resolving config, existing keys:" + String.join(", ", config.root().keySet()));

        if (options.schema != null && options.schemaValidation) {
            log.info("Validating config against schema " + options.schema.getLocation());
            List<ValidationException> errors = SchemaUtil.validate(options.schema, ConfUtil.configToJsonObject(config));
            if (!errors.isEmpty()) {
                if (options.onSchemaValidationFailure != null) {
                    options.onSchemaValidationFailure.accept(errors);
                } else {
                    errors.forEach(System.err::println);
                    throw new IllegalArgumentException("Failed validation");
                }
            }
            log.info("Finished validating");
        }

        Config conf;
        if (options.useSystemProperties) {
            log.info("Using System Properties");
            conf = com.typesafe.config.ConfigFactory.load(config);
        } else {
            ConfigResolveOptions resolver = ConfigResolveOptions.defaults().setAllowUnresolved(options.allowUnresolved).setUseSystemEnvironment(options.useSystemEnvironment).appendResolver(SYSTEM_PROPERTY);
            conf = config.resolve(resolver);
        }

        if (options.resolveStringValues) {
            conf = ConfUtil.resolveStringValues(conf);
        }

        return conf;
    }

    static Config loadWithSchemaFile(File schemaFile, File conf) throws IOException {
        return load(loaderOptions().conf(conf).schemaFile(schemaFile));
    }

    static Config loadWithSchema(String schemaString, File conf) throws IOException {
        return load(loaderOptions().conf(conf).schemaString(schemaString));
    }

    static Config loadWithOverride(File conf, File confOverride) throws IOException {
        return load(loaderOptions().conf(conf).confOverride(confOverride));
    }

    static Config resolveWithSystem(String baseName) {
        ConfigResolveOptions options = ConfigResolveOptions.defaults().setAllowUnresolved(true).appendResolver(SYSTEM_PROPERTY);
        return parseResources(baseName, ConfigParseOptions.defaults()).resolve(options);
    }

    static Config resolveWithSystem(File configFile) throws FileNotFoundException {
        if (!configFile.exists()) {
            throw new FileNotFoundException("config file '${configFile.absolutePath}' not found");
        }
        ConfigResolveOptions options = ConfigResolveOptions.defaults().setAllowUnresolved(true).appendResolver(SYSTEM_PROPERTY);
        ConfigParseOptions parseOpts = ConfigParseOptions.defaults();
        if (configFile.getName().toLowerCase().endsWith(".mhf")) {
            parseOpts.setSyntax(ConfigSyntax.CONF);
        }
        return ConfigFactory.parseFile(configFile, parseOpts).resolve(options);
    }

    static Config resolveStringWithSystem(String conf) {
        ConfigResolveOptions options = ConfigResolveOptions.defaults().setAllowUnresolved(true).appendResolver(SYSTEM_PROPERTY);
        ConfigParseOptions parseOpts = ConfigParseOptions.defaults().setSyntax(ConfigSyntax.CONF);
        return parseString(conf, parseOpts).resolve(options);
    }

    static void invalidateCaches() {
        ConfigFactory.invalidateCaches();
    }

}
