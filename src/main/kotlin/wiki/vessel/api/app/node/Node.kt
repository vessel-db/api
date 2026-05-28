/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.api.app.node

import kotlinx.serialization.Serializable
import wiki.vessel.api.app.Vessel

@Serializable
data class Node(
    val id: Long = Vessel.generator.nextId(),
    val name: String,
    val address: String,
    val token: String,
    val diskMb: Long,
    val memoryMb: Long,
    val createdAt: Long,
) {

    companion object {
        fun create(name: String, address: String, diskMb: Long, memoryMb: Long): Node {
            return Node(
                id = Vessel.generator.nextId(),
                name = name,
                address = address,
                token = generateToken(),
                diskMb = diskMb,
                memoryMb = memoryMb,
                createdAt = System.currentTimeMillis(),
            )
        }

        private fun generateToken(): String {
            val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            return (1..48).map { chars.random() }.joinToString("")
        }
    }

}