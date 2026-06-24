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
@file:Suppress("DEPRECATION")

package kotbase

import kotlin.test.*

// The suite of tests that verifies behavior
// with a deleted default collection are in
// cbl-java-common @ a2de0d43d09ce64fd3a1301dc35
class DeprecatedConfigFactoryTest : BaseDbTest() {
    private val testEndpoint = URLEndpoint("ws://foo.couchbase.com/db")

    ///// Test ReplicatorConfiguration Factory

    @Test
    fun testReplicatorConfigNoArgs() {
        assertFailsWith<IllegalArgumentException> { ReplicatorConfigurationFactory.newConfig() }
    }
    // Create from a source explicitly specifying a default collection
    @Test
    fun testReplicatorConfigFromCollectionWithDefault() {
        val config1 = ReplicatorConfigurationFactory
            .newConfig(testEndpoint, mapOf(listOf(testDatabase.defaultCollection) to CollectionConfiguration()))
        val config2 = config1.newConfig()
        assertNotSame(config1, config2)
        assertEquals(setOf(testCollection.database.defaultCollection), config2.collections)
    }
}
