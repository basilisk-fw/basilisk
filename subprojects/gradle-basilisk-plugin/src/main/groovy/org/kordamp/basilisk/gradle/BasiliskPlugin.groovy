/*
 * Copyright 2008-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kordamp.basilisk.gradle

import org.gradle.BuildAdapter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSet

/**
 * @author Andres Almiray
 */
class BasiliskPlugin implements Plugin<Project> {
    private static final boolean MACOSX = System.getProperty('os.name').contains('Mac OS')

    @Override
    void apply(Project project) {
        BasiliskExtension extension = project.extensions.create('basilisk', BasiliskExtension, project)

        applyDefaultPlugins(project, extension)
        applyDefaultDependencies(project)
        registerBuildListener(project, extension)
    }

    private void applyDefaultDependencies(final Project project) {
        project.configurations.maybeCreate('basilisk').visible = true
    }

    private void applyDefaultPlugins(Project project, BasiliskExtension extension) {
        project.apply(plugin: 'idea')
        project.apply(plugin: 'java')
        if (extension.applicationProject) {
            project.apply(plugin: 'application')
        }
    }

    private void configureDefaultSourceSets(Project project, String sourceSetName) {
        // configure default source directories
        project.sourceSets.main[sourceSetName].srcDirs += [
            'basilisk-app/conf',
            'basilisk-app/controllers',
            'basilisk-app/models',
            'basilisk-app/views',
            'basilisk-app/services',
            'basilisk-app/lifecycle'
        ]
        // configure default resource directories
        project.sourceSets.main.resources.srcDirs += [
            'basilisk-app/resources',
            'basilisk-app/i18n'
        ]
    }

    private static String resolveApplicationName(Project project) {
        if (project.hasProperty('applicationName')) {
            return project.applicationName
        }
        return project.name
    }

    private void processResources(Project project, SourceSet sourceSet, BasiliskExtension extension) {
        project.tasks."${sourceSet.processResourcesTaskName}" {
            filesMatching([
                '**/*.properties',
                '**/*.groovy',
                '**/*.html',
                '**/*.xml',
                '**/*.txt'
            ]) {
                expand([
                    'application_name'   : resolveApplicationName(project),
                    'application_version': project.version,
                    'basilisk_version'   : extension.version
                ] + extension.applicationProperties)
            }
        }
    }

    private void configureApplicationSettings(Project project, BasiliskExtension extension) {
        Task createDistributionFiles = project.tasks.create(name: 'createDistributionFiles', type: Copy, group: 'Application') {
            destinationDir = project.file("${project.buildDir}/assemble/distribution")
            from(project.file('src/media')) {
                into 'resources'
                include '*.icns', '*.ico'
            }
            from(project.file('.')) {
                include 'README*', 'INSTALL*', 'LICENSE*'
            }
        }
        project.applicationDistribution.from(createDistributionFiles)

        if (MACOSX) {
            List jvmArgs = project.applicationDefaultJvmArgs
            if (!(jvmArgs.find { it.startsWith('-Xdock:name=') })) {
                jvmArgs << "-Xdock:name=${resolveApplicationName(project)}"
            }
            if (!(jvmArgs.find { it.startsWith('-Xdock:icon=') })) {
                jvmArgs << ('-Xdock:icon=$APP_HOME/resources/' + extension.applicationIconName)
            }

            Task runTask = project.tasks.findByName('run')
            jvmArgs = (project.applicationDefaultJvmArgs + runTask.jvmArgs).unique()
            if (!(jvmArgs.find { it.startsWith('-Xdock:name=') })) {
                jvmArgs << "-Xdock:name=${resolveApplicationName(project)}"
            }

            String iconElem = jvmArgs.find { it.startsWith('-Xdock:icon=$APP_HOME/resources') }
            jvmArgs -= iconElem
            if (!(jvmArgs.find { it.startsWith('-Xdock:icon=') })) {
                File iconFile = project.file("src/media/${extension.applicationIconName}")
                if (!iconFile.exists()) iconFile = project.file('src/media/basilisk.icns')
                jvmArgs << "-Xdock:icon=${iconFile.canonicalPath}"
            }
            runTask.jvmArgs = jvmArgs
        }
    }

    private void createDefaultDirectoryStructure(Project project, BasiliskExtension extension, String sourceSetName) {
        if (extension.generateProjectStructure) {
            def createIfNotExists = { File dir ->
                if (!dir.exists()) {
                    dir.mkdirs()
                }
            }
            project.sourceSets.main[sourceSetName].srcDirs.each(createIfNotExists)
            project.sourceSets.test[sourceSetName].srcDirs.each(createIfNotExists)
            project.sourceSets.main.resources.srcDirs.each(createIfNotExists)
            project.sourceSets.test.resources.srcDirs.each(createIfNotExists)
        }
    }

    private void registerBuildListener(
        final Project project, final BasiliskExtension extension) {
        project.gradle.addBuildListener(new BuildAdapter() {
            @Override
            void projectsEvaluated(Gradle gradle) {
                if (extension.includeDefaultRepositories) {
                    project.repositories.mavenLocal()
                    // enable jcenter
                    project.repositories.jcenter()
                    // enable basilisk-plugins @ bintray
                    project.repositories.maven { url 'http://dl.bintray.com/basilisk/basilisk-plugins' }
                    project.repositories.maven { url 'https://dl.bintray.com/melix/thirdparty-apache' }
                }

                configureDefaultSourceSets(project, 'java')
                createDefaultDirectoryStructure(project, extension, 'java')

                // add default dependencies
                appendDependency('core')
                appendDependency('core-compile')
                appendDependency('core-test')
                appendDependency('javafx')

                if (extension.applicationProject) {
                    project.apply(plugin: 'application')
                }
                project.plugins.withId('application') { plugin ->
                    configureApplicationSettings(project, extension)
                }

                BasiliskPluginResolutionStrategy.applyTo(project)

                processResources(project, project.sourceSets.main, extension)
                processResources(project, project.sourceSets.test, extension)

                project.plugins.withId('org.kordamp.gradle.stats') { plugin ->
                    Task statsTask = project.tasks.findByName('stats')
                    statsTask.paths += [
                        model     : [name: 'Models', path: 'basilisk-app/models'],
                        view      : [name: 'Views', path: 'basilisk-app/views'],
                        controller: [name: 'Controllers', path: 'basilisk-app/controllers'],
                        service   : [name: 'Services', path: 'basilisk-app/services'],
                        config    : [name: 'Configuration', path: 'basilisk-app/conf'],
                        lifecycle : [name: 'Lifecycle', path: 'basilisk-app/lifecycle']
                    ]
                }

                // update Basilisk environment settings
                project.gradle.taskGraph.whenReady {
                    if (project.gradle.taskGraph.hasTask(':startScripts')) {
                        if (project.hasProperty('basiliskEnv')) {
                            project.applicationDefaultJvmArgs << "-Dbasilisk.env=${project.basiliskEnv}"
                        } else {
                            project.applicationDefaultJvmArgs << '-Dbasilisk.env=prod'
                        }
                    } else {
                        Task runTask = project.tasks.findByName('run')
                        if (runTask != null) {
                            if (project.hasProperty('basiliskEnv')) {
                                project.applicationDefaultJvmArgs << "-Dbasilisk.env=${project.basiliskEnv}"
                                def jvmArgs = []
                                jvmArgs.addAll(runTask.jvmArgs)
                                jvmArgs << "-Dbasilisk.env=${project.basiliskEnv}"
                                runTask.jvmArgs = jvmArgs
                            } else {
                                runTask.jvmArgs << '-Dbasilisk.env=dev'
                            }
                        }
                    }
                }
            }

            private void appendDependency(String artifactId) {
                String dependencyCoordinates = ['org.kordamp.basilisk', 'basilisk-' + artifactId, extension.version].join(':')

                if (artifactId.endsWith('-compile')) {
                    ['compileOnly', 'testCompileOnly'].each { conf ->
                        project.logger.info("Adding {} to '{}' configuration", dependencyCoordinates, conf)
                        project.dependencies.add(conf, dependencyCoordinates)
                    }
                    ['apt', 'testApt'].each { conf ->
                        if (!project.configurations.findByName(conf)) {
                            project.logger.info("Configuration '{}' does not exist. Apply the 'gradle-apt-plugin' and try again", conf)
                            return
                        }
                        project.logger.info("Adding {} to '{}' configuration", dependencyCoordinates, conf)
                        project.dependencies.add(conf, dependencyCoordinates)
                    }
                } else if (artifactId.endsWith('-test')) {
                    project.logger.info("Adding {} to 'testCompile' configuration", dependencyCoordinates)
                    project.dependencies.add('testCompile', dependencyCoordinates)
                } else {
                    project.logger.info("Adding {} to '{}' configuration", dependencyCoordinates, 'compile')
                    project.dependencies.add('compile', dependencyCoordinates)
                }
            }
        })
    }
}
