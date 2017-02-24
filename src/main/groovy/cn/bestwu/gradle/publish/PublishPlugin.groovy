package cn.bestwu.gradle.publish

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar

/**
 *
 * 注册task
 *
 * @author Peter Wu
 */
class PublishPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.plugins.apply('java')
        project.plugins.apply('maven-publish')

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

        project.task('javadocJar', type: Jar) {
            classifier = 'javadoc'
            from project.javadoc
        }

        project.artifacts {
            archives project.javadocJar, project.sourcesJar
        }

        project.publishing {

//    repositories {
//        maven {
//            name 'Central'
//            url 'https://oss.sonatype.org/service/local/staging/deploy/maven2/'
//            credentials {
//                username mavenCentralUsername
//                password mavenCentralPassword
//            }
//        }
//    }
            publications {
                mavenJava(MavenPublication) {
                    if (project.plugins.hasPlugin('war')) {
                        from project.components.web
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
                            packaging 'jar'
                            description "${project.name}"
                            url "https://bitbucket.org/bestwu/${project.name}"
                            licenses {
                                license {
                                    name 'The Apache Software License, Version 2.0'
                                    url 'http://www.apache.org/licenses/LICENSE-2.0.txt'
                                    distribution 'repo'
                                }
                            }
                            developers {
                                developer {
                                    id 'bestwu'
                                    name 'Peter Wu'
                                    email 'piterwu@outlook.com'
                                }
                            }
                            scm {
                                url "https://bitbucket.org/bestwu/${project.name}"
                                connection "scm:https://bestwu@bitbucket.org/bestwu/${project.name}.git"
                                developerConnection "scm:git@bitbucket.org:bestwu/${project.name}.git"
                            }
                        }
                    }
                }
            }
        }

//apply plugin: 'maven'
//apply plugin: 'signing'
//
//signing {
//    required { !version.endsWith("-SNAPSHOT") && gradle.taskGraph.hasTask("uploadArchives") }
//    sign configurations.archives
//}
//
//uploadArchives {
//    repositories.mavenDeployer {
//        beforeDeployment { MavenDeployment deployment -> signing.signPom(deployment) }
//        name = 'mavenCentralReleaseDeployer'
//        repository(url: 'https://oss.sonatype.org/service/local/staging/deploy/maven2/') {
//            authentication(userName: mavenCentralUsername, password: mavenCentralPassword)
//            releases(updatePolicy: 'always')
//            snapshots(updatePolicy: 'always')
//        }
//        pom.project {
//            name "${project.name}"
//            packaging 'jar'
//            description "${project.name}"
//            url "https://bitbucket.org/bestwu/${project.name}"
//            licenses {
//                license {
//                    name 'The Apache Software License, Version 2.0'
//                    url 'http://www.apache.org/licenses/LICENSE-2.0.txt'
//                    distribution 'repo'
//                }
//            }
//            developers {
//                developer {
//                    id 'bestwu'
//                    name 'Peter Wu'
//                    email 'piterwu@outlook.com'
//                }
//            }
//            scm {
//                url "https://bitbucket.org/bestwu/${project.name}"
//                connection "scm:https://bestwu@bitbucket.org/bestwu/${project.name}.git"
//                developerConnection "scm:git@bitbucket.org:bestwu/${project.name}.git"
//            }
//        }
//    }
//}
        //发布到snapshot
        project.plugins.apply('com.jfrog.artifactory')
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
                    publications('mavenJava')
                    publishArtifacts = true
                }
            }
        }
        project.artifactoryPublish.dependsOn project.publishToMavenLocal

        //发布到私有仓库并同步中央仓库及mavenCentral
        project.plugins.apply('com.jfrog.bintray')
        project.bintray {
            user = project.findProperty('bintrayUsername')
            key = project.findProperty('bintrayApiKey')
            publications = ['mavenJava']

            publish = true
            //    override = true
            pkg {
                repo = 'maven'
                name = "${project.name}"
                desc = "${project.name}"
                vcsUrl = "https://bitbucket.org/bestwu/${project.name}"
                licenses = ['Apache-2.0']
                labels = ["${project.name}"]

                version {
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