/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.api.app.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import wiki.vessel.api.app.user.User
import java.util.*

object JWTConfig {
    private const val SECRET = "supersecretkey"
    private const val ISSUER = "vessel"
    private const val AUDIENCE = "users"

    private val algorithm = Algorithm.HMAC256(SECRET)
    private val verifier = JWT.require(algorithm).withIssuer(ISSUER).build()

    fun generateToken(user: User, lifespan: Long = 36_00_000, sessionId: Long? = null): String {
        return JWT.create()
            .withAudience(AUDIENCE)
            .withSubject(user.id.toString())
            .withIssuer(ISSUER)
            .withExpiresAt(Date(System.currentTimeMillis() + lifespan))
            .withClaim("userId", user.id)
            .withClaim("version", user.tokenVersion)
            .apply { sessionId?.let { withClaim("sessionId", it) } }
            .sign(algorithm)
    }

    fun decodeToken(token: String): Map<String, Any>? {
        return try {
            val decoded = verifier.verify(token)
            buildMap {
                put("userId", decoded.subject.toLong())
                put("version", decoded.getClaim("version").asInt())
                decoded.getClaim("sessionId")?.asLong()?.let { put("sessionId", it) }
            }
        } catch (_: JWTVerificationException) { null }
    }

}
