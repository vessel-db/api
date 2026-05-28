/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.api.app.config

import io.github.cdimascio.dotenv.dotenv

object AppConfig {

    private val dotenv = dotenv {
        ignoreIfMissing = true
    }

    fun env(key: String, default: String): String =
        dotenv.get(key, default).ifBlank { default }

    val panelUrl: String = env("PANEL_URL", "http://localhost:4321")

    val port: Int = env("APP_PORT", "8080").toIntOrNull() ?: run {
        println("Invalid APP_PORT, defaulting to 8080.")
        8080
    }

    val environment: Environment = runCatching {
        Environment.valueOf(env("ENVIRONMENT", Environment.DEVELOPMENT.name).uppercase())
    }.getOrElse {
        println("Invalid ENVIRONMENT value, defaulting to DEVELOPMENT.")
        Environment.DEVELOPMENT
    }

    enum class Environment {
        DEVELOPMENT,
        PRODUCTION
    }
}