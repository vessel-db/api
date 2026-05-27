/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.mast.app.user

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import wiki.vessel.mast.app.Vessel
import wiki.vessel.mast.app.dto.auth.ChangePasswordDTO
import wiki.vessel.mast.app.dto.auth.CreateUserDTO
import wiki.vessel.mast.app.dto.auth.SetAdministratorDTO
import wiki.vessel.mast.app.util.PasswordUtil
import wiki.vessel.mast.app.util.badRequest
import wiki.vessel.mast.app.util.conflict
import wiki.vessel.mast.app.util.notFound
import wiki.vessel.mast.app.util.ok
import wiki.vessel.mast.app.util.requireAdmin
import wiki.vessel.mast.app.util.toBody

fun Route.userAdminRoutes() {

    route("/admin/users") {

        get {
            val admin = call.requireAdmin() ?: return@get
            call.respond(HttpStatusCode.OK, Vessel.users.findAll())
        }

        post {
            val admin = call.requireAdmin() ?: return@post
            val body = call.toBody<CreateUserDTO>() ?: return@post call.badRequest()

            if (Vessel.users.findBy("name", body.username) != null) {
                return@post call.conflict()
            }

            val user = User(
                username = body.username,
                passwordHash = PasswordUtil.hash(body.password),
                administrator = body.administrator
            )
            Vessel.users.save(user)

            call.respond(HttpStatusCode.Created, user)
        }

        delete("/{id}") {
            val admin = call.requireAdmin() ?: return@delete
            val id = call.parameters["id"]?.toLongOrNull() ?: return@delete call.badRequest()

            if (admin.id == id) return@delete call.badRequest() // prevent self deletion

            val user = Vessel.users.findBy("id", id) ?: return@delete call.notFound()
            Vessel.users.deleteById(user.id)

            call.ok()
        }

        patch("/{id}/password") {
            val admin = call.requireAdmin() ?: return@patch
            val id = call.parameters["id"]?.toLongOrNull() ?: return@patch call.badRequest()
            val body = call.toBody<ChangePasswordDTO>() ?: return@patch call.badRequest()

            val user = Vessel.users.findBy("id", id) ?: return@patch call.notFound()
            Vessel.users.save(user.copy(passwordHash = PasswordUtil.hash(body.password), tokenVersion = user.tokenVersion + 1))

            call.ok()
        }

        patch("/{id}/administrator") {
            val admin = call.requireAdmin() ?: return@patch
            val id = call.parameters["id"]?.toLongOrNull() ?: return@patch call.badRequest()
            val body = call.toBody<SetAdministratorDTO>() ?: return@patch call.badRequest()

            if (admin.id == id) return@patch call.badRequest()

            val user = Vessel.users.findBy("id", id) ?: return@patch call.notFound()
            Vessel.users.save(user.copy(administrator = body.administrator))

            call.ok()
        }

    }

}