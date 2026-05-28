/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.api.app.settings

object Settings {
    val loginAttemptsPerMinute = Setting("login_attempts_per_minute", 5)
    val passwordResetAttemptsPerMinute = Setting("password_reset_attempts_per_minute", 5)
}