/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.mast.app.util.ratelimit

import io.ktor.server.plugins.*
import net.atlantisservices.ratelimiter.RateLimiter
import wiki.vessel.mast.app.settings.Settings
import wiki.vessel.mast.app.settings.SettingsProvider
import kotlin.time.Duration.Companion.minutes

object RateLimiters {

    val loginAttemptsPerIP = RateLimiter(
        keyProvider = { call -> call.request.origin.remoteHost },
        limit = SettingsProvider[Settings.loginAttemptsPerMinute],
        window = 1.minutes
    )

    val passwordResetAttemptsPerMinute = RateLimiter(
        keyProvider = { call -> call.request.origin.remoteHost },
        limit = SettingsProvider[Settings.passwordResetAttemptsPerMinute],
        window = 1.minutes
    )

}