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

import java.io.File
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

object LoaderDummy

fun main(args: Array<String>) = println("Took ${measureTimeMillis {
    if (args.size < 2) "Usage: <version> <output> [<mappings>]".exitMessage()

    val (version, output) = args
    val gameJar = getMinecraftDir().resolve("versions").resolve(version).resolve("$version.jar")

    println("Remapping ${gameJar.absolutePathString()}")
    if (!gameJar.exists()) "Version has not been downloaded!".exitMessage()
    
    val mappingsArgs = args.getOrNull(2)
    val mappingsLines = if (mappingsArgs != null) {
        val mappingsFile = File(mappingsArgs)
        if (!mappingsFile.exists()) "Mappings file ${mappingsFile.absolutePath} does not exist!".exitMessage()
        
        mappingsFile.readLines()
    } else {
        (LoaderDummy::class.java.classLoader.getResourceAsStream("$version.mapping")
            ?: "No mapping found for version $version, specify one!".exitMessage()).bufferedReader().readLines()
    }
    
    val format = (allMappingsFormats.find { it.detect(mappingsLines) }
        ?: "No suitable mappings format was found!".exitMessage())

    remapJar(format.parse(mappingsLines), gameJar.toFile(), File(output))
}}ms")

private fun String.exitMessage(): Nothing {
    println(this)
    exitProcess(-1)
}