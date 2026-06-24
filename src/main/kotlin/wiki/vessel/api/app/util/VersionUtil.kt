/*
 * Vessel — Copyright (c) 2026 Atlantis Services
 *
 * Licensed under the MIT License. See LICENSE in the project
 * root for license information.
 */

package wiki.vessel.api.app.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URI

object VersionUtil {

    private var cachedLatest: String? = null
    private var cacheTime: Long = 0
    private const val CACHE_TTL_MS = 10 * 60 * 1_000L

    suspend fun fetchLatest(repo: String = "vessel-db/api"): String? {
        val now = System.currentTimeMillis()
        if (cachedLatest != null && now - cacheTime < CACHE_TTL_MS) return cachedLatest
        return withContext(Dispatchers.IO) {
            runCatching {
                val url = URI("https://api.github.com/repos/$repo/releases/latest").toURL()
                val conn = url.openConnection()
                conn.setRequestProperty("Accept", "application/vnd.github+json")
                conn.setRequestProperty("User-Agent", "vessel-panel")
                conn.connectTimeout = 3_000
                conn.readTimeout  = 3_000

                val json = conn.getInputStream().bufferedReader().readText()
                Regex(""""tag_name"\s*:\s*"v?([^"]+)"""")
                    .find(json)
                    ?.groupValues
                    ?.get(1)
            }.getOrNull().also {
                cachedLatest = it
                cacheTime = now
            }
        }
    }
}