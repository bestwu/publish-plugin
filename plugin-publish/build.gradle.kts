import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.version
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version ("1.2.10")
    id("cn.bestwu.plugin-publish") version "0.0.14"
}

group = "cn.bestwu.samples"
version = "1.0"


tasks.withType(JavaCompile::class.java) {
    sourceCompatibility = "1.7"
    targetCompatibility = "1.7"
    options.encoding = "UTF-8"
}

repositories {
    jcenter()
    maven{url=uri("https://plugins.gradle.org/m2")}
}
dependencies {
    compile(gradleApi())
    compile("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.7.3")
    compile("com.gradle.publish:plugin-publish-plugin:0.9.7")
    compile("org.jetbrains.dokka:dokka-gradle-plugin:0.9.15") {
        exclude(group = "org.jetbrains.kotlin")
    }

    compileOnly("org.jfrog.buildinfo:build-info-extractor-gradle:4.5.4") {
        exclude(module = "groovy-all")
    }
    testCompile("org.jetbrains.kotlin:kotlin-test-junit:1.1.61")

}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    languageVersion = "1.1"
    apiVersion = "1.1"
}
