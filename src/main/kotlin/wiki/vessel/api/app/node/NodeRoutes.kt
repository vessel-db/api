/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.api.app.node

import io.ktor.http.*
import io.ktor.server.application.call
import io.ktor.server.response.*
import io.ktor.server.routing.*
import wiki.vessel.api.app.Vessel
import wiki.vessel.api.app.dto.node.CreateNodeDTO
import wiki.vessel.api.app.util.*

fun Route.nodeRoutes() {

    route("/nodes") {

        get {
            val user = call.toUser() ?: return@get call.unauthorized()

            call.respond(HttpStatusCode.OK, Vessel.nodes.findAll())
        }

        get("/{id}") {
            val user = call.toUser() ?: return@get call.unauthorized()

            val id = call.parameters["id"] ?: return@get call.badRequest()
            val node = Vessel.nodes.findById(id) ?: return@get call.notFound()

            call.respond(HttpStatusCode.OK, node)
        }

    }

}