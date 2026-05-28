/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.api.app.util

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import kotlinx.coroutines.DelicateCoroutinesApi
import wiki.vessel.api.app.Vessel
import wiki.vessel.api.app.config.AppConfig
import wiki.vessel.api.app.config.JWTConfig
import wiki.vessel.api.app.user.User
import wiki.vessel.api.app.util.translation.Message

suspend fun ApplicationCall.message(status: HttpStatusCode, message: String) {
    respond(status, mapOf("message" to message))
}

suspend fun ApplicationCall.ok(params: Map<String, String> = mapOf()) = respond(HttpStatusCode.OK, params)
suspend fun ApplicationCall.unauthorized() = message(HttpStatusCode.Unauthorized, Message.UNAUTHORIZED)
suspend fun ApplicationCall.conflict() = message(HttpStatusCode.Conflict, Message.CONFLICT)
suspend fun ApplicationCall.forbidden() = message(HttpStatusCode.Forbidden, Message.FORBIDDEN)
suspend fun ApplicationCall.rateLimited() = message(HttpStatusCode.TooManyRequests, Message.RATE_LIMITED)
suspend fun ApplicationCall.notFound() = message(HttpStatusCode.NotFound, Message.NOT_FOUND)
suspend fun ApplicationCall.badRequest() = message(HttpStatusCode.BadRequest, Message.BAD_REQUEST)

suspend fun ApplicationCall.requireAdmin(): User? {
    val user = toUser() ?: run { unauthorized(); return null }
    if (!user.administrator) { forbidden(); return null }
    return user
}

@OptIn(DelicateCoroutinesApi::class)
fun User.Companion.resolveFromJWT(call: ApplicationCall): AuthResult {
    val token = call.request.headers["Authorization"]
        ?.takeIf { it.startsWith("Bearer ") }
        ?.removePrefix("Bearer ")?.trim()
        ?: call.request.cookies["token"]
        ?: call.request.queryParameters["token"]
        ?: return AuthResult.Missing

    val payload = JWTConfig.decodeToken(token) ?: return AuthResult.Invalid
    val userId = payload["userId"] as? Long ?: return AuthResult.Invalid
    val tokenVersion = payload["version"] as? Int ?: return AuthResult.Invalid

    val user = Vessel.users.findById(userId) ?: return AuthResult.Invalid
    if (user.tokenVersion != tokenVersion) return AuthResult.Invalid

    return AuthResult.Ok(user)
}

suspend fun ApplicationCall.toUser(): User? =
    when (val r = User.resolveFromJWT(this)) {
        is AuthResult.Ok -> r.user
        else -> null
    }

suspend inline fun <reified T : Any> ApplicationCall.toBody(): T? = try {
    receive<T>()
} catch (_: Exception) {
    null
}

val ApplicationCall.isSecure: Boolean
    get() = request.header("X-Forwarded-Proto") == "https" && AppConfig.environment == AppConfig.Environment.PRODUCTION
