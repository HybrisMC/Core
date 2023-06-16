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
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

fun initScreen() = Unit

val minecraftScreen = findNamedClass("net/minecraft/client/gui/screen/Screen") {
    methods {
        named("init")
        named("render")
        named("removed")
        named("resize")
        named("keyPressed")
        "ctor" { method.isConstructor() }
    }
}

private val minecraftScreenBridge by lazy {
    val shortName = "MinecraftScreenBridge"
    val fullName = "$generatedPrefix/$shortName"
    val target = minecraftScreen.nullable() ?: error("Minecraft screen changed?")
    val delegateField = FieldDescription(
        name = "screenDelegate",
        descriptor = "L${internalNameOf<CustomScreen>()};",
        owner = fullName,
        access = Opcodes.ACC_PUBLIC or Opcodes.ACC_FINAL
    )

    val mouseClicked = target.methodByName("mouseClicked")

    generateDefaultClass(
        name = shortName,
        extends = target.name,
        access = Opcodes.ACC_PUBLIC,
        defaultConstructor = false,
        debug = true
    ) {
        visitField(delegateField)
        generateMethod("<init>", "(${delegateField.descriptor}L${internalNameOf<TextComponent>()};)V") {
            loadThis()
            dup()

            val ctor by minecraftScreen.methods
            Type.getArgumentTypes(ctor().method.desc).forEach {
                load(2)
                getProperty(InstanceAccessor::delegate)
                cast(it)
            }

            invokeMethod(ctor())

            load(1)
            setField(delegateField)
            returnMethod()
        }

        generateMethod("init", "()V") {
            loadThis()
            getField(delegateField)
            invokeMethod(CustomScreen::init)
            returnMethod()
        }

        val render by minecraftScreen.methods
        val renderDesc = render().method.desc

        generateMethod("render", renderDesc) {
            loadThis()
            getField(delegateField)
            dup()
            invokeMethod(CustomScreen::handleScreenInput)

            val startIdx = Type.getArgumentTypes(renderDesc).indexOfFirst { it.sort == Type.INT } + 1
            load(startIdx, Opcodes.ILOAD)
            load(startIdx + 1, Opcodes.ILOAD)
            load(startIdx + 2, Opcodes.FLOAD)

            invokeMethod(CustomScreen::render)
            returnMethod()
        }

        generateMethod("removed", "()V") {
            loadThis()
            getField(delegateField)
            invokeMethod(CustomScreen::removed)
            returnMethod()
        }

        generateMethod("resize", "(Lnet/minecraft/client/MinecraftClient;II)V") {
            loadThis()
            getField(delegateField)
            load(1)
            load(2, Opcodes.ILOAD)
            load(3, Opcodes.ILOAD)
            invokeMethod(CustomScreen::resize)
            returnMethod()
        }

        if (mouseClicked != null) {
            generateMethod("mouseClicked", "(III)V") {
                loadThis()
                getField(delegateField)
                load(1, Opcodes.ILOAD)
                load(2, Opcodes.ILOAD)
                load(3, Opcodes.ILOAD)
                invokeMethod(CustomScreen::mouseClicked)
                returnMethod()
            }
        }

        val keyPressed by minecraftScreen.methods
        val keyPressedDesc = keyPressed().method.desc
        generateMethod("keyPressed", keyPressedDesc) {
            loadThis()
            getField(delegateField)
            load(1 + Type.getArgumentTypes(keyPressedDesc).indexOfFirst { it.sort == Type.INT }, Opcodes.ILOAD)
            invokeMethod(CustomScreen::keyPressed)

            val type = Type.getReturnType(keyPressedDesc)
            visitInsn(type.stubLoadInsn)
            returnMethod(type.getOpcode(Opcodes.IRETURN))
        }

        generateMethod("asMinecraftScreen", "()L${internalNameOf<Screen>()};") {
            loadThis()
            cast(asmTypeOf<Screen>())
            returnMethod(Opcodes.ARETURN)
        }
    }
}

private val delegateScreenCtor by lazy {
    minecraftScreenBridge.getConstructor(CustomScreen::class.java, TextComponent::class.java)
}

fun CustomScreen.asMinecraftScreen(title: TextComponent) = delegateScreenCtor.newInstance(this, title) as Screen
fun CustomScreen.asMinecraftScreen(title: String = "Untitled") =
    asMinecraftScreen(TextComponent.Serializer.create("""{"text": "$title"}"""))

fun CustomScreen.handleScreenInput() {
    MinecraftClient.instance.actualMouse?.let { mouse ->
        if (mouse.leftButtonClicked) mouseClicked(mouse.x.toInt(), mouse.y.toInt(), 0)
        if (mouse.rightButtonClicked) mouseClicked(mouse.x.toInt(), mouse.y.toInt(), 1)
    }
}


interface CustomScreen {
    fun mouseClicked(x: Int, y: Int, button: Int) {}
    fun keyPressed(key: Int) {}
    fun init() {}
    fun render(mouseX: Int, mouseY: Int, delta: Float) {}
    fun removed() {}
    fun resize(client: MinecraftClient, width: Int, height: Int) {}
}

private val mouseAccess = accessor<_, Mouse.Static>()

@Named("net/minecraft/client/Mouse")
// TODO: think of a system to safely handle multiversion
interface Mouse {
    val activeButton: Int
    val client: MinecraftClient
    val controlLeftTicks: Int
    val cursorDeltaX: Double
    val cursorDeltaY: Double
    val eventDeltaWheel: Double
    val glfwTime: Double
    val hasResolutionChanged: Boolean
    val lastMouseUpdateTime: Double
    val leftButtonClicked: Boolean
    val middleButtonClicked: Boolean
    val rightButtonClicked: Boolean
    val x: Double
    val y: Double
    fun isCursorLocked(): Boolean
    fun lockCursor()
    fun onCursorPos(window: Long, x: Double, y: Double)
    fun onMouseButton(window: Long, button: Int, action: Int, mods: Int)
    fun onMouseScroll(window: Long, horizontal: Double, vertical: Double)
    fun onResolutionChanged()
    fun unlockCursor()
    fun updateMouse()
    fun wasLeftButtonClicked(): Boolean
    fun wasRightButtonClicked(): Boolean

    interface Static : StaticAccessor<Mouse>
    companion object : Static by mouseAccess.static()
}