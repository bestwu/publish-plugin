package cn.bestwu.samples.gradle.publish

import com.gradle.publish.PluginBundleExtension
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.gradle.plugin.use.resolve.internal.ArtifactRepositoryPluginResolver.PLUGIN_MARKER_SUFFIX
import org.jetbrains.dokka.gradle.DokkaTask


/**
 *
 * 注册task
 *
 * @author Peter Wu
 */
class PluginPublishPlugin : AbstractPlugin() {
    /**
     * {@inheritDoc}
     */
    override fun apply(project: Project) {
        sourcesJar(project)

        project.plugins.apply("java-gradle-plugin")
        project.plugins.apply("com.gradle.plugin-publish")

        project.extensions.configure(GradlePluginDevelopmentExtension::class.java) {
            it.isAutomatedPublishing = false
        }

        project.afterEvaluate {

            when {
                project.plugins.hasPlugin("org.jetbrains.kotlin.jvm") -> {
                    if (!project.plugins.hasPlugin("org.jetbrains.dokka"))
                        project.plugins.apply("org.jetbrains.dokka")

                    project.tasks.create("dokkaJavadoc", DokkaTask::class.java) {
                        it.outputFormat = "javadoc"
                        it.outputDirectory = "${project.buildDir}/dokkaJavadoc"
                    }

                    project.tasks.create("javadocJar", Jar::class.java) {
                        it.classifier = "javadoc"
                        it.from(project.tasks.findByName("dokkaJavadoc").outputs)
                    }
                }
                project.plugins.hasPlugin("groovy") -> project.tasks.create("javadocJar", Jar::class.java) {
                    it.classifier = "javadoc"
                    it.from(project.tasks.findByName("groovydoc").outputs)
                }
                else -> project.tasks.create("javadocJar", Jar::class.java) {
                    it.classifier = "javadoc"
                    it.from(project.tasks.findByName("javadoc").outputs)
                }
            }

            val projectUrl = project.findProperty("projectUrl") as String?
            val projectVcsUrl = project.findProperty("vcsUrl") as String?
            val gradlePlugin = project.findProperty("gradlePlugin.plugins") as String?
            gradlePlugin?.split(",")?.forEach { plugin ->
                val pluginId = project.findProperty("gradlePlugin.plugins.$plugin.id") as String

                project.extensions.configure(GradlePluginDevelopmentExtension::class.java) {
                    it.plugins.create(plugin) {
                        it.id = pluginId
                        it.implementationClass = project.findProperty("gradlePlugin.plugins.$plugin.implementationClass") as String
                    }
                }
                project.extensions.configure(PluginBundleExtension::class.java) {
                    it.plugins.create(plugin) {
                        it.id = pluginId
                        it.displayName = plugin
                    }
                }
            }


            project.extensions.configure(GradlePluginDevelopmentExtension::class.java) {
                with(it.plugins) {
                    configPublish(project, names.toTypedArray())
                    forEach { plugin ->
                        project.extensions.configure(PublishingExtension::class.java) { p ->
                            val publication = p.publications.create(plugin.name, MavenPublication::class.java)
                            publication.groupId = plugin.id
                            publication.artifactId = plugin.id + PLUGIN_MARKER_SUFFIX
                            publication.pom.withXml { po ->
                                po.asNode().apply {

                                    appendNode("name", project.name)
                                    appendNode("description", project.name)
                                    if (!projectUrl.isNullOrBlank())
                                        appendNode("url", projectUrl)

                                    val dependency = appendNode("dependencies").appendNode("dependency")
                                    dependency.appendNode("groupId", project.group)
                                    dependency.appendNode("artifactId", project.name)
                                    dependency.appendNode("version", project.version)

                                    val license = appendNode("licenses").appendNode("license")
                                    license.appendNode("name", project.findProperty("license.name"))
                                    license.appendNode("url", project.findProperty("license.url"))
                                    license.appendNode("distribution", project.findProperty("license.distribution"))

                                    val developer = appendNode("developers").appendNode("developer")
                                    developer.appendNode("id", project.findProperty("developer.id"))
                                    developer.appendNode("name", project.findProperty("developer.name"))
                                    developer.appendNode("email", project.findProperty("developer.email"))

                                    if (projectVcsUrl != null && projectVcsUrl.isNotBlank()) {
                                        val scm = appendNode("scm")
                                        scm.appendNode("url", projectVcsUrl)
                                        val tag = if (projectVcsUrl.contains("git")) "git" else if (projectVcsUrl.contains("svn")) "svn" else projectVcsUrl
                                        scm.appendNode("connection", "scm:$tag:$projectVcsUrl")
                                        scm.appendNode("developerConnection", "scm:$tag:$projectVcsUrl")
                                    }
                                }

                            }
                        }
                    }
                }
            }

            //发布到gradle plugins
            val name = project.name
            project.extensions.configure(PluginBundleExtension::class.java) {
                if (!projectUrl.isNullOrBlank())
                    it.website = projectUrl
                if (!projectVcsUrl.isNullOrBlank())
                    it.vcsUrl = projectVcsUrl
                it.description = name
                it.tags = setOf(name)
            }
        }
    }

}