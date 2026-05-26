/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.mast.app.dto.instance

import kotlinx.serialization.Serializable
import wiki.vessel.mast.app.instance.InstancePermission

@Serializable
data class AddSubuserDTO(val userId: Long, val permissions: Set<InstancePermission> = emptySet())
