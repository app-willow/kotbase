/*
 * Copyright 2026 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.kotbase.gradle

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget

/**
 * Applies the CouchbaseLite XCFramework link options to every Apple ([Family.OSX]/[Family.IOS])
 * Kotlin Multiplatform target of the project, so consumers using Kotbase no longer have to repeat
 * `binaries.framework { linkerOpts("-F...", "-framework", "CouchbaseLite", "-rpath", ...) }` for
 * each target.
 *
 * Apply alongside the Kotlin Multiplatform plugin:
 * ```
 * plugins {
 *     kotlin("multiplatform")
 *     id("dev.kotbase.couchbase-lite-link")
 * }
 * couchbaseLite {
 *     xcframework = layout.projectDirectory.dir("vendor/CouchbaseLite/CouchbaseLite.xcframework").asFile
 * }
 * ```
 *
 * PROTOTYPE SCOPE: automates the per-target linking only; the XCFramework must already be present
 * on disk. Productionization (tracked separately) will add versioned download from
 * packages.couchbase.com with SHA-256 verification (and Enterprise auth), so `couchbaseLite { version = ... }`
 * is enough with no manual vendoring.
 */
class CouchbaseLiteLinkPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("couchbaseLite", CouchbaseLiteLinkExtension::class.java).apply {
            edition.convention(CouchbaseLiteEdition.COMMUNITY)
            frameworkName.convention("CouchbaseLite")
        }

        // React whenever the Kotlin Multiplatform plugin is applied (order-independent).
        project.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
            kotlin.targets.withType(KotlinNativeTarget::class.java).configureEach {
                if (!konanTarget.family.isAppleFamily) return@configureEach
                val sliceDir = appleSlice(konanTarget)
                binaries.all {
                    // Resolve lazily so the xcframework path can be set after the plugin is applied.
                    val xcframework = extension.xcframework.orNull
                        ?: throw GradleException(
                            "couchbaseLite.xcframework must be set to the CouchbaseLite.xcframework directory"
                        )
                    val path = xcframework.resolve(sliceDir).absolutePath
                    linkerOpts("-F$path", "-framework", extension.frameworkName.get(), "-rpath", path)
                }
            }
        }
    }

    /** Maps a Kotlin/Native Apple target to its XCFramework slice directory name. */
    private fun appleSlice(target: KonanTarget): String = when (target) {
        KonanTarget.IOS_ARM64 -> "ios-arm64"
        KonanTarget.IOS_SIMULATOR_ARM64, KonanTarget.IOS_X64 -> "ios-arm64_x86_64-simulator"
        KonanTarget.MACOS_ARM64, KonanTarget.MACOS_X64 -> "macos-arm64_x86_64"
        else -> throw GradleException("Unsupported Apple target for CouchbaseLite linking: $target")
    }
}
