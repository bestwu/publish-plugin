import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = "1.2.51"

plugins {
    kotlin("jvm") version "1.2.51"
    id("cn.bestwu.plugin-publish")
}

group = "cn.bestwu.gradle"
version = "0.0.22"

repositories {
    jcenter()
    gradlePluginPortal()
}
dependencies {
    compile(kotlin("reflect", kotlinVersion))
    compile(gradleApi())
    compile("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.3")
    compile("com.gradle.publish:plugin-publish-plugin:0.9.10")
    compile("org.jetbrains.dokka:dokka-gradle-plugin:0.9.17")

    compileOnly("org.jfrog.buildinfo:build-info-extractor-gradle:4.7.5") {
        exclude(module = "groovy-all")
    }
    testCompile("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}
