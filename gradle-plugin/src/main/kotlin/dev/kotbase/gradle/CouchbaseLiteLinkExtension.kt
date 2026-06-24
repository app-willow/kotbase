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

import org.gradle.api.provider.Property

/**
 * Configuration for the `dev.kotbase.couchbase-lite-link` plugin.
 *
 * Example:
 * ```
 * couchbaseLite {
 *     edition = CouchbaseLiteEdition.ENTERPRISE
 *     // points at an unzipped CouchbaseLite[-Enterprise].xcframework
 *     xcframework = layout.projectDirectory.dir("vendor/CouchbaseLite/CouchbaseLite.xcframework").asFile
 * }
 * ```
 */
abstract class CouchbaseLiteLinkExtension {

    /** Community (default) or Enterprise. Controls only the framework binary name fallback. */
    abstract val edition: Property<CouchbaseLiteEdition>

    /**
     * Path to the unzipped `CouchbaseLite.xcframework` directory (containing the per-platform
     * slice folders such as `ios-arm64` and `ios-arm64_x86_64-simulator`).
     *
     * Prototype: the framework must already be present (vendored or downloaded by the consumer).
     * Productionization will add versioned download + checksum verification (see KDoc on the plugin).
     */
    abstract val xcframework: Property<java.io.File>

    /** The framework name passed to `-framework`. Both editions ship a framework named `CouchbaseLite`. */
    abstract val frameworkName: Property<String>
}

enum class CouchbaseLiteEdition { COMMUNITY, ENTERPRISE }