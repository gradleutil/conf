package net.gradleutil.conf;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class LoaderOptions {
    public Boolean useSystemEnvironment = false;
    public Boolean useSystemProperties = false;
    public Boolean useReferences = false;
    public Boolean resolveStringValues = true;
    public Boolean allowUnresolved = false;
    public Boolean invalidateCaches = false;
    public Boolean singularizeClasses = true;
    public Boolean silent = true;
    public String baseName = "config";
    public String confString = null;
    public File conf = null;
    public File reference = null;
    public File confOverride = null;
    public File schemaFile = null;
    public Schema schema = null;
    public String schemaString = null;
    public Boolean schemaValidation = true;
    public Consumer<List<ValidationException>> onSchemaValidationFailure = null;
    public String schemaName = "schema.json";
    public String className = "Config";
    public String packageName = "conf.configuration";
    public Config config;
    public Log logger = new Log(this);
    public ClassLoader classLoader = LoaderOptions.class.getClassLoader();

    public static String jsonPrint(Config configObject, String path) throws Exception {
        String jsonString;
        if (configObject == null) {
            throw new Exception("No config loaded");
        }

        if (path != null) {
            if (!configObject.hasPath(path)) {
                throw new Exception("Config does not have path " + path + " (keys: " + configObject.root().keySet() + "...)");
            }

            jsonString = configObject.getValue(path).render(ConfigRenderOptions.concise().setFormatted(true));
        } else {
            jsonString = configObject.root().render(ConfigRenderOptions.concise().setFormatted(true));
        }

        return jsonString;
    }


    public LoaderOptions useSystemEnvironment(Boolean useSystemEnvironment) {
        this.useSystemEnvironment = useSystemEnvironment;
        return this;
    }

    public LoaderOptions useSystemProperties(Boolean useSystemProperties) {
        this.useSystemProperties = useSystemProperties;
        return this;
    }

    public LoaderOptions useReferences(Boolean useReferences) {
        this.useReferences = useReferences;
        return this;
    }

    public LoaderOptions resolveStringValues(Boolean resolveStringValues) {
        this.resolveStringValues = resolveStringValues;
        return this;
    }

    public LoaderOptions allowUnresolved(Boolean allowUnresolved) {
        this.allowUnresolved = allowUnresolved;
        return this;
    }

    public LoaderOptions invalidateCaches(Boolean invalidateCaches) {
        this.invalidateCaches = invalidateCaches;
        return this;
    }

    public LoaderOptions singularizeClasses(Boolean singularizeClasses) {
        this.singularizeClasses = singularizeClasses;
        return this;
    }

    public LoaderOptions silent(Boolean silent) {
        this.silent = silent;
        return this;
    }

    public LoaderOptions baseName(String baseName) {
        this.baseName = baseName;
        return this;
    }

    public LoaderOptions confString(String confString) {
        this.confString = confString;
        return this;
    }

    public LoaderOptions conf(File conf) {
        this.conf = conf;
        return this;
    }

    public LoaderOptions reference(File reference) {
        this.reference = reference;
        return this;
    }

    public LoaderOptions confOverride(File confOverride) {
        this.confOverride = confOverride;
        return this;
    }

    public LoaderOptions schemaFile(File schemaFile) {
        this.schemaFile = schemaFile;
        return this;
    }

    public LoaderOptions schema(Schema schema) {
        this.schema = schema;
        return this;
    }

    public LoaderOptions schemaString(String schemaString) {
        this.schemaString = schemaString;
        return this;
    }

    public LoaderOptions schemaValidation(Boolean schemaValidation) {
        this.schemaValidation = schemaValidation;
        return this;
    }

    public LoaderOptions onSchemaValidationFailure(Consumer<List<ValidationException>> onSchemaValidationFailure) {
        this.onSchemaValidationFailure = onSchemaValidationFailure;
        return this;
    }

    public LoaderOptions schemaName(String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    public LoaderOptions className(String className) {
        this.className = className;
        return this;
    }

    public LoaderOptions packageName(String packageName) {
        this.packageName = packageName;
        return this;
    }

    public LoaderOptions config(Config config) {
        this.config = config;
        return this;
    }

    public LoaderOptions classLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

}
