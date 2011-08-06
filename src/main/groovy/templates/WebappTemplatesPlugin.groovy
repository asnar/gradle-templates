package templates

import org.gradle.api.Plugin
import org.gradle.api.Project

class WebappTemplatesPlugin extends JavaTemplatesPlugin implements Plugin<Project> {
	void createBase(String path = System.getProperty('user.dir'), String projectName) {
		super.createBase(path)
		ProjectTemplate.fromRoot(path) {
			'src/main/webapp/WEB-INF' {
				'web.xml' template: '/templates/webapp/web-xml.tmpl', project: [name: projectName]
			}
		}
	}

	void apply(Project project) {
		project.apply(plugin: 'java-templates')

		def props = project.properties

		project.task('createWebappProject', group: TemplatesPlugin.group, description: 'Creates a new Gradle Webapp project in a new directory named after your project.') << {
			def projectName = props['newProjectName'] ?: TemplatesPlugin.prompt('Project Name:')
			def useJetty = props['useJettyPlugin'] ?: TemplatesPlugin.promptYesOrNo('Use Jetty Plugin?')
			if (projectName) {
				String projectGroup = props['projectGroup'] ?: TemplatesPlugin.prompt('Group:', projectName.toLowerCase())
				String projectVersion = props['projectVersion'] ?: TemplatesPlugin.prompt('Version:', '1.0')
				createBase(projectName, projectName)
				ProjectTemplate.fromRoot(projectName) {
					'build.gradle' template: '/templates/webapp/build.gradle.tmpl', useJetty: useJetty, projectGroup: projectGroup
					'gradle.properties' content: "version=${projectVersion}", append: true
				}
			} else {
				println 'No project name provided.'
			}
		}
		project.task('initWebappProject', group: TemplatesPlugin.group, description: 'Initializes a new Gradle Webapp project in the current directory.') << {
			createBase(project.name)
			def useJetty = props['useJettyPlugin'] ?: TemplatesPlugin.promptYesOrNo('Use Jetty Plugin?')
			File buildFile = new File('build.gradle')
			buildFile.exists() ?: buildFile.createNewFile()
			if (useJetty) {
				TemplatesPlugin.prependPlugin 'jetty', buildFile
			} else {
				TemplatesPlugin.prependPlugin 'war', buildFile
			}
		}
		project.task('exportWebappTemplates') << {
			def _ = '/templates/webapp'
			def templates = [
					"$_/build.gradle.tmpl",
					"$_/web-xml.tmpl"
			]
			TemplatesPlugin.exportTemplates(templates)
		}
	}
}