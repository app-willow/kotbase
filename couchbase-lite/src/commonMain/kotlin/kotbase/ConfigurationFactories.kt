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

/**
 * Configuration factory for new DatabaseConfigurations
 *
 * Usage:
 *
 *     val dbConfig = DatabaseConfigurationFactory.newConfig(...)
 */
@Deprecated("Use DatabaseConfiguration()")
public val DatabaseConfigurationFactory: DatabaseConfiguration? = null

/**
 * Create a DatabaseConfiguration, overriding the receiver's
 * values with the passed parameters:
 *
 * @param databasePath The directory in which the database is stored.
 * @param fullSync enable full sync mode, where operating system crash or power loss
 * will not cause a loss of data, but performance is **dramatically** slower.
 *
 * @see DatabaseConfiguration
 */
@Deprecated(
    "Use DatabaseConfiguration() and properties",
    ReplaceWith("DatabaseConfiguration().apply { directory = databasePath\nisFullSync = fullSync }")
)
public fun DatabaseConfiguration?.newConfig(
    databasePath: String? = null,
    fullSync: Boolean? = null
): DatabaseConfiguration {
    return DatabaseConfiguration(this).apply {
        databasePath?.let { directory = it }
        fullSync?.let { isFullSync = it }
    }
}

/**
 * Configuration factory for new ReplicatorConfigurations
 *
 * Usage:
 *
 *     val replConfig = ReplicatorConfigurationFactory.newConfig(...)
 */
@Deprecated("Use ReplicatorConfiguration()")
public val ReplicatorConfigurationFactory: ReplicatorConfiguration? = null

/**
 * Create a ReplicatorConfiguration, overriding the receiver's
 * values with the passed parameters.
 *
 * Note: A document that is blocked by a document Id filter will not be auto-purged
 *       regardless of the setting of the enableAutoPurge property
 *
 * @param target (required) The replication endpoint.
 * @param collections a map of collections to be replicated, to their configurations.
 * @param type replicator type: push, pull, or push and pull: default is push and pull.
 * @param continuous continuous flag: true for continuous, false by default.
 * @param authenticator connection authenticator.
 * @param headers extra HTTP headers to send in all requests to the remote target.
 * @param pinnedServerCertificate target server's SSL certificate.
 * @param maxAttempts max retry attempts after connection failure.
 * @param maxAttemptWaitTime max time between retry attempts (exponential backoff).
 * @param heartbeat heartbeat interval, in seconds.
 * @param enableAutoPurge auto-purge enabled.
 * @param acceptParentDomainCookies Advanced: accept cookies for parent domains.
 *
 * @see ReplicatorConfiguration
 */
@Deprecated(
    "Use ReplicatorConfiguration() and properties",
    ReplaceWith("ReplicatorConfiguration(target).apply { collections?.forEach { (collection, config) -> addCollections(collection, config) }\nthis.type = type\nisContinuous = continuous\nthis.authenticator = authenticator\nthis.headers = headers\nthis.pinnedServerCertificate = pinnedServerCertificate\nthis.maxAttempts = maxAttempts\nthis.maxAttemptWaitTime = maxAttemptWaitTime\nthis.heartbeat = heartbeat\nisAutoPurgeEnabled = enableAutoPurge\nisAcceptParentDomainCookies = acceptParentDomainCookies }")
)
public fun ReplicatorConfiguration?.newConfig(
    target: Endpoint? = null,
    collections: Map<out kotlin.collections.Collection<Collection>, CollectionConfiguration?>? = null,
    type: ReplicatorType? = null,
    continuous: Boolean? = null,
    authenticator: Authenticator? = null,
    headers: Map<String, String>? = null,
    pinnedServerCertificate: ByteArray? = null,
    maxAttempts: Int? = null,
    maxAttemptWaitTime: Int? = null,
    heartbeat: Int? = null,
    enableAutoPurge: Boolean? = null,
    acceptParentDomainCookies: Boolean? = null
): ReplicatorConfiguration {
    val orig = this
    return ReplicatorConfiguration(
        requireNotNull(target ?: this?.target) { "A ReplicatorConfiguration must specify an endpoint" }
    ).apply {
        (type ?: orig?.type)?.let { this.type = it }
        (continuous ?: orig?.isContinuous)?.let { this.isContinuous = it }
        this.authenticator = authenticator ?: orig?.authenticator
        this.headers = headers ?: orig?.headers
        (acceptParentDomainCookies ?: orig?.isAcceptParentDomainCookies)?.let { this.isAcceptParentDomainCookies = it }
        this.pinnedServerCertificate = pinnedServerCertificate ?: orig?.pinnedServerCertificate
        (maxAttempts ?: orig?.maxAttempts)?.let { this.maxAttempts = it }
        (maxAttemptWaitTime ?: orig?.maxAttemptWaitTime)?.let { this.maxAttemptWaitTime = it }
        (heartbeat ?: orig?.heartbeat)?.let { this.heartbeat = it }
        (enableAutoPurge ?: orig?.isAutoPurgeEnabled)?.let { this.isAutoPurgeEnabled = it }
        if (collections != null) {
            collections.forEach { (collection, config) ->
                addCollections(collection, config)
            }
        } else {
            orig?.collections?.forEach { collection ->
                val config = orig.getCollectionConfiguration(collection)
                addCollection(collection, config)
            }
        }
    }
}

@Suppress("DEPRECATION")
@Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
public fun DatabaseConfiguration?.newConfig(databasePath: String? = null): DatabaseConfiguration =
    newConfig(databasePath = databasePath)
