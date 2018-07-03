import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude
import org.gradle.kotlin.dsl.*

plugins {
    kotlin("jvm") version ("1.2.10")
    id("cn.bestwu.kotlin-publish")
}
group = "cn.bestwu.samples"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
}
dependencies {
    compile(kotlin("stdlib"))
}