/*
 * Copyright 2008-2016 the original author or authors.
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

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ResolvableDependencies

/**
 * @author Andres Almiray
 */
class BasiliskPluginResolutionStrategy {
    private static final String PLUGIN_PREFIX = 'basilisk-'
    private static final String PLUGIN_SUFFIX = '-plugin'
    private static final String BASILISK_CONFIGURATION = 'basilisk'
    private static final List<String> CONFIGURATION_NAMES = [
        'compile',
        'compileOnly',
        'testCompileOnly',
        'testCompile',
        'runtime'
    ]

    private static final Map<String, List<String>> DEPENDENCY_MAP = [:]

    static void applyTo(Project project) {
        BasiliskPluginDependencyResolver resolver = new BasiliskPluginDependencyResolver(project)

        Configuration basiliskConfiguration = project.configurations.getByName(BASILISK_CONFIGURATION)
        basiliskConfiguration.incoming.beforeResolve(resolver)
        basiliskConfiguration.resolve()

        CONFIGURATION_NAMES.each { String configurationName ->
            if (project.extensions.getByName(BASILISK_CONFIGURATION).disableDependencyResolution) {
                return
            }
            DEPENDENCY_MAP.get(project.name, [:])[configurationName].each { String dependency ->
                project.logger.info("Adding {} to '{}' configuration of {}", dependency, configurationName, project.name)
                resolver.project.dependencies.add(configurationName, dependency)
            }
        }
    }

    private static class BasiliskPluginDependencyResolver implements Action<ResolvableDependencies> {
        final Project project

        BasiliskPluginDependencyResolver(Project project) {
            this.project = project
        }

        @Override
        void execute(ResolvableDependencies resolvableDependencies) {
            if (basiliskExtension.disableDependencyResolution) {
                return
            }

            resolvableDependencies.dependencies.each { Dependency dependency ->
                String pluginName = dependency.name
                if (pluginName.startsWith(PLUGIN_PREFIX) && pluginName.endsWith(PLUGIN_SUFFIX)) {
                    String bomDependency = "${dependency.group}:${dependency.name}:${dependency.version}@pom"

                    project.logger.info("Resolving {}", bomDependency)
                    File bomFile = project.configurations.detachedConfiguration(
                        project.dependencies.create(bomDependency)
                    ).singleFile
                    def bom = new XmlSlurper().parse(bomFile)

                    bom.dependencyManagement.dependencies.dependency.each { importedDependency ->
                        if (Boolean.parseBoolean(importedDependency.optional?.text() ?: 'false')) {
                            return
                        }

                        String groupId = importedDependency.groupId.text()
                        String artifactId = importedDependency.artifactId.text()
                        String version = importedDependency.version.text()
                        String scope = importedDependency.scope?.text() ?: 'compile'

                        groupId = groupId == '${project.groupId}' ? dependency.group : groupId
                        version = version == '${project.version}' ? dependency.version : version

                        String dependencyCoordinates = [groupId, artifactId, version].join(':')
                        project.logger.info("Processing {} in scope {}", dependencyCoordinates, scope)

                        appendDependency(artifactId, scope, dependencyCoordinates)
                    }
                } else {
                    project.logger.warn("Dependency {}:{}:{} does not appear to be a valid Basilisk plugin!",
                        dependency.group, dependency.name, dependency.version)
                }
            }
        }

        private BasiliskExtension getBasiliskExtension() {
            project.extensions.getByName(BASILISK_CONFIGURATION)
        }

        private void appendDependency(String artifactId, String scope, String dependencyCoordinates) {
            Map projectDependencyMap = DEPENDENCY_MAP.get(project.name, [:])
            if (artifactId.endsWith('-compile')) {
                projectDependencyMap.get('compileOnly', []) << dependencyCoordinates
                projectDependencyMap.get('testCompileOnly', []) << dependencyCoordinates
            } else if (scope == 'test' && artifactId.endsWith('-test')) {
                projectDependencyMap.get('testCompile', []) << dependencyCoordinates
            } else {
                projectDependencyMap.get(scope, []) << dependencyCoordinates
            }
        }
    }
}