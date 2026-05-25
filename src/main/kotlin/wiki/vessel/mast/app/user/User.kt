/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.mast.app.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import wiki.vessel.mast.app.Vessel

@Serializable
data class User(
    @SerialName("_id") val id: Long = Vessel.generator.nextId(),
    val passwordHash: String,
    val administrator: Boolean = false,
    val registeredAt: Long = System.currentTimeMillis(),

    val passwordResetToken: String? = null,
    val passwordResetTokenIssuedAt: Long? = null,

    val tokenVersion: Int = 0,
    )