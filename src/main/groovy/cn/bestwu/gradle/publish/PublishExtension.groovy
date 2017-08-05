package cn.bestwu.gradle.publish

import org.gradle.api.tasks.Input

/**
 * @author Peter Wu
 */
class PublishExtension {
    @Input
    String projectUrl

    @Input
    String vcsUrl
}
