/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.api.app

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.atlantisservices.sdb.SdbRepository
import net.atlantisservices.snowflake.SnowflakeService
import org.slf4j.LoggerFactory
import wiki.vessel.api.app.config.AppConfig
import wiki.vessel.api.app.service.ServiceRegistry
import wiki.vessel.api.app.user.User
import wiki.vessel.api.app.user.UserService

object Vessel {

    private val logger = LoggerFactory.getLogger(Vessel::class.java)

    lateinit var generator: SnowflakeService
        private set

    private lateinit var server: NettyApplicationEngine

    lateinit var users: SdbRepository<User>

    fun start() {
        if (::server.isInitialized) {
            logger.warn("Server already started.")
            return
        }

        generator = SnowflakeService.default()

        ServiceRegistry.register(
            UserService(),
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

        if (AppConfig.environment == AppConfig.Environment.PRODUCTION) {
            intercept(ApplicationCallPipeline.Plugins) {
                val acceptsHtml = call.request.acceptItems()
                    .any { it.value.equals(ContentType.Text.Html.toString(), ignoreCase = true) }

                if (acceptsHtml) {
                    call.respondRedirect(AppConfig.panelUrl)
                    finish()
                }
            }
        }

        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                }
            )
        }

        install(CORS) {
            allowHost("localhost:4321", schemes = listOf("http"))
            allowHost(
                AppConfig.panelUrl.replace("http://", "").replace("https://", ""),
                schemes = listOf("https", "http")
            )

            allowCredentials = true

            allowMethod(HttpMethod.Options)
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Patch)
            allowMethod(HttpMethod.Delete)

            allowHeader(HttpHeaders.Authorization)
            allowHeader(HttpHeaders.ContentType)
        }

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