package net.gradleutil.conf.util


import com.typesafe.config.Config
import com.typesafe.config.ConfigRenderOptions
import net.gradleutil.conf.BeanConfigLoader
import net.gradleutil.conf.Loader
import org.everit.json.schema.ReferenceSchema
import org.everit.json.schema.Schema
import org.jboss.dna.common.text.Inflector
import org.json.JSONObject

import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.spi.FileSystemProvider
import java.util.regex.Pattern

class ConfUtil {

    static String configToJson(Config configObject, String path = '') {
        def jsonString
        if (!configObject) {
            throw new Exception("No config loaded")
        }
        if (path) {
            if (!configObject.hasPath(path)) {
                throw new Exception("Config does not have path ${path}")
            }
            jsonString = configObject.getValue(path).render(ConfigRenderOptions.concise().setFormatted(true))
        } else {
            jsonString = configObject.root().render(ConfigRenderOptions.concise().setFormatted(true))
        }
        return jsonString
    }

    static URL getResourceUrl(ClassLoader classLoader, String resourcePath){
        URL resource
        List<URL> zips
        resource = classLoader.getResource(resourcePath)
        zips = []
        if(!resource){
            if(classLoader instanceof URLClassLoader){
                resource = classLoader.getURLs().find{it.file.endsWith(resourcePath)}
            }
        }
        if(!resource){
            resource = getResourceUrl(classLoader.parent, resourcePath)
        }
        if(!resource){
            throw new Exception("could not find resource '${resourcePath}' in classLoader (zips:${zips})")
        }
        return resource
    }

    static List<URL> getClassLoaderUrls(ClassLoader classLoader, Boolean recurse){
        List<URL> urls = []
        if(classLoader instanceof URLClassLoader){
            urls.addAll classLoader.getURLs()
        }
        if(classLoader.parent){
            urls.addAll getClassLoaderUrls(classLoader.parent, recurse)
        }
        return urls
    }


    static Path getJarSafePath(ClassLoader classLoader, String resourcePath) {
        URL resource = getResourceUrl(classLoader,resourcePath)
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


    static void setBeanFromConfigFile(Object bean, File conf, File confOverride, Boolean ignoreMissingProperties = true) {
        if(!conf.exists()){
            throw new IllegalArgumentException("conf ${conf.absolutePath} does not exist")
        }
        def options = Loader.defaultOptions()
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

    static ident(String string, Boolean convertToCamelCase = true, Boolean upperCamel = false, Boolean singularize) {
        def id
        if (convertToCamelCase) {
            id = javaIdent(string.replace('$', ''))
            id = inflector.camelCase(id, upperCamel, '-_ '.chars)
        } else {
            id = string.with { upperCamel ? it.capitalize() : it }
        }
        if (!isValidJavaIdent(id)) {
            id = javaIdent(string)
        }
        if (singularize && string.length() > 3) {
            id = inflector.singularize(id)
        }
        id.with { upperCamel ? it.capitalize() : it }
    }

    private static final Pattern ID_PATTERN = Pattern.compile("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*")

    static boolean isValidJavaIdent(String identifier) {
        return ID_PATTERN.matcher(identifier).matches()
    }


    static String javaIdent(String s) {
        StringBuilder sb = new StringBuilder()
        if (!Character.isJavaIdentifierStart(s.charAt(0))) {
            sb.append('_')
        }
        char lastChar = ' '
        for (char c : s.toCharArray()) {
            if (!Character.isJavaIdentifierPart(c)) {
                if (lastChar != '_' as char) {
                    sb.append('_')
                }
            } else {
                sb.append(c)
            }
            lastChar = c
        }
        return sb.toString()
    }

    static final inflector = new Inflector() {
        @Override
        protected void initialize() {
            Inflector inflect = this
            inflect.addPluralize('$', "s")
            inflect.addPluralize('s$', "s")
            inflect.addPluralize('(ax|test)is$', '$1es')
            inflect.addPluralize('(octop|vir)us$', '$1i')
            inflect.addPluralize('(octop|vir)i$', '$1i') // already plural
            inflect.addPluralize('(alias|status)$', '$1es')
            inflect.addPluralize('(bu)s$', '$1ses')
            inflect.addPluralize('(buffal|tomat)o$', '$1oes')
            inflect.addPluralize('([ti])um$', '$1a')
            inflect.addPluralize('([ti])a$', '$1a') // already plural
            inflect.addPluralize('sis$', 'ses')
            inflect.addPluralize('(?:([^f])fe|([lr])f)$', '$1$2ves')
            inflect.addPluralize('(hive)$', '$1s')
            inflect.addPluralize('([^aeiouy]|qu)y$', '$1ies')
            inflect.addPluralize('(x|ch|ss|sh)$', '$1es')
            inflect.addPluralize('(matr|vert|ind)ix|ex$', '$1ices')
            inflect.addPluralize('([m|l])ouse$', '$1ice')
            inflect.addPluralize('([m|l])ice$', '$1ice')
            inflect.addPluralize('^(ox)$', '$1en')
            inflect.addPluralize('(quiz)$', '$1zes')
            // Need to check for the following words that are already pluralized:
            inflect.addPluralize('(people|men|children|sexes|moves|stadiums)$', '$1') // irregulars
            inflect.addPluralize('(oxen|octopi|viri|aliases|quizzes)$', '$1') // special rules

            inflect.addSingularize('s$', '')
            inflect.addSingularize('(s|si|u)s$', '$1s') // '-us' and '-ss' are already singular
            inflect.addSingularize('(n)ews$', '$1ews')
            inflect.addSingularize('([ti])a$', '$1um')
            inflect.addSingularize('((a)naly|(b)a|(d)iagno|(p)arenthe|(p)rogno|(s)ynop|(t)he)ses$', '$1$2sis')
            inflect.addSingularize('(^analy)ses$', '$1sis')
            inflect.addSingularize('(^analy)sis$', '$1sis') // already singular, but ends in 's'
            inflect.addSingularize('([^f])ves$', '$1fe')
            inflect.addSingularize('(hive)s$', '$1')
            inflect.addSingularize('(tive)s$', '$1')
            inflect.addSingularize('([lr])ves$', '$1f')
            inflect.addSingularize('([^aeiouy]|qu)ies$', '$1y')
            inflect.addSingularize('(s)eries$', '$1eries')
            inflect.addSingularize('(m)ovies$', '$1ovie')
            inflect.addSingularize('(x|ch|ss|sh)es$', '$1')
            inflect.addSingularize('([m|l])ice$', '$1ouse')
            inflect.addSingularize('(bus)es$', '$1')
            inflect.addSingularize('(o)es$', '$1')
            inflect.addSingularize('(shoe)s$', '$1')
            inflect.addSingularize('(cris|ax|test)is$', '$1is') // already singular, but ends in 's'
            inflect.addSingularize('(cris|ax|test)es$', '$1is')
            inflect.addSingularize('(octop|vir)i$', '$1us')
            inflect.addSingularize('(octop|vir)us$', '$1us') // already singular, but ends in 's'
            inflect.addSingularize('(alias|status)es$', '$1')
            inflect.addSingularize('(alias|status)$', '$1') // already singular, but ends in 's'
            inflect.addSingularize('(credentials|creds)$', '$1') // already singular, but ends in 's'
            inflect.addSingularize('^(https)$', '$1') // already singular, but ends in 's'
            inflect.addSingularize('^(ox)en', '$1')
            inflect.addSingularize('(vert|ind)ices$', '$1ex')
            inflect.addSingularize('(matr)ices$', '$1ix')
            inflect.addSingularize('(quiz)zes$', '$1')

            inflect.addIrregular('person', 'people')
            inflect.addIrregular('man', 'men')
            inflect.addIrregular('child', 'children')
            inflect.addIrregular('sex', 'sexes')
            inflect.addIrregular('move', 'moves')
            inflect.addIrregular('stadium', 'stadiums')

            inflect.addUncountable('equipment', 'information', 'rice', 'money', 'species', 'series', 'fish', 'sheep')
        }

    }

}
