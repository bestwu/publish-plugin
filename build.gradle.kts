import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.10"
    id("cn.bestwu.plugin-publish")
}

group = "cn.bestwu.gradle"
version = "0.0.31"

repositories {
    jcenter()
    gradlePluginPortal()
}
dependencies {
    compile(kotlin("reflect"))
    compile(gradleApi())
    compile("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")
    compile("com.gradle.publish:plugin-publish-plugin:0.10.0")
    compile("org.jetbrains.dokka:dokka-gradle-plugin:0.9.17")

    compileOnly("org.jfrog.buildinfo:build-info-extractor-gradle:4.8.1") {
        exclude(module = "groovy-all")
    }
    testCompile(kotlin("test-junit"))
}
tasks {
    "dokkaJavadoc"(DokkaTask::class) {
        noStdlibLink = true
    }
}