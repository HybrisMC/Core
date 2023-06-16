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

fun MethodsContext.named(name: String, block: MethodContext.() -> Unit = {}) = name {
    method named name
    block()
}

fun FieldsContext.named(name: String, block: FieldContext.() -> Unit = {}) = name {
    node named name
    block()
}

fun MethodsContext.namedTransform(name: String, block: MethodTransformContext.() -> Unit) = name {
    method named name
    transform(block)
}

fun MethodsContext.clinit(block: MethodContext.() -> Unit) = "clinit" {
    method.isStaticInit()
    block()
}

fun MethodsContext.clinitTransform(block: MethodTransformContext.() -> Unit) = "clinit" {
    method.isStaticInit()
    transform(block)
}

fun findNamedClass(name: String, block: ClassContext.() -> Unit = {}) = finders.findClass {
    node named name.replace('.', '/')
    block()
}

fun findMinecraftClass(block: ClassContext.() -> Unit) = finders.findClass {
    node match { it.name.startsWith("net/minecraft/") }
    block()
}

fun ClassContext.constantReplacement(from: Any, to: Any) = methods {
    unnamedMethod {
        constants has from
        transform { replaceConstant(from, to) }
    }
}

fun ClassContext.constantReplacements(map: Map<Any, Any>) = methods {
    map.forEach { (from, to) ->
        unnamedMethod {
            constants has from
            transform { replaceConstant(from, to) }
        }
    }
}

fun ClassContext.constantReplacements(vararg pairs: Pair<Any, Any>) = constantReplacements(pairs.toMap())