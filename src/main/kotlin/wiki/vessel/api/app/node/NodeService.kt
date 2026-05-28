/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.api.app.node

import io.ktor.server.routing.*
import net.atlantisservices.sdb.SdbRepository
import wiki.vessel.api.app.Vessel
import wiki.vessel.api.app.service.Service
import wiki.vessel.api.app.service.ServicePriority

class NodeService : Service() {
    override val name: String = "node"
    override val priority: ServicePriority = ServicePriority.CRITICAL

    override fun start() {
        Vessel.nodes = SdbRepository(Node::class, "nodes")
        println("Loaded ${Vessel.nodes.count()} nodes")
    }

    override fun routing(route: Route) {
        route.nodeRoutes()
        route.nodeAdminRoutes()
    }
}