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

package dev.hybrismc.core

import com.grappenmaker.jvmutil.*
import dev.hybrismc.meta.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.network.util.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.spongepowered.asm.launch.MixinBootstrap
import org.spongepowered.asm.mixin.MixinEnvironment
import org.spongepowered.asm.service.MixinService
import java.io.InputStream
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Paths
import kotlin.io.path.*

lateinit var globalGameLoader: GameLoader
lateinit var globalAccessorRegistry: AccessorRegistry

val finders = FinderContext()
val mixinService
    get() = MixinService.getService() as? HybrisMixinService ?: error("Mixin service initialized incorrectly!")

val http = createDefaultHTTP()
private fun String.log() = println("[Hybris Bootstrap] $this")

object CurrentVersion {
    lateinit var minecraftVersion: MinecraftVersion
    lateinit var versionEntry: VersionEntry
    val coreVersion by lazy {
        javaClass.classLoader.getResourceAsStream("hybrisVersion.txt")?.readBytes()?.decodeToString() ?: "unknown"
    }
}

fun main(args: Array<String>) {
    "Hybris Core version ${CurrentVersion.coreVersion}".log()

    val version = args.firstOrNull() ?: error("Specify a version!")
    "Launching version $version".log()

    val versionEntry = runBlocking { http.fetchVersion(version) } ?: error("No such version $version")
    "Fetched version info, verifying libs...".log()

    CurrentVersion.minecraftVersion = mcVersionByID(version) ?: error("No version enum constant was found")
    CurrentVersion.versionEntry = versionEntry

    val gameDir = getMinecraftDir().also { it.createDirectories() }
    val gamePath = gameDir.resolve("versions").resolve(version).resolve("$version.jar")

    if (!gamePath.exists()) {
        "Game Jar missing, downloading game...".log()
        runBlocking { http.download(versionEntry.downloads.client.url, gamePath) }
    }

    val libraryFiles = versionEntry.relevantLibraries.composeDownloads(gameDir.resolve("libraries"))

    http.downloadAll(
        libraryFiles
            .filterNot { it.second.exists() }
            .also { if (it.isNotEmpty()) "Downloading ${it.size} missing libraries...".log() }
    )

    // TODO: load version specific JAR (module)
    // TODO: version config system?
    // TODO: version specific generation?

    val urls = libraryFiles.map { it.second.toUri().toURL() } + gamePath.toUri().toURL()
    val mappings = loadMappings(
        (ClassLoader.getSystemResourceAsStream("mappings/mappings.tiny")
            ?: error("No tiny mappings on classpath!")).bufferedReader().readLines()
    )

    val gameArgs = (buildList {
        fun addArgument(name: String, value: String) {
            val prefix = "--$name"
            if (prefix !in args) {
                add(prefix)
                add(value)
            }
        }

        addArgument("accessToken", "0")
        addArgument("version", versionEntry.id)
        addArgument("assetIndex", versionEntry.assetIndex.id)
        addArgument("assetsDir", gameDir.resolve("assets").absolutePathString())
    } + args.drop(1)).toTypedArray()

    globalGameLoader = GameLoader(
        mappings = mappings,
        urls = urls.toTypedArray(),
        parent = GameLoader::class.java.classLoader,
        dump = System.getProperty("hybris.dump")?.toBooleanStrictOrNull() == true
    )

    globalAccessorRegistry = AccessorRegistry(globalGameLoader)

    "Initializing code generators".log()
    initBridge()
    initScreen()

    // Patch game to not break stack traces / crash reports
    findMinecraftClass {
        strings hasPartial "Stacktrace:"
        methods {
            "firstTwoElementsOfStackTraceMatch" {
                method calls { method named "getFileName" }
                transform {
                    replaceCall(
                        matcher = { it.name == "getFileName" },
                        replacement = { pop(); loadConstant("") }
                    )
                }
            }
        }
    }

    "Initializing Mixin".log()
    MixinBootstrap.init()

    "Launching game".log()
    globalGameLoader.loadClass(versionEntry.mainClass).getMethod("main", Array<String>::class.java)(null, gameArgs)
}

class GameLoader(
    mappings: Mappings,
    urls: Array<URL>,
    parent: ClassLoader,
    private val dump: Boolean = false
) : URLClassLoader("GameLoader", urls, parent) {
    private val knownNames = mappings.classes.map { it.names[mappings.namespace("named")] }.toSet()
    private val remapper = MappingsRemapper(
        mappings,
        from = "official",
        to = "named",
        loader = ::cachedClass
    )

    private val demapper = mappings.asSimpleRemapper("named", "official")
    private val disallowedReloading = setOf(
        "dev.hybrismc.", "com.grappenmaker.", "org.slf4j."
    )

    private val disallowedInheritance = setOf("org.slf4j.", "com.google.")

    private val classCache = hashMapOf<String, ByteArray?>()
    private fun cachedClass(name: String) =
        classCache.getOrPut(name) { getResourceAsStream("$name.class")?.readBytes() }

    private val dumpPath = Paths.get("dump")

    private fun disallowedInheritance(name: String) = disallowedInheritance.any { name.startsWith(it) }
    private fun findLoaded(name: String) = if (disallowedInheritance(name)) null else findLoadedClass(name)

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        findLoaded(name)?.let { return it }
        if (isSystemClass(name) || disallowedReloading.any { name.startsWith(it) }) {
            return super.loadClass(name, resolve)
        }

        val clazz = findClass(name)
        if (resolve) resolveClass(clazz)
        return clazz
    }

    fun untransformedBytes(internalName: String): ByteArray? {
        return when (internalName) {
            !in knownNames -> (getResourceAsStream("$internalName.class") ?: return null).readBytes()
            else -> {
                remapUnmapped(
                    (getResourceAsStream(
                        "${demapper.map(internalName) ?: internalName}.class"
                    ) ?: return null).readBytes()
                )
            }
        }
    }

    private fun remapUnmapped(bytes: ByteArray): ByteArray? {
        val reader = ClassReader(bytes)
        val writer = ClassWriter(reader, 0)
        reader.accept(LambdaAwareRemapper(AccessWideningVisitor(writer), remapper), 0)

        return writer.toByteArray()
    }

    fun transform(internalName: String, bytes: ByteArray): ByteArray {
        val afterAccessor = DirectAccessors.transform(internalName, bytes) ?: bytes
        return finders.transform(this, internalName, afterAccessor) ?: afterAccessor
    }

    override fun findClass(name: String): Class<*> {
        findLoaded(name)?.let { return it }
        val internalName = name.replace('.', '/')
        val transformed = transform(internalName, untransformedBytes(internalName) ?: return parent.loadClass(name))
        val mixed = mixinService.transformer.transformClass(
            /* environment = */ MixinEnvironment.getDefaultEnvironment(),
            /* name = */ name,
            /* classBytes = */ transformed
        ) ?: transformed

        return create(name, mixed)
    }

    private fun create(name: String, bytes: ByteArray): Class<*> {
        if (dump) {
            runCatching {
                "${name.replace('.', '/')}.class".split('/')
                    .fold(dumpPath) { acc, curr -> acc.resolve(curr) }
                    .also { it.parent.createDirectories() }
                    .writeBytes(bytes)
            }.onFailure { println("Failed dumping $name"); it.printStackTrace() }
        }

        return defineClass(name, bytes, 0, bytes.size)
    }

    override fun getResource(name: String): URL? =
        if (name.endsWith(".class") && disallowedInheritance(name.substringBeforeLast('.').replace('/', '.')))
            findResource(name) ?: parent.getResource(name) else super.getResource(name)

    override fun getResourceAsStream(name: String): InputStream? = when {
        !name.endsWith(".class") -> super.getResourceAsStream(name)
        else -> {
            val internalName = name.dropLast(6)
            super.getResourceAsStream(if (internalName in knownNames) "${demapper.map(internalName)}.class" else name)
        }
    }
}