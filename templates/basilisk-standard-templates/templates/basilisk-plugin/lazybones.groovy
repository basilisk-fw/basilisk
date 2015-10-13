import org.apache.commons.io.FileUtils
import uk.co.cacoethes.util.NameType

Map props = [:]
String projectName = projectDir.name
projectName = !projectName.startsWith('basilisk-') ? 'basilisk-' + projectName : projectName
projectName += !projectName.endsWith('-plugin') ? '-plugin' : ''
String pluginName = projectName - 'basilisk-' - '-plugin'

if (projectName =~ /\-/) {
    props.project_class_name = transformText(pluginName, from: NameType.HYPHENATED, to: NameType.CAMEL_CASE).capitalize()
    pluginName = pluginName.toLowerCase()
} else {
    props.project_class_name = transformText(pluginName, from: NameType.NATURAL, to: NameType.CAMEL_CASE).capitalize()
    pluginName = transformText(pluginName, from: NameType.NATURAL, to: NameType.HYPHENATED).toLowerCase()()
}

props.plugin_name = pluginName
props.plugin_full_name = 'basilisk-' + pluginName + '-plugin'
props.plugin_base_name = 'basilisk-' + pluginName
props.plugin_natural_name = transformText(pluginName, from: NameType.HYPHENATED, to: NameType.NATURAL)
props.plugin_full_natural_name = transformText(props.plugin_full_name, from: NameType.HYPHENATED, to: NameType.NATURAL)

props.project_year = Calendar.instance.get(Calendar.YEAR)
props.project_author = System.getProperty('user.name')
props.project_group = ask("Define value for 'group' [org.kordamp.basilisk.plugins]: ", "org.kordamp.basilisk.plugins", "group")
props.project_version = ask("Define value for 'version' [0.1.0-SNAPSHOT]: ", "0.1.0-SNAPSHOT", "version")
props.project_package = ask("Define value for 'package' [org.kordamp.basilisk.runtime." + pluginName + "]: ",
    'org.kordamp.basilisk.runtime.' + pluginName, "package")
props.basilisk_version = ask("Define value for 'basiliskVersion' [0.0.0]: ", "0.0.0", "basiliskVersion")
props.project_website = ask("Define value for 'website' [http://artifacts.basilisk-framework.org/plugin/" + pluginName + "]: ",
    "http://artifacts.basilisk-framework.org/plugin/" + pluginName, "website")
props.project_issue_tracker = ask("Define value for 'issueTracker' [http://artifacts.basilisk-framework.org/plugin/" + pluginName + "/issues]: ",
    "http://artifacts.basilisk-framework.org/plugin/" + pluginName + "/issues", "issueTracker")
props.project_vcs = ask("Define value for 'vcs' [https://github.com/basilisk/" + props.plugin_full_name + ".git]: ",
    "https://github.com/basilisk/" + props.plugin_full_name, "vcs")

String packagePath = props.project_package.replace('.' as char, '/' as char)

processTemplates 'settings.gradle', props
processTemplates 'gradle.properties', props
processTemplates 'gradle/docs.gradle', props
processTemplates 'gradle/publishing.gradle', props
processTemplates 'subprojects/guide/guide.gradle', props
processTemplates 'subprojects/plugin/gradle.properties', props
processTemplates 'subprojects/plugin/src/main/java/*.java', props
processTemplates 'subprojects/guide/src/javadoc/*.html', props

File mainSources = new File(projectDir, 'subprojects/plugin/src/main/java')

File mainSourcesPath = new File(mainSources, packagePath)
mainSourcesPath.mkdirs()

def renameFile = { File from, String path ->
    if (from.file) {
        File to = new File(path)
        to.parentFile.mkdirs()
        FileUtils.moveFile(from, to)
    }
}

mainSources.eachFile { File file ->
    renameFile(file, mainSourcesPath.absolutePath + '/' + props.project_class_name + file.name)
}

File pluginDir = new File(projectDir, 'subprojects/plugin')
File guideDir = new File(projectDir, 'subprojects/guide')
renameFile(new File(pluginDir, 'plugin.gradle'), pluginDir.absolutePath + '/basilisk-' + pluginName + '-core.gradle')
renameFile(new File(guideDir, 'guide.gradle'), guideDir.absolutePath + '/basilisk-' + pluginName + '-guide.gradle')
FileUtils.moveDirectory(pluginDir, new File(projectDir.absolutePath + '/subprojects/basilisk-' + pluginName + '-core'))
FileUtils.moveDirectory(guideDir, new File(projectDir.absolutePath + '/subprojects/basilisk-' + pluginName + '-guide'))

renameFile(projectDir, projectName)
new File(projectName, 'lazybones.groovy').delete()
