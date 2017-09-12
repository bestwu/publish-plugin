package cn.bestwu.gradle.publish

import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar

/**
 *
 * 注册task
 *
 * @author Peter Wu
 */
class PublishPlugin extends AbstractPlugin {

    @Override
    void apply(Project project) {
        project.afterEvaluate {
            project.task('javadocJar', type: Jar) {
                classifier = 'javadoc'
                from project.javadoc
            }

            configPublish(project)
        }
    }
}