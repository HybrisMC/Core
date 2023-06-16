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

import com.grappenmaker.jvmutil.CompoundLoader
import com.grappenmaker.jvmutil.generateClass
import dev.hybrismc.meta.LoaderDummy
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.isDirectory
import kotlin.io.path.writeBytes

// Everything related to class generation
// Prefix for every classname that gets generated
const val generatedPrefix = "dev/hybrismc/generated"
val combinedAppLoader by lazy {
    CompoundLoader(
        loaders = listOf(globalGameLoader, LoaderDummy::class.java.classLoader),
        parent = LoaderDummy::class.java.classLoader
    )
}

/**
 * Utility to get a class by [name] with app loader
 */
fun getAppClass(name: String): Class<*> = combinedAppLoader.loadClass(name)

// ClassLoader used for loading generated classes
object GenerationLoader : ClassLoader(combinedAppLoader) {
    private val dumpPath: Path? = System.getProperty("generation.dump")
        ?.let { Paths.get(it).also { f -> f.createDirectories() } }
        ?.takeIf { it.isDirectory() }

    fun createClass(name: String, bytes: ByteArray): Class<*> {
        // Dump if property set
        if (dumpPath != null) {
            "${name.replace('.', '/')}.class".split('/').fold(dumpPath) { acc, curr -> acc.resolve(curr) }
                .also { it.parent.createDirectories() }
                .writeBytes(bytes)
        }

        return defineClass(name, bytes, 0, bytes.size)
    }
}

// Increment-on-get counter to generate unique names for unnamed classes
var unnamedCounter = 0
    get() = field++

// Utility that acts as a shortcut for generateClass
inline fun generateDefaultClass(
    name: String = "Unnamed$unnamedCounter",
    extends: String = "java/lang/Object",
    implements: List<String> = listOf(),
    defaultConstructor: Boolean = true,
    access: Int = Opcodes.ACC_PUBLIC or Opcodes.ACC_FINAL,
    debug: Boolean = false,
    generator: ClassVisitor.() -> Unit
) = generateClass(
    name = "$generatedPrefix/$name",
    extends, implements, defaultConstructor, access,
    loader = { bytes, cName -> GenerationLoader.createClass(cName, bytes) },
    debug = debug,
    generator = generator
)

// Utility to create an instance of a given class
// (useful when class extends an interface)
// Assumes there is a no-arg constructor
inline fun <reified T> Class<*>.instance() = getConstructor().newInstance() as T

private object LoaderDummy