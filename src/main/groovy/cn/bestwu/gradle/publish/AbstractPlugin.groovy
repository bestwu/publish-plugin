package cn.bestwu.gradle.publish

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
/**
 *
 * @author Peter Wu
 * @since
 */
abstract class AbstractPlugin implements Plugin<Project> {
    protected void configPublish(Project project,Object... publicationName) {
        if (!project.plugins.hasPlugin('maven-publish'))
            project.plugins.apply('maven-publish')
        if (!project.plugins.hasPlugin('com.jfrog.bintray'))
            project.plugins.apply('com.jfrog.bintray')

        project.javadoc {
            options {
                encoding 'UTF-8'
                charSet 'UTF-8'
                author true
                version true
            }
        }

        project.task('sourcesJar', type: Jar, dependsOn: project.compileJava) {
            classifier = 'sources'
            from project.sourceSets.main.allSource
        }

        def projectUrl = project.findProperty('projectUrl')
        def projectVcsUrl = project.findProperty('vcsUrl')
        project.publishing {
            publications {
                mavenJava(MavenPublication) {
                    def jar = 'jar'
                    if (project.plugins.hasPlugin('war')) {
                        from project.components.web
                        jar = 'war'
                    } else {
                        from project.components.java
                    }

                    artifact(project.sourcesJar) {
                        classifier 'sources'
                    }

                    artifact(project.javadocJar) {
                        classifier 'javadoc'
                    }

                    pom.withXml {
                        Node root = asNode()

                        //compile
                        root.dependencies.'*'.findAll() {
                            it.scope.text() == 'runtime' && project.configurations.compile.dependencies.find { dep ->
                                dep.group == it.groupId.text() && dep.name == it.artifactId.text()
                            }
                        }.each {
                            it.scope*.value = 'compile'
                        }
                        //provided
                        root.dependencies.'*'.findAll() {
                            it.scope.text() == 'runtime' && project.configurations.provided.dependencies.find { dep ->
                                dep.group == it.groupId.text() && dep.name == it.artifactId.text()
                            }
                        }.each {
                            it.scope*.value = 'provided'
                        }
                        //optional
                        root.dependencies.'*'.findAll() {
                            it.scope.text() == 'runtime' && project.configurations.optional.dependencies.find { dep ->
                                dep.group == it.groupId.text() && dep.name == it.artifactId.text()
                            }
                        }.each {
                            it.scope*.value = 'compile'
                            it.appendNode('optional', 'true')
                        }

                        root.children().last() + {
                            resolveStrategy = DELEGATE_FIRST
                            name "${project.name}"
                            packaging jar
                            description "${project.name}"
                            url projectUrl
                            licenses {
                                license {
                                    name project.findProperty('license.name')
                                    url project.findProperty('license.url')
                                    distribution project.findProperty('license.distribution')
                                }
                            }
                            developers {
                                developer {
                                    id project.findProperty('developer.id')
                                    name project.findProperty('developer.name')
                                    email project.findProperty('developer.email')
                                }
                            }
                            scm {
                                url projectVcsUrl
                                connection "scm:git:$projectVcsUrl"
                                developerConnection "scm:git:$projectVcsUrl"
                            }
                        }
                    }
                }
            }
        }

        def publicationNames = []
        publicationNames.add('mavenJava')
        publicationNames.addAll(publicationName)
        if (project.plugins.hasPlugin('com.jfrog.artifactory')) {
            //发布到snapshot
            project.artifactory {
                contextUrl = project.findProperty('snapshotContextUrl')
                publish {
                    repository {
                        repoKey = project.findProperty('snapshotRepoKey')
                        username = project.findProperty('snapshotUsername')
                        password = project.findProperty('snapshotPassword')
                        maven = true
                    }
                    defaults {
                        publications publicationNames.toArray()
                        publishArtifacts = true
                    }
                }
            }
            project.artifactoryPublish.dependsOn project.publishToMavenLocal
        }

        //发布到私有仓库并同步中央仓库及mavenCentral
        project.bintray {
            user = project.findProperty('bintrayUsername')
            key = project.findProperty('bintrayApiKey')
            publications = publicationNames.toArray()

            publish = true
            //    override = true
            pkg {
                repo = 'maven'
                name = "${project.name}"
                desc = "${project.name}"
                websiteUrl = projectUrl
                vcsUrl = projectVcsUrl
                licenses = [project.findProperty('license.shortName')]
                labels = ["${project.name}"]

                version {
                    desc = "${project.name} ${project.version}"
                    mavenCentralSync {
                        sync = true
                        user = project.findProperty('mavenCentralUsername')
                        password = project.findProperty('mavenCentralPassword')
                        close = '1'
                    }
                }
            }
        }
        project.bintrayUpload.dependsOn project.publishToMavenLocal
    }
}
