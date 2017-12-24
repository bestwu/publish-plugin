import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.version

plugins {
    kotlin("jvm") version ("1.2.10")
    id("cn.bestwu.kotlin-publish") version "0.0.13"
}

group = "cn.bestwu.samples"
version = "1.0"

tasks.withType(JavaCompile::class.java) {
    sourceCompatibility = "1.7"
}

repositories {
    jcenter()
    maven{url=uri("https://plugins.gradle.org/m2")}
}

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib:1.2.0")
    compile("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.7.3")
    compile("com.gradle.publish:plugin-publish-plugin:0.9.7")

    compileOnly("org.jfrog.buildinfo:build-info-extractor-gradle:4.5.4") {
        exclude(module = "groovy-all")
    }
    testCompile("junit:junit:4.12")
}
