/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.mast.app.user

import io.ktor.server.routing.*
import net.atlantisservices.sdb.SdbRepository
import wiki.vessel.mast.app.Vessel
import wiki.vessel.mast.app.service.Service
import wiki.vessel.mast.app.service.ServicePriority

class UserService : Service() {
    override val name: String = "user"
    override val priority: ServicePriority = ServicePriority.CRITICAL

    override fun start() {
        Vessel.users = SdbRepository(User::class, "users")

        if (Vessel.users.count() == 0) {
            val admin = User.create("admin", "admin")
            Vessel.users.save(admin)
            println("Created administrator user 'admin' with password 'admin'")
        }

        println("Loaded ${Vessel.users.count()} users")
    }

    override fun routing(route: Route) {
        route.userRoutes()
        route.userAdminRoutes()
    }
}