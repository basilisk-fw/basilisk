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
package org.kordamp.basilisk.gradle.tasks

import groovy.xml.MarkupBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * @author Andres Almiray
 */
class GenerateBomTask extends DefaultTask {
    static final TASK_NAME = 'generateBom'

    @OutputDirectory File outputDir
    @Input List<String> additionalDependencies = []

    GenerateBomTask() {
        outputDir = project.file("${project.buildDir.path}/bom")
    }

    @OutputFile
    File getOutputFile() {
        return new File(getOutputDir(), "${project.name}-${project.version}.pom")
    }

    @TaskAction
    void generate() {
        outputDir.mkdirs()

        // force evaluation of 'publishJars' property
        Set includedProjects = project.subprojects.grep {
            "${it.publishJars}" == 'true'
        }

        MarkupBuilder pom = new MarkupBuilder(new FileWriter(getOutputFile()))
        pom.mkp.xmlDeclaration(version: '1.0', encoding: 'UTF-8')
        pom.project(xmlns: 'http://maven.apache.org/POM/4.0.0',
            'xsi:schemaLocation': 'http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd',
            'xmlns:xsi': 'http://www.w3.org/2001/XMLSchema-instance') {
            modelVersion('4.0.0')
            groupId(project.group)
            artifactId(project.name)
            version(project.version)
            packaging('pom')
            dependencyManagement {
                dependencies {
                    includedProjects.each { prj ->
                        String projectName = prj.name
                        String scopeName = 'compile'
                        boolean opt = prj.hasProperty('optional') ? prj.optional : false
                        if (projectName.endsWith('-compile')) scopeName = 'provided'
                        if (projectName.endsWith('-test')) scopeName = 'test'

                        dependency {
                            groupId('${project.groupId}')
                            artifactId(projectName)
                            version('${project.version}')
                            scope(scopeName)
                            if (opt) optional(true)
                        }
                    }
                    additionalDependencies.each { String dep ->
                        def (groupName, artifactName, versionNum) = dep.split(':')
                        String scopeName = 'compile'
                        if (artifactName.endsWith('-compile')) scopeName = 'provided'
                        if (artifactName.endsWith('-test')) scopeName = 'test'

                        dependency {
                            groupId(groupName)
                            artifactId(artifactName)
                            version(versionNum)
                            scope(scopeName)
                        }
                    }
                }
            }
        }
    }
}
