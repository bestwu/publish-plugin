package cn.bestwu.gradle.publish

import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.jetbrains.dokka.gradle.DokkaTask

/**
 *
 * 注册task
 *
 * @author Peter Wu
 */
class KotlinPublishPlugin extends AbstractPlugin {

    @Override
    void apply(Project project) {
        sourcesJar(project)

        project.plugins.apply('org.jetbrains.dokka')
        project.afterEvaluate {
            project.task('dokkaJavadoc', type: DokkaTask) {
                outputFormat = "javadoc"
                outputDirectory = "$project.buildDir/dokkaJavadoc"
            }

            project.task('javadocJar', type: Jar) {
                classifier = 'javadoc'
                from project.dokkaJavadoc
            }

            configPublish(project)
        }
    }
}