/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.mast.app

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.atlantisservices.sdb.SdbRepository
import net.atlantisservices.snowflake.SnowflakeService
import org.slf4j.LoggerFactory
import wiki.vessel.mast.app.config.AppConfig
import wiki.vessel.mast.app.instance.Instance
import wiki.vessel.mast.app.instance.InstanceService
import wiki.vessel.mast.app.instance.InstanceSubuser
import wiki.vessel.mast.app.service.ServiceRegistry
import wiki.vessel.mast.app.user.User
import wiki.vessel.mast.app.user.UserService
import wiki.vessel.mast.app.user.create

object Vessel {

    private val logger = LoggerFactory.getLogger(Vessel::class.java)

    lateinit var generator: SnowflakeService
        private set

    private lateinit var server: NettyApplicationEngine

    lateinit var users: SdbRepository<User>
    lateinit var instances: SdbRepository<Instance>
    lateinit var subusers: SdbRepository<InstanceSubuser>

    fun start() {
        if (::server.isInitialized) {
            logger.warn("Server already started.")
            return
        }

        generator = SnowflakeService.default()

        ServiceRegistry.register(
            UserService(),
            InstanceService()
        )

        ServiceRegistry.startAll()

        logger.info("Starting Vessel on port ${AppConfig.port} [${AppConfig.environment}]")

        server = embeddedServer(Netty, port = AppConfig.port) { module() }
        server.start(wait = true)
    }

    fun stop() {
        if (!::server.isInitialized) {
            logger.warn("Server was not running.")
            return
        }

        logger.info("Stopping Vessel...")
        ServiceRegistry.stopAll()
        server.stop(gracePeriodMillis = 1000, timeoutMillis = 5000)
        logger.info("Vessel stopped.")
    }

    private fun Application.module() {

        routing {

            get("/monitor/status") {
                call.respond(
                    buildJsonObject {
                        put("status", "ok")
                        put("time", System.currentTimeMillis().toString())
                    }
                )
            }

            route("/v1") {
                ServiceRegistry.registerRouting(this)
            }

        }

    }
}