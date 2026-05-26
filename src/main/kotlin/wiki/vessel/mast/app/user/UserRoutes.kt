/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.mast.app.user

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import wiki.vessel.mast.app.Vessel
import wiki.vessel.mast.app.config.AppConfig
import wiki.vessel.mast.app.config.AppConfig.Environment.*
import wiki.vessel.mast.app.config.JWTConfig
import wiki.vessel.mast.app.dto.auth.AuthenticationDTO
import wiki.vessel.mast.app.util.*
import wiki.vessel.mast.app.util.ratelimit.RateLimiters
import java.time.Duration

fun Route.userRoutes() {

    route("/auth/me") {
        get {
            val user = call.toUser() ?: return@get call.unauthorized()

            call.respond(HttpStatusCode.OK, user)
        }
    }

    route("/auth") {

        post("/login") {
            val body = call.toBody<AuthenticationDTO>() ?: return@post call.badRequest()

            if (!RateLimiters.loginAttemptsPerIP.isAllowed(call)) {
                return@post call.rateLimited()
            }

            val username = body.username
            val password = body.password

            val user = Vessel.users.findBy("username", username)

            if (user == null || !PasswordUtil.verify(user.passwordHash, password)) return@post call.unauthorized()

            val tokenLifespan = Duration.ofDays(30).toMillis()
            val token = JWTConfig.generateToken(user, tokenLifespan)

            when (AppConfig.environment) {

                DEVELOPMENT -> {
                    call.response.cookies.append(
                        name = "token",
                        value = token,
                        httpOnly = true,
                        secure = call.isSecure,
                        path = "/",
                        maxAge = tokenLifespan
                    )
                }

                PRODUCTION -> {
                    call.response.cookies.append(
                        name = "token",
                        value = token,
                        httpOnly = true,
                        secure = call.isSecure,
                        path = "/",
                        domain = AppConfig.panelUrl.replace("https://", "").replace("http://", "").replace("www.", ""),
                        maxAge = tokenLifespan
                    )
                }

            }

            call.ok(buildMap { "token" to token })
        }

        post("/logout") {

            when (AppConfig.environment) {

                DEVELOPMENT -> {
                    call.response.cookies.append(
                        name = "token",
                        value = "",
                        httpOnly = true,
                        secure = call.isSecure,
                        path = "/",
                        maxAge = 0L
                    )
                }

                PRODUCTION -> {
                    call.response.cookies.append(
                        name = "token",
                        value = "",
                        httpOnly = true,
                        secure = call.isSecure,
                        path = "/",
                        domain = AppConfig.panelUrl.replace("https://", "").replace("http://", "").replace("www.", ""),
                        maxAge = 0L
                    )
                }

            }

            call.ok()

        }

    }

}