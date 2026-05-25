/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.mast.app.service

import io.ktor.server.routing.Route

abstract class Service {
    abstract val name: String
    abstract val priority: ServicePriority
    abstract fun start()
    open fun stop() {}
    open fun routing(route: Route) {}
}