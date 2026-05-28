/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.api.app.dto.node

import kotlinx.serialization.Serializable

@Serializable
data class CreateNodeDTO(
    val name: String,
    val address: String,
    val diskMb: Long,
    val memoryMb: Long,
)