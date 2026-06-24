/*
 * Copyright 2022-2023 Jeff Lockhart
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
package kotbase

import java.security.cert.X509Certificate
import com.couchbase.lite.ReplicatorConfiguration as CBLReplicatorConfiguration

public actual class ReplicatorConfiguration
private constructor(
    public actual val target: Endpoint,
    private val collectionConfigurations: MutableMap<Collection, CollectionConfiguration>
) {

    public actual constructor(target: Endpoint) : this(target, mutableMapOf())

    public actual constructor(config: ReplicatorConfiguration) : this(
        config.target,
        config.collectionConfigurations.entries.associate { (collection, collectionConfig) ->
            collection to CollectionConfiguration(collectionConfig)
        }.toMutableMap()
    ) {
        type = config.type
        isContinuous = config.isContinuous
        isAutoPurgeEnabled = config.isAutoPurgeEnabled
        headers = config.headers
        isAcceptParentDomainCookies = config.isAcceptParentDomainCookies
        authenticator = config.authenticator
        pinnedServerCertificate = config.pinnedServerCertificate
        pinnedServerX509Cert = config.pinnedServerX509Cert
        maxAttempts = config.maxAttempts
        maxAttemptWaitTime = config.maxAttemptWaitTime
        heartbeat = config.heartbeat
    }

    public actual fun addCollection(collection: Collection, config: CollectionConfiguration?): ReplicatorConfiguration {
        checkCollection(collection)
        collectionConfigurations[collection] = config?.let(::CollectionConfiguration) ?: CollectionConfiguration()
        return this
    }

    public actual fun addCollections(
        collections: kotlin.collections.Collection<Collection>,
        config: CollectionConfiguration?
    ): ReplicatorConfiguration {
        collections.forEach { collection ->
            checkCollection(collection)
            collectionConfigurations[collection] = config?.let(::CollectionConfiguration) ?: CollectionConfiguration()
        }
        return this
    }

    // All collections in a replicator configuration must belong to the same database
    // and must not have been deleted.
    private fun checkCollection(collection: Collection) {
        val database = collectionConfigurations.keys.firstOrNull()?.database
        require(database == null || database == collection.database) {
            "All collections in a replicator configuration must belong to the same database"
        }
        val exists = try {
            collection.database.getCollection(collection.name, collection.scope.name) != null
        } catch (e: CouchbaseLiteException) {
            false
        }
        require(exists) { "Collection ${collection.fullName} has been deleted" }
    }

    public actual fun removeCollection(collection: Collection): ReplicatorConfiguration {
        collectionConfigurations.remove(collection)
        return this
    }

    public actual fun setType(type: ReplicatorType): ReplicatorConfiguration {
        this.type = type
        return this
    }

    public actual fun setContinuous(continuous: Boolean): ReplicatorConfiguration {
        this.isContinuous = continuous
        return this
    }

    public actual fun setAutoPurgeEnabled(enabled: Boolean): ReplicatorConfiguration {
        this.isAutoPurgeEnabled = enabled
        return this
    }

    public actual fun setHeaders(headers: Map<String, String>?): ReplicatorConfiguration {
        this.headers = headers
        return this
    }

    public actual fun setAcceptParentDomainCookies(acceptParentCookies: Boolean): ReplicatorConfiguration {
        this.isAcceptParentDomainCookies = acceptParentCookies
        return this
    }

    public actual fun setAuthenticator(authenticator: Authenticator?): ReplicatorConfiguration {
        this.authenticator = authenticator
        return this
    }

    public actual fun setPinnedServerCertificate(pinnedCert: ByteArray?): ReplicatorConfiguration {
        this.pinnedServerCertificate = pinnedCert
        return this
    }

    public actual fun setMaxAttempts(maxAttempts: Int): ReplicatorConfiguration {
        this.maxAttempts = maxAttempts
        return this
    }

    public actual fun setMaxAttemptWaitTime(maxAttemptWaitTime: Int): ReplicatorConfiguration {
        this.maxAttemptWaitTime = maxAttemptWaitTime
        return this
    }

    public actual fun setHeartbeat(heartbeat: Int): ReplicatorConfiguration {
        this.heartbeat = heartbeat
        return this
    }

    public actual fun getCollectionConfiguration(collection: Collection): CollectionConfiguration? =
        collectionConfigurations[collection]?.let(::CollectionConfiguration)

    public actual val collections: Set<Collection>
        get() = collectionConfigurations.keys.toSet()

    public actual var type: ReplicatorType = Defaults.Replicator.TYPE

    public actual var isContinuous: Boolean = Defaults.Replicator.CONTINUOUS

    public actual var isAutoPurgeEnabled: Boolean = Defaults.Replicator.ENABLE_AUTO_PURGE

    public actual var headers: Map<String, String>? = null

    public actual var isAcceptParentDomainCookies: Boolean = Defaults.Replicator.ACCEPT_PARENT_COOKIES

    public actual var authenticator: Authenticator? = null

    public actual var pinnedServerCertificate: ByteArray? = null

    // Stored as 0 = "use the SDK default"; the getters resolve to the effective default,
    // matching the Couchbase Lite getters' behavior.
    public actual var maxAttempts: Int = 0
        get() = when {
            field != 0 -> field
            isContinuous -> Defaults.Replicator.MAX_ATTEMPTS_CONTINUOUS
            else -> Defaults.Replicator.MAX_ATTEMPTS_SINGLE_SHOT
        }
        set(value) {
            require(value >= 0) { "max attempts must be >= 0" }
            field = value
        }

    public actual var maxAttemptWaitTime: Int = 0
        get() = if (field != 0) field else Defaults.Replicator.MAX_ATTEMPTS_WAIT_TIME
        set(value) {
            require(value >= 0) { "max attempt wait time must be >= 0" }
            field = value
        }

    public actual var heartbeat: Int = 0
        get() = if (field != 0) field else Defaults.Replicator.HEARTBEAT
        set(value) {
            require(value in 0..MAX_HEARTBEAT_SECONDS) { "heartbeat must be between 0 and $MAX_HEARTBEAT_SECONDS seconds" }
            field = value
        }

    // JVM-only pinned X509 certificate, surfaced through the extensions below.
    internal var pinnedServerX509Cert: X509Certificate? = null

    /**
     * Build the underlying Couchbase Lite replicator configuration from the current state.
     * As of CBL 4.0 the collection set is fixed at construction, so Kotbase keeps the
     * configuration's state and materializes the native configuration on demand.
     */
    internal val actual: CBLReplicatorConfiguration
        get() {
            val collectionConfigs = collectionConfigurations.map { (collection, config) ->
                config.toActual(collection.actual)
            }
            return CBLReplicatorConfiguration(collectionConfigs, target.actual).also {
                it.type = type.actual
                it.isContinuous = isContinuous
                it.isAutoPurgeEnabled = isAutoPurgeEnabled
                it.headers = headers
                it.isAcceptParentDomainCookies = isAcceptParentDomainCookies
                it.setAuthenticator(authenticator?.actual)
                it.pinnedServerCertificate = pinnedServerCertificate
                it.maxAttempts = maxAttempts
                it.maxAttemptWaitTime = maxAttemptWaitTime
                it.heartbeat = heartbeat
                pinnedServerX509Cert?.let { cert -> it.setPinnedServerX509Certificate(cert) }
            }
        }

    public actual companion object {
        // This is a long time: just under 25 days. This many seconds is just less than
        // Integer.MAX_VALUE milliseconds and fits in the heartbeat property.
        private const val MAX_HEARTBEAT_SECONDS = 2147483
    }
}

/**
 * Sets the certificate used to authenticate the target server.
 * A server will be authenticated if it presents a chain of certificates (possibly of length 1)
 * in which any one of the certificates matches the one passed here.
 * The default is no pinned certificate.
 *
 * @param pinnedCert the SSL certificate.
 * @return this.
 */
public fun ReplicatorConfiguration.setPinnedServerX509Certificate(
    pinnedCert: X509Certificate?
): ReplicatorConfiguration {
    pinnedServerX509Cert = pinnedCert
    return this
}

/**
 * The remote target's SSL certificate.
 */
public var ReplicatorConfiguration.pinnedServerX509Certificate: X509Certificate?
    get() = pinnedServerX509Cert
    set(value) {
        pinnedServerX509Cert = value
    }

public actual fun ReplicatorConfiguration.setAllowReplicatingInBackground(allowReplicatingInBackground: Boolean): ReplicatorConfiguration {
    // no-op
    return this
}

public actual var ReplicatorConfiguration.allowReplicatingInBackground: Boolean
    get() = Defaults.Replicator.ALLOW_REPLICATING_IN_BACKGROUND
    set(_) {
        // no-op
    }