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

import com.grappenmaker.jvmutil.asClassNode
import com.grappenmaker.jvmutil.internalNameOf
import com.grappenmaker.jvmutil.isSystemClass
import dev.hybrismc.meta.MinecraftVersion
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.launch.MixinBootstrap
import org.spongepowered.asm.launch.platform.container.IContainerHandle
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.MixinEnvironment
import org.spongepowered.asm.mixin.Mixins
import org.spongepowered.asm.mixin.transformer.IMixinTransformer
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory
import org.spongepowered.asm.service.*
import org.spongepowered.asm.service.modlauncher.LoggerAdapterLog4j2
import org.spongepowered.asm.util.Constants
import org.spongepowered.asm.util.ReEntranceLock
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.file.Files
import java.util.*
import kotlin.io.path.writeBytes

@Retention
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
@Suppress("unused")
annotation class OnlyOn(vararg val versions: MinecraftVersion)

object GameClassProvider : IClassProvider, IClassBytecodeProvider {
    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("Deprecated in Java")
    override fun getClassPath() = emptyArray<URL>()

    override fun findClass(name: String): Class<*> = findClass(name, true)
    override fun findClass(name: String, initialize: Boolean): Class<*> =
        Class.forName(name, initialize, globalGameLoader)

    override fun findAgentClass(name: String, initialize: Boolean) = findClass(name, initialize)
    override fun getClassNode(name: String) = getClassNode(name, true)
    override fun getClassNode(name: String, runTransformers: Boolean): ClassNode {
        val untransformed = globalGameLoader.untransformedBytes(name.replace('.', '/')) ?: error(
            "Class for $name does not exist!"
        )

        return (if (runTransformers) globalGameLoader.transform(name, untransformed) else untransformed).asClassNode()
            .also { it.processMixinClass() }
    }

    private inline fun <reified T : Any> List<AnnotationNode>.findAnnotation() =
        find { it.desc == "L${internalNameOf<T>()};" }

    private fun List<AnnotationNode>.findOnlyOn(): List<String>? {
        val values = findAnnotation<OnlyOn>()?.values ?: return null
        return (values.windowed(2, 2).associate { (k, v) -> k as String to v }["versions"] as? List<*>)
            ?.map { (it as Array<*>)[1] as String }
    }

    private fun List<AnnotationNode>.checkTarget() =
        findOnlyOn()?.contains(CurrentVersion.minecraftVersion.name) ?: true

    private fun ClassNode.processMixinClass() {
        if (visibleAnnotations?.checkTarget() == false) {
            visibleAnnotations?.findAnnotation<Mixin>()?.values?.clear()
            invisibleAnnotations?.findAnnotation<Mixin>()?.values?.clear()
            return
        }

        fields.forEach { if (it.visibleAnnotations?.checkTarget() == false) fields.remove(it) }
        methods.forEach { if (it.visibleAnnotations?.checkTarget() == false) methods.remove(it) }
    }
}

class HybrisMixinService : IMixinService {
    private var transformerFactory: IMixinTransformerFactory? = null
    val transformer: IMixinTransformer by lazy { transformerFactory!!.createTransformer() }
    private val lock = ReEntranceLock(1)

    object Handle : IContainerHandle {
        override fun getAttribute(name: String) = null
        override fun getNestedContainers() = emptyList<IContainerHandle>()
    }

    override fun getName() = "HybrMixin (in ${javaClass.classLoader})"
    override fun isValid() = true

    override fun prepare() {}
    override fun getInitialPhase(): MixinEnvironment.Phase = MixinEnvironment.Phase.PREINIT
    override fun offer(internal: IMixinInternal?) {
        if (internal is IMixinTransformerFactory) transformerFactory = internal
    }

    override fun init() {}
    override fun beginPhase() {}
    override fun checkEnv(bootSource: Any?) {}
    override fun getReEntranceLock() = lock

    override fun getClassProvider() = GameClassProvider
    override fun getBytecodeProvider() = GameClassProvider
    override fun getTransformerProvider() = null
    override fun getClassTracker() = null
    override fun getAuditTrail() = null
    override fun getPlatformAgents() = emptyList<String>()
    override fun getPrimaryContainer() = Handle
    override fun getMixinContainers() = listOf(Handle)
    override fun getResourceAsStream(name: String): InputStream? = globalGameLoader.getResourceAsStream(name)
    override fun getSideName() = Constants.SIDE_CLIENT
    override fun getMinCompatibilityLevel() = null
    override fun getMaxCompatibilityLevel() = null
    override fun getLogger(name: String) = LoggerAdapterLog4j2(name)
}

@Suppress("unused")
class StubPropertyService : IGlobalPropertyService {
    private val properties = mutableMapOf<Key, Any?>()

    data class Key(val name: String) : IPropertyKey

    override fun resolveKey(name: String) = Key(name)

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> getProperty(key: IPropertyKey) = properties[key as Key] as T?

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> getProperty(key: IPropertyKey, defaultValue: T) =
        properties[key as Key] as? T? ?: defaultValue

    override fun setProperty(key: IPropertyKey, value: Any?) {
        properties[key as Key] = value
    }

    override fun getPropertyString(key: IPropertyKey, defaultValue: String) = getProperty(key, defaultValue)
}

@Suppress("unused")
class BootstrapService : IMixinServiceBootstrap {
    override fun getName() = "HybrMixin"
    override fun getServiceClassName(): String = HybrisMixinService::class.java.name
    override fun bootstrap() {}
}