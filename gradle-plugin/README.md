# Kotbase CouchbaseLite link plugin (prototype)

A consumer-facing Gradle plugin that automatically applies the `CouchbaseLite` XCFramework link
options to **every Apple target** of a Kotlin Multiplatform module, so apps using Kotbase no longer
repeat per-target `binaries.framework { linkerOpts(...) }` boilerplate.

> **Status: prototype.** This automates the linking only — the XCFramework must already be present
> on disk (vendored or downloaded by the consumer). See "Productionization" below.

## Why

Kotbase publishes Kotlin/Native `.klib` artifacts to Maven. The `CouchbaseLite` framework is linked
in the **consumer's** final binary, and Kotlin/Native does not propagate a dependency's `linkerOpts`
transitively — so today every consumer must hand-write the framework link for each Apple target.
This plugin centralizes that into one `apply` + a single config block.

## Usage

```kotlin
// build.gradle.kts
plugins {
    kotlin("multiplatform")
    id("dev.kotbase.couchbase-lite-link")
}

couchbaseLite {
    edition = dev.kotbase.gradle.CouchbaseLiteEdition.ENTERPRISE // default COMMUNITY
    // Unzipped CouchbaseLite[-Enterprise].xcframework directory:
    xcframework = layout.projectDirectory.dir("vendor/CouchbaseLite/CouchbaseLite.xcframework").asFile
}
```

The plugin then adds, for each Apple target, the equivalent of:

```kotlin
binaries.all {
    val path = "<xcframework>/<slice>"   // ios-arm64 / ios-arm64_x86_64-simulator / macos-arm64_x86_64
    linkerOpts("-F$path", "-framework", "CouchbaseLite", "-rpath", path)
}
```

## Productionization (tracked separately)

* Versioned download from `packages.couchbase.com` with SHA-256 verification (CE + EE, EE auth),
  driven by `couchbaseLite { version = "4.0.4" }` — no manual vendoring
* Vector Search XCFramework support (same slice-selection logic)
* Publishing to the Gradle Plugin Portal / Maven Central, plugin tests (`TestKit`), and docs