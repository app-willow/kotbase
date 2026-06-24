plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

group = "dev.kotbase"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // The Kotlin Multiplatform Gradle plugin types (KotlinNativeTarget, KonanTarget) are needed at
    // compile time; they are provided by the consumer's build that also applies the KMP plugin.
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.21")
}

gradlePlugin {
    plugins {
        create("couchbaseLiteLink") {
            id = "dev.kotbase.couchbase-lite-link"
            implementationClass = "dev.kotbase.gradle.CouchbaseLiteLinkPlugin"
            displayName = "Kotbase CouchbaseLite link"
            description = "Automatically links the CouchbaseLite XCFramework for all Apple targets " +
                "of a Kotlin Multiplatform module using Kotbase."
        }
    }
}