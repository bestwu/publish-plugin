package cn.bestwu.gradle.publish

import com.gradle.publish.PluginBundleExtension
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
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
        beforeConfigigure(project)

        project.plugins.apply("java-gradle-plugin")
        project.plugins.apply("com.gradle.plugin-publish")

        project.extensions.configure(GradlePluginDevelopmentExtension::class.java) {
            it.isAutomatedPublishing = false
        }
        if (project.plugins.hasPlugin("org.jetbrains.kotlin.jvm")) {
            if (!project.plugins.hasPlugin("org.jetbrains.dokka"))
                project.plugins.apply("org.jetbrains.dokka")

            project.tasks.create("dokkaJavadoc", DokkaTask::class.java) {
                it.outputFormat = "javadoc"
                it.outputDirectory = "${project.buildDir}/dokkaJavadoc"
                it.noStdlibLink = true
            }
        }
        project.afterEvaluate { _ ->

            configureDoc(project)

            val projectUrl = project.findProperty("projectUrl") as? String
            val projectVcsUrl = project.findProperty("vcsUrl") as? String
            configureGradlePlugins(project)


            configurePluginsPublication(project, projectUrl, projectVcsUrl)

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

    /**
     * 配置每个插件的发布信息
     */
    private fun PluginPublishPlugin.configurePluginsPublication(project: Project, projectUrl: String?, projectVcsUrl: String?) {
        project.extensions.configure(GradlePluginDevelopmentExtension::class.java) {
            with(it.plugins) {
                configPublish(project, names.toTypedArray())
                forEach { plugin ->
                    project.extensions.configure(PublishingExtension::class.java) { p ->
                        val publication = p.publications.create(plugin.name, MavenPublication::class.java)
                        publication.groupId = plugin.id
                        publication.artifactId = plugin.id + ".gradle.plugin"
                        publication.pom.withXml { po ->
                            po.asNode().apply {
                                val dependency = appendNode("dependencies").appendNode("dependency")
                                dependency.appendNode("groupId", project.group)
                                dependency.appendNode("artifactId", project.name)
                                dependency.appendNode("version", project.version)

                                configurePomXml(project, projectUrl, projectVcsUrl)
                            }

                        }
                    }
                }
            }
        }
    }

    /**
     * 配置文档生成
     */
    private fun configureDoc(project: Project) {
        when {
            project.plugins.hasPlugin("org.jetbrains.kotlin.jvm") -> {
                project.tasks.create("javadocJar", Jar::class.java) {
                    it.classifier = "javadoc"
                    it.from(project.tasks.getByName("dokkaJavadoc").outputs)
                }
            }
            project.plugins.hasPlugin("groovy") -> project.tasks.create("javadocJar", Jar::class.java) {
                it.classifier = "javadoc"
                it.from(project.tasks.getByName("groovydoc").outputs)
            }
            else -> project.tasks.create("javadocJar", Jar::class.java) {
                it.classifier = "javadoc"
                it.from(project.tasks.getByName("javadoc").outputs)
            }
        }
    }


    /**
     * 配置GradlePlugin
     */
    private fun configureGradlePlugins(project: Project) {
        val gradlePlugin = project.findProperty("gradlePlugin.plugins") as? String
        gradlePlugin?.split(",")?.forEach { plugin ->
            val pluginId = project.findProperty("gradlePlugin.plugins.$plugin.id") as String

            project.extensions.configure(GradlePluginDevelopmentExtension::class.java) { extension ->
                extension.plugins.create(plugin) {
                    it.id = pluginId
                    it.implementationClass = project.findProperty("gradlePlugin.plugins.$plugin.implementationClass") as String
                }
            }
            project.extensions.configure(PluginBundleExtension::class.java) { extension ->
                extension.plugins.create(plugin) {
                    it.id = pluginId
                    it.displayName = plugin
                }
            }
        }
    }

}