/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.api.app.settings

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import java.io.File

object SettingsProvider {
    private val lock = Any()
    private val values = mutableMapOf<String, Any>()
    private var dirty = false
    private var filePath = "settings.json"

    private val json = Json { ignoreUnknownKeys = true }

    fun load(path: String) {
        filePath = path
        val file = File(path)

        if (!file.exists()) {
            val stream = this::class.java.getResourceAsStream("/settings.json")
                ?: throw IllegalStateException("Default settings not found")
            file.writeText(stream.bufferedReader().use { it.readText() })
        }

        val raw = json.parseToJsonElement(file.readText()).jsonObject
        synchronized(lock) {
            raw.forEach { (k, v) ->
                values[k] = when (v) {
                    is JsonPrimitive if v.isString -> v.content
                    is JsonPrimitive -> v.intOrNull ?: v.longOrNull ?: v.doubleOrNull ?: v.booleanOrNull ?: v.content
                    else -> v.toString()
                }
            }
        }

        startFlushLoop()
    }

    operator fun <T : Any> get(key: Setting<T>): T = synchronized(lock) {
        @Suppress("UNCHECKED_CAST")
        (values[key.name] as? T) ?: key.default
    }

    operator fun <T : Any> set(key: Setting<T>, value: T) = synchronized(lock) {
        values[key.name] = value
        dirty = true
    }

    private fun startFlushLoop(intervalMillis: Long = 300_000L) {
        GlobalScope.launch {
            while (true) {
                delay(intervalMillis)
                saveIfDirty()
            }
        }
    }

    private fun saveIfDirty() = synchronized(lock) {
        if (!dirty) return
        val obj = buildJsonObject { values.forEach { (k, v) -> put(k, v.toString()) } }
        File(filePath).writeText(obj.toString())
        dirty = false
    }
}