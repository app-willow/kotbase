import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget()
    jvm()
    // Link the vendored CouchbaseLite XCFramework directly (instead of the deprecated
    // Kotlin CocoaPods plugin). The iosApp embeds composeApp.framework + the XCFramework
    // via its Xcode project. See vendor/CouchbaseLite/.
    iosArm64 {
        binaries.framework {
            baseName = "composeApp"
            val path = "$rootDir/vendor/CouchbaseLite/CouchbaseLite.xcframework/ios-arm64"
            linkerOpts("-F$path", "-framework", "CouchbaseLite", "-rpath", path)
            binaryOption("bundleId", "dev.kotbase.gettingstarted.compose")
        }
    }
    listOf(
        iosX64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "composeApp"
            val path = "$rootDir/vendor/CouchbaseLite/CouchbaseLite.xcframework/ios-arm64_x86_64-simulator"
            linkerOpts("-F$path", "-framework", "CouchbaseLite", "-rpath", path)
            binaryOption("bundleId", "dev.kotbase.gettingstarted.compose")
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotbase)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(libs.kotlinx.datetime)
        }
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            api(libs.androidx.activity.compose)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }

        configureEach {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
        }
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
}

android {
    namespace = "dev.kotbase.gettingstarted.compose"
    compileSdk = 36
    defaultConfig {
        applicationId = "dev.kotbase.gettingstarted.compose"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            packageName ="Kotbase"
        }
        buildTypes.release.proguard {
            configurationFiles.from("proguard-rules.pro")
        }
    }
}
