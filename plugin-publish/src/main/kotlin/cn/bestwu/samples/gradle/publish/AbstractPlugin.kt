package cn.bestwu.samples.gradle.publish

import com.jfrog.bintray.gradle.BintrayExtension
import groovy.lang.Closure
import groovy.util.Node
import groovy.util.NodeList
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.internal.Cast
import org.gradle.jvm.tasks.Jar
import org.jfrog.gradle.plugin.artifactory.dsl.ArtifactoryPluginConvention
import org.jfrog.gradle.plugin.artifactory.dsl.DoubleDelegateWrapper
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask

/**
 * @author Peter Wu
 * @since
 */
/**
 * 获取单一节点
 */
fun Node.getAt(name: String): Node {
    return (get(name) as NodeList)[0] as Node
}

/**
 * 配置工具类
 */
class KotlinClosure1<in T : Any, V : Any>(
        /**
         *
         */
        private val function: T.() -> V?,
        owner: Any? = null,
        thisObject: Any? = null) : Closure<V?>(owner, thisObject) {
    /**
     * 实际调用方法
     */
    @Suppress("unused") // to be called dynamically by Groovy
    fun doCall(it: T): V? = it.function()
}

/**
 * 配置工具类
 */
fun <T : Any> Any.closureOf(action: T.() -> Unit): Closure<Any?> =
        KotlinClosure1(action, this, this)

/**
 * 配置工具类
 */
fun <T> Any.delegateClosureOf(action: T.() -> Unit) =
        object : Closure<Unit>(this, this) {
            @Suppress("unused") // to be called dynamically by Groovy
            fun doCall() = Cast.uncheckedCast<T>(delegate).action()
        }

/**
 * 抽象类
 */
abstract class AbstractPlugin : Plugin<Project> {
    /**
     * 公用配置
     */
    protected fun configPublish(project: Project, publicationName: Array<String> = arrayOf()) {

        if (!project.plugins.hasPlugin("maven-publish")) {
            project.plugins.apply("maven-publish")
        }
        if (!project.plugins.hasPlugin("com.jfrog.bintray")) {
            project.plugins.apply("com.jfrog.bintray")
        }

        project.tasks.withType(Javadoc::class.java) {
            with(it.options as StandardJavadocDocletOptions) {
                encoding = "UTF-8"
                charSet = "UTF-8"
                isAuthor = true
                isVersion = true
            }
        }

        val projectUrl = project.findProperty("projectUrl") as String?
        val projectVcsUrl = project.findProperty("vcsUrl") as String?

        project.extensions.configure(PublishingExtension::class.java) { p ->
            p.publications.create("mavenJava", MavenPublication::class.java) { m ->
                var jar = "jar"
                if (project.plugins.hasPlugin("war")) {
                    m.from(project.components.getByName("web"))
                    jar = "war"
                } else {
                    m.from(project.components.getByName("java"))
                }

                m.artifact(project.tasks.getByName("sourcesJar")) {
                    it.classifier = "sources"
                }

                m.artifact(project.tasks.getByName("javadocJar")) {
                    it.classifier = "javadoc"
                }

                m.pom.withXml { po ->
                    val root = po.asNode()

                    //compile
                    setDependencyScope(root, project, "compile")
                    //provided
                    setDependencyScope(root, project, "provided")
                    //optional
                    setDependencyScope(root, project, "optional")

                    root.apply {
                        appendNode("name", project.name)
                        appendNode("packaging", jar)
                        appendNode("description", project.name)
                        if (!projectUrl.isNullOrBlank()) {
                            appendNode("url", projectUrl)
                        }

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

        val publicationNames = mutableSetOf<String>()
        publicationNames.add("mavenJava")
        publicationNames.addAll(publicationName)
        if (project.plugins.hasPlugin("com.jfrog.artifactory")) {
            //发布到snapshot
            val conv = project.convention.plugins.get("artifactory") as ArtifactoryPluginConvention
            conv.setContextUrl(project.findProperty("snapshotContextUrl"))
            conv.publish(closureOf<PublisherConfig> {

                repository(closureOf<DoubleDelegateWrapper> {

                    //                        setRepoKey(project.findProperty("snapshotRepoKey"))
//                        setUsername(project.findProperty("snapshotUsername"))
//                        setPassword(project.findProperty("snapshotPassword"))
//                        setMavenCompatible(true)
                    setProperty("repoKey", project.findProperty("snapshotRepoKey"))
                    setProperty("username", project.findProperty("snapshotUsername"))
                    setProperty("password", project.findProperty("snapshotPassword"))
                    setProperty("maven", true)
                })
                defaults(closureOf<ArtifactoryTask> {
                    setPublishArtifacts(true)
                    publications(*publicationNames.toTypedArray())
                })
            })

            project.tasks.getByName("artifactoryPublish").dependsOn("publishToMavenLocal")
        }


        //发布到私有仓库并同步中央仓库及mavenCentral

        project.extensions.configure(BintrayExtension::class.java) { bintray ->
            with(bintray) {
                user = project.findProperty("bintrayUsername") as String
                key = project.findProperty("bintrayApiKey") as String
                setPublications(*publicationNames.toTypedArray())

                publish = true
                //    override = true

                with(pkg) {
                    repo = "maven"
                    name = project.name
                    desc = project.name
                    if (!projectUrl.isNullOrBlank()) {
                        websiteUrl = projectUrl
                    }
                    if (!projectVcsUrl.isNullOrBlank())
                        vcsUrl = projectVcsUrl
                    setLicenses(project.findProperty("license.shortName") as String)
                    setLabels(project.name)

                    with(version) {
                        desc = "${project.name} ${project.version}"
                        with(mavenCentralSync) {
                            sync = true
                            user = project.findProperty("mavenCentralUsername") as String
                            password = project.findProperty("mavenCentralPassword") as String
                            close = "1"
                        }
                    }
                }
            }
        }
        project.tasks.getByName("bintrayUpload").dependsOn("publishToMavenLocal")
    }

    private fun setDependencyScope(root: Node, project: Project, scope: String) {
        root.getAt("dependencies").children().filter {
            val node = it as Node
            node.getAt("scope").text() == "runtime" && project.configurations.getAt(scope).dependencies.any { dep ->
                dep.group == node.getAt("groupId").text() && dep.name == node.getAt("artifactId").text()
            }
        }.forEach {
            val node = it as Node
            if (scope == "optional") {
                node.getAt("scope").setValue("compile")
                node.appendNode("optional", "true")
            } else
                node.getAt("scope").setValue(scope)
        }
    }

    /**
     * 源文件打包Task
     */
    protected fun sourcesJar(project: Project): Task {
        project.pluginManager.apply(JavaPlugin::class.java)
        return project.tasks.create("sourcesJar", Jar::class.java) {
            it.dependsOn("classes")
            it.classifier = "sources"
            it.from(project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets.findByName("main").allSource)
        }
    }

}
