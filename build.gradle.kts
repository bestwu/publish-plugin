import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = "1.2.50"

plugins {
    kotlin("jvm") version "1.2.50"
    id("cn.bestwu.plugin-publish")
}

group = "cn.bestwu.gradle"
version = "0.0.20"

repositories {
    jcenter()
    gradlePluginPortal()
}
dependencies {
    compile(kotlin("reflect", kotlinVersion))
    compile(gradleApi())
    compile("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.2")
    compile("com.gradle.publish:plugin-publish-plugin:0.9.10")
    compile("org.jetbrains.dokka:dokka-gradle-plugin:0.9.17")

    compileOnly("org.jfrog.buildinfo:build-info-extractor-gradle:4.7.4") {
        exclude(module = "groovy-all")
    }
    testCompile("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}
