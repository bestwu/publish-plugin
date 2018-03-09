import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.version
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version ("1.2.10")
    id("cn.bestwu.plugin-publish") version "0.0.18"
}

group = "cn.bestwu.samples"
version = "1.0"


repositories {
    jcenter()
}
dependencies {
    compile(gradleApi())
    compile(kotlin("stdlib","1.2.10"))
}
