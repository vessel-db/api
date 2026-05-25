/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.mast.app.user

import io.ktor.server.routing.Route
import wiki.vessel.mast.app.service.Service
import wiki.vessel.mast.app.service.ServicePriority

class UserService : Service() {
    override val name: String
        get() = "user"
    override val priority: ServicePriority
        get() = ServicePriority.CRITICAL

    override fun start() {

    }

    override fun routing(route: Route) {
        super.routing(route)
    }

}