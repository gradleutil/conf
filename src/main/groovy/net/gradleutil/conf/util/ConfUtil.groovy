package net.gradleutil.conf.util

import com.typesafe.config.*
import com.typesafe.config.parser.ConfigDocument
import com.typesafe.config.parser.ConfigDocumentFactory
import net.gradleutil.conf.BeanConfigLoader
import net.gradleutil.conf.Loader
import net.gradleutil.conf.config.impl.ConfigObjectVisitor
import net.gradleutil.conf.json.JsonObject

import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.spi.FileSystemProvider

import static com.typesafe.config.ConfigFactory.parseString

class ConfUtil {

    static String configToJson(Config configObject, String path = '') {
        def jsonString
        if (!configObject) {
            throw new Exception("No config loaded")
        }
        if (path) {
            if (!configObject.hasPath(path)) {
                throw new Exception("Config does not have path ${path} (keys: ${configObject.root().keySet().take(3).toString()}...)")
            }
            jsonString = configObject.getValue(path).render(ConfigRenderOptions.concise().setFormatted(true))
        } else {
            jsonString = configObject.root().render(ConfigRenderOptions.concise().setFormatted(true))
        }
        return jsonString
    }

    static JsonObject configToJsonObject(Config configObject, String path = '') {
        return new JsonObject(configToJson(configObject, path))
    }

    static URL getResourceUrl(ClassLoader classLoader, String resourcePath) {
        URL resource
        List<URL> zips
        resource = classLoader.getResource(resourcePath)
        zips = []
        if (!resource) {
            if (classLoader instanceof URLClassLoader) {
                resource = classLoader.getURLs().find { it.file.endsWith(resourcePath) }
            }
        }
        if (!resource) {
            resource = getResourceUrl(classLoader.parent, resourcePath)
        }
        if (!resource) {
            throw new Exception("could not find resource '${resourcePath}' in classLoader (zips:${zips})")
        }
        return resource
    }

    static List<URL> getClassLoaderUrls(ClassLoader classLoader, Boolean recurse) {
        List<URL> urls = []
        if (classLoader instanceof URLClassLoader) {
            urls.addAll classLoader.getURLs()
        }
        if (classLoader.parent) {
            urls.addAll getClassLoaderUrls(classLoader.parent, recurse)
        }
        return urls
    }


    static Path getJarSafePath(ClassLoader classLoader, String resourcePath) {
        URL resource = getResourceUrl(classLoader, resourcePath)
        URI uri = resource.toURI()
        if ("jar" == uri.getScheme()) {
            for (FileSystemProvider provider : FileSystemProvider.installedProviders()) {
                if (provider.getScheme().equalsIgnoreCase("jar")) {
                    try {
                        provider.getFileSystem(uri)
                    } catch (FileSystemNotFoundException e) {
                        provider.newFileSystem(uri, Collections.emptyMap())
                    }
                }
            }
        }
        Paths.get(uri)
    }


    /**
     * sets bean from conf, NOT ignoring missing properties and NOT using system props
     * @param bean
     * @param confString
     * @param ignoreMissingProperties
     * @param useSystemProps
     * @param silent
     */
    static void setBeanFromConf(Object bean, String confString, Boolean ignoreMissingProperties = false, Boolean useSystemProps = false, Boolean silent = true) {
        if (!confString) {
            throw new IllegalArgumentException("conf is required")
        }
        def options = Loader.loaderOptions()
                .allowUnresolved(ignoreMissingProperties)
                .silent(silent)
                .confString(confString)
                .useSystemProperties(useSystemProps)
        options.config = Loader.load(options)
        BeanConfigLoader.setBeanFromConfig(bean, options)
    }


    /**
     * sets bean from conf file, ignoring missing properties and using system props
     * @param bean
     * @param conf
     * @param confOverride
     * @param ignoreMissingProperties
     */
    static void setBeanFromConfigFile(Object bean, File conf, File confOverride, Boolean ignoreMissingProperties = true) {
        if (!conf.exists()) {
            throw new IllegalArgumentException("conf ${conf.absolutePath} does not exist")
        }
        def options = Loader.loaderOptions()
                .allowUnresolved(ignoreMissingProperties)
                .silent(true)
                .conf(conf)
                .confOverride(confOverride)
                .useSystemProperties(true)
        options.config = Loader.load(options)
        BeanConfigLoader.setBeanFromConfig(bean, options)
    }


    static void copyFolder(Path source, Path target, CopyOption... options) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

            @Override
            FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                Files.createDirectories(target.resolve(source.relativize(dir)))
                return FileVisitResult.CONTINUE
            }

            @Override
            FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                Files.copy(file, target.resolve(source.relativize(file)), options)
                return FileVisitResult.CONTINUE
            }
        })
    }

    /**
     * @param string
     * @param convertToCamelCase
     * @param upperCamel
     * @param singularize
     * @return a valid identifier (variable name) in at least java
     */
    static String ident(String string, Boolean convertToCamelCase = true, Boolean upperCamel = false, Boolean singularize) {
        def id
        id = ReservedWordChecker.javaIdent(string.replace('$', ''))

        if (convertToCamelCase) {
            id = Inflector.instance.camelCase(id, upperCamel, '-_ '.toCharArray())
        } else {
            id = string.with { upperCamel ? it.capitalize() : it }
        }
        if (!ReservedWordChecker.isValidJavaIdent(id)) {
            id = ReservedWordChecker.javaIdent(string)
        }
        if (singularize && string.length() > 3) {
            id = Inflector.instance.singularize(id)
        }
        id.with { upperCamel ? it.capitalize() : it }
    }


    static Config resolveStringValues(final Config config) {

        ConfigDocument configDocument
        configDocument = ConfigDocumentFactory.parseString(configToJson(config), ConfigParseOptions.defaults().setSyntax(ConfigSyntax.JSON))

        Map<String, String> replacements = [:]

        new ConfigObjectVisitor() {

            ConfigObject currentObject = null

            @Override
            void visitObject(ConfigObject configObject) {
                currentObject = configObject
            }

            @Override
            void visitString(ConfigValue configValue) {
                String string = configValue.unwrapped()
                def token = string.find(/\$\{(.*)}/)?.replace('"', '')
                if (token) {
                    String findPath = ((token =~ /\{(.*)}/).findAll() as List<List>)*.last().last()
                    def foundValue
                    if (findPath.contains('[')) {
                        def arr = ((findPath =~ /(([^\[]+)+(\[([^]]+)])?.?)/).findAll() as List<List>)
                        Config listObject
                        listObject = null
                        def attribute = ''
                        arr.each {
                            if (it[4]) {
                                def listObjectPath = it[2] as String
                                def index = it[4] as Integer
                                listObject = listObject ? listObject.getConfigList(listObjectPath).get(index) : config.getConfigList(listObjectPath).get(index)
                            } else {
                                attribute = it[2] as String
                            }
                        }
                        foundValue = listObject.getValue(attribute).unwrapped()
                    } else {
                        foundValue = config.entrySet().find { it.key == findPath }?.value?.unwrapped()
                    }
                    if (foundValue) {
                        if (foundValue instanceof String) {
                            foundValue = foundValue.replace(replacements).replace('"', '')
                            foundValue = '"' + foundValue + '"'
                            replacements.put(token, foundValue)
//							configDocument = configDocument.withValueText(stackPath, "\"${newValue}\"")
                        } else if (foundValue instanceof Integer) {
                            replacements.put(token, foundValue.toString())
//							configDocument = configDocument.withValueText(stackPath, newValue)
                        }

                        //def newConfigValue = ConfigValueFactory.fromAnyRef(newValue)
                        //configDocument = configDocument.withValue(stackPath, newConfigValue)
                    }
                }
            }
        }.visit(config)

        def string = configDocument.render().replace(replacements)
        parseString(string)

    }
}
