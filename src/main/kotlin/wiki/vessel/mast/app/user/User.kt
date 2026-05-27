/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.mast.app.user

import kotlinx.serialization.Serializable
import wiki.vessel.mast.app.Vessel
import wiki.vessel.mast.app.util.PasswordUtil

@Serializable
data class User(
    val id: Long = Vessel.generator.nextId(),
    val username: String,
    val passwordHash: String,
    val administrator: Boolean = false,
    val registeredAt: Long = System.currentTimeMillis(),
    val tokenVersion: Int = 0,
)

fun User.Companion.create(username: String, password: String): User {
    return User(username = username, passwordHash = PasswordUtil.hash(password), administrator = true)
}