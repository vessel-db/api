/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.mast.app.util

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import kotlinx.coroutines.DelicateCoroutinesApi
import wiki.vessel.mast.app.Vessel
import wiki.vessel.mast.app.config.AppConfig
import wiki.vessel.mast.app.config.JWTConfig
import wiki.vessel.mast.app.instance.Instance
import wiki.vessel.mast.app.instance.InstancePermission
import wiki.vessel.mast.app.user.User
import wiki.vessel.mast.app.util.translation.Lang
import wiki.vessel.mast.app.util.translation.Message

suspend fun ApplicationCall.respond(status: HttpStatusCode, message: Message) {
    respond(status, mapOf("message" to Lang[message.key]))
}

suspend fun ApplicationCall.ok(params: Map<String, String> = mapOf()) = respond(HttpStatusCode.OK, params)
suspend fun ApplicationCall.unauthorized() = respond(HttpStatusCode.Unauthorized, Message.UNAUTHORIZED)
suspend fun ApplicationCall.conflict() = respond(HttpStatusCode.Conflict, Message.CONFLICT)
suspend fun ApplicationCall.forbidden() = respond(HttpStatusCode.Forbidden, Message.FORBIDDEN)
suspend fun ApplicationCall.rateLimited() = respond(HttpStatusCode.TooManyRequests, Message.RATE_LIMITED)
suspend fun ApplicationCall.notFound() = respond(HttpStatusCode.NotFound, Message.NOT_FOUND)
suspend fun ApplicationCall.badRequest() = respond(HttpStatusCode.BadRequest, Message.BAD_REQUEST)

suspend fun ApplicationCall.requireAdmin(): User? {
    val user = toUser() ?: run { unauthorized(); return null }
    if (!user.administrator) { forbidden(); return null }
    return user
}

suspend fun ApplicationCall.resolveInstance(instanceId: Long): Pair<Instance, Set<InstancePermission>?>? {
    val user = toUser() ?: run { unauthorized(); return null }
    val instance = Vessel.instances.findBy("id", instanceId) ?: run { notFound(); return null }

    if (instance.ownerId == user.id || user.administrator) return instance to null

    val subuser = Vessel.subusers.findBy("instanceId" to instanceId, "userId" to user.id)
        ?: run { forbidden(); return null }

    return instance to subuser.permissions
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
