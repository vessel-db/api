/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.mast.app.instance

import kotlinx.serialization.Serializable
import wiki.vessel.mast.app.Vessel

@Serializable
data class Instance(
    val id: Long = Vessel.generator.nextId(),
    val ownerId: Long,
    val name: String,
    val type: InstanceType,
    val host: String,
    val port: Int,
    val database: String,
    val username: String,
    val passwordEncrypted: String,
    val createdAt: Long = System.currentTimeMillis()
)