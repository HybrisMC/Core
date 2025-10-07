/*
 * HybrisMC, a Minecraft toolchain and client
 * Copyright (C) 2023, The HybrisMC Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.hybrismc.meta

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.network.util.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.nio.channels.Channels
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream

suspend inline fun <reified T : Any> HttpClient.fetch(url: String) = get(url).body<T>()

suspend fun HttpClient.fetchVersionManifest(): VersionManifest =
    fetch("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json")

suspend fun HttpClient.fetchVersion(version: String, manifest: VersionManifest? = null): VersionEntry? {
    val versionInfo = (manifest ?: fetchVersionManifest()).versions.find { it.id == version } ?: return null
    return fetch(versionInfo.url)
}

suspend fun HttpClient.download(url: String, target: Path) = prepareGet(url).execute { res ->
    val channel = res.bodyAsChannel()
    val buf = DefaultByteBufferPool.borrow()

    target.parent.createDirectories()
    Channels.newChannel(target.outputStream()).use { out ->
        try {
            while (!channel.isClosedForRead) {
                channel.readAvailable(buf)
                buf.flip()
                out.write(buf)
                buf.rewind()
            }
        } finally {
            DefaultByteBufferPool.recycle(buf)
        }
    }
}

fun HttpClient.downloadAll(files: List<Pair<LibraryArtifact, Path>>) {
    runBlocking {
        files.map { (artifact, target) ->
            async {
                println("Downloading ${artifact.path} from ${artifact.url}...")
                download(artifact.url, target)
                println("Done downloading ${artifact.path}")
            }
        }.awaitAll()
    }
}

fun createDefaultHTTP(block: HttpClientConfig<*>.() -> Unit = {}) = HttpClient(CIO) {
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    engine { requestTimeout = 0 }
    block()
}