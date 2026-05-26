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
data class InstanceSubuser(
    val id: Long = Vessel.generator.nextId(),
    val instanceId: Long,
    val userId: Long,
    val permissions: Set<InstancePermission> = emptySet(),
    val addedAt: Long = System.currentTimeMillis()
)