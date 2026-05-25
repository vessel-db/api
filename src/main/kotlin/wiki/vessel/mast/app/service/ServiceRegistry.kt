/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.mast.app.service

import io.ktor.server.routing.Route
import org.slf4j.LoggerFactory

object ServiceRegistry {
    private val logger = LoggerFactory.getLogger(ServiceRegistry::class.java)
    private val services = mutableListOf<Service>()

    fun register(vararg s: Service) = services.addAll(s)

    fun registerRouting(route: Route) {
        services.forEach { it.routing(route) }
    }

    fun startAll() {
        for (service in services) {
            runCatching { service.start() }.onFailure { e ->
                when (service.priority) {
                    ServicePriority.CRITICAL -> {
                        logger.error("Critical service '${service.name}' failed to start: ${e.message}")
                        stopAll()
                        throw IllegalStateException("Startup aborted.")
                    }
                    ServicePriority.OPTIONAL -> {
                        logger.warn("Optional service '${service.name}' failed to start: ${e.message}")
                    }
                }
            }.onSuccess {
                logger.info("Service '${service.name}' started.")
            }
        }
    }

    fun stopAll() = services.reversed().forEach { runCatching { it.stop() } }

    @Suppress("UNCHECKED_CAST")
    fun <T : Service> get(clazz: Class<T>): T =
        services.filterIsInstance(clazz).firstOrNull()
            ?: throw IllegalStateException("Service ${clazz.simpleName} not registered.")

}