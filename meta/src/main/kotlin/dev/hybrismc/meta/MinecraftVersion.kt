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
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.network.util.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.channels.Channels
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream

suspend inline fun <reified T : Any> HttpClient.fetch(url: String) = get(url).body<T>()

suspend fun HttpClient.fetchVersionManifest(): VersionManifest =
    fetch("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json")

suspend fun HttpClient.fetchVersion(version: String): VersionEntry? {
    val versionManifest = fetchVersionManifest()
    val versionInfo = versionManifest.versions.find { it.id == version } ?: return null
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

@Serializable
data class VersionManifest(val latest: LatestInfo, val versions: List<VersionInfo>)

@Serializable
data class LatestInfo(
    val release: String,
    val snapshot: String
)

@Serializable
data class VersionInfo(
    val complianceLevel: Int = 0,
    val id: String,
    val releaseTime: String,
    val sha1: String,
    val time: String,
    val type: String,
    val url: String
)

@Serializable
data class VersionEntry(
    val assetIndex: AssetIndexInfo,
    val complianceLevel: Int,
    val id: String,
    val libraries: List<Library>,
    val mainClass: String,
    val minecraftArguments: String = "",
    val minimumLauncherVersion: Int,
    val releaseTime: String,
    val time: String,
    val type: String,
    val downloads: VersionDownloads,
    val javaVersion: JavaVersionInfo,
)

@Serializable
data class JavaVersionInfo(
    val majorVersion: Int,
    val component: String,
)

val VersionEntry.relevantLibraries get() = libraries.filter { lib -> lib.rules?.all { it.matches } ?: true }

fun LibraryArtifact.asDownload(targetDir: Path) = this to targetDir.resolve(path)

fun List<Library>.composeDownloads(targetDir: Path) =
    flatMap { lib -> lib.downloads.allDownloads.map { it.asDownload(targetDir) } }

@Serializable
data class VersionDownloads(
    val client: VersionDownloadsEntry,
)

@Serializable
data class VersionDownloadsEntry(
    val sha1: String,
    val size: Int,
    val url: String,
)

@Serializable
data class AssetIndexInfo(
    val id: String,
    val sha1: String,
    val size: Int,
    val totalSize: Int,
    val url: String
)

@Serializable
data class Library(
    val downloads: LibraryDownloads,
    val name: String,
    val rules: List<OSRule>? = null
)

@Serializable
data class LibraryDownloads(
    val artifact: LibraryArtifact? = null,
    val classifiers: Map<String, LibraryArtifact>? = null
)

val LibraryDownloads.natives get() = classifiers?.get("natives-${osIdentifier()}")
val LibraryDownloads.allDownloads get() = listOfNotNull(artifact) + listOfNotNull(natives)

@Serializable
data class LibraryArtifact(
    val path: String,
    val sha1: String,
    val size: Int,
    val url: String
)

@Serializable
data class OSRule(
    val action: String,
    val os: OSInfo? = null,
)

val OSRule.allow
    get() = when (action) {
        "allow" -> true
        "disallow" -> false
        else -> error("Invalid action $action")
    }

val OSRule.matches get() = os == null || allow xor (os.name != osIdentifier())

@Serializable
data class OSInfo(
    val name: String? = null,
    val version: String? = null,
    val arch: String? = null,
)

fun getMinecraftDir(): Path = with(System.getProperty("os.name")) {
    when {
        startsWith("Mac OS") -> Paths.get(System.getenv("HOME"), "Library", "Application Support", "minecraft")
        startsWith("Linux") -> Paths.get(System.getenv("HOME"), ".minecraft")
        startsWith("Windows") -> Paths.get(System.getenv("APPDATA"), ".minecraft")
        else -> error("Unsupported platform")
    }
}

fun osIdentifier() = with(System.getProperty("os.name")) {
    when {
        startsWith("Mac OS") -> "osx"
        startsWith("Linux") -> "linux"
        startsWith("Windows") -> "windows"
        else -> error("Unsupported platform")
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

enum class MinecraftVersion(val id: String) {
    V1_7("1.7.10"), V1_8("1.8.9"), V1_12("1.12.2"),
    V1_16_1("1.16.1"), V1_18("1.18.2"), V1_19("1.19.4"),
    V1_20("1.20.1");
}

fun mcVersionByID(id: String) = enumValues<MinecraftVersion>().find { it.id == id }