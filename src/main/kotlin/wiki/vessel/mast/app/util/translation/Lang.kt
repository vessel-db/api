/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.mast.app.util.translation

import org.yaml.snakeyaml.Yaml
import java.io.File

object Lang {
    private var messages: Map<String, String> = emptyMap()

    fun load(path: String) {
        val file = File(path)
        if (!file.exists()) {
            val stream = Lang::class.java.getResourceAsStream("/lang.yml")
                ?: throw IllegalStateException("Default lang not found")
            file.writeText(stream.bufferedReader().use { it.readText() })
        }
        messages = Yaml().load<Map<String, Any>>(file.readText())
            .flatMap { (_, v) -> (v as Map<String, String>).entries }
            .associate { it.key to it.value }
    }

    operator fun get(key: String): String = messages[key] ?: "[$key]"
}