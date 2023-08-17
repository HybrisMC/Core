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

import dev.hybrismc.meta.LoaderDummy

class ShaderProgram(vertex: String, fragment: String) {
    val id = GLCompat.glCreateProgram()
    val vertexShader = Shader(vertex, ShaderType.VERTEX)
    val fragmentShader = Shader(fragment, ShaderType.FRAGMENT)
    var initialized = false
        private set

    var usable = false
        private set

    val shaderStart = System.currentTimeMillis()

    private fun cleanupShaders() = applyToShaders { GLCompat.glDeleteShader(it.id) }

    inline fun use(setup: () -> Unit = {}) {
        ensureInitialized()

        if (usable) {
            GLCompat.glUseProgram(id)
            setup()
        }
    }

    fun unuse() = GLCompat.glUseProgram(0)

    fun ensureInitialized() {
        if (!initialized) {
            initialized = true

            applyToShaders {
                it.compile()
                GLCompat.glAttachShader(id, it.id)
            }

            GLCompat.glLinkProgram(id)

            validate(GLConstants.GL_LINK_STATUS) {
                cleanupShaders()
                GLCompat.glDeleteProgram(id)
            }

            GLCompat.glValidateProgram(id)
            validate(GLConstants.GL_VALIDATE_STATUS)
            cleanupShaders()

            usable = true
        }
    }

    private val uniformCache = hashMapOf<String, Int>()
    private val attribCache = hashMapOf<String, Int>()
    fun getUniform(name: String) = uniformCache.getOrPut(name) { GLCompat.glGetUniformLocation(id, name) }
    fun getAttribute(name: String) = attribCache.getOrPut(name) { GLCompat.glGetAttribLocation(id, name) }

    inline fun render(block: GLCompat.() -> Unit) {
        val current = GLCompat.glGetInteger(GLConstants.GL_CURRENT_PROGRAM)
        use()
        GLCompat.block()
        GLCompat.glUseProgram(current)
    }

    inline fun applyToShaders(block: (Shader) -> Unit) {
        block(vertexShader)
        block(fragmentShader)
    }

    private inline fun validate(type: Int, whenFailed: () -> Unit = {}) {
        if (GLCompat.glGetProgrami(id, type) != GLConstants.GL_TRUE) {
            whenFailed()

            val length = GLCompat.glGetProgrami(id, GLConstants.GL_INFO_LOG_LENGTH)
            error("Could not validate ShaderProgram: ${GLCompat.glGetProgramInfoLog(id, length)}")
        }
    }

    fun defaultUniforms() {
        val time = (System.currentTimeMillis() - shaderStart) / 1000f
        val window = MinecraftClient.instance.actualWindow

        GLCompat.glUniform1f(getUniform("u_time"), time)
        GLCompat.glUniform2f(
            getUniform("u_resolution"),
            window.width.toFloat(),
            window.height.toFloat()
        )
    }
}

class Shader(private val program: String, val type: ShaderType) {
    val id = GLCompat.glCreateShader(type.id)
    private var compiled = false

    fun compile() {
        if (!compiled) {
            compiled = true
            GLCompat.glShaderSource(id, program)
            GLCompat.glCompileShader(id)
            if (GLCompat.glGetShaderi(id, GLConstants.GL_COMPILE_STATUS) != GLConstants.GL_TRUE) {
                val length = GLCompat.glGetShaderi(id, GLConstants.GL_INFO_LOG_LENGTH)
                error("Shader did not compile: ${GLCompat.glGetShaderInfoLog(id, length)}")
            }
        }
    }
}

enum class ShaderType(val id: Int) {
    VERTEX(GLConstants.GL_VERTEX_SHADER),
    FRAGMENT(GLConstants.GL_FRAGMENT_SHADER)
}

fun loadShader(name: String) = ShaderProgram(
    vertex = LoaderDummy::class.java.classLoader.getResourceAsStream("$name.vert")?.readBytes()?.decodeToString()
        ?: error("Vertex shader $name.vert has not been found on the classpath!"),
    fragment = LoaderDummy::class.java.classLoader.getResourceAsStream("$name.frag")?.readBytes()?.decodeToString()
        ?: error("Fragment shader $name.frag has not been found on the classpath!")
)