import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.version

plugins {
    groovy
    id("cn.bestwu.groovy-publish") version "0.0.14"
}

group = "cn.bestwu.samples"
version = "1.0-SNAPSHOT"

tasks.withType(JavaCompile::class.java) {
    sourceCompatibility = "1.7"
}

repositories {
    jcenter()
    maven{url=uri("https://plugins.gradle.org/m2")}
}

dependencies {
    compile("org.codehaus.groovy:groovy-all:2.4.13")
    compile("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.7.3")
    compile("com.gradle.publish:plugin-publish-plugin:0.9.7")

    compileOnly("org.jfrog.buildinfo:build-info-extractor-gradle:4.5.4") {
        exclude(module = "groovy-all")
    }
    testCompile("junit:junit:4.12")
}
