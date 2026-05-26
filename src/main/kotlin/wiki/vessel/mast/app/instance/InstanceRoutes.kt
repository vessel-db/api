package wiki.vessel.mast.app.instance

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import wiki.vessel.mast.app.Vessel
import wiki.vessel.mast.app.dto.instance.AddSubuserDTO
import wiki.vessel.mast.app.dto.instance.CreateInstanceDTO
import wiki.vessel.mast.app.dto.instance.UpdateSubuserDTO
import wiki.vessel.mast.app.util.*

fun Route.instanceRoutes() {

    route("/instances") {

        get {
            val user = call.toUser() ?: return@get call.unauthorized()

            val owned = Vessel.instances.findAllBy("ownerId", user.id)
            val subuserOn = Vessel.subusers.findAllBy("userId", user.id)
                .mapNotNull { Vessel.instances.findBy("id", it.instanceId) }

            call.respond(HttpStatusCode.OK, (owned + subuserOn).distinctBy { it.id })
        }

        post {
            val user = call.toUser() ?: return@post call.unauthorized()
            val body = call.toBody<CreateInstanceDTO>() ?: return@post call.badRequest()

            val instance = Instance(
                ownerId = user.id,
                name = body.name,
                type = body.type,
                host = body.host,
                port = body.port,
                database = body.database,
                username = body.username,
                passwordEncrypted = PasswordUtil.hash(body.password)
            )

            Vessel.instances.save(instance)
            call.respond(HttpStatusCode.Created, instance)
        }

        route("/{id}") {

            delete {
                val instance = call.resolveInstanceOwner() ?: return@delete

                Vessel.subusers.findAllBy("instanceId", instance.id)
                    .forEach { Vessel.subusers.deleteById(it.id) }
                Vessel.instances.deleteById(instance.id)

                call.ok()
            }

            route("/subusers") {

                get {
                    val (instance, _) = call.resolveInstance() ?: return@get
                    call.respond(HttpStatusCode.OK, Vessel.subusers.findAllBy("instanceId", instance.id))
                }

                post {
                    val instance = call.resolveInstanceOwner() ?: return@post
                    val body = call.toBody<AddSubuserDTO>() ?: return@post call.badRequest()

                    val target = Vessel.users.findBy("id", body.userId) ?: return@post call.notFound()

                    if (target.id == instance.ownerId) return@post call.badRequest()

                    val existing = Vessel.subusers.findAllBy("instanceId", instance.id)
                        .find { it.userId == body.userId }
                    if (existing != null) return@post call.conflict()

                    val subuser = InstanceSubuser(
                        instanceId = instance.id,
                        userId = target.id,
                        permissions = body.permissions
                    )

                    Vessel.subusers.save(subuser)
                    call.respond(HttpStatusCode.Created, subuser)
                }

                patch("/{userId}") {
                    val instance = call.resolveInstanceOwner() ?: return@patch
                    val userId = call.parameters["userId"]?.toLongOrNull() ?: return@patch call.badRequest()
                    val body = call.toBody<UpdateSubuserDTO>() ?: return@patch call.badRequest()

                    val subuser = Vessel.subusers.findAllBy("instanceId", instance.id)
                        .find { it.userId == userId } ?: return@patch call.notFound()

                    Vessel.subusers.save(subuser.copy(permissions = body.permissions))
                    call.ok()
                }

                delete("/{userId}") {
                    val instance = call.resolveInstanceOwner() ?: return@delete
                    val userId = call.parameters["userId"]?.toLongOrNull() ?: return@delete call.badRequest()

                    val subuser = Vessel.subusers.findAllBy("instanceId", instance.id)
                        .find { it.userId == userId } ?: return@delete call.notFound()

                    Vessel.subusers.deleteById(subuser.id)
                    call.ok()
                }

            }

        }

    }

}

suspend fun ApplicationCall.resolveInstanceOwner(): Instance? {
    val user = toUser() ?: run { unauthorized(); return null }
    val id = parameters["id"]?.toLongOrNull() ?: run { badRequest(); return null }
    val instance = Vessel.instances.findBy("id", id) ?: run { notFound(); return null }
    if (instance.ownerId != user.id && !user.administrator) { forbidden(); return null }
    return instance
}

suspend fun ApplicationCall.resolveInstance(): Pair<Instance, Set<InstancePermission>?>? {
    val user = toUser() ?: run { unauthorized(); return null }
    val id = parameters["id"]?.toLongOrNull() ?: run { badRequest(); return null }
    val instance = Vessel.instances.findBy("id", id) ?: run { notFound(); return null }

    if (instance.ownerId == user.id || user.administrator) return instance to null

    val subuser = Vessel.subusers.findAllBy("instanceId", instance.id)
        .find { it.userId == user.id } ?: run { forbidden(); return null }

    return instance to subuser.permissions
}