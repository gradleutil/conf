package net.gradleutil.conf

import com.typesafe.config.ConfigException
import com.typesafe.config.ConfigValue
import net.gradleutil.conf.bean.PersonList
import net.gradleutil.conf.json.schema.SchemaUtil

import static net.gradleutil.conf.Loader.load

class EditorTest extends AbstractTest {

	def "form editor"() {
		setup:
        def editorFile = new File(baseDir, 'editor.html')
		def configFile = new File('src/test/resources/json/MinecraftConfig.json')
		def schemaFile = new File('src/test/resources/json/MinecraftConfig.schema.json')
		def uiSchemaFile = new File('src/test/resources/json/MinecraftConfig.ui.schema.json')

		when:
		println 'file:///' + configFile.absolutePath
		println 'file:///' + schemaFile.absolutePath
		println 'file:///' + uiSchemaFile.absolutePath
        editorFile.text = SchemaUtil.editor(schemaFile.text, configFile.text)

		then:
		println 'file:///'+editorFile.absolutePath

	}

}
