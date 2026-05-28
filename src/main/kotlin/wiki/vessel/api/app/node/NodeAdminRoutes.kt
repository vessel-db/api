package wiki.vessel.api.app.node

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import wiki.vessel.api.app.Vessel
import wiki.vessel.api.app.dto.node.CreateNodeDTO
import wiki.vessel.api.app.util.badRequest
import wiki.vessel.api.app.util.notFound
import wiki.vessel.api.app.util.ok
import wiki.vessel.api.app.util.requireAdmin
import wiki.vessel.api.app.util.toBody

fun Route.nodeAdminRoutes() {

    route("/nodes") {

        post {
            val user = call.requireAdmin() ?: return@post

            val body = call.toBody<CreateNodeDTO>() ?: return@post call.badRequest()

            val node = Node.create(
                name = body.name,
                address = body.address,
                diskMb = body.diskMb,
                memoryMb = body.memoryMb,
            )

            Vessel.nodes.save(node)
            call.respond(HttpStatusCode.Created, node)
        }

        delete("/{id}") {
            val user = call.requireAdmin() ?: return@delete

            val id = call.parameters["id"] ?: return@delete call.badRequest()
            val node = Vessel.nodes.findById(id) ?: return@delete call.notFound()

            Vessel.nodes.delete(node)
            call.ok()
        }

    }

}