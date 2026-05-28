/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.api.app.node

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import wiki.vessel.api.app.Vessel
import wiki.vessel.api.app.util.badRequest
import wiki.vessel.api.app.util.notFound
import wiki.vessel.api.app.util.toUser
import wiki.vessel.api.app.util.unauthorized

fun Route.nodeRoutes() {

    route("/nodes") {

        get {
            call.toUser() ?: return@get call.unauthorized()

            call.respond(HttpStatusCode.OK, Vessel.nodes.findAll())
        }

        get("/{id}") {
            call.toUser() ?: return@get call.unauthorized()

            val id = call.parameters["id"] ?: return@get call.badRequest()
            val node = Vessel.nodes.findById(id) ?: return@get call.notFound()

            call.respond(HttpStatusCode.OK, node)
        }

    }

}