package cn.bestwu.gradle.publish

import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.jetbrains.dokka.gradle.DokkaTask

import static org.gradle.plugin.use.resolve.internal.ArtifactRepositoryPluginResolver.PLUGIN_MARKER_SUFFIX

/**
 *
 * 注册task
 *
 * @author Peter Wu
 */
class PluginPublishPlugin extends AbstractPlugin {

    @Override
    void apply(Project project) {
        project.plugins.apply('java-gradle-plugin')
        project.plugins.apply('com.gradle.plugin-publish')

        project.gradlePlugin.automatedPublishing false

        project.afterEvaluate {
            if (project.plugins.hasPlugin('org.jetbrains.kotlin.jvm')) {
                if (!project.plugins.hasPlugin('org.jetbrains.dokka'))
                    project.plugins.apply('org.jetbrains.dokka')

                project.task('dokkaJavadoc', type: DokkaTask) {
                    outputFormat = "javadoc"
                    outputDirectory = "$project.buildDir/dokkaJavadoc"
                }

                project.task('javadocJar', type: Jar) {
                    classifier = 'javadoc'
                    from project.dokkaJavadoc
                }
            } else if (project.plugins.hasPlugin('groovy')) {
                project.task('javadocJar', type: Jar) {
                    classifier = 'javadoc'
                    from project.groovydoc
                }
            } else {
                project.task('javadocJar', type: Jar) {
                    classifier = 'javadoc'
                    from project.javadoc
                }
            }

            def projectUrl = project.findProperty('projectUrl')
            def projectVcsUrl = project.findProperty('vcsUrl')
            project.findProperty('gradlePlugin.plugins').toString().tokenize(',').each { plugin ->
                project.gradlePlugin.plugins.create(plugin) {
                    id = project.findProperty("gradlePlugin.plugins.${plugin}.id")
                    implementationClass = project.findProperty("gradlePlugin.plugins.${plugin}.implementationClass")
                }

                project.pluginBundle.plugins.create(plugin) {
                    id = project.findProperty("pluginBundle.plugins.${plugin}.id")
                    displayName = plugin
                }
            }
            def gradlePlugins = project.gradlePlugin.plugins
            configPublish(project, gradlePlugins.names.toArray())
            gradlePlugins.forEach({ declaration ->
                def publication = project.publishing.publications.create(declaration.name, MavenPublication.class)
                publication.groupId declaration.id
                publication.artifactId declaration.id + PLUGIN_MARKER_SUFFIX
                publication.pom.withXml {
                    asNode().children().last() + {
                        resolveStrategy = DELEGATE_FIRST
                        name "${project.name}"
                        description "${project.name}"
                        url projectUrl

                        dependencies {
                            dependency {
                                groupId project.group
                                artifactId project.name
                                version project.version
                            }
                        }
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
            })
            //发布到gradle plugins
            String name = "${project.name}"
            project.pluginBundle {
                website = projectUrl
                vcsUrl = projectVcsUrl
                description = "${name}"
                tags = [name]
            }
        }
    }

}