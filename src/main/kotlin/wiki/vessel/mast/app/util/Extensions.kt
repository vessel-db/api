/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.mast.app.util

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import wiki.vessel.mast.app.util.translation.Lang
import wiki.vessel.mast.app.util.translation.Message

suspend fun ApplicationCall.respond(status: HttpStatusCode, message: Message) {
    respond(status, mapOf("message" to Lang[message.key]))
}

suspend fun ApplicationCall.ok(params: Map<String, String> = mapOf()) = respond(HttpStatusCode.OK, params)
suspend fun ApplicationCall.unauthorized() = respond(HttpStatusCode.Unauthorized, Message.UNAUTHORIZED)
suspend fun ApplicationCall.rateLimited() = respond(HttpStatusCode.TooManyRequests, Message.RATE_LIMITED)
suspend fun ApplicationCall.notFound() = respond(HttpStatusCode.NotFound, Message.NOT_FOUND)
suspend fun ApplicationCall.badRequest() = respond(HttpStatusCode.BadRequest, Message.BAD_REQUEST)

suspend inline fun <reified T : Any> ApplicationCall.toBody(): T? = try {
    receive<T>()
} catch (_: Exception) {
    null
}