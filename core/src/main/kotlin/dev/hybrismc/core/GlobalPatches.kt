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
import net.java.games.input.ControllerEnvironment
import net.java.games.input.Mouse
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import kotlin.reflect.KProperty

fun applyGlobalPatches() {
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

    findMinecraftClass {
        fun MethodContext.matchMouseCall(name: String) = method calls {
            method named name
            method ownedBy Type.getObjectType("org/lwjgl/input/Mouse")
        }

        methods {
            "ctor" {
                method.isConstructor()
                transform {
                    methodExit {
                        getObject<RawInputThread>()
                        invokeMethod(Thread::start)
                    }
                }
            }

            "mouseXYChange" {
                matchMouseCall("getDX")
                matchMouseCall("getDY")

                transform {
                    fun replaceMouseCall(name: String, prop: KProperty<Float>) = replaceCall(
                        matcher = { it.name == name },
                        replacement = {
                            getProperty(prop)
                            visitInsn(Opcodes.F2I)
                        }
                    )

                    replaceMouseCall("getDX", RawInputThread::dx)
                    replaceMouseCall("getDY", RawInputThread::dy)
                }
            }

            "setGrabbed" {
                matchMouseCall("setGrabbed")
                method references { field isType Type.INT_TYPE }
                transform { methodExit { invokeMethod(RawInputThread::resetMouse) } }
            }
        }
    }

    findNamedClass("net/minecraft/client/MinecraftClient") {
        methods {
            namedTransform("initializeGame") {
                methodExit(Opcodes.RETURN) { invokeMethod(::testMixin) }
            }
        }
    }
}

fun testMixin() {
    MinecraftClient.instance.setScreen(TestScreen().asMinecraftScreen())
}

class TestScreen : CustomScreen {
    override fun render(mouseX: Int, mouseY: Int, delta: Float) {
        GLCompat.glClearColor(0f, 0f, 0f, 0f)
        GLCompat.glClear(GLConstants.GL_COLOR_BUFFER_BIT)
        MinecraftClient.instance.textRenderer.draw("Here!", mouseX, mouseY, 0xFFFFFF)
    }
}

object RawInputThread : Thread("MouseInputThread") {
    init {
        isDaemon = true
    }

    @JvmStatic
    var dx = 0f
        get() {
            val temp = field
            field = 0f
            return temp
        }

    @JvmStatic
    var dy = 0f
        get() {
            val temp = field
            field = 0f
            return temp
        }

    @JvmStatic
    fun resetMouse() {
        dx = 0f
        dy = 0f
    }

    private var env: ControllerEnvironment = ControllerEnvironment.getDefaultEnvironment()
    fun rescanMice() {
        env = Class.forName("net.java.games.input.DefaultControllerEnvironment")
            .getDeclaredConstructor().also { it.isAccessible = true }.newInstance() as ControllerEnvironment
    }

    override fun run() {
        while (true) {
            env.controllers.forEach { mouse ->
                if (mouse is Mouse) {
                    mouse.poll()
                    dx += mouse.x.pollData
                    dy -= mouse.y.pollData
                }
            }

            sleep(1)
        }
    }
}