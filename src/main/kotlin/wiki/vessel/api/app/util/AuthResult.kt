/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.api.app.util

import wiki.vessel.api.app.user.User

sealed class AuthResult {
    data class Ok(val user: User) : AuthResult()
    data object Missing : AuthResult()
    data object Invalid : AuthResult()
}