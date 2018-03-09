import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.version

plugins {
    groovy
    id("cn.bestwu.groovy-publish") version "0.0.18"
}

group = "cn.bestwu.samples"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
}

dependencies {
    compile("org.codehaus.groovy:groovy-all:2.4.13")
}