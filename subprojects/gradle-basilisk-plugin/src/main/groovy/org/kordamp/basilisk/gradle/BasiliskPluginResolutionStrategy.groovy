/*
 * Copyright 2008-2015 the original author or authors.
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

    static void applyTo(Project project) {
        BasiliskPluginDependencyResolver resolver = new BasiliskPluginDependencyResolver(project)
        project.configurations.getByName(BASILISK_CONFIGURATION).incoming.beforeResolve(resolver)
        CONFIGURATION_NAMES.each { String configurationName ->
            Configuration configuration = project.configurations.getByName(configurationName)
            configuration.incoming.beforeResolve(new BasiliskDependencyResolver(configurationName, resolver))
        }
    }

    private static class BasiliskDependencyResolver implements Action<ResolvableDependencies> {
        private final BasiliskPluginDependencyResolver resolver
        private final String configurationName

        BasiliskDependencyResolver(String configurationName, BasiliskPluginDependencyResolver resolver) {
            this.resolver = resolver
            this.configurationName = configurationName
        }

        @Override
        void execute(ResolvableDependencies resolvableDependencies) {
            if (resolver.project.extensions.getByName(BASILISK_CONFIGURATION).disableDependencyResolution) {
                return
            }

            if (!resolver.dependencyMap) {
                resolver.project.configurations.getByName(BASILISK_CONFIGURATION).resolve()
            }
            resolver.dependencyMap[configurationName].each { String dependency ->
                resolver.project.dependencies.add(configurationName, dependency)
            }
        }
    }

    private static class BasiliskPluginDependencyResolver implements Action<ResolvableDependencies> {
        final Project project
        final Map<String, List<String>> dependencyMap = [:]

        BasiliskPluginDependencyResolver(Project project) {
            this.project = project
        }

        @Override
        void execute(ResolvableDependencies resolvableDependencies) {
            String toolkit = basiliskExtension.toolkit
            project.logger.info("UI toolkit for project {} is {}", project.name, toolkit)
            String toolkitRegex = (BasiliskExtension.TOOLKIT_NAMES - toolkit).join('|')

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

                        if (toolkit) {
                            if (artifactId =~ /$toolkitRegex/) {
                                return
                            } else {
                                appendDependency(artifactId, scope, dependencyCoordinates)
                            }
                        } else {
                            appendDependency(artifactId, scope, dependencyCoordinates)
                        }
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
            if (artifactId.endsWith('-compile')) {
                project.logger.info("Adding {} to 'compileOnly' configuration", dependencyCoordinates)
                dependencyMap.get('compileOnly', []) << dependencyCoordinates
                project.logger.info("Adding {} to 'testCompileOnly' configuration", dependencyCoordinates)
                dependencyMap.get('testCompileOnly', []) << dependencyCoordinates
            } else if (scope == 'test' && artifactId.endsWith('-test')) {
                project.logger.info("Adding {} to 'testCompile' configuration", dependencyCoordinates)
                dependencyMap.get('testCompile', []) << dependencyCoordinates
            } else {
                project.logger.info("Adding {} to '{}' configuration", dependencyCoordinates, scope)
                dependencyMap.get(scope, []) << dependencyCoordinates
            }
        }
    }
}