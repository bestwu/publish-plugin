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
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    languageVersion = "1.1"
    apiVersion = "1.1"
}
