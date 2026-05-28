/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.api.app.util

import org.mindrot.jbcrypt.BCrypt

object PasswordUtil {

    fun verify(hash: String, password: String): Boolean = BCrypt.checkpw(password, hash)
    fun hash(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt())

}