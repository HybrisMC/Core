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
import com.grappenmaker.jvmutil.MethodImplementation
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.lang.reflect.Method
import java.nio.*

// Useful for huge static class accessors like GL
inline fun <reified V : Any> AccessorRegistry.overloadableExactAccessor(className: String): Lazy<V> {
    val implName = { m: Method -> m.name + m.asDescription().descriptor }

    val finder = findNamedClass(className) {
        methods {
            (V::class.java.declaredMethods).forEach {
                (implName(it)) {
                    method named it.name
                    arguments hasExact Type.getArgumentTypes(it.asDescription().descriptor).toList()
                }
            }
        }
    }

    return lazy {
        getAppClass(className.replace('/', '.'))
        require(finder.hasValue) { "Exact accessor for ${V::class} was not found, incompatible?" }

        internalGenerateAccessor(
            methods = finder.methods.contents,
            fields = emptyMap(),
            accessorName = "${V::class.simpleName}ExactAccessor",
            typeToImplement = V::class.java,
            constructorImpl = { _, _ ->
                // Create constructor
                generateMethod("<init>", "()V") {
                    // Call super() on Object
                    loadThis()
                    visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)

                    // End constructor
                    returnMethod()
                }
            },
            receiverLoader = { _, reflected -> require(!reflected) { "Exact accessor was reflected" } },
            implementationSelector = { methods, _, target, _ ->
                methods[implName(target)]?.nullable()?.let { MethodImplementation(target, it) }
                    ?: NotImplementedImplementation(target)
            },
            registry = this,
            allowNotImplemented = true
        ).instance()
    }
}

fun initGLCompat() = Unit

private inline fun <reified V : Any> AccessorRegistry.glAccessor(version: Int) =
    overloadableExactAccessor<V>("org.lwjgl.opengl.GL$version")

// CBA to map these, xd

val gl11Access by globalAccessorRegistry.glAccessor<GL11>(11)

interface GL11 {
    fun glAccum(arg0: Int, arg1: Float)
    fun glAlphaFunc(arg0: Int, arg1: Float)
    fun glClearColor(arg0: Float, arg1: Float, arg2: Float, arg3: Float)
    fun glClearAccum(arg0: Float, arg1: Float, arg2: Float, arg3: Float)
    fun glClear(arg0: Int)
    fun glCallLists(arg0: ByteBuffer)
    fun glCallLists(arg0: IntBuffer)
    fun glCallLists(arg0: ShortBuffer)
    fun glCallList(arg0: Int)
    fun glBlendFunc(arg0: Int, arg1: Int)
    fun glBitmap(arg0: Int, arg1: Int, arg2: Float, arg3: Float, arg4: Float, arg5: Float, arg6: ByteBuffer)
    fun glBitmap(arg0: Int, arg1: Int, arg2: Float, arg3: Float, arg4: Float, arg5: Float, arg6: Long)
    fun glBindTexture(arg0: Int, arg1: Int)
    fun glPrioritizeTextures(arg0: IntBuffer, arg1: FloatBuffer)
    fun glAreTexturesResident(arg0: IntBuffer, arg1: ByteBuffer): Boolean
    fun glBegin(arg0: Int)
    fun glEnd()
    fun glArrayElement(arg0: Int)
    fun glClearDepth(arg0: Double)
    fun glDeleteLists(arg0: Int, arg1: Int)
    fun glDeleteTextures(arg0: IntBuffer)
    fun glDeleteTextures(arg0: Int)
    fun glCullFace(arg0: Int)
    fun glCopyTexSubImage2D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Int, arg7: Int)
    fun glCopyTexSubImage1D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int)
    fun glCopyTexImage2D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Int, arg7: Int)
    fun glCopyTexImage1D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Int)
    fun glCopyPixels(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int)
    fun glColorPointer(arg0: Int, arg1: Int, arg2: Int, arg3: Long)
    fun glColorPointer(arg0: Int, arg1: Int, arg2: Int, arg3: ByteBuffer)
    fun glColorMaterial(arg0: Int, arg1: Int)
    fun glColorMask(arg0: Boolean, arg1: Boolean, arg2: Boolean, arg3: Boolean)
    fun glColor3b(arg0: Byte, arg1: Byte, arg2: Byte)
    fun glColor3f(arg0: Float, arg1: Float, arg2: Float)
    fun glColor3d(arg0: Double, arg1: Double, arg2: Double)
    fun glColor3ub(arg0: Byte, arg1: Byte, arg2: Byte)
    fun glColor4b(arg0: Byte, arg1: Byte, arg2: Byte, arg3: Byte)
    fun glColor4f(arg0: Float, arg1: Float, arg2: Float, arg3: Float)
    fun glColor4d(arg0: Double, arg1: Double, arg2: Double, arg3: Double)
    fun glColor4ub(arg0: Byte, arg1: Byte, arg2: Byte, arg3: Byte)
    fun glClipPlane(arg0: Int, arg1: DoubleBuffer)
    fun glClearStencil(arg0: Int)
    fun glEvalPoint1(arg0: Int)
    fun glEvalPoint2(arg0: Int, arg1: Int)
    fun glEvalMesh1(arg0: Int, arg1: Int, arg2: Int)
    fun glEvalMesh2(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int)
    fun glEvalCoord1f(arg0: Float)
    fun glEvalCoord1d(arg0: Double)
    fun glEvalCoord2f(arg0: Float, arg1: Float)
    fun glEvalCoord2d(arg0: Double, arg1: Double)
    fun glEnableClientState(arg0: Int)
    fun glDisableClientState(arg0: Int)
    fun glEnable(arg0: Int)
    fun glDisable(arg0: Int)
    fun glEdgeFlagPointer(arg0: Int, arg1: ByteBuffer)
    fun glEdgeFlagPointer(arg0: Int, arg1: Long)
    fun glEdgeFlag(arg0: Boolean)
    fun glDrawPixels(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: ByteBuffer)
    fun glDrawPixels(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: IntBuffer)
    fun glDrawPixels(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: ShortBuffer)
    fun glDrawPixels(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Long)
    fun glDrawElements(arg0: Int, arg1: ByteBuffer)
    fun glDrawElements(arg0: Int, arg1: IntBuffer)
    fun glDrawElements(arg0: Int, arg1: ShortBuffer)
    fun glDrawElements(arg0: Int, arg1: Int, arg2: Int, arg3: Long)
    fun glDrawBuffer(arg0: Int)
    fun glDrawArrays(arg0: Int, arg1: Int, arg2: Int)
    fun glDepthRange(arg0: Double, arg1: Double)
    fun glDepthMask(arg0: Boolean)
    fun glDepthFunc(arg0: Int)
    fun glFeedbackBuffer(arg0: Int, arg1: FloatBuffer)
    fun glGetPixelMapfv(arg0: Int, arg1: Long)
    fun glGetPixelMapuiv(arg0: Int, arg1: Long)
    fun glGetPixelMapusv(arg0: Int, arg1: Long)
    fun glGetError(): Int
    fun glGetClipPlane(arg0: Int, arg1: DoubleBuffer)
    fun glGetBoolean(arg0: Int): Boolean
    fun glGetDouble(arg0: Int): Double
    fun glGetFloat(arg0: Int): Float
    fun glGetInteger(arg0: Int): Int
    fun glGenTextures(arg0: IntBuffer)
    fun glGenTextures(): Int
    fun glGenLists(arg0: Int): Int
    fun glFrustum(arg0: Double, arg1: Double, arg2: Double, arg3: Double, arg4: Double, arg5: Double)
    fun glFrontFace(arg0: Int)
    fun glFogf(arg0: Int, arg1: Float)
    fun glFogi(arg0: Int, arg1: Int)
    fun glFlush()
    fun glFinish()
    fun glIsEnabled(arg0: Int): Boolean
    fun glInterleavedArrays(arg0: Int, arg1: Int, arg2: ByteBuffer)
    fun glInterleavedArrays(arg0: Int, arg1: Int, arg2: DoubleBuffer)
    fun glInterleavedArrays(arg0: Int, arg1: Int, arg2: FloatBuffer)
    fun glInterleavedArrays(arg0: Int, arg1: Int, arg2: IntBuffer)
    fun glInterleavedArrays(arg0: Int, arg1: Int, arg2: ShortBuffer)
    fun glInterleavedArrays(arg0: Int, arg1: Int, arg2: Long)
    fun glInitNames()
    fun glHint(arg0: Int, arg1: Int)
    fun glGetTexParameterf(arg0: Int, arg1: Int): Float
    fun glGetTexParameteri(arg0: Int, arg1: Int): Int
    fun glGetTexLevelParameterf(arg0: Int, arg1: Int, arg2: Int): Float
    fun glGetTexLevelParameteri(arg0: Int, arg1: Int, arg2: Int): Int
    fun glGetTexImage(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: ByteBuffer)
    fun glGetTexImage(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: DoubleBuffer)
    fun glGetTexImage(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: FloatBuffer)
    fun glGetTexImage(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: IntBuffer)
    fun glGetTexImage(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: ShortBuffer)
    fun glGetTexImage(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Long)
    fun glGetTexGeni(arg0: Int, arg1: Int): Int
    fun glGetTexGenf(arg0: Int, arg1: Int): Float
    fun glGetTexGend(arg0: Int, arg1: Int): Double
    fun glGetTexEnvi(arg0: Int, arg1: Int): Int
    fun glGetTexEnvf(arg0: Int, arg1: Int): Float
    fun glGetString(arg0: Int): String
    fun glGetPolygonStipple(arg0: ByteBuffer)
    fun glGetPolygonStipple(arg0: Long)
    fun glIsList(arg0: Int): Boolean
    fun glMaterialf(arg0: Int, arg1: Int, arg2: Float)
    fun glMateriali(arg0: Int, arg1: Int, arg2: Int)
    fun glMapGrid1f(arg0: Int, arg1: Float, arg2: Float)
    fun glMapGrid1d(arg0: Int, arg1: Double, arg2: Double)
    fun glMapGrid2f(arg0: Int, arg1: Float, arg2: Float, arg3: Int, arg4: Float, arg5: Float)
    fun glMapGrid2d(arg0: Int, arg1: Double, arg2: Double, arg3: Int, arg4: Double, arg5: Double)
    fun glMap2f(
        arg0: Int,
        arg1: Float,
        arg2: Float,
        arg3: Int,
        arg4: Int,
        arg5: Float,
        arg6: Float,
        arg7: Int,
        arg8: Int,
        arg9: FloatBuffer
    )

    fun glMap2d(
        arg0: Int,
        arg1: Double,
        arg2: Double,
        arg3: Int,
        arg4: Int,
        arg5: Double,
        arg6: Double,
        arg7: Int,
        arg8: Int,
        arg9: DoubleBuffer
    )

    fun glMap1f(arg0: Int, arg1: Float, arg2: Float, arg3: Int, arg4: Int, arg5: FloatBuffer)
    fun glMap1d(arg0: Int, arg1: Double, arg2: Double, arg3: Int, arg4: Int, arg5: DoubleBuffer)
    fun glLogicOp(arg0: Int)
    fun glLoadName(arg0: Int)
    fun glLoadIdentity()
    fun glListBase(arg0: Int)
    fun glLineWidth(arg0: Float)
    fun glLineStipple(arg0: Int, arg1: Short)
    fun glLightModelf(arg0: Int, arg1: Float)
    fun glLightModeli(arg0: Int, arg1: Int)
    fun glLightf(arg0: Int, arg1: Int, arg2: Float)
    fun glLighti(arg0: Int, arg1: Int, arg2: Int)
    fun glIsTexture(arg0: Int): Boolean
    fun glMatrixMode(arg0: Int)
    fun glPolygonStipple(arg0: ByteBuffer)
    fun glPolygonStipple(arg0: Long)
    fun glPolygonOffset(arg0: Float, arg1: Float)
    fun glPolygonMode(arg0: Int, arg1: Int)
    fun glPointSize(arg0: Float)
    fun glPixelZoom(arg0: Float, arg1: Float)
    fun glPixelTransferf(arg0: Int, arg1: Float)
    fun glPixelTransferi(arg0: Int, arg1: Int)
    fun glPixelStoref(arg0: Int, arg1: Float)
    fun glPixelStorei(arg0: Int, arg1: Int)
    fun glPixelMapfv(arg0: Int, arg1: Int, arg2: Long)
    fun glPixelMapuiv(arg0: Int, arg1: Int, arg2: Long)
    fun glPixelMapusv(arg0: Int, arg1: Int, arg2: Long)
    fun glPassThrough(arg0: Float)
    fun glOrtho(arg0: Double, arg1: Double, arg2: Double, arg3: Double, arg4: Double, arg5: Double)
    fun glNormalPointer(arg0: Int, arg1: Int, arg2: Long)
    fun glNormalPointer(arg0: Int, arg1: Int, arg2: ByteBuffer)
    fun glNormal3b(arg0: Byte, arg1: Byte, arg2: Byte)
    fun glNormal3f(arg0: Float, arg1: Float, arg2: Float)
    fun glNormal3d(arg0: Double, arg1: Double, arg2: Double)
    fun glNormal3i(arg0: Int, arg1: Int, arg2: Int)
    fun glNewList(arg0: Int, arg1: Int)
    fun glEndList()
    fun glShadeModel(arg0: Int)
    fun glSelectBuffer(arg0: IntBuffer)
    fun glScissor(arg0: Int, arg1: Int, arg2: Int, arg3: Int)
    fun glScalef(arg0: Float, arg1: Float, arg2: Float)
    fun glScaled(arg0: Double, arg1: Double, arg2: Double)
    fun glRotatef(arg0: Float, arg1: Float, arg2: Float, arg3: Float)
    fun glRotated(arg0: Double, arg1: Double, arg2: Double, arg3: Double)
    fun glRenderMode(arg0: Int): Int
    fun glRectf(arg0: Float, arg1: Float, arg2: Float, arg3: Float)
    fun glRectd(arg0: Double, arg1: Double, arg2: Double, arg3: Double)
    fun glRecti(arg0: Int, arg1: Int, arg2: Int, arg3: Int)
    fun glReadPixels(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: ByteBuffer)
    fun glReadPixels(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: FloatBuffer)
    fun glReadPixels(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: IntBuffer)
    fun glReadPixels(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: ShortBuffer)
    fun glReadPixels(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Long)
    fun glReadBuffer(arg0: Int)
    fun glRasterPos2f(arg0: Float, arg1: Float)
    fun glRasterPos2d(arg0: Double, arg1: Double)
    fun glRasterPos2i(arg0: Int, arg1: Int)
    fun glRasterPos3f(arg0: Float, arg1: Float, arg2: Float)
    fun glRasterPos3d(arg0: Double, arg1: Double, arg2: Double)
    fun glRasterPos3i(arg0: Int, arg1: Int, arg2: Int)
    fun glRasterPos4f(arg0: Float, arg1: Float, arg2: Float, arg3: Float)
    fun glRasterPos4d(arg0: Double, arg1: Double, arg2: Double, arg3: Double)
    fun glRasterPos4i(arg0: Int, arg1: Int, arg2: Int, arg3: Int)
    fun glPushName(arg0: Int)
    fun glPopName()
    fun glPushMatrix()
    fun glPopMatrix()
    fun glPushClientAttrib(arg0: Int)
    fun glPopClientAttrib()
    fun glPushAttrib(arg0: Int)
    fun glPopAttrib()
    fun glStencilFunc(arg0: Int, arg1: Int, arg2: Int)
    fun glVertexPointer(arg0: Int, arg1: Int, arg2: Int, arg3: Long)
    fun glVertexPointer(arg0: Int, arg1: Int, arg2: Int, arg3: ByteBuffer)
    fun glVertex2f(arg0: Float, arg1: Float)
    fun glVertex2d(arg0: Double, arg1: Double)
    fun glVertex2i(arg0: Int, arg1: Int)
    fun glVertex3f(arg0: Float, arg1: Float, arg2: Float)
    fun glVertex3d(arg0: Double, arg1: Double, arg2: Double)
    fun glVertex3i(arg0: Int, arg1: Int, arg2: Int)
    fun glVertex4f(arg0: Float, arg1: Float, arg2: Float, arg3: Float)
    fun glVertex4d(arg0: Double, arg1: Double, arg2: Double, arg3: Double)
    fun glVertex4i(arg0: Int, arg1: Int, arg2: Int, arg3: Int)
    fun glTranslatef(arg0: Float, arg1: Float, arg2: Float)
    fun glTranslated(arg0: Double, arg1: Double, arg2: Double)
    fun glTexImage1D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Int, arg7: ByteBuffer)
    fun glTexImage1D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Int, arg7: DoubleBuffer)
    fun glTexImage1D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Int, arg7: FloatBuffer)
    fun glTexImage1D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Int, arg7: IntBuffer)
    fun glTexImage1D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Int, arg7: ShortBuffer)
    fun glTexImage1D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Int, arg7: Long)
    fun glTexImage2D(
        arg0: Int,
        arg1: Int,
        arg2: Int,
        arg3: Int,
        arg4: Int,
        arg5: Int,
        arg6: Int,
        arg7: Int,
        arg8: ByteBuffer
    )

    fun glTexImage2D(
        arg0: Int,
        arg1: Int,
        arg2: Int,
        arg3: Int,
        arg4: Int,
        arg5: Int,
        arg6: Int,
        arg7: Int,
        arg8: DoubleBuffer
    )

    fun glTexImage2D(
        arg0: Int,
        arg1: Int,
        arg2: Int,
        arg3: Int,
        arg4: Int,
        arg5: Int,
        arg6: Int,
        arg7: Int,
        arg8: FloatBuffer
    )

    fun glTexImage2D(
        arg0: Int,
        arg1: Int,
        arg2: Int,
        arg3: Int,
        arg4: Int,
        arg5: Int,
        arg6: Int,
        arg7: Int,
        arg8: IntBuffer
    )

    fun glTexImage2D(
        arg0: Int,
        arg1: Int,
        arg2: Int,
        arg3: Int,
        arg4: Int,
        arg5: Int,
        arg6: Int,
        arg7: Int,
        arg8: ShortBuffer
    )

    fun glTexImage2D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Int, arg7: Int, arg8: Long)
    fun glTexSubImage1D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: ByteBuffer)
    fun glTexSubImage1D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: DoubleBuffer)
    fun glTexSubImage1D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: FloatBuffer)
    fun glTexSubImage1D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: IntBuffer)
    fun glTexSubImage1D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: ShortBuffer)
    fun glTexSubImage1D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Long)
    fun glTexSubImage2D(
        arg0: Int,
        arg1: Int,
        arg2: Int,
        arg3: Int,
        arg4: Int,
        arg5: Int,
        arg6: Int,
        arg7: Int,
        arg8: ByteBuffer
    )

    fun glTexSubImage2D(
        arg0: Int,
        arg1: Int,
        arg2: Int,
        arg3: Int,
        arg4: Int,
        arg5: Int,
        arg6: Int,
        arg7: Int,
        arg8: DoubleBuffer
    )

    fun glTexSubImage2D(
        arg0: Int,
        arg1: Int,
        arg2: Int,
        arg3: Int,
        arg4: Int,
        arg5: Int,
        arg6: Int,
        arg7: Int,
        arg8: FloatBuffer
    )

    fun glTexSubImage2D(
        arg0: Int,
        arg1: Int,
        arg2: Int,
        arg3: Int,
        arg4: Int,
        arg5: Int,
        arg6: Int,
        arg7: Int,
        arg8: IntBuffer
    )

    fun glTexSubImage2D(
        arg0: Int,
        arg1: Int,
        arg2: Int,
        arg3: Int,
        arg4: Int,
        arg5: Int,
        arg6: Int,
        arg7: Int,
        arg8: ShortBuffer
    )

    fun glTexSubImage2D(
        arg0: Int,
        arg1: Int,
        arg2: Int,
        arg3: Int,
        arg4: Int,
        arg5: Int,
        arg6: Int,
        arg7: Int,
        arg8: Long
    )

    fun glTexParameterf(arg0: Int, arg1: Int, arg2: Float)
    fun glTexParameteri(arg0: Int, arg1: Int, arg2: Int)
    fun glTexGenf(arg0: Int, arg1: Int, arg2: Float)
    fun glTexGend(arg0: Int, arg1: Int, arg2: Double)
    fun glTexGeni(arg0: Int, arg1: Int, arg2: Int)
    fun glTexEnvf(arg0: Int, arg1: Int, arg2: Float)
    fun glTexEnvi(arg0: Int, arg1: Int, arg2: Int)
    fun glTexCoordPointer(arg0: Int, arg1: Int, arg2: Int, arg3: Long)
    fun glTexCoordPointer(arg0: Int, arg1: Int, arg2: Int, arg3: ByteBuffer)
    fun glTexCoord1f(arg0: Float)
    fun glTexCoord1d(arg0: Double)
    fun glTexCoord2f(arg0: Float, arg1: Float)
    fun glTexCoord2d(arg0: Double, arg1: Double)
    fun glTexCoord3f(arg0: Float, arg1: Float, arg2: Float)
    fun glTexCoord3d(arg0: Double, arg1: Double, arg2: Double)
    fun glTexCoord4f(arg0: Float, arg1: Float, arg2: Float, arg3: Float)
    fun glTexCoord4d(arg0: Double, arg1: Double, arg2: Double, arg3: Double)
    fun glStencilOp(arg0: Int, arg1: Int, arg2: Int)
    fun glStencilMask(arg0: Int)
    fun glViewport(arg0: Int, arg1: Int, arg2: Int, arg3: Int)

    companion object : GL11 by gl11Access
}

val gl13Access by globalAccessorRegistry.glAccessor<GL13>(13)

interface GL13 {
    fun glActiveTexture(arg0: Int)
    fun glClientActiveTexture(arg0: Int)
    fun glCompressedTexImage1D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: ByteBuffer)
    fun glCompressedTexImage1D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Long)
    fun glCompressedTexImage2D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: ByteBuffer)
    fun glCompressedTexImage2D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Int, arg7: Long)
    fun glCompressedTexImage3D(
        arg0: Int,
        arg1: Int,
        arg2: Int,
        arg3: Int,
        arg4: Int,
        arg5: Int,
        arg6: Int,
        arg7: ByteBuffer
    )

    fun glCompressedTexImage3D(
        arg0: Int,
        arg1: Int,
        arg2: Int,
        arg3: Int,
        arg4: Int,
        arg5: Int,
        arg6: Int,
        arg7: Int,
        arg8: Long
    )

    fun glCompressedTexSubImage1D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: ByteBuffer)
    fun glCompressedTexSubImage1D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Long)
    fun glCompressedTexSubImage2D(
        arg0: Int,
        arg1: Int,
        arg2: Int,
        arg3: Int,
        arg4: Int,
        arg5: Int,
        arg6: Int,
        arg7: ByteBuffer
    )

    fun glCompressedTexSubImage2D(
        arg0: Int,
        arg1: Int,
        arg2: Int,
        arg3: Int,
        arg4: Int,
        arg5: Int,
        arg6: Int,
        arg7: Int,
        arg8: Long
    )

    fun glCompressedTexSubImage3D(
        arg0: Int,
        arg1: Int,
        arg2: Int,
        arg3: Int,
        arg4: Int,
        arg5: Int,
        arg6: Int,
        arg7: Int,
        arg8: Int,
        arg9: ByteBuffer
    )

    fun glCompressedTexSubImage3D(
        arg0: Int,
        arg1: Int,
        arg2: Int,
        arg3: Int,
        arg4: Int,
        arg5: Int,
        arg6: Int,
        arg7: Int,
        arg8: Int,
        arg9: Int,
        arg10: Long
    )

    fun glGetCompressedTexImage(arg0: Int, arg1: Int, arg2: ByteBuffer)
    fun glGetCompressedTexImage(arg0: Int, arg1: Int, arg2: Long)
    fun glMultiTexCoord1f(arg0: Int, arg1: Float)
    fun glMultiTexCoord1d(arg0: Int, arg1: Double)
    fun glMultiTexCoord2f(arg0: Int, arg1: Float, arg2: Float)
    fun glMultiTexCoord2d(arg0: Int, arg1: Double, arg2: Double)
    fun glMultiTexCoord3f(arg0: Int, arg1: Float, arg2: Float, arg3: Float)
    fun glMultiTexCoord3d(arg0: Int, arg1: Double, arg2: Double, arg3: Double)
    fun glMultiTexCoord4f(arg0: Int, arg1: Float, arg2: Float, arg3: Float, arg4: Float)
    fun glMultiTexCoord4d(arg0: Int, arg1: Double, arg2: Double, arg3: Double, arg4: Double)
    fun glSampleCoverage(arg0: Float, arg1: Boolean)

    companion object : GL13 by gl13Access
}

val gl14Access by globalAccessorRegistry.glAccessor<GL14>(14)

interface GL14 {
    fun glBlendEquation(arg0: Int)
    fun glBlendColor(arg0: Float, arg1: Float, arg2: Float, arg3: Float)
    fun glFogCoordf(arg0: Float)
    fun glFogCoordd(arg0: Double)
    fun glFogCoordPointer(arg0: Int, arg1: Int, arg2: Long)
    fun glMultiDrawArrays(arg0: Int, arg1: IntBuffer, arg2: IntBuffer)
    fun glPointParameteri(arg0: Int, arg1: Int)
    fun glPointParameterf(arg0: Int, arg1: Float)
    fun glSecondaryColor3b(arg0: Byte, arg1: Byte, arg2: Byte)
    fun glSecondaryColor3f(arg0: Float, arg1: Float, arg2: Float)
    fun glSecondaryColor3d(arg0: Double, arg1: Double, arg2: Double)
    fun glSecondaryColor3ub(arg0: Byte, arg1: Byte, arg2: Byte)
    fun glSecondaryColorPointer(arg0: Int, arg1: Int, arg2: Int, arg3: Long)
    fun glBlendFuncSeparate(arg0: Int, arg1: Int, arg2: Int, arg3: Int)
    fun glWindowPos2f(arg0: Float, arg1: Float)
    fun glWindowPos2d(arg0: Double, arg1: Double)
    fun glWindowPos2i(arg0: Int, arg1: Int)
    fun glWindowPos3f(arg0: Float, arg1: Float, arg2: Float)
    fun glWindowPos3d(arg0: Double, arg1: Double, arg2: Double)
    fun glWindowPos3i(arg0: Int, arg1: Int, arg2: Int)

    companion object : GL14 by gl14Access
}

val gl15Access by globalAccessorRegistry.glAccessor<GL15>(15)

interface GL15 {
    fun glBindBuffer(arg0: Int, arg1: Int)
    fun glDeleteBuffers(arg0: IntBuffer)
    fun glDeleteBuffers(arg0: Int)
    fun glGenBuffers(arg0: IntBuffer)
    fun glGenBuffers(): Int
    fun glIsBuffer(arg0: Int): Boolean
    fun glBufferData(arg0: Int, arg1: Long, arg2: Int)
    fun glBufferData(arg0: Int, arg1: ByteBuffer, arg2: Int)
    fun glBufferData(arg0: Int, arg1: DoubleBuffer, arg2: Int)
    fun glBufferData(arg0: Int, arg1: FloatBuffer, arg2: Int)
    fun glBufferData(arg0: Int, arg1: IntBuffer, arg2: Int)
    fun glBufferData(arg0: Int, arg1: ShortBuffer, arg2: Int)
    fun glBufferSubData(arg0: Int, arg1: Long, arg2: ByteBuffer)
    fun glBufferSubData(arg0: Int, arg1: Long, arg2: DoubleBuffer)
    fun glBufferSubData(arg0: Int, arg1: Long, arg2: FloatBuffer)
    fun glBufferSubData(arg0: Int, arg1: Long, arg2: IntBuffer)
    fun glBufferSubData(arg0: Int, arg1: Long, arg2: ShortBuffer)
    fun glGetBufferSubData(arg0: Int, arg1: Long, arg2: ByteBuffer)
    fun glGetBufferSubData(arg0: Int, arg1: Long, arg2: DoubleBuffer)
    fun glGetBufferSubData(arg0: Int, arg1: Long, arg2: FloatBuffer)
    fun glGetBufferSubData(arg0: Int, arg1: Long, arg2: IntBuffer)
    fun glGetBufferSubData(arg0: Int, arg1: Long, arg2: ShortBuffer)
    fun glMapBuffer(arg0: Int, arg1: Int, arg2: ByteBuffer): ByteBuffer
    fun glMapBuffer(arg0: Int, arg1: Int, arg2: Long, arg3: ByteBuffer): ByteBuffer
    fun glUnmapBuffer(arg0: Int): Boolean
    fun glGetBufferParameteri(arg0: Int, arg1: Int): Int
    fun glGenQueries(arg0: IntBuffer)
    fun glGenQueries(): Int
    fun glDeleteQueries(arg0: IntBuffer)
    fun glDeleteQueries(arg0: Int)
    fun glIsQuery(arg0: Int): Boolean
    fun glBeginQuery(arg0: Int, arg1: Int)
    fun glEndQuery(arg0: Int)
    fun glGetQueryi(arg0: Int, arg1: Int): Int
    fun glGetQueryObjecti(arg0: Int, arg1: Int): Int
    fun glGetQueryObjectui(arg0: Int, arg1: Int): Int

    companion object : GL15 by gl15Access
}

val gl20Access by globalAccessorRegistry.glAccessor<GL20>(20)

interface GL20 {
    fun glShaderSource(arg0: Int, arg1: CharSequence)
    fun glShaderSource(arg0: Int, arg1: Array<CharSequence>)
    fun glCreateShader(arg0: Int): Int
    fun glIsShader(arg0: Int): Boolean
    fun glCompileShader(arg0: Int)
    fun glDeleteShader(arg0: Int)
    fun glCreateProgram(): Int
    fun glIsProgram(arg0: Int): Boolean
    fun glAttachShader(arg0: Int, arg1: Int)
    fun glDetachShader(arg0: Int, arg1: Int)
    fun glLinkProgram(arg0: Int)
    fun glUseProgram(arg0: Int)
    fun glValidateProgram(arg0: Int)
    fun glDeleteProgram(arg0: Int)
    fun glUniform1f(arg0: Int, arg1: Float)
    fun glUniform2f(arg0: Int, arg1: Float, arg2: Float)
    fun glUniform3f(arg0: Int, arg1: Float, arg2: Float, arg3: Float)
    fun glUniform4f(arg0: Int, arg1: Float, arg2: Float, arg3: Float, arg4: Float)
    fun glUniform1i(arg0: Int, arg1: Int)
    fun glUniform2i(arg0: Int, arg1: Int, arg2: Int)
    fun glUniform3i(arg0: Int, arg1: Int, arg2: Int, arg3: Int)
    fun glUniform4i(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int)
    fun glGetShaderi(arg0: Int, arg1: Int): Int
    fun glGetProgrami(arg0: Int, arg1: Int): Int
    fun glGetShaderInfoLog(arg0: Int, arg1: IntBuffer, arg2: ByteBuffer)
    fun glGetShaderInfoLog(arg0: Int, arg1: Int): String
    fun glGetProgramInfoLog(arg0: Int, arg1: IntBuffer, arg2: ByteBuffer)
    fun glGetProgramInfoLog(arg0: Int, arg1: Int): String
    fun glGetAttachedShaders(arg0: Int, arg1: IntBuffer, arg2: IntBuffer)
    fun glGetUniformLocation(arg0: Int, arg1: ByteBuffer): Int
    fun glGetUniformLocation(arg0: Int, arg1: CharSequence): Int
    fun glGetActiveUniform(arg0: Int, arg1: Int, arg2: IntBuffer, arg3: IntBuffer, arg4: IntBuffer, arg5: ByteBuffer)
    fun glGetShaderSource(arg0: Int, arg1: IntBuffer, arg2: ByteBuffer)
    fun glGetShaderSource(arg0: Int, arg1: Int): String
    fun glVertexAttrib1s(arg0: Int, arg1: Short)
    fun glVertexAttrib1f(arg0: Int, arg1: Float)
    fun glVertexAttrib1d(arg0: Int, arg1: Double)
    fun glVertexAttrib2s(arg0: Int, arg1: Short, arg2: Short)
    fun glVertexAttrib2f(arg0: Int, arg1: Float, arg2: Float)
    fun glVertexAttrib2d(arg0: Int, arg1: Double, arg2: Double)
    fun glVertexAttrib3s(arg0: Int, arg1: Short, arg2: Short, arg3: Short)
    fun glVertexAttrib3f(arg0: Int, arg1: Float, arg2: Float, arg3: Float)
    fun glVertexAttrib3d(arg0: Int, arg1: Double, arg2: Double, arg3: Double)
    fun glVertexAttrib4s(arg0: Int, arg1: Short, arg2: Short, arg3: Short, arg4: Short)
    fun glVertexAttrib4f(arg0: Int, arg1: Float, arg2: Float, arg3: Float, arg4: Float)
    fun glVertexAttrib4d(arg0: Int, arg1: Double, arg2: Double, arg3: Double, arg4: Double)
    fun glVertexAttrib4Nub(arg0: Int, arg1: Byte, arg2: Byte, arg3: Byte, arg4: Byte)
    fun glVertexAttribPointer(arg0: Int, arg1: Int, arg2: Int, arg3: Boolean, arg4: Int, arg5: Long)
    fun glVertexAttribPointer(arg0: Int, arg1: Int, arg2: Int, arg3: Boolean, arg4: Int, arg5: ByteBuffer)
    fun glEnableVertexAttribArray(arg0: Int)
    fun glDisableVertexAttribArray(arg0: Int)
    fun glBindAttribLocation(arg0: Int, arg1: Int, arg2: ByteBuffer)
    fun glBindAttribLocation(arg0: Int, arg1: Int, arg2: CharSequence)
    fun glGetActiveAttrib(arg0: Int, arg1: Int, arg2: IntBuffer, arg3: IntBuffer, arg4: IntBuffer, arg5: ByteBuffer)
    fun glGetAttribLocation(arg0: Int, arg1: ByteBuffer): Int
    fun glGetAttribLocation(arg0: Int, arg1: CharSequence): Int
    fun glDrawBuffers(arg0: IntBuffer)
    fun glDrawBuffers(arg0: Int)
    fun glStencilOpSeparate(arg0: Int, arg1: Int, arg2: Int, arg3: Int)
    fun glStencilFuncSeparate(arg0: Int, arg1: Int, arg2: Int, arg3: Int)
    fun glStencilMaskSeparate(arg0: Int, arg1: Int)
    fun glBlendEquationSeparate(arg0: Int, arg1: Int)

    companion object : GL20 by gl20Access
}

val gl30Access by globalAccessorRegistry.glAccessor<GL30>(30)

interface GL30 {
    fun glGetStringi(arg0: Int, arg1: Int): String
    fun glClearBufferfi(arg0: Int, arg1: Int, arg2: Float, arg3: Int)
    fun glVertexAttribI1i(arg0: Int, arg1: Int)
    fun glVertexAttribI2i(arg0: Int, arg1: Int, arg2: Int)
    fun glVertexAttribI3i(arg0: Int, arg1: Int, arg2: Int, arg3: Int)
    fun glVertexAttribI4i(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int)
    fun glVertexAttribI1ui(arg0: Int, arg1: Int)
    fun glVertexAttribI2ui(arg0: Int, arg1: Int, arg2: Int)
    fun glVertexAttribI3ui(arg0: Int, arg1: Int, arg2: Int, arg3: Int)
    fun glVertexAttribI4ui(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int)
    fun glVertexAttribIPointer(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: ByteBuffer)
    fun glVertexAttribIPointer(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: IntBuffer)
    fun glVertexAttribIPointer(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: ShortBuffer)
    fun glVertexAttribIPointer(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Long)
    fun glUniform1ui(arg0: Int, arg1: Int)
    fun glUniform2ui(arg0: Int, arg1: Int, arg2: Int)
    fun glUniform3ui(arg0: Int, arg1: Int, arg2: Int, arg3: Int)
    fun glUniform4ui(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int)
    fun glBindFragDataLocation(arg0: Int, arg1: Int, arg2: ByteBuffer)
    fun glBindFragDataLocation(arg0: Int, arg1: Int, arg2: CharSequence)
    fun glGetFragDataLocation(arg0: Int, arg1: ByteBuffer): Int
    fun glGetFragDataLocation(arg0: Int, arg1: CharSequence): Int
    fun glBeginConditionalRender(arg0: Int, arg1: Int)
    fun glEndConditionalRender()
    fun glMapBufferRange(arg0: Int, arg1: Long, arg2: Long, arg3: Int, arg4: ByteBuffer): ByteBuffer
    fun glFlushMappedBufferRange(arg0: Int, arg1: Long, arg2: Long)
    fun glClampColor(arg0: Int, arg1: Int)
    fun glIsRenderbuffer(arg0: Int): Boolean
    fun glBindRenderbuffer(arg0: Int, arg1: Int)
    fun glDeleteRenderbuffers(arg0: IntBuffer)
    fun glDeleteRenderbuffers(arg0: Int)
    fun glGenRenderbuffers(arg0: IntBuffer)
    fun glGenRenderbuffers(): Int
    fun glRenderbufferStorage(arg0: Int, arg1: Int, arg2: Int, arg3: Int)
    fun glGetRenderbufferParameteri(arg0: Int, arg1: Int): Int
    fun glIsFramebuffer(arg0: Int): Boolean
    fun glBindFramebuffer(arg0: Int, arg1: Int)
    fun glDeleteFramebuffers(arg0: IntBuffer)
    fun glDeleteFramebuffers(arg0: Int)
    fun glGenFramebuffers(arg0: IntBuffer)
    fun glGenFramebuffers(): Int
    fun glCheckFramebufferStatus(arg0: Int): Int
    fun glFramebufferTexture1D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int)
    fun glFramebufferTexture2D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int)
    fun glFramebufferTexture3D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int)
    fun glFramebufferRenderbuffer(arg0: Int, arg1: Int, arg2: Int, arg3: Int)
    fun glGetFramebufferAttachmentParameteri(arg0: Int, arg1: Int, arg2: Int): Int
    fun glGenerateMipmap(arg0: Int)
    fun glRenderbufferStorageMultisample(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int)
    fun glBlitFramebuffer(
        arg0: Int,
        arg1: Int,
        arg2: Int,
        arg3: Int,
        arg4: Int,
        arg5: Int,
        arg6: Int,
        arg7: Int,
        arg8: Int,
        arg9: Int
    )

    fun glTexParameterIi(arg0: Int, arg1: Int, arg2: Int)
    fun glTexParameterIui(arg0: Int, arg1: Int, arg2: Int)
    fun glGetTexParameterIi(arg0: Int, arg1: Int): Int
    fun glGetTexParameterIui(arg0: Int, arg1: Int): Int
    fun glFramebufferTextureLayer(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int)
    fun glColorMaski(arg0: Int, arg1: Boolean, arg2: Boolean, arg3: Boolean, arg4: Boolean)
    fun glEnablei(arg0: Int, arg1: Int)
    fun glDisablei(arg0: Int, arg1: Int)
    fun glIsEnabledi(arg0: Int, arg1: Int): Boolean
    fun glBindBufferRange(arg0: Int, arg1: Int, arg2: Int, arg3: Long, arg4: Long)
    fun glBindBufferBase(arg0: Int, arg1: Int, arg2: Int)
    fun glBeginTransformFeedback(arg0: Int)
    fun glEndTransformFeedback()
    fun glTransformFeedbackVaryings(arg0: Int, arg1: Array<CharSequence>, arg2: Int)
    fun glGetTransformFeedbackVarying(
        arg0: Int,
        arg1: Int,
        arg2: IntBuffer,
        arg3: IntBuffer,
        arg4: IntBuffer,
        arg5: ByteBuffer
    )

    fun glGetTransformFeedbackVarying(arg0: Int, arg1: Int, arg2: Int, arg3: IntBuffer, arg4: IntBuffer): String
    fun glBindVertexArray(arg0: Int)
    fun glDeleteVertexArrays(arg0: IntBuffer)
    fun glDeleteVertexArrays(arg0: Int)
    fun glGenVertexArrays(arg0: IntBuffer)
    fun glGenVertexArrays(): Int
    fun glIsVertexArray(arg0: Int): Boolean

    companion object : GL30 by gl30Access
}

object GLCompat : GL11 by gl11Access, GL13 by gl13Access, GL14 by gl14Access,
    GL15 by gl15Access, GL20 by gl20Access, GL30 by gl30Access

// merged into one class for convenience

@Suppress("unused")
object GLConstants {
    const val GL_DEPTH_BUFFER_BIT = 0x00000100
    const val GL_STENCIL_BUFFER_BIT = 0x00000400
    const val GL_COLOR_BUFFER_BIT = 0x00004000
    const val GL_FALSE = 0
    const val GL_TRUE = 1
    const val GL_POINTS = 0x0000
    const val GL_LINES = 0x0001
    const val GL_LINE_LOOP = 0x0002
    const val GL_LINE_STRIP = 0x0003
    const val GL_TRIANGLES = 0x0004
    const val GL_TRIANGLE_STRIP = 0x0005
    const val GL_TRIANGLE_FAN = 0x0006
    const val GL_NEVER = 0x0200
    const val GL_LESS = 0x0201
    const val GL_EQUAL = 0x0202
    const val GL_LEQUAL = 0x0203
    const val GL_GREATER = 0x0204
    const val GL_NOTEQUAL = 0x0205
    const val GL_GEQUAL = 0x0206
    const val GL_ALWAYS = 0x0207
    const val GL_ZERO = 0
    const val GL_ONE = 1
    const val GL_SRC_COLOR = 0x0300
    const val GL_ONE_MINUS_SRC_COLOR = 0x0301
    const val GL_SRC_ALPHA = 0x0302
    const val GL_ONE_MINUS_SRC_ALPHA = 0x0303
    const val GL_DST_ALPHA = 0x0304
    const val GL_ONE_MINUS_DST_ALPHA = 0x0305
    const val GL_DST_COLOR = 0x0306
    const val GL_ONE_MINUS_DST_COLOR = 0x0307
    const val GL_SRC_ALPHA_SATURATE = 0x0308
    const val GL_NONE = 0
    const val GL_FRONT_LEFT = 0x0400
    const val GL_FRONT_RIGHT = 0x0401
    const val GL_BACK_LEFT = 0x0402
    const val GL_BACK_RIGHT = 0x0403
    const val GL_FRONT = 0x0404
    const val GL_BACK = 0x0405
    const val GL_LEFT = 0x0406
    const val GL_RIGHT = 0x0407
    const val GL_FRONT_AND_BACK = 0x0408
    const val GL_NO_ERROR = 0
    const val GL_INVALID_ENUM = 0x0500
    const val GL_INVALID_VALUE = 0x0501
    const val GL_INVALID_OPERATION = 0x0502
    const val GL_OUT_OF_MEMORY = 0x0505
    const val GL_CW = 0x0900
    const val GL_CCW = 0x0901
    const val GL_POINT_SIZE = 0x0B11
    const val GL_POINT_SIZE_RANGE = 0x0B12
    const val GL_POINT_SIZE_GRANULARITY = 0x0B13
    const val GL_LINE_SMOOTH = 0x0B20
    const val GL_LINE_WIDTH = 0x0B21
    const val GL_LINE_WIDTH_RANGE = 0x0B22
    const val GL_LINE_WIDTH_GRANULARITY = 0x0B23
    const val GL_POLYGON_MODE = 0x0B40
    const val GL_POLYGON_SMOOTH = 0x0B41
    const val GL_CULL_FACE = 0x0B44
    const val GL_CULL_FACE_MODE = 0x0B45
    const val GL_FRONT_FACE = 0x0B46
    const val GL_DEPTH_RANGE = 0x0B70
    const val GL_DEPTH_TEST = 0x0B71
    const val GL_DEPTH_WRITEMASK = 0x0B72
    const val GL_DEPTH_CLEAR_VALUE = 0x0B73
    const val GL_DEPTH_FUNC = 0x0B74
    const val GL_STENCIL_TEST = 0x0B90
    const val GL_STENCIL_CLEAR_VALUE = 0x0B91
    const val GL_STENCIL_FUNC = 0x0B92
    const val GL_STENCIL_VALUE_MASK = 0x0B93
    const val GL_STENCIL_FAIL = 0x0B94
    const val GL_STENCIL_PASS_DEPTH_FAIL = 0x0B95
    const val GL_STENCIL_PASS_DEPTH_PASS = 0x0B96
    const val GL_STENCIL_REF = 0x0B97
    const val GL_STENCIL_WRITEMASK = 0x0B98
    const val GL_VIEWPORT = 0x0BA2
    const val GL_DITHER = 0x0BD0
    const val GL_BLEND_DST = 0x0BE0
    const val GL_BLEND_SRC = 0x0BE1
    const val GL_BLEND = 0x0BE2
    const val GL_LOGIC_OP_MODE = 0x0BF0
    const val GL_DRAW_BUFFER = 0x0C01
    const val GL_READ_BUFFER = 0x0C02
    const val GL_SCISSOR_BOX = 0x0C10
    const val GL_SCISSOR_TEST = 0x0C11
    const val GL_COLOR_CLEAR_VALUE = 0x0C22
    const val GL_COLOR_WRITEMASK = 0x0C23
    const val GL_DOUBLEBUFFER = 0x0C32
    const val GL_STEREO = 0x0C33
    const val GL_LINE_SMOOTH_HINT = 0x0C52
    const val GL_POLYGON_SMOOTH_HINT = 0x0C53
    const val GL_UNPACK_SWAP_BYTES = 0x0CF0
    const val GL_UNPACK_LSB_FIRST = 0x0CF1
    const val GL_UNPACK_ROW_LENGTH = 0x0CF2
    const val GL_UNPACK_SKIP_ROWS = 0x0CF3
    const val GL_UNPACK_SKIP_PIXELS = 0x0CF4
    const val GL_UNPACK_ALIGNMENT = 0x0CF5
    const val GL_PACK_SWAP_BYTES = 0x0D00
    const val GL_PACK_LSB_FIRST = 0x0D01
    const val GL_PACK_ROW_LENGTH = 0x0D02
    const val GL_PACK_SKIP_ROWS = 0x0D03
    const val GL_PACK_SKIP_PIXELS = 0x0D04
    const val GL_PACK_ALIGNMENT = 0x0D05
    const val GL_MAX_TEXTURE_SIZE = 0x0D33
    const val GL_MAX_VIEWPORT_DIMS = 0x0D3A
    const val GL_SUBPIXEL_BITS = 0x0D50
    const val GL_TEXTURE_1D = 0x0DE0
    const val GL_TEXTURE_2D = 0x0DE1
    const val GL_TEXTURE_WIDTH = 0x1000
    const val GL_TEXTURE_HEIGHT = 0x1001
    const val GL_TEXTURE_BORDER_COLOR = 0x1004
    const val GL_DONT_CARE = 0x1100
    const val GL_FASTEST = 0x1101
    const val GL_NICEST = 0x1102
    const val GL_BYTE = 0x1400
    const val GL_UNSIGNED_BYTE = 0x1401
    const val GL_SHORT = 0x1402
    const val GL_UNSIGNED_SHORT = 0x1403
    const val GL_INT = 0x1404
    const val GL_UNSIGNED_INT = 0x1405
    const val GL_FLOAT = 0x1406
    const val GL_CLEAR = 0x1500
    const val GL_AND = 0x1501
    const val GL_AND_REVERSE = 0x1502
    const val GL_COPY = 0x1503
    const val GL_AND_INVERTED = 0x1504
    const val GL_NOOP = 0x1505
    const val GL_XOR = 0x1506
    const val GL_OR = 0x1507
    const val GL_NOR = 0x1508
    const val GL_EQUIV = 0x1509
    const val GL_INVERT = 0x150A
    const val GL_OR_REVERSE = 0x150B
    const val GL_COPY_INVERTED = 0x150C
    const val GL_OR_INVERTED = 0x150D
    const val GL_NAND = 0x150E
    const val GL_SET = 0x150F
    const val GL_TEXTURE = 0x1702
    const val GL_COLOR = 0x1800
    const val GL_DEPTH = 0x1801
    const val GL_STENCIL = 0x1802
    const val GL_STENCIL_INDEX = 0x1901
    const val GL_DEPTH_COMPONENT = 0x1902
    const val GL_RED = 0x1903
    const val GL_GREEN = 0x1904
    const val GL_BLUE = 0x1905
    const val GL_ALPHA = 0x1906
    const val GL_RGB = 0x1907
    const val GL_RGBA = 0x1908
    const val GL_POINT = 0x1B00
    const val GL_LINE = 0x1B01
    const val GL_FILL = 0x1B02
    const val GL_KEEP = 0x1E00
    const val GL_REPLACE = 0x1E01
    const val GL_INCR = 0x1E02
    const val GL_DECR = 0x1E03
    const val GL_VENDOR = 0x1F00
    const val GL_RENDERER = 0x1F01
    const val GL_VERSION = 0x1F02
    const val GL_EXTENSIONS = 0x1F03
    const val GL_NEAREST = 0x2600
    const val GL_LINEAR = 0x2601
    const val GL_NEAREST_MIPMAP_NEAREST = 0x2700
    const val GL_LINEAR_MIPMAP_NEAREST = 0x2701
    const val GL_NEAREST_MIPMAP_LINEAR = 0x2702
    const val GL_LINEAR_MIPMAP_LINEAR = 0x2703
    const val GL_TEXTURE_MAG_FILTER = 0x2800
    const val GL_TEXTURE_MIN_FILTER = 0x2801
    const val GL_TEXTURE_WRAP_S = 0x2802
    const val GL_TEXTURE_WRAP_T = 0x2803
    const val GL_REPEAT = 0x2901
    const val GL_COLOR_LOGIC_OP = 0x0BF2
    const val GL_POLYGON_OFFSET_UNITS = 0x2A00
    const val GL_POLYGON_OFFSET_POINT = 0x2A01
    const val GL_POLYGON_OFFSET_LINE = 0x2A02
    const val GL_POLYGON_OFFSET_FILL = 0x8037
    const val GL_POLYGON_OFFSET_FACTOR = 0x8038
    const val GL_TEXTURE_BINDING_1D = 0x8068
    const val GL_TEXTURE_BINDING_2D = 0x8069
    const val GL_TEXTURE_INTERNAL_FORMAT = 0x1003
    const val GL_TEXTURE_RED_SIZE = 0x805C
    const val GL_TEXTURE_GREEN_SIZE = 0x805D
    const val GL_TEXTURE_BLUE_SIZE = 0x805E
    const val GL_TEXTURE_ALPHA_SIZE = 0x805F
    const val GL_DOUBLE = 0x140A
    const val GL_PROXY_TEXTURE_1D = 0x8063
    const val GL_PROXY_TEXTURE_2D = 0x8064
    const val GL_R3_G3_B2 = 0x2A10
    const val GL_RGB4 = 0x804F
    const val GL_RGB5 = 0x8050
    const val GL_RGB8 = 0x8051
    const val GL_RGB10 = 0x8052
    const val GL_RGB12 = 0x8053
    const val GL_RGB16 = 0x8054
    const val GL_RGBA2 = 0x8055
    const val GL_RGBA4 = 0x8056
    const val GL_RGB5_A1 = 0x8057
    const val GL_RGBA8 = 0x8058
    const val GL_RGB10_A2 = 0x8059
    const val GL_RGBA12 = 0x805A
    const val GL_RGBA16 = 0x805B
    const val GL_UNSIGNED_BYTE_3_3_2 = 0x8032
    const val GL_UNSIGNED_SHORT_4_4_4_4 = 0x8033
    const val GL_UNSIGNED_SHORT_5_5_5_1 = 0x8034
    const val GL_UNSIGNED_INT_8_8_8_8 = 0x8035
    const val GL_UNSIGNED_INT_10_10_10_2 = 0x8036
    const val GL_TEXTURE_BINDING_3D = 0x806A
    const val GL_PACK_SKIP_IMAGES = 0x806B
    const val GL_PACK_IMAGE_HEIGHT = 0x806C
    const val GL_UNPACK_SKIP_IMAGES = 0x806D
    const val GL_UNPACK_IMAGE_HEIGHT = 0x806E
    const val GL_TEXTURE_3D = 0x806F
    const val GL_PROXY_TEXTURE_3D = 0x8070
    const val GL_TEXTURE_DEPTH = 0x8071
    const val GL_TEXTURE_WRAP_R = 0x8072
    const val GL_MAX_3D_TEXTURE_SIZE = 0x8073
    const val GL_UNSIGNED_BYTE_2_3_3_REV = 0x8362
    const val GL_UNSIGNED_SHORT_5_6_5 = 0x8363
    const val GL_UNSIGNED_SHORT_5_6_5_REV = 0x8364
    const val GL_UNSIGNED_SHORT_4_4_4_4_REV = 0x8365
    const val GL_UNSIGNED_SHORT_1_5_5_5_REV = 0x8366
    const val GL_UNSIGNED_INT_8_8_8_8_REV = 0x8367
    const val GL_UNSIGNED_INT_2_10_10_10_REV = 0x8368
    const val GL_BGR = 0x80E0
    const val GL_BGRA = 0x80E1
    const val GL_MAX_ELEMENTS_VERTICES = 0x80E8
    const val GL_MAX_ELEMENTS_INDICES = 0x80E9
    const val GL_CLAMP_TO_EDGE = 0x812F
    const val GL_TEXTURE_MIN_LOD = 0x813A
    const val GL_TEXTURE_MAX_LOD = 0x813B
    const val GL_TEXTURE_BASE_LEVEL = 0x813C
    const val GL_TEXTURE_MAX_LEVEL = 0x813D
    const val GL_SMOOTH_POINT_SIZE_RANGE = 0x0B12
    const val GL_SMOOTH_POINT_SIZE_GRANULARITY = 0x0B13
    const val GL_SMOOTH_LINE_WIDTH_RANGE = 0x0B22
    const val GL_SMOOTH_LINE_WIDTH_GRANULARITY = 0x0B23
    const val GL_ALIASED_LINE_WIDTH_RANGE = 0x846E
    const val GL_TEXTURE0 = 0x84C0
    const val GL_TEXTURE1 = 0x84C1
    const val GL_TEXTURE2 = 0x84C2
    const val GL_TEXTURE3 = 0x84C3
    const val GL_TEXTURE4 = 0x84C4
    const val GL_TEXTURE5 = 0x84C5
    const val GL_TEXTURE6 = 0x84C6
    const val GL_TEXTURE7 = 0x84C7
    const val GL_TEXTURE8 = 0x84C8
    const val GL_TEXTURE9 = 0x84C9
    const val GL_TEXTURE10 = 0x84CA
    const val GL_TEXTURE11 = 0x84CB
    const val GL_TEXTURE12 = 0x84CC
    const val GL_TEXTURE13 = 0x84CD
    const val GL_TEXTURE14 = 0x84CE
    const val GL_TEXTURE15 = 0x84CF
    const val GL_TEXTURE16 = 0x84D0
    const val GL_TEXTURE17 = 0x84D1
    const val GL_TEXTURE18 = 0x84D2
    const val GL_TEXTURE19 = 0x84D3
    const val GL_TEXTURE20 = 0x84D4
    const val GL_TEXTURE21 = 0x84D5
    const val GL_TEXTURE22 = 0x84D6
    const val GL_TEXTURE23 = 0x84D7
    const val GL_TEXTURE24 = 0x84D8
    const val GL_TEXTURE25 = 0x84D9
    const val GL_TEXTURE26 = 0x84DA
    const val GL_TEXTURE27 = 0x84DB
    const val GL_TEXTURE28 = 0x84DC
    const val GL_TEXTURE29 = 0x84DD
    const val GL_TEXTURE30 = 0x84DE
    const val GL_TEXTURE31 = 0x84DF
    const val GL_ACTIVE_TEXTURE = 0x84E0
    const val GL_MULTISAMPLE = 0x809D
    const val GL_SAMPLE_ALPHA_TO_COVERAGE = 0x809E
    const val GL_SAMPLE_ALPHA_TO_ONE = 0x809F
    const val GL_SAMPLE_COVERAGE = 0x80A0
    const val GL_SAMPLE_BUFFERS = 0x80A8
    const val GL_SAMPLES = 0x80A9
    const val GL_SAMPLE_COVERAGE_VALUE = 0x80AA
    const val GL_SAMPLE_COVERAGE_INVERT = 0x80AB
    const val GL_TEXTURE_CUBE_MAP = 0x8513
    const val GL_TEXTURE_BINDING_CUBE_MAP = 0x8514
    const val GL_TEXTURE_CUBE_MAP_POSITIVE_X = 0x8515
    const val GL_TEXTURE_CUBE_MAP_NEGATIVE_X = 0x8516
    const val GL_TEXTURE_CUBE_MAP_POSITIVE_Y = 0x8517
    const val GL_TEXTURE_CUBE_MAP_NEGATIVE_Y = 0x8518
    const val GL_TEXTURE_CUBE_MAP_POSITIVE_Z = 0x8519
    const val GL_TEXTURE_CUBE_MAP_NEGATIVE_Z = 0x851A
    const val GL_PROXY_TEXTURE_CUBE_MAP = 0x851B
    const val GL_MAX_CUBE_MAP_TEXTURE_SIZE = 0x851C
    const val GL_COMPRESSED_RGB = 0x84ED
    const val GL_COMPRESSED_RGBA = 0x84EE
    const val GL_TEXTURE_COMPRESSION_HINT = 0x84EF
    const val GL_TEXTURE_COMPRESSED_IMAGE_SIZE = 0x86A0
    const val GL_TEXTURE_COMPRESSED = 0x86A1
    const val GL_NUM_COMPRESSED_TEXTURE_FORMATS = 0x86A2
    const val GL_COMPRESSED_TEXTURE_FORMATS = 0x86A3
    const val GL_CLAMP_TO_BORDER = 0x812D
    const val GL_BLEND_DST_RGB = 0x80C8
    const val GL_BLEND_SRC_RGB = 0x80C9
    const val GL_BLEND_DST_ALPHA = 0x80CA
    const val GL_BLEND_SRC_ALPHA = 0x80CB
    const val GL_POINT_FADE_THRESHOLD_SIZE = 0x8128
    const val GL_DEPTH_COMPONENT16 = 0x81A5
    const val GL_DEPTH_COMPONENT24 = 0x81A6
    const val GL_DEPTH_COMPONENT32 = 0x81A7
    const val GL_MIRRORED_REPEAT = 0x8370
    const val GL_MAX_TEXTURE_LOD_BIAS = 0x84FD
    const val GL_TEXTURE_LOD_BIAS = 0x8501
    const val GL_INCR_WRAP = 0x8507
    const val GL_DECR_WRAP = 0x8508
    const val GL_TEXTURE_DEPTH_SIZE = 0x884A
    const val GL_TEXTURE_COMPARE_MODE = 0x884C
    const val GL_TEXTURE_COMPARE_FUNC = 0x884D
    const val GL_BLEND_COLOR = 0x8005
    const val GL_BLEND_EQUATION = 0x8009
    const val GL_CONSTANT_COLOR = 0x8001
    const val GL_ONE_MINUS_CONSTANT_COLOR = 0x8002
    const val GL_CONSTANT_ALPHA = 0x8003
    const val GL_ONE_MINUS_CONSTANT_ALPHA = 0x8004
    const val GL_FUNC_ADD = 0x8006
    const val GL_FUNC_REVERSE_SUBTRACT = 0x800B
    const val GL_FUNC_SUBTRACT = 0x800A
    const val GL_MIN = 0x8007
    const val GL_MAX = 0x8008
    const val GL_BUFFER_SIZE = 0x8764
    const val GL_BUFFER_USAGE = 0x8765
    const val GL_QUERY_COUNTER_BITS = 0x8864
    const val GL_CURRENT_QUERY = 0x8865
    const val GL_QUERY_RESULT = 0x8866
    const val GL_QUERY_RESULT_AVAILABLE = 0x8867
    const val GL_ARRAY_BUFFER = 0x8892
    const val GL_ELEMENT_ARRAY_BUFFER = 0x8893
    const val GL_ARRAY_BUFFER_BINDING = 0x8894
    const val GL_ELEMENT_ARRAY_BUFFER_BINDING = 0x8895
    const val GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING = 0x889F
    const val GL_READ_ONLY = 0x88B8
    const val GL_WRITE_ONLY = 0x88B9
    const val GL_READ_WRITE = 0x88BA
    const val GL_BUFFER_ACCESS = 0x88BB
    const val GL_BUFFER_MAPPED = 0x88BC
    const val GL_BUFFER_MAP_POINTER = 0x88BD
    const val GL_STREAM_DRAW = 0x88E0
    const val GL_STREAM_READ = 0x88E1
    const val GL_STREAM_COPY = 0x88E2
    const val GL_STATIC_DRAW = 0x88E4
    const val GL_STATIC_READ = 0x88E5
    const val GL_STATIC_COPY = 0x88E6
    const val GL_DYNAMIC_DRAW = 0x88E8
    const val GL_DYNAMIC_READ = 0x88E9
    const val GL_DYNAMIC_COPY = 0x88EA
    const val GL_SAMPLES_PASSED = 0x8914
    const val GL_SRC1_ALPHA = 0x8589
    const val GL_BLEND_EQUATION_RGB = 0x8009
    const val GL_VERTEX_ATTRIB_ARRAY_ENABLED = 0x8622
    const val GL_VERTEX_ATTRIB_ARRAY_SIZE = 0x8623
    const val GL_VERTEX_ATTRIB_ARRAY_STRIDE = 0x8624
    const val GL_VERTEX_ATTRIB_ARRAY_TYPE = 0x8625
    const val GL_CURRENT_VERTEX_ATTRIB = 0x8626
    const val GL_VERTEX_PROGRAM_POINT_SIZE = 0x8642
    const val GL_VERTEX_ATTRIB_ARRAY_POINTER = 0x8645
    const val GL_STENCIL_BACK_FUNC = 0x8800
    const val GL_STENCIL_BACK_FAIL = 0x8801
    const val GL_STENCIL_BACK_PASS_DEPTH_FAIL = 0x8802
    const val GL_STENCIL_BACK_PASS_DEPTH_PASS = 0x8803
    const val GL_MAX_DRAW_BUFFERS = 0x8824
    const val GL_DRAW_BUFFER0 = 0x8825
    const val GL_DRAW_BUFFER1 = 0x8826
    const val GL_DRAW_BUFFER2 = 0x8827
    const val GL_DRAW_BUFFER3 = 0x8828
    const val GL_DRAW_BUFFER4 = 0x8829
    const val GL_DRAW_BUFFER5 = 0x882A
    const val GL_DRAW_BUFFER6 = 0x882B
    const val GL_DRAW_BUFFER7 = 0x882C
    const val GL_DRAW_BUFFER8 = 0x882D
    const val GL_DRAW_BUFFER9 = 0x882E
    const val GL_DRAW_BUFFER10 = 0x882F
    const val GL_DRAW_BUFFER11 = 0x8830
    const val GL_DRAW_BUFFER12 = 0x8831
    const val GL_DRAW_BUFFER13 = 0x8832
    const val GL_DRAW_BUFFER14 = 0x8833
    const val GL_DRAW_BUFFER15 = 0x8834
    const val GL_BLEND_EQUATION_ALPHA = 0x883D
    const val GL_MAX_VERTEX_ATTRIBS = 0x8869
    const val GL_VERTEX_ATTRIB_ARRAY_NORMALIZED = 0x886A
    const val GL_MAX_TEXTURE_IMAGE_UNITS = 0x8872
    const val GL_FRAGMENT_SHADER = 0x8B30
    const val GL_VERTEX_SHADER = 0x8B31
    const val GL_MAX_FRAGMENT_UNIFORM_COMPONENTS = 0x8B49
    const val GL_MAX_VERTEX_UNIFORM_COMPONENTS = 0x8B4A
    const val GL_MAX_VARYING_FLOATS = 0x8B4B
    const val GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS = 0x8B4C
    const val GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS = 0x8B4D
    const val GL_SHADER_TYPE = 0x8B4F
    const val GL_FLOAT_VEC2 = 0x8B50
    const val GL_FLOAT_VEC3 = 0x8B51
    const val GL_FLOAT_VEC4 = 0x8B52
    const val GL_INT_VEC2 = 0x8B53
    const val GL_INT_VEC3 = 0x8B54
    const val GL_INT_VEC4 = 0x8B55
    const val GL_BOOL = 0x8B56
    const val GL_BOOL_VEC2 = 0x8B57
    const val GL_BOOL_VEC3 = 0x8B58
    const val GL_BOOL_VEC4 = 0x8B59
    const val GL_FLOAT_MAT2 = 0x8B5A
    const val GL_FLOAT_MAT3 = 0x8B5B
    const val GL_FLOAT_MAT4 = 0x8B5C
    const val GL_SAMPLER_1D = 0x8B5D
    const val GL_SAMPLER_2D = 0x8B5E
    const val GL_SAMPLER_3D = 0x8B5F
    const val GL_SAMPLER_CUBE = 0x8B60
    const val GL_SAMPLER_1D_SHADOW = 0x8B61
    const val GL_SAMPLER_2D_SHADOW = 0x8B62
    const val GL_DELETE_STATUS = 0x8B80
    const val GL_COMPILE_STATUS = 0x8B81
    const val GL_LINK_STATUS = 0x8B82
    const val GL_VALIDATE_STATUS = 0x8B83
    const val GL_INFO_LOG_LENGTH = 0x8B84
    const val GL_ATTACHED_SHADERS = 0x8B85
    const val GL_ACTIVE_UNIFORMS = 0x8B86
    const val GL_ACTIVE_UNIFORM_MAX_LENGTH = 0x8B87
    const val GL_SHADER_SOURCE_LENGTH = 0x8B88
    const val GL_ACTIVE_ATTRIBUTES = 0x8B89
    const val GL_ACTIVE_ATTRIBUTE_MAX_LENGTH = 0x8B8A
    const val GL_FRAGMENT_SHADER_DERIVATIVE_HINT = 0x8B8B
    const val GL_SHADING_LANGUAGE_VERSION = 0x8B8C
    const val GL_CURRENT_PROGRAM = 0x8B8D
    const val GL_POINT_SPRITE_COORD_ORIGIN = 0x8CA0
    const val GL_LOWER_LEFT = 0x8CA1
    const val GL_UPPER_LEFT = 0x8CA2
    const val GL_STENCIL_BACK_REF = 0x8CA3
    const val GL_STENCIL_BACK_VALUE_MASK = 0x8CA4
    const val GL_STENCIL_BACK_WRITEMASK = 0x8CA5
    const val GL_PIXEL_PACK_BUFFER = 0x88EB
    const val GL_PIXEL_UNPACK_BUFFER = 0x88EC
    const val GL_PIXEL_PACK_BUFFER_BINDING = 0x88ED
    const val GL_PIXEL_UNPACK_BUFFER_BINDING = 0x88EF
    const val GL_FLOAT_MAT2x3 = 0x8B65
    const val GL_FLOAT_MAT2x4 = 0x8B66
    const val GL_FLOAT_MAT3x2 = 0x8B67
    const val GL_FLOAT_MAT3x4 = 0x8B68
    const val GL_FLOAT_MAT4x2 = 0x8B69
    const val GL_FLOAT_MAT4x3 = 0x8B6A
    const val GL_SRGB = 0x8C40
    const val GL_SRGB8 = 0x8C41
    const val GL_SRGB_ALPHA = 0x8C42
    const val GL_SRGB8_ALPHA8 = 0x8C43
    const val GL_COMPRESSED_SRGB = 0x8C48
    const val GL_COMPRESSED_SRGB_ALPHA = 0x8C49
    const val GL_COMPARE_REF_TO_TEXTURE = 0x884E
    const val GL_CLIP_DISTANCE0 = 0x3000
    const val GL_CLIP_DISTANCE1 = 0x3001
    const val GL_CLIP_DISTANCE2 = 0x3002
    const val GL_CLIP_DISTANCE3 = 0x3003
    const val GL_CLIP_DISTANCE4 = 0x3004
    const val GL_CLIP_DISTANCE5 = 0x3005
    const val GL_CLIP_DISTANCE6 = 0x3006
    const val GL_CLIP_DISTANCE7 = 0x3007
    const val GL_MAX_CLIP_DISTANCES = 0x0D32
    const val GL_MAJOR_VERSION = 0x821B
    const val GL_MINOR_VERSION = 0x821C
    const val GL_NUM_EXTENSIONS = 0x821D
    const val GL_CONTEXT_FLAGS = 0x821E
    const val GL_COMPRESSED_RED = 0x8225
    const val GL_COMPRESSED_RG = 0x8226
    const val GL_CONTEXT_FLAG_FORWARD_COMPATIBLE_BIT = 0x00000001
    const val GL_RGBA32F = 0x8814
    const val GL_RGB32F = 0x8815
    const val GL_RGBA16F = 0x881A
    const val GL_RGB16F = 0x881B
    const val GL_VERTEX_ATTRIB_ARRAY_INTEGER = 0x88FD
    const val GL_MAX_ARRAY_TEXTURE_LAYERS = 0x88FF
    const val GL_MIN_PROGRAM_TEXEL_OFFSET = 0x8904
    const val GL_MAX_PROGRAM_TEXEL_OFFSET = 0x8905
    const val GL_CLAMP_READ_COLOR = 0x891C
    const val GL_FIXED_ONLY = 0x891D
    const val GL_MAX_VARYING_COMPONENTS = 0x8B4B
    const val GL_TEXTURE_1D_ARRAY = 0x8C18
    const val GL_PROXY_TEXTURE_1D_ARRAY = 0x8C19
    const val GL_TEXTURE_2D_ARRAY = 0x8C1A
    const val GL_PROXY_TEXTURE_2D_ARRAY = 0x8C1B
    const val GL_TEXTURE_BINDING_1D_ARRAY = 0x8C1C
    const val GL_TEXTURE_BINDING_2D_ARRAY = 0x8C1D
    const val GL_R11F_G11F_B10F = 0x8C3A
    const val GL_UNSIGNED_INT_10F_11F_11F_REV = 0x8C3B
    const val GL_RGB9_E5 = 0x8C3D
    const val GL_UNSIGNED_INT_5_9_9_9_REV = 0x8C3E
    const val GL_TEXTURE_SHARED_SIZE = 0x8C3F
    const val GL_TRANSFORM_FEEDBACK_VARYING_MAX_LENGTH = 0x8C76
    const val GL_TRANSFORM_FEEDBACK_BUFFER_MODE = 0x8C7F
    const val GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_COMPONENTS = 0x8C80
    const val GL_TRANSFORM_FEEDBACK_VARYINGS = 0x8C83
    const val GL_TRANSFORM_FEEDBACK_BUFFER_START = 0x8C84
    const val GL_TRANSFORM_FEEDBACK_BUFFER_SIZE = 0x8C85
    const val GL_PRIMITIVES_GENERATED = 0x8C87
    const val GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN = 0x8C88
    const val GL_RASTERIZER_DISCARD = 0x8C89
    const val GL_MAX_TRANSFORM_FEEDBACK_INTERLEAVED_COMPONENTS = 0x8C8A
    const val GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_ATTRIBS = 0x8C8B
    const val GL_INTERLEAVED_ATTRIBS = 0x8C8C
    const val GL_SEPARATE_ATTRIBS = 0x8C8D
    const val GL_TRANSFORM_FEEDBACK_BUFFER = 0x8C8E
    const val GL_TRANSFORM_FEEDBACK_BUFFER_BINDING = 0x8C8F
    const val GL_RGBA32UI = 0x8D70
    const val GL_RGB32UI = 0x8D71
    const val GL_RGBA16UI = 0x8D76
    const val GL_RGB16UI = 0x8D77
    const val GL_RGBA8UI = 0x8D7C
    const val GL_RGB8UI = 0x8D7D
    const val GL_RGBA32I = 0x8D82
    const val GL_RGB32I = 0x8D83
    const val GL_RGBA16I = 0x8D88
    const val GL_RGB16I = 0x8D89
    const val GL_RGBA8I = 0x8D8E
    const val GL_RGB8I = 0x8D8F
    const val GL_RED_INTEGER = 0x8D94
    const val GL_GREEN_INTEGER = 0x8D95
    const val GL_BLUE_INTEGER = 0x8D96
    const val GL_RGB_INTEGER = 0x8D98
    const val GL_RGBA_INTEGER = 0x8D99
    const val GL_BGR_INTEGER = 0x8D9A
    const val GL_BGRA_INTEGER = 0x8D9B
    const val GL_SAMPLER_1D_ARRAY = 0x8DC0
    const val GL_SAMPLER_2D_ARRAY = 0x8DC1
    const val GL_SAMPLER_1D_ARRAY_SHADOW = 0x8DC3
    const val GL_SAMPLER_2D_ARRAY_SHADOW = 0x8DC4
    const val GL_SAMPLER_CUBE_SHADOW = 0x8DC5
    const val GL_UNSIGNED_INT_VEC2 = 0x8DC6
    const val GL_UNSIGNED_INT_VEC3 = 0x8DC7
    const val GL_UNSIGNED_INT_VEC4 = 0x8DC8
    const val GL_INT_SAMPLER_1D = 0x8DC9
    const val GL_INT_SAMPLER_2D = 0x8DCA
    const val GL_INT_SAMPLER_3D = 0x8DCB
    const val GL_INT_SAMPLER_CUBE = 0x8DCC
    const val GL_INT_SAMPLER_1D_ARRAY = 0x8DCE
    const val GL_INT_SAMPLER_2D_ARRAY = 0x8DCF
    const val GL_UNSIGNED_INT_SAMPLER_1D = 0x8DD1
    const val GL_UNSIGNED_INT_SAMPLER_2D = 0x8DD2
    const val GL_UNSIGNED_INT_SAMPLER_3D = 0x8DD3
    const val GL_UNSIGNED_INT_SAMPLER_CUBE = 0x8DD4
    const val GL_UNSIGNED_INT_SAMPLER_1D_ARRAY = 0x8DD6
    const val GL_UNSIGNED_INT_SAMPLER_2D_ARRAY = 0x8DD7
    const val GL_QUERY_WAIT = 0x8E13
    const val GL_QUERY_NO_WAIT = 0x8E14
    const val GL_QUERY_BY_REGION_WAIT = 0x8E15
    const val GL_QUERY_BY_REGION_NO_WAIT = 0x8E16
    const val GL_BUFFER_ACCESS_FLAGS = 0x911F
    const val GL_BUFFER_MAP_LENGTH = 0x9120
    const val GL_BUFFER_MAP_OFFSET = 0x9121
    const val GL_DEPTH_COMPONENT32F = 0x8CAC
    const val GL_DEPTH32F_STENCIL8 = 0x8CAD
    const val GL_FLOAT_32_UNSIGNED_INT_24_8_REV = 0x8DAD
    const val GL_INVALID_FRAMEBUFFER_OPERATION = 0x0506
    const val GL_FRAMEBUFFER_ATTACHMENT_COLOR_ENCODING = 0x8210
    const val GL_FRAMEBUFFER_ATTACHMENT_COMPONENT_TYPE = 0x8211
    const val GL_FRAMEBUFFER_ATTACHMENT_RED_SIZE = 0x8212
    const val GL_FRAMEBUFFER_ATTACHMENT_GREEN_SIZE = 0x8213
    const val GL_FRAMEBUFFER_ATTACHMENT_BLUE_SIZE = 0x8214
    const val GL_FRAMEBUFFER_ATTACHMENT_ALPHA_SIZE = 0x8215
    const val GL_FRAMEBUFFER_ATTACHMENT_DEPTH_SIZE = 0x8216
    const val GL_FRAMEBUFFER_ATTACHMENT_STENCIL_SIZE = 0x8217
    const val GL_FRAMEBUFFER_DEFAULT = 0x8218
    const val GL_FRAMEBUFFER_UNDEFINED = 0x8219
    const val GL_DEPTH_STENCIL_ATTACHMENT = 0x821A
    const val GL_MAX_RENDERBUFFER_SIZE = 0x84E8
    const val GL_DEPTH_STENCIL = 0x84F9
    const val GL_UNSIGNED_INT_24_8 = 0x84FA
    const val GL_DEPTH24_STENCIL8 = 0x88F0
    const val GL_TEXTURE_STENCIL_SIZE = 0x88F1
    const val GL_TEXTURE_RED_TYPE = 0x8C10
    const val GL_TEXTURE_GREEN_TYPE = 0x8C11
    const val GL_TEXTURE_BLUE_TYPE = 0x8C12
    const val GL_TEXTURE_ALPHA_TYPE = 0x8C13
    const val GL_TEXTURE_DEPTH_TYPE = 0x8C16
    const val GL_UNSIGNED_NORMALIZED = 0x8C17
    const val GL_FRAMEBUFFER_BINDING = 0x8CA6
    const val GL_DRAW_FRAMEBUFFER_BINDING = 0x8CA6
    const val GL_RENDERBUFFER_BINDING = 0x8CA7
    const val GL_READ_FRAMEBUFFER = 0x8CA8
    const val GL_DRAW_FRAMEBUFFER = 0x8CA9
    const val GL_READ_FRAMEBUFFER_BINDING = 0x8CAA
    const val GL_RENDERBUFFER_SAMPLES = 0x8CAB
    const val GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE = 0x8CD0
    const val GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME = 0x8CD1
    const val GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL = 0x8CD2
    const val GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE = 0x8CD3
    const val GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LAYER = 0x8CD4
    const val GL_FRAMEBUFFER_COMPLETE = 0x8CD5
    const val GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT = 0x8CD6
    const val GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = 0x8CD7
    const val GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER = 0x8CDB
    const val GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER = 0x8CDC
    const val GL_FRAMEBUFFER_UNSUPPORTED = 0x8CDD
    const val GL_MAX_COLOR_ATTACHMENTS = 0x8CDF
    const val GL_COLOR_ATTACHMENT0 = 0x8CE0
    const val GL_COLOR_ATTACHMENT1 = 0x8CE1
    const val GL_COLOR_ATTACHMENT2 = 0x8CE2
    const val GL_COLOR_ATTACHMENT3 = 0x8CE3
    const val GL_COLOR_ATTACHMENT4 = 0x8CE4
    const val GL_COLOR_ATTACHMENT5 = 0x8CE5
    const val GL_COLOR_ATTACHMENT6 = 0x8CE6
    const val GL_COLOR_ATTACHMENT7 = 0x8CE7
    const val GL_COLOR_ATTACHMENT8 = 0x8CE8
    const val GL_COLOR_ATTACHMENT9 = 0x8CE9
    const val GL_COLOR_ATTACHMENT10 = 0x8CEA
    const val GL_COLOR_ATTACHMENT11 = 0x8CEB
    const val GL_COLOR_ATTACHMENT12 = 0x8CEC
    const val GL_COLOR_ATTACHMENT13 = 0x8CED
    const val GL_COLOR_ATTACHMENT14 = 0x8CEE
    const val GL_COLOR_ATTACHMENT15 = 0x8CEF
    const val GL_COLOR_ATTACHMENT16 = 0x8CF0
    const val GL_COLOR_ATTACHMENT17 = 0x8CF1
    const val GL_COLOR_ATTACHMENT18 = 0x8CF2
    const val GL_COLOR_ATTACHMENT19 = 0x8CF3
    const val GL_COLOR_ATTACHMENT20 = 0x8CF4
    const val GL_COLOR_ATTACHMENT21 = 0x8CF5
    const val GL_COLOR_ATTACHMENT22 = 0x8CF6
    const val GL_COLOR_ATTACHMENT23 = 0x8CF7
    const val GL_COLOR_ATTACHMENT24 = 0x8CF8
    const val GL_COLOR_ATTACHMENT25 = 0x8CF9
    const val GL_COLOR_ATTACHMENT26 = 0x8CFA
    const val GL_COLOR_ATTACHMENT27 = 0x8CFB
    const val GL_COLOR_ATTACHMENT28 = 0x8CFC
    const val GL_COLOR_ATTACHMENT29 = 0x8CFD
    const val GL_COLOR_ATTACHMENT30 = 0x8CFE
    const val GL_COLOR_ATTACHMENT31 = 0x8CFF
    const val GL_DEPTH_ATTACHMENT = 0x8D00
    const val GL_STENCIL_ATTACHMENT = 0x8D20
    const val GL_FRAMEBUFFER = 0x8D40
    const val GL_RENDERBUFFER = 0x8D41
    const val GL_RENDERBUFFER_WIDTH = 0x8D42
    const val GL_RENDERBUFFER_HEIGHT = 0x8D43
    const val GL_RENDERBUFFER_INTERNAL_FORMAT = 0x8D44
    const val GL_STENCIL_INDEX1 = 0x8D46
    const val GL_STENCIL_INDEX4 = 0x8D47
    const val GL_STENCIL_INDEX8 = 0x8D48
    const val GL_STENCIL_INDEX16 = 0x8D49
    const val GL_RENDERBUFFER_RED_SIZE = 0x8D50
    const val GL_RENDERBUFFER_GREEN_SIZE = 0x8D51
    const val GL_RENDERBUFFER_BLUE_SIZE = 0x8D52
    const val GL_RENDERBUFFER_ALPHA_SIZE = 0x8D53
    const val GL_RENDERBUFFER_DEPTH_SIZE = 0x8D54
    const val GL_RENDERBUFFER_STENCIL_SIZE = 0x8D55
    const val GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE = 0x8D56
    const val GL_MAX_SAMPLES = 0x8D57
    const val GL_FRAMEBUFFER_SRGB = 0x8DB9
    const val GL_HALF_FLOAT = 0x140B
    const val GL_MAP_READ_BIT = 0x0001
    const val GL_MAP_WRITE_BIT = 0x0002
    const val GL_MAP_INVALIDATE_RANGE_BIT = 0x0004
    const val GL_MAP_INVALIDATE_BUFFER_BIT = 0x0008
    const val GL_MAP_FLUSH_EXPLICIT_BIT = 0x0010
    const val GL_MAP_UNSYNCHRONIZED_BIT = 0x0020
    const val GL_COMPRESSED_RED_RGTC1 = 0x8DBB
    const val GL_COMPRESSED_SIGNED_RED_RGTC1 = 0x8DBC
    const val GL_COMPRESSED_RG_RGTC2 = 0x8DBD
    const val GL_COMPRESSED_SIGNED_RG_RGTC2 = 0x8DBE
    const val GL_RG = 0x8227
    const val GL_RG_INTEGER = 0x8228
    const val GL_R8 = 0x8229
    const val GL_R16 = 0x822A
    const val GL_RG8 = 0x822B
    const val GL_RG16 = 0x822C
    const val GL_R16F = 0x822D
    const val GL_R32F = 0x822E
    const val GL_RG16F = 0x822F
    const val GL_RG32F = 0x8230
    const val GL_R8I = 0x8231
    const val GL_R8UI = 0x8232
    const val GL_R16I = 0x8233
    const val GL_R16UI = 0x8234
    const val GL_R32I = 0x8235
    const val GL_R32UI = 0x8236
    const val GL_RG8I = 0x8237
    const val GL_RG8UI = 0x8238
    const val GL_RG16I = 0x8239
    const val GL_RG16UI = 0x823A
    const val GL_RG32I = 0x823B
    const val GL_RG32UI = 0x823C
    const val GL_VERTEX_ARRAY_BINDING = 0x85B5
    const val GL_SAMPLER_2D_RECT = 0x8B63
    const val GL_SAMPLER_2D_RECT_SHADOW = 0x8B64
    const val GL_SAMPLER_BUFFER = 0x8DC2
    const val GL_INT_SAMPLER_2D_RECT = 0x8DCD
    const val GL_INT_SAMPLER_BUFFER = 0x8DD0
    const val GL_UNSIGNED_INT_SAMPLER_2D_RECT = 0x8DD5
    const val GL_UNSIGNED_INT_SAMPLER_BUFFER = 0x8DD8
    const val GL_TEXTURE_BUFFER = 0x8C2A
    const val GL_MAX_TEXTURE_BUFFER_SIZE = 0x8C2B
    const val GL_TEXTURE_BINDING_BUFFER = 0x8C2C
    const val GL_TEXTURE_BUFFER_DATA_STORE_BINDING = 0x8C2D
    const val GL_TEXTURE_RECTANGLE = 0x84F5
    const val GL_TEXTURE_BINDING_RECTANGLE = 0x84F6
    const val GL_PROXY_TEXTURE_RECTANGLE = 0x84F7
    const val GL_MAX_RECTANGLE_TEXTURE_SIZE = 0x84F8
    const val GL_R8_SNORM = 0x8F94
    const val GL_RG8_SNORM = 0x8F95
    const val GL_RGB8_SNORM = 0x8F96
    const val GL_RGBA8_SNORM = 0x8F97
    const val GL_R16_SNORM = 0x8F98
    const val GL_RG16_SNORM = 0x8F99
    const val GL_RGB16_SNORM = 0x8F9A
    const val GL_RGBA16_SNORM = 0x8F9B
    const val GL_SIGNED_NORMALIZED = 0x8F9C
    const val GL_PRIMITIVE_RESTART = 0x8F9D
    const val GL_PRIMITIVE_RESTART_INDEX = 0x8F9E
    const val GL_COPY_READ_BUFFER = 0x8F36
    const val GL_COPY_WRITE_BUFFER = 0x8F37
    const val GL_UNIFORM_BUFFER = 0x8A11
    const val GL_UNIFORM_BUFFER_BINDING = 0x8A28
    const val GL_UNIFORM_BUFFER_START = 0x8A29
    const val GL_UNIFORM_BUFFER_SIZE = 0x8A2A
    const val GL_MAX_VERTEX_UNIFORM_BLOCKS = 0x8A2B
    const val GL_MAX_GEOMETRY_UNIFORM_BLOCKS = 0x8A2C
    const val GL_MAX_FRAGMENT_UNIFORM_BLOCKS = 0x8A2D
    const val GL_MAX_COMBINED_UNIFORM_BLOCKS = 0x8A2E
    const val GL_MAX_UNIFORM_BUFFER_BINDINGS = 0x8A2F
    const val GL_MAX_UNIFORM_BLOCK_SIZE = 0x8A30
    const val GL_MAX_COMBINED_VERTEX_UNIFORM_COMPONENTS = 0x8A31
    const val GL_MAX_COMBINED_GEOMETRY_UNIFORM_COMPONENTS = 0x8A32
    const val GL_MAX_COMBINED_FRAGMENT_UNIFORM_COMPONENTS = 0x8A33
    const val GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT = 0x8A34
    const val GL_ACTIVE_UNIFORM_BLOCK_MAX_NAME_LENGTH = 0x8A35
    const val GL_ACTIVE_UNIFORM_BLOCKS = 0x8A36
    const val GL_UNIFORM_TYPE = 0x8A37
    const val GL_UNIFORM_SIZE = 0x8A38
    const val GL_UNIFORM_NAME_LENGTH = 0x8A39
    const val GL_UNIFORM_BLOCK_INDEX = 0x8A3A
    const val GL_UNIFORM_OFFSET = 0x8A3B
    const val GL_UNIFORM_ARRAY_STRIDE = 0x8A3C
    const val GL_UNIFORM_MATRIX_STRIDE = 0x8A3D
    const val GL_UNIFORM_IS_ROW_MAJOR = 0x8A3E
    const val GL_UNIFORM_BLOCK_BINDING = 0x8A3F
    const val GL_UNIFORM_BLOCK_DATA_SIZE = 0x8A40
    const val GL_UNIFORM_BLOCK_NAME_LENGTH = 0x8A41
    const val GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS = 0x8A42
    const val GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES = 0x8A43
    const val GL_UNIFORM_BLOCK_REFERENCED_BY_VERTEX_SHADER = 0x8A44
    const val GL_UNIFORM_BLOCK_REFERENCED_BY_GEOMETRY_SHADER = 0x8A45
    const val GL_UNIFORM_BLOCK_REFERENCED_BY_FRAGMENT_SHADER = 0x8A46
    const val GL_INVALID_INDEX = 0xFFFFFFFF
    const val GL_CONTEXT_CORE_PROFILE_BIT = 0x00000001
    const val GL_CONTEXT_COMPATIBILITY_PROFILE_BIT = 0x00000002
    const val GL_LINES_ADJACENCY = 0x000A
    const val GL_LINE_STRIP_ADJACENCY = 0x000B
    const val GL_TRIANGLES_ADJACENCY = 0x000C
    const val GL_TRIANGLE_STRIP_ADJACENCY = 0x000D
    const val GL_PROGRAM_POINT_SIZE = 0x8642
    const val GL_MAX_GEOMETRY_TEXTURE_IMAGE_UNITS = 0x8C29
    const val GL_FRAMEBUFFER_ATTACHMENT_LAYERED = 0x8DA7
    const val GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS = 0x8DA8
    const val GL_GEOMETRY_SHADER = 0x8DD9
    const val GL_GEOMETRY_VERTICES_OUT = 0x8916
    const val GL_GEOMETRY_INPUT_TYPE = 0x8917
    const val GL_GEOMETRY_OUTPUT_TYPE = 0x8918
    const val GL_MAX_GEOMETRY_UNIFORM_COMPONENTS = 0x8DDF
    const val GL_MAX_GEOMETRY_OUTPUT_VERTICES = 0x8DE0
    const val GL_MAX_GEOMETRY_TOTAL_OUTPUT_COMPONENTS = 0x8DE1
    const val GL_MAX_VERTEX_OUTPUT_COMPONENTS = 0x9122
    const val GL_MAX_GEOMETRY_INPUT_COMPONENTS = 0x9123
    const val GL_MAX_GEOMETRY_OUTPUT_COMPONENTS = 0x9124
    const val GL_MAX_FRAGMENT_INPUT_COMPONENTS = 0x9125
    const val GL_CONTEXT_PROFILE_MASK = 0x9126
    const val GL_DEPTH_CLAMP = 0x864F
    const val GL_QUADS_FOLLOW_PROVOKING_VERTEX_CONVENTION = 0x8E4C
    const val GL_FIRST_VERTEX_CONVENTION = 0x8E4D
    const val GL_LAST_VERTEX_CONVENTION = 0x8E4E
    const val GL_PROVOKING_VERTEX = 0x8E4F
    const val GL_TEXTURE_CUBE_MAP_SEAMLESS = 0x884F
    const val GL_MAX_SERVER_WAIT_TIMEOUT = 0x9111
    const val GL_OBJECT_TYPE = 0x9112
    const val GL_SYNC_CONDITION = 0x9113
    const val GL_SYNC_STATUS = 0x9114
    const val GL_SYNC_FLAGS = 0x9115
    const val GL_SYNC_FENCE = 0x9116
    const val GL_SYNC_GPU_COMMANDS_COMPLETE = 0x9117
    const val GL_UNSIGNALED = 0x9118
    const val GL_SIGNALED = 0x9119
    const val GL_ALREADY_SIGNALED = 0x911A
    const val GL_TIMEOUT_EXPIRED = 0x911B
    const val GL_CONDITION_SATISFIED = 0x911C
    const val GL_WAIT_FAILED = 0x911D
    const val GL_TIMEOUT_IGNORED = ULong.MAX_VALUE
    const val GL_SYNC_FLUSH_COMMANDS_BIT = 0x00000001
    const val GL_SAMPLE_POSITION = 0x8E50
    const val GL_SAMPLE_MASK = 0x8E51
    const val GL_SAMPLE_MASK_VALUE = 0x8E52
    const val GL_MAX_SAMPLE_MASK_WORDS = 0x8E59
    const val GL_TEXTURE_2D_MULTISAMPLE = 0x9100
    const val GL_PROXY_TEXTURE_2D_MULTISAMPLE = 0x9101
    const val GL_TEXTURE_2D_MULTISAMPLE_ARRAY = 0x9102
    const val GL_PROXY_TEXTURE_2D_MULTISAMPLE_ARRAY = 0x9103
    const val GL_TEXTURE_BINDING_2D_MULTISAMPLE = 0x9104
    const val GL_TEXTURE_BINDING_2D_MULTISAMPLE_ARRAY = 0x9105
    const val GL_TEXTURE_SAMPLES = 0x9106
    const val GL_TEXTURE_FIXED_SAMPLE_LOCATIONS = 0x9107
    const val GL_SAMPLER_2D_MULTISAMPLE = 0x9108
    const val GL_INT_SAMPLER_2D_MULTISAMPLE = 0x9109
    const val GL_UNSIGNED_INT_SAMPLER_2D_MULTISAMPLE = 0x910A
    const val GL_SAMPLER_2D_MULTISAMPLE_ARRAY = 0x910B
    const val GL_INT_SAMPLER_2D_MULTISAMPLE_ARRAY = 0x910C
    const val GL_UNSIGNED_INT_SAMPLER_2D_MULTISAMPLE_ARRAY = 0x910D
    const val GL_MAX_COLOR_TEXTURE_SAMPLES = 0x910E
    const val GL_MAX_DEPTH_TEXTURE_SAMPLES = 0x910F
    const val GL_MAX_INTEGER_SAMPLES = 0x9110
    const val GL_VERTEX_ATTRIB_ARRAY_DIVISOR = 0x88FE
    const val GL_SRC1_COLOR = 0x88F9
    const val GL_ONE_MINUS_SRC1_COLOR = 0x88FA
    const val GL_ONE_MINUS_SRC1_ALPHA = 0x88FB
    const val GL_MAX_DUAL_SOURCE_DRAW_BUFFERS = 0x88FC
    const val GL_ANY_SAMPLES_PASSED = 0x8C2F
    const val GL_SAMPLER_BINDING = 0x8919
    const val GL_RGB10_A2UI = 0x906F
    const val GL_TEXTURE_SWIZZLE_R = 0x8E42
    const val GL_TEXTURE_SWIZZLE_G = 0x8E43
    const val GL_TEXTURE_SWIZZLE_B = 0x8E44
    const val GL_TEXTURE_SWIZZLE_A = 0x8E45
    const val GL_TEXTURE_SWIZZLE_RGBA = 0x8E46
    const val GL_TIME_ELAPSED = 0x88BF
    const val GL_TIMESTAMP = 0x8E28
    const val GL_INT_2_10_10_10_REV = 0x8D9F
    const val GL_SAMPLE_SHADING = 0x8C36
    const val GL_MIN_SAMPLE_SHADING_VALUE = 0x8C37
    const val GL_MIN_PROGRAM_TEXTURE_GATHER_OFFSET = 0x8E5E
    const val GL_MAX_PROGRAM_TEXTURE_GATHER_OFFSET = 0x8E5F
    const val GL_TEXTURE_CUBE_MAP_ARRAY = 0x9009
    const val GL_TEXTURE_BINDING_CUBE_MAP_ARRAY = 0x900A
    const val GL_PROXY_TEXTURE_CUBE_MAP_ARRAY = 0x900B
    const val GL_SAMPLER_CUBE_MAP_ARRAY = 0x900C
    const val GL_SAMPLER_CUBE_MAP_ARRAY_SHADOW = 0x900D
    const val GL_INT_SAMPLER_CUBE_MAP_ARRAY = 0x900E
    const val GL_UNSIGNED_INT_SAMPLER_CUBE_MAP_ARRAY = 0x900F
    const val GL_DRAW_INDIRECT_BUFFER = 0x8F3F
    const val GL_DRAW_INDIRECT_BUFFER_BINDING = 0x8F43
    const val GL_GEOMETRY_SHADER_INVOCATIONS = 0x887F
    const val GL_MAX_GEOMETRY_SHADER_INVOCATIONS = 0x8E5A
    const val GL_MIN_FRAGMENT_INTERPOLATION_OFFSET = 0x8E5B
    const val GL_MAX_FRAGMENT_INTERPOLATION_OFFSET = 0x8E5C
    const val GL_FRAGMENT_INTERPOLATION_OFFSET_BITS = 0x8E5D
    const val GL_MAX_VERTEX_STREAMS = 0x8E71
    const val GL_DOUBLE_VEC2 = 0x8FFC
    const val GL_DOUBLE_VEC3 = 0x8FFD
    const val GL_DOUBLE_VEC4 = 0x8FFE
    const val GL_DOUBLE_MAT2 = 0x8F46
    const val GL_DOUBLE_MAT3 = 0x8F47
    const val GL_DOUBLE_MAT4 = 0x8F48
    const val GL_DOUBLE_MAT2x3 = 0x8F49
    const val GL_DOUBLE_MAT2x4 = 0x8F4A
    const val GL_DOUBLE_MAT3x2 = 0x8F4B
    const val GL_DOUBLE_MAT3x4 = 0x8F4C
    const val GL_DOUBLE_MAT4x2 = 0x8F4D
    const val GL_DOUBLE_MAT4x3 = 0x8F4E
    const val GL_ACTIVE_SUBROUTINES = 0x8DE5
    const val GL_ACTIVE_SUBROUTINE_UNIFORMS = 0x8DE6
    const val GL_ACTIVE_SUBROUTINE_UNIFORM_LOCATIONS = 0x8E47
    const val GL_ACTIVE_SUBROUTINE_MAX_LENGTH = 0x8E48
    const val GL_ACTIVE_SUBROUTINE_UNIFORM_MAX_LENGTH = 0x8E49
    const val GL_MAX_SUBROUTINES = 0x8DE7
    const val GL_MAX_SUBROUTINE_UNIFORM_LOCATIONS = 0x8DE8
    const val GL_NUM_COMPATIBLE_SUBROUTINES = 0x8E4A
    const val GL_COMPATIBLE_SUBROUTINES = 0x8E4B
    const val GL_PATCHES = 0x000E
    const val GL_PATCH_VERTICES = 0x8E72
    const val GL_PATCH_DEFAULT_INNER_LEVEL = 0x8E73
    const val GL_PATCH_DEFAULT_OUTER_LEVEL = 0x8E74
    const val GL_TESS_CONTROL_OUTPUT_VERTICES = 0x8E75
    const val GL_TESS_GEN_MODE = 0x8E76
    const val GL_TESS_GEN_SPACING = 0x8E77
    const val GL_TESS_GEN_VERTEX_ORDER = 0x8E78
    const val GL_TESS_GEN_POINT_MODE = 0x8E79
    const val GL_ISOLINES = 0x8E7A
    const val GL_QUADS = 0x0007
    const val GL_FRACTIONAL_ODD = 0x8E7B
    const val GL_FRACTIONAL_EVEN = 0x8E7C
    const val GL_MAX_PATCH_VERTICES = 0x8E7D
    const val GL_MAX_TESS_GEN_LEVEL = 0x8E7E
    const val GL_MAX_TESS_CONTROL_UNIFORM_COMPONENTS = 0x8E7F
    const val GL_MAX_TESS_EVALUATION_UNIFORM_COMPONENTS = 0x8E80
    const val GL_MAX_TESS_CONTROL_TEXTURE_IMAGE_UNITS = 0x8E81
    const val GL_MAX_TESS_EVALUATION_TEXTURE_IMAGE_UNITS = 0x8E82
    const val GL_MAX_TESS_CONTROL_OUTPUT_COMPONENTS = 0x8E83
    const val GL_MAX_TESS_PATCH_COMPONENTS = 0x8E84
    const val GL_MAX_TESS_CONTROL_TOTAL_OUTPUT_COMPONENTS = 0x8E85
    const val GL_MAX_TESS_EVALUATION_OUTPUT_COMPONENTS = 0x8E86
    const val GL_MAX_TESS_CONTROL_UNIFORM_BLOCKS = 0x8E89
    const val GL_MAX_TESS_EVALUATION_UNIFORM_BLOCKS = 0x8E8A
    const val GL_MAX_TESS_CONTROL_INPUT_COMPONENTS = 0x886C
    const val GL_MAX_TESS_EVALUATION_INPUT_COMPONENTS = 0x886D
    const val GL_MAX_COMBINED_TESS_CONTROL_UNIFORM_COMPONENTS = 0x8E1E
    const val GL_MAX_COMBINED_TESS_EVALUATION_UNIFORM_COMPONENTS = 0x8E1F
    const val GL_UNIFORM_BLOCK_REFERENCED_BY_TESS_CONTROL_SHADER = 0x84F0
    const val GL_UNIFORM_BLOCK_REFERENCED_BY_TESS_EVALUATION_SHADER = 0x84F1
    const val GL_TESS_EVALUATION_SHADER = 0x8E87
    const val GL_TESS_CONTROL_SHADER = 0x8E88
    const val GL_TRANSFORM_FEEDBACK = 0x8E22
    const val GL_TRANSFORM_FEEDBACK_BUFFER_PAUSED = 0x8E23
    const val GL_TRANSFORM_FEEDBACK_BUFFER_ACTIVE = 0x8E24
    const val GL_TRANSFORM_FEEDBACK_BINDING = 0x8E25
    const val GL_MAX_TRANSFORM_FEEDBACK_BUFFERS = 0x8E70
    const val GL_FIXED = 0x140C
    const val GL_IMPLEMENTATION_COLOR_READ_TYPE = 0x8B9A
    const val GL_IMPLEMENTATION_COLOR_READ_FORMAT = 0x8B9B
    const val GL_LOW_FLOAT = 0x8DF0
    const val GL_MEDIUM_FLOAT = 0x8DF1
    const val GL_HIGH_FLOAT = 0x8DF2
    const val GL_LOW_INT = 0x8DF3
    const val GL_MEDIUM_INT = 0x8DF4
    const val GL_HIGH_INT = 0x8DF5
    const val GL_SHADER_COMPILER = 0x8DFA
    const val GL_SHADER_BINARY_FORMATS = 0x8DF8
    const val GL_NUM_SHADER_BINARY_FORMATS = 0x8DF9
    const val GL_MAX_VERTEX_UNIFORM_VECTORS = 0x8DFB
    const val GL_MAX_VARYING_VECTORS = 0x8DFC
    const val GL_MAX_FRAGMENT_UNIFORM_VECTORS = 0x8DFD
    const val GL_RGB565 = 0x8D62
    const val GL_PROGRAM_BINARY_RETRIEVABLE_HINT = 0x8257
    const val GL_PROGRAM_BINARY_LENGTH = 0x8741
    const val GL_NUM_PROGRAM_BINARY_FORMATS = 0x87FE
    const val GL_PROGRAM_BINARY_FORMATS = 0x87FF
    const val GL_VERTEX_SHADER_BIT = 0x00000001
    const val GL_FRAGMENT_SHADER_BIT = 0x00000002
    const val GL_GEOMETRY_SHADER_BIT = 0x00000004
    const val GL_TESS_CONTROL_SHADER_BIT = 0x00000008
    const val GL_TESS_EVALUATION_SHADER_BIT = 0x00000010
    const val GL_ALL_SHADER_BITS = 0xFFFFFFFF
    const val GL_PROGRAM_SEPARABLE = 0x8258
    const val GL_ACTIVE_PROGRAM = 0x8259
    const val GL_PROGRAM_PIPELINE_BINDING = 0x825A
    const val GL_MAX_VIEWPORTS = 0x825B
    const val GL_VIEWPORT_SUBPIXEL_BITS = 0x825C
    const val GL_VIEWPORT_BOUNDS_RANGE = 0x825D
    const val GL_LAYER_PROVOKING_VERTEX = 0x825E
    const val GL_VIEWPORT_INDEX_PROVOKING_VERTEX = 0x825F
    const val GL_UNDEFINED_VERTEX = 0x8260
    const val GL_COPY_READ_BUFFER_BINDING = 0x8F36
    const val GL_COPY_WRITE_BUFFER_BINDING = 0x8F37
    const val GL_TRANSFORM_FEEDBACK_ACTIVE = 0x8E24
    const val GL_TRANSFORM_FEEDBACK_PAUSED = 0x8E23
    const val GL_UNPACK_COMPRESSED_BLOCK_WIDTH = 0x9127
    const val GL_UNPACK_COMPRESSED_BLOCK_HEIGHT = 0x9128
    const val GL_UNPACK_COMPRESSED_BLOCK_DEPTH = 0x9129
    const val GL_UNPACK_COMPRESSED_BLOCK_SIZE = 0x912A
    const val GL_PACK_COMPRESSED_BLOCK_WIDTH = 0x912B
    const val GL_PACK_COMPRESSED_BLOCK_HEIGHT = 0x912C
    const val GL_PACK_COMPRESSED_BLOCK_DEPTH = 0x912D
    const val GL_PACK_COMPRESSED_BLOCK_SIZE = 0x912E
    const val GL_NUM_SAMPLE_COUNTS = 0x9380
    const val GL_MIN_MAP_BUFFER_ALIGNMENT = 0x90BC
    const val GL_ATOMIC_COUNTER_BUFFER = 0x92C0
    const val GL_ATOMIC_COUNTER_BUFFER_BINDING = 0x92C1
    const val GL_ATOMIC_COUNTER_BUFFER_START = 0x92C2
    const val GL_ATOMIC_COUNTER_BUFFER_SIZE = 0x92C3
    const val GL_ATOMIC_COUNTER_BUFFER_DATA_SIZE = 0x92C4
    const val GL_ATOMIC_COUNTER_BUFFER_ACTIVE_ATOMIC_COUNTERS = 0x92C5
    const val GL_ATOMIC_COUNTER_BUFFER_ACTIVE_ATOMIC_COUNTER_INDICES = 0x92C6
    const val GL_ATOMIC_COUNTER_BUFFER_REFERENCED_BY_VERTEX_SHADER = 0x92C7
    const val GL_ATOMIC_COUNTER_BUFFER_REFERENCED_BY_TESS_CONTROL_SHADER = 0x92C8
    const val GL_ATOMIC_COUNTER_BUFFER_REFERENCED_BY_TESS_EVALUATION_SHADER = 0x92C9
    const val GL_ATOMIC_COUNTER_BUFFER_REFERENCED_BY_GEOMETRY_SHADER = 0x92CA
    const val GL_ATOMIC_COUNTER_BUFFER_REFERENCED_BY_FRAGMENT_SHADER = 0x92CB
    const val GL_MAX_VERTEX_ATOMIC_COUNTER_BUFFERS = 0x92CC
    const val GL_MAX_TESS_CONTROL_ATOMIC_COUNTER_BUFFERS = 0x92CD
    const val GL_MAX_TESS_EVALUATION_ATOMIC_COUNTER_BUFFERS = 0x92CE
    const val GL_MAX_GEOMETRY_ATOMIC_COUNTER_BUFFERS = 0x92CF
    const val GL_MAX_FRAGMENT_ATOMIC_COUNTER_BUFFERS = 0x92D0
    const val GL_MAX_COMBINED_ATOMIC_COUNTER_BUFFERS = 0x92D1
    const val GL_MAX_VERTEX_ATOMIC_COUNTERS = 0x92D2
    const val GL_MAX_TESS_CONTROL_ATOMIC_COUNTERS = 0x92D3
    const val GL_MAX_TESS_EVALUATION_ATOMIC_COUNTERS = 0x92D4
    const val GL_MAX_GEOMETRY_ATOMIC_COUNTERS = 0x92D5
    const val GL_MAX_FRAGMENT_ATOMIC_COUNTERS = 0x92D6
    const val GL_MAX_COMBINED_ATOMIC_COUNTERS = 0x92D7
    const val GL_MAX_ATOMIC_COUNTER_BUFFER_SIZE = 0x92D8
    const val GL_MAX_ATOMIC_COUNTER_BUFFER_BINDINGS = 0x92DC
    const val GL_ACTIVE_ATOMIC_COUNTER_BUFFERS = 0x92D9
    const val GL_UNIFORM_ATOMIC_COUNTER_BUFFER_INDEX = 0x92DA
    const val GL_UNSIGNED_INT_ATOMIC_COUNTER = 0x92DB
    const val GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT = 0x00000001
    const val GL_ELEMENT_ARRAY_BARRIER_BIT = 0x00000002
    const val GL_UNIFORM_BARRIER_BIT = 0x00000004
    const val GL_TEXTURE_FETCH_BARRIER_BIT = 0x00000008
    const val GL_SHADER_IMAGE_ACCESS_BARRIER_BIT = 0x00000020
    const val GL_COMMAND_BARRIER_BIT = 0x00000040
    const val GL_PIXEL_BUFFER_BARRIER_BIT = 0x00000080
    const val GL_TEXTURE_UPDATE_BARRIER_BIT = 0x00000100
    const val GL_BUFFER_UPDATE_BARRIER_BIT = 0x00000200
    const val GL_FRAMEBUFFER_BARRIER_BIT = 0x00000400
    const val GL_TRANSFORM_FEEDBACK_BARRIER_BIT = 0x00000800
    const val GL_ATOMIC_COUNTER_BARRIER_BIT = 0x00001000
    const val GL_ALL_BARRIER_BITS = 0xFFFFFFFF
    const val GL_MAX_IMAGE_UNITS = 0x8F38
    const val GL_MAX_COMBINED_IMAGE_UNITS_AND_FRAGMENT_OUTPUTS = 0x8F39
    const val GL_IMAGE_BINDING_NAME = 0x8F3A
    const val GL_IMAGE_BINDING_LEVEL = 0x8F3B
    const val GL_IMAGE_BINDING_LAYERED = 0x8F3C
    const val GL_IMAGE_BINDING_LAYER = 0x8F3D
    const val GL_IMAGE_BINDING_ACCESS = 0x8F3E
    const val GL_IMAGE_1D = 0x904C
    const val GL_IMAGE_2D = 0x904D
    const val GL_IMAGE_3D = 0x904E
    const val GL_IMAGE_2D_RECT = 0x904F
    const val GL_IMAGE_CUBE = 0x9050
    const val GL_IMAGE_BUFFER = 0x9051
    const val GL_IMAGE_1D_ARRAY = 0x9052
    const val GL_IMAGE_2D_ARRAY = 0x9053
    const val GL_IMAGE_CUBE_MAP_ARRAY = 0x9054
    const val GL_IMAGE_2D_MULTISAMPLE = 0x9055
    const val GL_IMAGE_2D_MULTISAMPLE_ARRAY = 0x9056
    const val GL_INT_IMAGE_1D = 0x9057
    const val GL_INT_IMAGE_2D = 0x9058
    const val GL_INT_IMAGE_3D = 0x9059
    const val GL_INT_IMAGE_2D_RECT = 0x905A
    const val GL_INT_IMAGE_CUBE = 0x905B
    const val GL_INT_IMAGE_BUFFER = 0x905C
    const val GL_INT_IMAGE_1D_ARRAY = 0x905D
    const val GL_INT_IMAGE_2D_ARRAY = 0x905E
    const val GL_INT_IMAGE_CUBE_MAP_ARRAY = 0x905F
    const val GL_INT_IMAGE_2D_MULTISAMPLE = 0x9060
    const val GL_INT_IMAGE_2D_MULTISAMPLE_ARRAY = 0x9061
    const val GL_UNSIGNED_INT_IMAGE_1D = 0x9062
    const val GL_UNSIGNED_INT_IMAGE_2D = 0x9063
    const val GL_UNSIGNED_INT_IMAGE_3D = 0x9064
    const val GL_UNSIGNED_INT_IMAGE_2D_RECT = 0x9065
    const val GL_UNSIGNED_INT_IMAGE_CUBE = 0x9066
    const val GL_UNSIGNED_INT_IMAGE_BUFFER = 0x9067
    const val GL_UNSIGNED_INT_IMAGE_1D_ARRAY = 0x9068
    const val GL_UNSIGNED_INT_IMAGE_2D_ARRAY = 0x9069
    const val GL_UNSIGNED_INT_IMAGE_CUBE_MAP_ARRAY = 0x906A
    const val GL_UNSIGNED_INT_IMAGE_2D_MULTISAMPLE = 0x906B
    const val GL_UNSIGNED_INT_IMAGE_2D_MULTISAMPLE_ARRAY = 0x906C
    const val GL_MAX_IMAGE_SAMPLES = 0x906D
    const val GL_IMAGE_BINDING_FORMAT = 0x906E
    const val GL_IMAGE_FORMAT_COMPATIBILITY_TYPE = 0x90C7
    const val GL_IMAGE_FORMAT_COMPATIBILITY_BY_SIZE = 0x90C8
    const val GL_IMAGE_FORMAT_COMPATIBILITY_BY_CLASS = 0x90C9
    const val GL_MAX_VERTEX_IMAGE_UNIFORMS = 0x90CA
    const val GL_MAX_TESS_CONTROL_IMAGE_UNIFORMS = 0x90CB
    const val GL_MAX_TESS_EVALUATION_IMAGE_UNIFORMS = 0x90CC
    const val GL_MAX_GEOMETRY_IMAGE_UNIFORMS = 0x90CD
    const val GL_MAX_FRAGMENT_IMAGE_UNIFORMS = 0x90CE
    const val GL_MAX_COMBINED_IMAGE_UNIFORMS = 0x90CF
    const val GL_COMPRESSED_RGBA_BPTC_UNORM = 0x8E8C
    const val GL_COMPRESSED_SRGB_ALPHA_BPTC_UNORM = 0x8E8D
    const val GL_COMPRESSED_RGB_BPTC_SIGNED_FLOAT = 0x8E8E
    const val GL_COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT = 0x8E8F
    const val GL_TEXTURE_IMMUTABLE_FORMAT = 0x912F
    const val GL_NUM_SHADING_LANGUAGE_VERSIONS = 0x82E9
    const val GL_VERTEX_ATTRIB_ARRAY_LONG = 0x874E
    const val GL_COMPRESSED_RGB8_ETC2 = 0x9274
    const val GL_COMPRESSED_SRGB8_ETC2 = 0x9275
    const val GL_COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2 = 0x9276
    const val GL_COMPRESSED_SRGB8_PUNCHTHROUGH_ALPHA1_ETC2 = 0x9277
    const val GL_COMPRESSED_RGBA8_ETC2_EAC = 0x9278
    const val GL_COMPRESSED_SRGB8_ALPHA8_ETC2_EAC = 0x9279
    const val GL_COMPRESSED_R11_EAC = 0x9270
    const val GL_COMPRESSED_SIGNED_R11_EAC = 0x9271
    const val GL_COMPRESSED_RG11_EAC = 0x9272
    const val GL_COMPRESSED_SIGNED_RG11_EAC = 0x9273
    const val GL_PRIMITIVE_RESTART_FIXED_INDEX = 0x8D69
    const val GL_ANY_SAMPLES_PASSED_CONSERVATIVE = 0x8D6A
    const val GL_MAX_ELEMENT_INDEX = 0x8D6B
    const val GL_COMPUTE_SHADER = 0x91B9
    const val GL_MAX_COMPUTE_UNIFORM_BLOCKS = 0x91BB
    const val GL_MAX_COMPUTE_TEXTURE_IMAGE_UNITS = 0x91BC
    const val GL_MAX_COMPUTE_IMAGE_UNIFORMS = 0x91BD
    const val GL_MAX_COMPUTE_SHARED_MEMORY_SIZE = 0x8262
    const val GL_MAX_COMPUTE_UNIFORM_COMPONENTS = 0x8263
    const val GL_MAX_COMPUTE_ATOMIC_COUNTER_BUFFERS = 0x8264
    const val GL_MAX_COMPUTE_ATOMIC_COUNTERS = 0x8265
    const val GL_MAX_COMBINED_COMPUTE_UNIFORM_COMPONENTS = 0x8266
    const val GL_MAX_COMPUTE_WORK_GROUP_INVOCATIONS = 0x90EB
    const val GL_MAX_COMPUTE_WORK_GROUP_COUNT = 0x91BE
    const val GL_MAX_COMPUTE_WORK_GROUP_SIZE = 0x91BF
    const val GL_COMPUTE_WORK_GROUP_SIZE = 0x8267
    const val GL_UNIFORM_BLOCK_REFERENCED_BY_COMPUTE_SHADER = 0x90EC
    const val GL_ATOMIC_COUNTER_BUFFER_REFERENCED_BY_COMPUTE_SHADER = 0x90ED
    const val GL_DISPATCH_INDIRECT_BUFFER = 0x90EE
    const val GL_DISPATCH_INDIRECT_BUFFER_BINDING = 0x90EF
    const val GL_COMPUTE_SHADER_BIT = 0x00000020
    const val GL_DEBUG_OUTPUT_SYNCHRONOUS = 0x8242
    const val GL_DEBUG_NEXT_LOGGED_MESSAGE_LENGTH = 0x8243
    const val GL_DEBUG_CALLBACK_FUNCTION = 0x8244
    const val GL_DEBUG_CALLBACK_USER_PARAM = 0x8245
    const val GL_DEBUG_SOURCE_API = 0x8246
    const val GL_DEBUG_SOURCE_WINDOW_SYSTEM = 0x8247
    const val GL_DEBUG_SOURCE_SHADER_COMPILER = 0x8248
    const val GL_DEBUG_SOURCE_THIRD_PARTY = 0x8249
    const val GL_DEBUG_SOURCE_APPLICATION = 0x824A
    const val GL_DEBUG_SOURCE_OTHER = 0x824B
    const val GL_DEBUG_TYPE_ERROR = 0x824C
    const val GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR = 0x824D
    const val GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR = 0x824E
    const val GL_DEBUG_TYPE_PORTABILITY = 0x824F
    const val GL_DEBUG_TYPE_PERFORMANCE = 0x8250
    const val GL_DEBUG_TYPE_OTHER = 0x8251
    const val GL_MAX_DEBUG_MESSAGE_LENGTH = 0x9143
    const val GL_MAX_DEBUG_LOGGED_MESSAGES = 0x9144
    const val GL_DEBUG_LOGGED_MESSAGES = 0x9145
    const val GL_DEBUG_SEVERITY_HIGH = 0x9146
    const val GL_DEBUG_SEVERITY_MEDIUM = 0x9147
    const val GL_DEBUG_SEVERITY_LOW = 0x9148
    const val GL_DEBUG_TYPE_MARKER = 0x8268
    const val GL_DEBUG_TYPE_PUSH_GROUP = 0x8269
    const val GL_DEBUG_TYPE_POP_GROUP = 0x826A
    const val GL_DEBUG_SEVERITY_NOTIFICATION = 0x826B
    const val GL_MAX_DEBUG_GROUP_STACK_DEPTH = 0x826C
    const val GL_DEBUG_GROUP_STACK_DEPTH = 0x826D
    const val GL_BUFFER = 0x82E0
    const val GL_SHADER = 0x82E1
    const val GL_PROGRAM = 0x82E2
    const val GL_VERTEX_ARRAY = 0x8074
    const val GL_QUERY = 0x82E3
    const val GL_PROGRAM_PIPELINE = 0x82E4
    const val GL_SAMPLER = 0x82E6
    const val GL_MAX_LABEL_LENGTH = 0x82E8
    const val GL_DEBUG_OUTPUT = 0x92E0
    const val GL_CONTEXT_FLAG_DEBUG_BIT = 0x00000002
    const val GL_MAX_UNIFORM_LOCATIONS = 0x826E
    const val GL_FRAMEBUFFER_DEFAULT_WIDTH = 0x9310
    const val GL_FRAMEBUFFER_DEFAULT_HEIGHT = 0x9311
    const val GL_FRAMEBUFFER_DEFAULT_LAYERS = 0x9312
    const val GL_FRAMEBUFFER_DEFAULT_SAMPLES = 0x9313
    const val GL_FRAMEBUFFER_DEFAULT_FIXED_SAMPLE_LOCATIONS = 0x9314
    const val GL_MAX_FRAMEBUFFER_WIDTH = 0x9315
    const val GL_MAX_FRAMEBUFFER_HEIGHT = 0x9316
    const val GL_MAX_FRAMEBUFFER_LAYERS = 0x9317
    const val GL_MAX_FRAMEBUFFER_SAMPLES = 0x9318
    const val GL_INTERNALFORMAT_SUPPORTED = 0x826F
    const val GL_INTERNALFORMAT_PREFERRED = 0x8270
    const val GL_INTERNALFORMAT_RED_SIZE = 0x8271
    const val GL_INTERNALFORMAT_GREEN_SIZE = 0x8272
    const val GL_INTERNALFORMAT_BLUE_SIZE = 0x8273
    const val GL_INTERNALFORMAT_ALPHA_SIZE = 0x8274
    const val GL_INTERNALFORMAT_DEPTH_SIZE = 0x8275
    const val GL_INTERNALFORMAT_STENCIL_SIZE = 0x8276
    const val GL_INTERNALFORMAT_SHARED_SIZE = 0x8277
    const val GL_INTERNALFORMAT_RED_TYPE = 0x8278
    const val GL_INTERNALFORMAT_GREEN_TYPE = 0x8279
    const val GL_INTERNALFORMAT_BLUE_TYPE = 0x827A
    const val GL_INTERNALFORMAT_ALPHA_TYPE = 0x827B
    const val GL_INTERNALFORMAT_DEPTH_TYPE = 0x827C
    const val GL_INTERNALFORMAT_STENCIL_TYPE = 0x827D
    const val GL_MAX_WIDTH = 0x827E
    const val GL_MAX_HEIGHT = 0x827F
    const val GL_MAX_DEPTH = 0x8280
    const val GL_MAX_LAYERS = 0x8281
    const val GL_MAX_COMBINED_DIMENSIONS = 0x8282
    const val GL_COLOR_COMPONENTS = 0x8283
    const val GL_DEPTH_COMPONENTS = 0x8284
    const val GL_STENCIL_COMPONENTS = 0x8285
    const val GL_COLOR_RENDERABLE = 0x8286
    const val GL_DEPTH_RENDERABLE = 0x8287
    const val GL_STENCIL_RENDERABLE = 0x8288
    const val GL_FRAMEBUFFER_RENDERABLE = 0x8289
    const val GL_FRAMEBUFFER_RENDERABLE_LAYERED = 0x828A
    const val GL_FRAMEBUFFER_BLEND = 0x828B
    const val GL_READ_PIXELS = 0x828C
    const val GL_READ_PIXELS_FORMAT = 0x828D
    const val GL_READ_PIXELS_TYPE = 0x828E
    const val GL_TEXTURE_IMAGE_FORMAT = 0x828F
    const val GL_TEXTURE_IMAGE_TYPE = 0x8290
    const val GL_GET_TEXTURE_IMAGE_FORMAT = 0x8291
    const val GL_GET_TEXTURE_IMAGE_TYPE = 0x8292
    const val GL_MIPMAP = 0x8293
    const val GL_MANUAL_GENERATE_MIPMAP = 0x8294
    const val GL_AUTO_GENERATE_MIPMAP = 0x8295
    const val GL_COLOR_ENCODING = 0x8296
    const val GL_SRGB_READ = 0x8297
    const val GL_SRGB_WRITE = 0x8298
    const val GL_FILTER = 0x829A
    const val GL_VERTEX_TEXTURE = 0x829B
    const val GL_TESS_CONTROL_TEXTURE = 0x829C
    const val GL_TESS_EVALUATION_TEXTURE = 0x829D
    const val GL_GEOMETRY_TEXTURE = 0x829E
    const val GL_FRAGMENT_TEXTURE = 0x829F
    const val GL_COMPUTE_TEXTURE = 0x82A0
    const val GL_TEXTURE_SHADOW = 0x82A1
    const val GL_TEXTURE_GATHER = 0x82A2
    const val GL_TEXTURE_GATHER_SHADOW = 0x82A3
    const val GL_SHADER_IMAGE_LOAD = 0x82A4
    const val GL_SHADER_IMAGE_STORE = 0x82A5
    const val GL_SHADER_IMAGE_ATOMIC = 0x82A6
    const val GL_IMAGE_TEXEL_SIZE = 0x82A7
    const val GL_IMAGE_COMPATIBILITY_CLASS = 0x82A8
    const val GL_IMAGE_PIXEL_FORMAT = 0x82A9
    const val GL_IMAGE_PIXEL_TYPE = 0x82AA
    const val GL_SIMULTANEOUS_TEXTURE_AND_DEPTH_TEST = 0x82AC
    const val GL_SIMULTANEOUS_TEXTURE_AND_STENCIL_TEST = 0x82AD
    const val GL_SIMULTANEOUS_TEXTURE_AND_DEPTH_WRITE = 0x82AE
    const val GL_SIMULTANEOUS_TEXTURE_AND_STENCIL_WRITE = 0x82AF
    const val GL_TEXTURE_COMPRESSED_BLOCK_WIDTH = 0x82B1
    const val GL_TEXTURE_COMPRESSED_BLOCK_HEIGHT = 0x82B2
    const val GL_TEXTURE_COMPRESSED_BLOCK_SIZE = 0x82B3
    const val GL_CLEAR_BUFFER = 0x82B4
    const val GL_TEXTURE_VIEW = 0x82B5
    const val GL_VIEW_COMPATIBILITY_CLASS = 0x82B6
    const val GL_FULL_SUPPORT = 0x82B7
    const val GL_CAVEAT_SUPPORT = 0x82B8
    const val GL_IMAGE_CLASS_4_X_32 = 0x82B9
    const val GL_IMAGE_CLASS_2_X_32 = 0x82BA
    const val GL_IMAGE_CLASS_1_X_32 = 0x82BB
    const val GL_IMAGE_CLASS_4_X_16 = 0x82BC
    const val GL_IMAGE_CLASS_2_X_16 = 0x82BD
    const val GL_IMAGE_CLASS_1_X_16 = 0x82BE
    const val GL_IMAGE_CLASS_4_X_8 = 0x82BF
    const val GL_IMAGE_CLASS_2_X_8 = 0x82C0
    const val GL_IMAGE_CLASS_1_X_8 = 0x82C1
    const val GL_IMAGE_CLASS_11_11_10 = 0x82C2
    const val GL_IMAGE_CLASS_10_10_10_2 = 0x82C3
    const val GL_VIEW_CLASS_128_BITS = 0x82C4
    const val GL_VIEW_CLASS_96_BITS = 0x82C5
    const val GL_VIEW_CLASS_64_BITS = 0x82C6
    const val GL_VIEW_CLASS_48_BITS = 0x82C7
    const val GL_VIEW_CLASS_32_BITS = 0x82C8
    const val GL_VIEW_CLASS_24_BITS = 0x82C9
    const val GL_VIEW_CLASS_16_BITS = 0x82CA
    const val GL_VIEW_CLASS_8_BITS = 0x82CB
    const val GL_VIEW_CLASS_S3TC_DXT1_RGB = 0x82CC
    const val GL_VIEW_CLASS_S3TC_DXT1_RGBA = 0x82CD
    const val GL_VIEW_CLASS_S3TC_DXT3_RGBA = 0x82CE
    const val GL_VIEW_CLASS_S3TC_DXT5_RGBA = 0x82CF
    const val GL_VIEW_CLASS_RGTC1_RED = 0x82D0
    const val GL_VIEW_CLASS_RGTC2_RG = 0x82D1
    const val GL_VIEW_CLASS_BPTC_UNORM = 0x82D2
    const val GL_VIEW_CLASS_BPTC_FLOAT = 0x82D3
    const val GL_UNIFORM = 0x92E1
    const val GL_UNIFORM_BLOCK = 0x92E2
    const val GL_PROGRAM_INPUT = 0x92E3
    const val GL_PROGRAM_OUTPUT = 0x92E4
    const val GL_BUFFER_VARIABLE = 0x92E5
    const val GL_SHADER_STORAGE_BLOCK = 0x92E6
    const val GL_VERTEX_SUBROUTINE = 0x92E8
    const val GL_TESS_CONTROL_SUBROUTINE = 0x92E9
    const val GL_TESS_EVALUATION_SUBROUTINE = 0x92EA
    const val GL_GEOMETRY_SUBROUTINE = 0x92EB
    const val GL_FRAGMENT_SUBROUTINE = 0x92EC
    const val GL_COMPUTE_SUBROUTINE = 0x92ED
    const val GL_VERTEX_SUBROUTINE_UNIFORM = 0x92EE
    const val GL_TESS_CONTROL_SUBROUTINE_UNIFORM = 0x92EF
    const val GL_TESS_EVALUATION_SUBROUTINE_UNIFORM = 0x92F0
    const val GL_GEOMETRY_SUBROUTINE_UNIFORM = 0x92F1
    const val GL_FRAGMENT_SUBROUTINE_UNIFORM = 0x92F2
    const val GL_COMPUTE_SUBROUTINE_UNIFORM = 0x92F3
    const val GL_TRANSFORM_FEEDBACK_VARYING = 0x92F4
    const val GL_ACTIVE_RESOURCES = 0x92F5
    const val GL_MAX_NAME_LENGTH = 0x92F6
    const val GL_MAX_NUM_ACTIVE_VARIABLES = 0x92F7
    const val GL_MAX_NUM_COMPATIBLE_SUBROUTINES = 0x92F8
    const val GL_NAME_LENGTH = 0x92F9
    const val GL_TYPE = 0x92FA
    const val GL_ARRAY_SIZE = 0x92FB
    const val GL_OFFSET = 0x92FC
    const val GL_BLOCK_INDEX = 0x92FD
    const val GL_ARRAY_STRIDE = 0x92FE
    const val GL_MATRIX_STRIDE = 0x92FF
    const val GL_IS_ROW_MAJOR = 0x9300
    const val GL_ATOMIC_COUNTER_BUFFER_INDEX = 0x9301
    const val GL_BUFFER_BINDING = 0x9302
    const val GL_BUFFER_DATA_SIZE = 0x9303
    const val GL_NUM_ACTIVE_VARIABLES = 0x9304
    const val GL_ACTIVE_VARIABLES = 0x9305
    const val GL_REFERENCED_BY_VERTEX_SHADER = 0x9306
    const val GL_REFERENCED_BY_TESS_CONTROL_SHADER = 0x9307
    const val GL_REFERENCED_BY_TESS_EVALUATION_SHADER = 0x9308
    const val GL_REFERENCED_BY_GEOMETRY_SHADER = 0x9309
    const val GL_REFERENCED_BY_FRAGMENT_SHADER = 0x930A
    const val GL_REFERENCED_BY_COMPUTE_SHADER = 0x930B
    const val GL_TOP_LEVEL_ARRAY_SIZE = 0x930C
    const val GL_TOP_LEVEL_ARRAY_STRIDE = 0x930D
    const val GL_LOCATION = 0x930E
    const val GL_LOCATION_INDEX = 0x930F
    const val GL_IS_PER_PATCH = 0x92E7
    const val GL_SHADER_STORAGE_BUFFER = 0x90D2
    const val GL_SHADER_STORAGE_BUFFER_BINDING = 0x90D3
    const val GL_SHADER_STORAGE_BUFFER_START = 0x90D4
    const val GL_SHADER_STORAGE_BUFFER_SIZE = 0x90D5
    const val GL_MAX_VERTEX_SHADER_STORAGE_BLOCKS = 0x90D6
    const val GL_MAX_GEOMETRY_SHADER_STORAGE_BLOCKS = 0x90D7
    const val GL_MAX_TESS_CONTROL_SHADER_STORAGE_BLOCKS = 0x90D8
    const val GL_MAX_TESS_EVALUATION_SHADER_STORAGE_BLOCKS = 0x90D9
    const val GL_MAX_FRAGMENT_SHADER_STORAGE_BLOCKS = 0x90DA
    const val GL_MAX_COMPUTE_SHADER_STORAGE_BLOCKS = 0x90DB
    const val GL_MAX_COMBINED_SHADER_STORAGE_BLOCKS = 0x90DC
    const val GL_MAX_SHADER_STORAGE_BUFFER_BINDINGS = 0x90DD
    const val GL_MAX_SHADER_STORAGE_BLOCK_SIZE = 0x90DE
    const val GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT = 0x90DF
    const val GL_SHADER_STORAGE_BARRIER_BIT = 0x00002000
    const val GL_MAX_COMBINED_SHADER_OUTPUT_RESOURCES = 0x8F39
    const val GL_DEPTH_STENCIL_TEXTURE_MODE = 0x90EA
    const val GL_TEXTURE_BUFFER_OFFSET = 0x919D
    const val GL_TEXTURE_BUFFER_SIZE = 0x919E
    const val GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT = 0x919F
    const val GL_TEXTURE_VIEW_MIN_LEVEL = 0x82DB
    const val GL_TEXTURE_VIEW_NUM_LEVELS = 0x82DC
    const val GL_TEXTURE_VIEW_MIN_LAYER = 0x82DD
    const val GL_TEXTURE_VIEW_NUM_LAYERS = 0x82DE
    const val GL_TEXTURE_IMMUTABLE_LEVELS = 0x82DF
    const val GL_VERTEX_ATTRIB_BINDING = 0x82D4
    const val GL_VERTEX_ATTRIB_RELATIVE_OFFSET = 0x82D5
    const val GL_VERTEX_BINDING_DIVISOR = 0x82D6
    const val GL_VERTEX_BINDING_OFFSET = 0x82D7
    const val GL_VERTEX_BINDING_STRIDE = 0x82D8
    const val GL_MAX_VERTEX_ATTRIB_RELATIVE_OFFSET = 0x82D9
    const val GL_MAX_VERTEX_ATTRIB_BINDINGS = 0x82DA
    const val GL_VERTEX_BINDING_BUFFER = 0x8F4F
    const val GL_DISPLAY_LIST = 0x82E7
    const val GL_STACK_UNDERFLOW = 0x0504
    const val GL_STACK_OVERFLOW = 0x0503
    const val GL_MAX_VERTEX_ATTRIB_STRIDE = 0x82E5
    const val GL_PRIMITIVE_RESTART_FOR_PATCHES_SUPPORTED = 0x8221
    const val GL_TEXTURE_BUFFER_BINDING = 0x8C2A
    const val GL_MAP_PERSISTENT_BIT = 0x0040
    const val GL_MAP_COHERENT_BIT = 0x0080
    const val GL_DYNAMIC_STORAGE_BIT = 0x0100
    const val GL_CLIENT_STORAGE_BIT = 0x0200
    const val GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT = 0x00004000
    const val GL_BUFFER_IMMUTABLE_STORAGE = 0x821F
    const val GL_BUFFER_STORAGE_FLAGS = 0x8220
    const val GL_CLEAR_TEXTURE = 0x9365
    const val GL_LOCATION_COMPONENT = 0x934A
    const val GL_TRANSFORM_FEEDBACK_BUFFER_INDEX = 0x934B
    const val GL_TRANSFORM_FEEDBACK_BUFFER_STRIDE = 0x934C
    const val GL_QUERY_BUFFER = 0x9192
    const val GL_QUERY_BUFFER_BARRIER_BIT = 0x00008000
    const val GL_QUERY_BUFFER_BINDING = 0x9193
    const val GL_QUERY_RESULT_NO_WAIT = 0x9194
    const val GL_MIRROR_CLAMP_TO_EDGE = 0x8743
    const val GL_CONTEXT_LOST = 0x0507
    const val GL_NEGATIVE_ONE_TO_ONE = 0x935E
    const val GL_ZERO_TO_ONE = 0x935F
    const val GL_CLIP_ORIGIN = 0x935C
    const val GL_CLIP_DEPTH_MODE = 0x935D
    const val GL_QUERY_WAIT_INVERTED = 0x8E17
    const val GL_QUERY_NO_WAIT_INVERTED = 0x8E18
    const val GL_QUERY_BY_REGION_WAIT_INVERTED = 0x8E19
    const val GL_QUERY_BY_REGION_NO_WAIT_INVERTED = 0x8E1A
    const val GL_MAX_CULL_DISTANCES = 0x82F9
    const val GL_MAX_COMBINED_CLIP_AND_CULL_DISTANCES = 0x82FA
    const val GL_TEXTURE_TARGET = 0x1006
    const val GL_QUERY_TARGET = 0x82EA
    const val GL_GUILTY_CONTEXT_RESET = 0x8253
    const val GL_INNOCENT_CONTEXT_RESET = 0x8254
    const val GL_UNKNOWN_CONTEXT_RESET = 0x8255
    const val GL_RESET_NOTIFICATION_STRATEGY = 0x8256
    const val GL_LOSE_CONTEXT_ON_RESET = 0x8252
    const val GL_NO_RESET_NOTIFICATION = 0x8261
    const val GL_CONTEXT_FLAG_ROBUST_ACCESS_BIT = 0x00000004
    const val GL_COLOR_TABLE = 0x80D0
    const val GL_POST_CONVOLUTION_COLOR_TABLE = 0x80D1
    const val GL_POST_COLOR_MATRIX_COLOR_TABLE = 0x80D2
    const val GL_PROXY_COLOR_TABLE = 0x80D3
    const val GL_PROXY_POST_CONVOLUTION_COLOR_TABLE = 0x80D4
    const val GL_PROXY_POST_COLOR_MATRIX_COLOR_TABLE = 0x80D5
    const val GL_CONVOLUTION_1D = 0x8010
    const val GL_CONVOLUTION_2D = 0x8011
    const val GL_SEPARABLE_2D = 0x8012
    const val GL_HISTOGRAM = 0x8024
    const val GL_PROXY_HISTOGRAM = 0x8025
    const val GL_MINMAX = 0x802E
    const val GL_CONTEXT_RELEASE_BEHAVIOR = 0x82FB
    const val GL_CONTEXT_RELEASE_BEHAVIOR_FLUSH = 0x82FC
    const val GL_SHADER_BINARY_FORMAT_SPIR_V = 0x9551
    const val GL_SPIR_V_BINARY = 0x9552
    const val GL_PARAMETER_BUFFER = 0x80EE
    const val GL_PARAMETER_BUFFER_BINDING = 0x80EF
    const val GL_CONTEXT_FLAG_NO_ERROR_BIT = 0x00000008
    const val GL_VERTICES_SUBMITTED = 0x82EE
    const val GL_PRIMITIVES_SUBMITTED = 0x82EF
    const val GL_VERTEX_SHADER_INVOCATIONS = 0x82F0
    const val GL_TESS_CONTROL_SHADER_PATCHES = 0x82F1
    const val GL_TESS_EVALUATION_SHADER_INVOCATIONS = 0x82F2
    const val GL_GEOMETRY_SHADER_PRIMITIVES_EMITTED = 0x82F3
    const val GL_FRAGMENT_SHADER_INVOCATIONS = 0x82F4
    const val GL_COMPUTE_SHADER_INVOCATIONS = 0x82F5
    const val GL_CLIPPING_INPUT_PRIMITIVES = 0x82F6
    const val GL_CLIPPING_OUTPUT_PRIMITIVES = 0x82F7
    const val GL_POLYGON_OFFSET_CLAMP = 0x8E1B
    const val GL_SPIR_V_EXTENSIONS = 0x9553
    const val GL_NUM_SPIR_V_EXTENSIONS = 0x9554
    const val GL_TEXTURE_MAX_ANISOTROPY = 0x84FE
    const val GL_MAX_TEXTURE_MAX_ANISOTROPY = 0x84FF
    const val GL_TRANSFORM_FEEDBACK_OVERFLOW = 0x82EC
    const val GL_TRANSFORM_FEEDBACK_STREAM_OVERFLOW = 0x82ED
    const val GL_VERSION_1_0 = 1
    const val GL_VERSION_1_1 = 1
    const val GL_VERSION_1_2 = 1
    const val GL_VERSION_1_3 = 1
    const val GL_VERSION_1_4 = 1
    const val GL_VERSION_1_5 = 1
    const val GL_VERSION_2_0 = 1
    const val GL_VERSION_2_1 = 1
    const val GL_VERSION_3_0 = 1
    const val GL_VERSION_3_1 = 1
    const val GL_VERSION_3_2 = 1
    const val GL_VERSION_3_3 = 1
    const val GL_VERSION_4_0 = 1
    const val GL_VERSION_4_1 = 1
    const val GL_VERSION_4_2 = 1
    const val GL_VERSION_4_3 = 1
    const val GL_VERSION_4_4 = 1
    const val GL_VERSION_4_5 = 1
    const val GL_VERSION_4_6 = 1
    const val GL_MULTISAMPLE_3DFX = 0x86B2
    const val GL_SAMPLE_BUFFERS_3DFX = 0x86B3
    const val GL_SAMPLES_3DFX = 0x86B4
    const val GL_MULTISAMPLE_BIT_3DFX = 0x20000000
    const val GL_COMPRESSED_RGB_FXT1_3DFX = 0x86B0
    const val GL_COMPRESSED_RGBA_FXT1_3DFX = 0x86B1
    const val GL_FACTOR_MIN_AMD = 0x901C
    const val GL_FACTOR_MAX_AMD = 0x901D
    const val GL_MAX_DEBUG_MESSAGE_LENGTH_AMD = 0x9143
    const val GL_MAX_DEBUG_LOGGED_MESSAGES_AMD = 0x9144
    const val GL_DEBUG_LOGGED_MESSAGES_AMD = 0x9145
    const val GL_DEBUG_SEVERITY_HIGH_AMD = 0x9146
    const val GL_DEBUG_SEVERITY_MEDIUM_AMD = 0x9147
    const val GL_DEBUG_SEVERITY_LOW_AMD = 0x9148
    const val GL_DEBUG_CATEGORY_API_ERROR_AMD = 0x9149
    const val GL_DEBUG_CATEGORY_WINDOW_SYSTEM_AMD = 0x914A
    const val GL_DEBUG_CATEGORY_DEPRECATION_AMD = 0x914B
    const val GL_DEBUG_CATEGORY_UNDEFINED_BEHAVIOR_AMD = 0x914C
    const val GL_DEBUG_CATEGORY_PERFORMANCE_AMD = 0x914D
    const val GL_DEBUG_CATEGORY_SHADER_COMPILER_AMD = 0x914E
    const val GL_DEBUG_CATEGORY_APPLICATION_AMD = 0x914F
    const val GL_DEBUG_CATEGORY_OTHER_AMD = 0x9150
    const val GL_DEPTH_CLAMP_NEAR_AMD = 0x901E
    const val GL_DEPTH_CLAMP_FAR_AMD = 0x901F
    const val GL_RENDERBUFFER_STORAGE_SAMPLES_AMD = 0x91B2
    const val GL_MAX_COLOR_FRAMEBUFFER_SAMPLES_AMD = 0x91B3
    const val GL_MAX_COLOR_FRAMEBUFFER_STORAGE_SAMPLES_AMD = 0x91B4
    const val GL_MAX_DEPTH_STENCIL_FRAMEBUFFER_SAMPLES_AMD = 0x91B5
    const val GL_NUM_SUPPORTED_MULTISAMPLE_MODES_AMD = 0x91B6
    const val GL_SUPPORTED_MULTISAMPLE_MODES_AMD = 0x91B7
    const val GL_SUBSAMPLE_DISTANCE_AMD = 0x883F
    const val GL_PIXELS_PER_SAMPLE_PATTERN_X_AMD = 0x91AE
    const val GL_PIXELS_PER_SAMPLE_PATTERN_Y_AMD = 0x91AF
    const val GL_ALL_PIXELS_AMD = 0xFFFFFFFF
    const val GL_FLOAT16_NV = 0x8FF8
    const val GL_FLOAT16_VEC2_NV = 0x8FF9
    const val GL_FLOAT16_VEC3_NV = 0x8FFA
    const val GL_FLOAT16_VEC4_NV = 0x8FFB
    const val GL_FLOAT16_MAT2_AMD = 0x91C5
    const val GL_FLOAT16_MAT3_AMD = 0x91C6
    const val GL_FLOAT16_MAT4_AMD = 0x91C7
    const val GL_FLOAT16_MAT2x3_AMD = 0x91C8
    const val GL_FLOAT16_MAT2x4_AMD = 0x91C9
    const val GL_FLOAT16_MAT3x2_AMD = 0x91CA
    const val GL_FLOAT16_MAT3x4_AMD = 0x91CB
    const val GL_FLOAT16_MAT4x2_AMD = 0x91CC
    const val GL_FLOAT16_MAT4x3_AMD = 0x91CD
    const val GL_INT64_NV = 0x140E
    const val GL_UNSIGNED_INT64_NV = 0x140F
    const val GL_INT8_NV = 0x8FE0
    const val GL_INT8_VEC2_NV = 0x8FE1
    const val GL_INT8_VEC3_NV = 0x8FE2
    const val GL_INT8_VEC4_NV = 0x8FE3
    const val GL_INT16_NV = 0x8FE4
    const val GL_INT16_VEC2_NV = 0x8FE5
    const val GL_INT16_VEC3_NV = 0x8FE6
    const val GL_INT16_VEC4_NV = 0x8FE7
    const val GL_INT64_VEC2_NV = 0x8FE9
    const val GL_INT64_VEC3_NV = 0x8FEA
    const val GL_INT64_VEC4_NV = 0x8FEB
    const val GL_UNSIGNED_INT8_NV = 0x8FEC
    const val GL_UNSIGNED_INT8_VEC2_NV = 0x8FED
    const val GL_UNSIGNED_INT8_VEC3_NV = 0x8FEE
    const val GL_UNSIGNED_INT8_VEC4_NV = 0x8FEF
    const val GL_UNSIGNED_INT16_NV = 0x8FF0
    const val GL_UNSIGNED_INT16_VEC2_NV = 0x8FF1
    const val GL_UNSIGNED_INT16_VEC3_NV = 0x8FF2
    const val GL_UNSIGNED_INT16_VEC4_NV = 0x8FF3
    const val GL_UNSIGNED_INT64_VEC2_NV = 0x8FF5
    const val GL_UNSIGNED_INT64_VEC3_NV = 0x8FF6
    const val GL_UNSIGNED_INT64_VEC4_NV = 0x8FF7
    const val GL_VERTEX_ELEMENT_SWIZZLE_AMD = 0x91A4
    const val GL_VERTEX_ID_SWIZZLE_AMD = 0x91A5
    const val GL_DATA_BUFFER_AMD = 0x9151
    const val GL_PERFORMANCE_MONITOR_AMD = 0x9152
    const val GL_QUERY_OBJECT_AMD = 0x9153
    const val GL_VERTEX_ARRAY_OBJECT_AMD = 0x9154
    const val GL_SAMPLER_OBJECT_AMD = 0x9155
    const val GL_OCCLUSION_QUERY_EVENT_MASK_AMD = 0x874F
    const val GL_QUERY_DEPTH_PASS_EVENT_BIT_AMD = 0x00000001
    const val GL_QUERY_DEPTH_FAIL_EVENT_BIT_AMD = 0x00000002
    const val GL_QUERY_STENCIL_FAIL_EVENT_BIT_AMD = 0x00000004
    const val GL_QUERY_DEPTH_BOUNDS_FAIL_EVENT_BIT_AMD = 0x00000008
    const val GL_QUERY_ALL_EVENT_BITS_AMD = 0xFFFFFFFF
    const val GL_COUNTER_TYPE_AMD = 0x8BC0
    const val GL_COUNTER_RANGE_AMD = 0x8BC1
    const val GL_UNSIGNED_INT64_AMD = 0x8BC2
    const val GL_PERCENTAGE_AMD = 0x8BC3
    const val GL_PERFMON_RESULT_AVAILABLE_AMD = 0x8BC4
    const val GL_PERFMON_RESULT_SIZE_AMD = 0x8BC5
    const val GL_PERFMON_RESULT_AMD = 0x8BC6
    const val GL_EXTERNAL_VIRTUAL_MEMORY_BUFFER_AMD = 0x9160
    const val GL_QUERY_BUFFER_AMD = 0x9192
    const val GL_QUERY_BUFFER_BINDING_AMD = 0x9193
    const val GL_QUERY_RESULT_NO_WAIT_AMD = 0x9194
    const val GL_VIRTUAL_PAGE_SIZE_X_AMD = 0x9195
    const val GL_VIRTUAL_PAGE_SIZE_Y_AMD = 0x9196
    const val GL_VIRTUAL_PAGE_SIZE_Z_AMD = 0x9197
    const val GL_MAX_SPARSE_TEXTURE_SIZE_AMD = 0x9198
    const val GL_MAX_SPARSE_3D_TEXTURE_SIZE_AMD = 0x9199
    const val GL_MAX_SPARSE_ARRAY_TEXTURE_LAYERS = 0x919A
    const val GL_MIN_SPARSE_LEVEL_AMD = 0x919B
    const val GL_MIN_LOD_WARNING_AMD = 0x919C
    const val GL_TEXTURE_STORAGE_SPARSE_BIT_AMD = 0x00000001
    const val GL_SET_AMD = 0x874A
    const val GL_REPLACE_VALUE_AMD = 0x874B
    const val GL_STENCIL_OP_VALUE_AMD = 0x874C
    const val GL_STENCIL_BACK_OP_VALUE_AMD = 0x874D
    const val GL_STREAM_RASTERIZATION_AMD = 0x91A0
    const val GL_SAMPLER_BUFFER_AMD = 0x9001
    const val GL_INT_SAMPLER_BUFFER_AMD = 0x9002
    const val GL_UNSIGNED_INT_SAMPLER_BUFFER_AMD = 0x9003
    const val GL_TESSELLATION_MODE_AMD = 0x9004
    const val GL_TESSELLATION_FACTOR_AMD = 0x9005
    const val GL_DISCRETE_AMD = 0x9006
    const val GL_CONTINUOUS_AMD = 0x9007
    const val GL_AUX_DEPTH_STENCIL_APPLE = 0x8A14
    const val GL_UNPACK_CLIENT_STORAGE_APPLE = 0x85B2
    const val GL_ELEMENT_ARRAY_APPLE = 0x8A0C
    const val GL_ELEMENT_ARRAY_TYPE_APPLE = 0x8A0D
    const val GL_ELEMENT_ARRAY_POINTER_APPLE = 0x8A0E
    const val GL_DRAW_PIXELS_APPLE = 0x8A0A
    const val GL_FENCE_APPLE = 0x8A0B
    const val GL_HALF_APPLE = 0x140B
    const val GL_RGBA_FLOAT32_APPLE = 0x8814
    const val GL_RGB_FLOAT32_APPLE = 0x8815
    const val GL_ALPHA_FLOAT32_APPLE = 0x8816
    const val GL_INTENSITY_FLOAT32_APPLE = 0x8817
    const val GL_LUMINANCE_FLOAT32_APPLE = 0x8818
    const val GL_LUMINANCE_ALPHA_FLOAT32_APPLE = 0x8819
    const val GL_RGBA_FLOAT16_APPLE = 0x881A
    const val GL_RGB_FLOAT16_APPLE = 0x881B
    const val GL_ALPHA_FLOAT16_APPLE = 0x881C
    const val GL_INTENSITY_FLOAT16_APPLE = 0x881D
    const val GL_LUMINANCE_FLOAT16_APPLE = 0x881E
    const val GL_LUMINANCE_ALPHA_FLOAT16_APPLE = 0x881F
    const val GL_COLOR_FLOAT_APPLE = 0x8A0F
    const val GL_BUFFER_SERIALIZED_MODIFY_APPLE = 0x8A12
    const val GL_BUFFER_FLUSHING_UNMAP_APPLE = 0x8A13
    const val GL_BUFFER_OBJECT_APPLE = 0x85B3
    const val GL_RELEASED_APPLE = 0x8A19
    const val GL_VOLATILE_APPLE = 0x8A1A
    const val GL_RETAINED_APPLE = 0x8A1B
    const val GL_UNDEFINED_APPLE = 0x8A1C
    const val GL_PURGEABLE_APPLE = 0x8A1D
    const val GL_RGB_422_APPLE = 0x8A1F
    const val GL_UNSIGNED_SHORT_8_8_APPLE = 0x85BA
    const val GL_UNSIGNED_SHORT_8_8_REV_APPLE = 0x85BB
    const val GL_RGB_RAW_422_APPLE = 0x8A51
    const val GL_PACK_ROW_BYTES_APPLE = 0x8A15
    const val GL_UNPACK_ROW_BYTES_APPLE = 0x8A16
    const val GL_LIGHT_MODEL_SPECULAR_VECTOR_APPLE = 0x85B0
    const val GL_TEXTURE_RANGE_LENGTH_APPLE = 0x85B7
    const val GL_TEXTURE_RANGE_POINTER_APPLE = 0x85B8
    const val GL_TEXTURE_STORAGE_HINT_APPLE = 0x85BC
    const val GL_STORAGE_PRIVATE_APPLE = 0x85BD
    const val GL_STORAGE_CACHED_APPLE = 0x85BE
    const val GL_STORAGE_SHARED_APPLE = 0x85BF
    const val GL_TRANSFORM_HINT_APPLE = 0x85B1
    const val GL_VERTEX_ARRAY_BINDING_APPLE = 0x85B5
    const val GL_VERTEX_ARRAY_RANGE_APPLE = 0x851D
    const val GL_VERTEX_ARRAY_RANGE_LENGTH_APPLE = 0x851E
    const val GL_VERTEX_ARRAY_STORAGE_HINT_APPLE = 0x851F
    const val GL_VERTEX_ARRAY_RANGE_POINTER_APPLE = 0x8521
    const val GL_STORAGE_CLIENT_APPLE = 0x85B4
    const val GL_VERTEX_ATTRIB_MAP1_APPLE = 0x8A00
    const val GL_VERTEX_ATTRIB_MAP2_APPLE = 0x8A01
    const val GL_VERTEX_ATTRIB_MAP1_SIZE_APPLE = 0x8A02
    const val GL_VERTEX_ATTRIB_MAP1_COEFF_APPLE = 0x8A03
    const val GL_VERTEX_ATTRIB_MAP1_ORDER_APPLE = 0x8A04
    const val GL_VERTEX_ATTRIB_MAP1_DOMAIN_APPLE = 0x8A05
    const val GL_VERTEX_ATTRIB_MAP2_SIZE_APPLE = 0x8A06
    const val GL_VERTEX_ATTRIB_MAP2_COEFF_APPLE = 0x8A07
    const val GL_VERTEX_ATTRIB_MAP2_ORDER_APPLE = 0x8A08
    const val GL_VERTEX_ATTRIB_MAP2_DOMAIN_APPLE = 0x8A09
    const val GL_YCBCR_422_APPLE = 0x85B9
    const val GL_PRIMITIVE_BOUNDING_BOX_ARB = 0x92BE
    const val GL_MULTISAMPLE_LINE_WIDTH_RANGE_ARB = 0x9381
    const val GL_MULTISAMPLE_LINE_WIDTH_GRANULARITY_ARB = 0x9382
    const val GL_UNSIGNED_INT64_ARB = 0x140F
    const val GL_SYNC_CL_EVENT_ARB = 0x8240
    const val GL_SYNC_CL_EVENT_COMPLETE_ARB = 0x8241
    const val GL_RGBA_FLOAT_MODE_ARB = 0x8820
    const val GL_CLAMP_VERTEX_COLOR_ARB = 0x891A
    const val GL_CLAMP_FRAGMENT_COLOR_ARB = 0x891B
    const val GL_CLAMP_READ_COLOR_ARB = 0x891C
    const val GL_FIXED_ONLY_ARB = 0x891D
    const val GL_MAX_COMPUTE_VARIABLE_GROUP_INVOCATIONS_ARB = 0x9344
    const val GL_MAX_COMPUTE_FIXED_GROUP_INVOCATIONS_ARB = 0x90EB
    const val GL_MAX_COMPUTE_VARIABLE_GROUP_SIZE_ARB = 0x9345
    const val GL_MAX_COMPUTE_FIXED_GROUP_SIZE_ARB = 0x91BF
    const val GL_DEBUG_OUTPUT_SYNCHRONOUS_ARB = 0x8242
    const val GL_DEBUG_NEXT_LOGGED_MESSAGE_LENGTH_ARB = 0x8243
    const val GL_DEBUG_CALLBACK_FUNCTION_ARB = 0x8244
    const val GL_DEBUG_CALLBACK_USER_PARAM_ARB = 0x8245
    const val GL_DEBUG_SOURCE_API_ARB = 0x8246
    const val GL_DEBUG_SOURCE_WINDOW_SYSTEM_ARB = 0x8247
    const val GL_DEBUG_SOURCE_SHADER_COMPILER_ARB = 0x8248
    const val GL_DEBUG_SOURCE_THIRD_PARTY_ARB = 0x8249
    const val GL_DEBUG_SOURCE_APPLICATION_ARB = 0x824A
    const val GL_DEBUG_SOURCE_OTHER_ARB = 0x824B
    const val GL_DEBUG_TYPE_ERROR_ARB = 0x824C
    const val GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR_ARB = 0x824D
    const val GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR_ARB = 0x824E
    const val GL_DEBUG_TYPE_PORTABILITY_ARB = 0x824F
    const val GL_DEBUG_TYPE_PERFORMANCE_ARB = 0x8250
    const val GL_DEBUG_TYPE_OTHER_ARB = 0x8251
    const val GL_MAX_DEBUG_MESSAGE_LENGTH_ARB = 0x9143
    const val GL_MAX_DEBUG_LOGGED_MESSAGES_ARB = 0x9144
    const val GL_DEBUG_LOGGED_MESSAGES_ARB = 0x9145
    const val GL_DEBUG_SEVERITY_HIGH_ARB = 0x9146
    const val GL_DEBUG_SEVERITY_MEDIUM_ARB = 0x9147
    const val GL_DEBUG_SEVERITY_LOW_ARB = 0x9148
    const val GL_DEPTH_COMPONENT16_ARB = 0x81A5
    const val GL_DEPTH_COMPONENT24_ARB = 0x81A6
    const val GL_DEPTH_COMPONENT32_ARB = 0x81A7
    const val GL_TEXTURE_DEPTH_SIZE_ARB = 0x884A
    const val GL_DEPTH_TEXTURE_MODE_ARB = 0x884B
    const val GL_MAX_DRAW_BUFFERS_ARB = 0x8824
    const val GL_DRAW_BUFFER0_ARB = 0x8825
    const val GL_DRAW_BUFFER1_ARB = 0x8826
    const val GL_DRAW_BUFFER2_ARB = 0x8827
    const val GL_DRAW_BUFFER3_ARB = 0x8828
    const val GL_DRAW_BUFFER4_ARB = 0x8829
    const val GL_DRAW_BUFFER5_ARB = 0x882A
    const val GL_DRAW_BUFFER6_ARB = 0x882B
    const val GL_DRAW_BUFFER7_ARB = 0x882C
    const val GL_DRAW_BUFFER8_ARB = 0x882D
    const val GL_DRAW_BUFFER9_ARB = 0x882E
    const val GL_DRAW_BUFFER10_ARB = 0x882F
    const val GL_DRAW_BUFFER11_ARB = 0x8830
    const val GL_DRAW_BUFFER12_ARB = 0x8831
    const val GL_DRAW_BUFFER13_ARB = 0x8832
    const val GL_DRAW_BUFFER14_ARB = 0x8833
    const val GL_DRAW_BUFFER15_ARB = 0x8834
    const val GL_FRAGMENT_PROGRAM_ARB = 0x8804
    const val GL_PROGRAM_FORMAT_ASCII_ARB = 0x8875
    const val GL_PROGRAM_LENGTH_ARB = 0x8627
    const val GL_PROGRAM_FORMAT_ARB = 0x8876
    const val GL_PROGRAM_BINDING_ARB = 0x8677
    const val GL_PROGRAM_INSTRUCTIONS_ARB = 0x88A0
    const val GL_MAX_PROGRAM_INSTRUCTIONS_ARB = 0x88A1
    const val GL_PROGRAM_NATIVE_INSTRUCTIONS_ARB = 0x88A2
    const val GL_MAX_PROGRAM_NATIVE_INSTRUCTIONS_ARB = 0x88A3
    const val GL_PROGRAM_TEMPORARIES_ARB = 0x88A4
    const val GL_MAX_PROGRAM_TEMPORARIES_ARB = 0x88A5
    const val GL_PROGRAM_NATIVE_TEMPORARIES_ARB = 0x88A6
    const val GL_MAX_PROGRAM_NATIVE_TEMPORARIES_ARB = 0x88A7
    const val GL_PROGRAM_PARAMETERS_ARB = 0x88A8
    const val GL_MAX_PROGRAM_PARAMETERS_ARB = 0x88A9
    const val GL_PROGRAM_NATIVE_PARAMETERS_ARB = 0x88AA
    const val GL_MAX_PROGRAM_NATIVE_PARAMETERS_ARB = 0x88AB
    const val GL_PROGRAM_ATTRIBS_ARB = 0x88AC
    const val GL_MAX_PROGRAM_ATTRIBS_ARB = 0x88AD
    const val GL_PROGRAM_NATIVE_ATTRIBS_ARB = 0x88AE
    const val GL_MAX_PROGRAM_NATIVE_ATTRIBS_ARB = 0x88AF
    const val GL_MAX_PROGRAM_LOCAL_PARAMETERS_ARB = 0x88B4
    const val GL_MAX_PROGRAM_ENV_PARAMETERS_ARB = 0x88B5
    const val GL_PROGRAM_UNDER_NATIVE_LIMITS_ARB = 0x88B6
    const val GL_PROGRAM_ALU_INSTRUCTIONS_ARB = 0x8805
    const val GL_PROGRAM_TEX_INSTRUCTIONS_ARB = 0x8806
    const val GL_PROGRAM_TEX_INDIRECTIONS_ARB = 0x8807
    const val GL_PROGRAM_NATIVE_ALU_INSTRUCTIONS_ARB = 0x8808
    const val GL_PROGRAM_NATIVE_TEX_INSTRUCTIONS_ARB = 0x8809
    const val GL_PROGRAM_NATIVE_TEX_INDIRECTIONS_ARB = 0x880A
    const val GL_MAX_PROGRAM_ALU_INSTRUCTIONS_ARB = 0x880B
    const val GL_MAX_PROGRAM_TEX_INSTRUCTIONS_ARB = 0x880C
    const val GL_MAX_PROGRAM_TEX_INDIRECTIONS_ARB = 0x880D
    const val GL_MAX_PROGRAM_NATIVE_ALU_INSTRUCTIONS_ARB = 0x880E
    const val GL_MAX_PROGRAM_NATIVE_TEX_INSTRUCTIONS_ARB = 0x880F
    const val GL_MAX_PROGRAM_NATIVE_TEX_INDIRECTIONS_ARB = 0x8810
    const val GL_PROGRAM_STRING_ARB = 0x8628
    const val GL_PROGRAM_ERROR_POSITION_ARB = 0x864B
    const val GL_CURRENT_MATRIX_ARB = 0x8641
    const val GL_TRANSPOSE_CURRENT_MATRIX_ARB = 0x88B7
    const val GL_CURRENT_MATRIX_STACK_DEPTH_ARB = 0x8640
    const val GL_MAX_PROGRAM_MATRICES_ARB = 0x862F
    const val GL_MAX_PROGRAM_MATRIX_STACK_DEPTH_ARB = 0x862E
    const val GL_MAX_TEXTURE_COORDS_ARB = 0x8871
    const val GL_MAX_TEXTURE_IMAGE_UNITS_ARB = 0x8872
    const val GL_PROGRAM_ERROR_STRING_ARB = 0x8874
    const val GL_MATRIX0_ARB = 0x88C0
    const val GL_MATRIX1_ARB = 0x88C1
    const val GL_MATRIX2_ARB = 0x88C2
    const val GL_MATRIX3_ARB = 0x88C3
    const val GL_MATRIX4_ARB = 0x88C4
    const val GL_MATRIX5_ARB = 0x88C5
    const val GL_MATRIX6_ARB = 0x88C6
    const val GL_MATRIX7_ARB = 0x88C7
    const val GL_MATRIX8_ARB = 0x88C8
    const val GL_MATRIX9_ARB = 0x88C9
    const val GL_MATRIX10_ARB = 0x88CA
    const val GL_MATRIX11_ARB = 0x88CB
    const val GL_MATRIX12_ARB = 0x88CC
    const val GL_MATRIX13_ARB = 0x88CD
    const val GL_MATRIX14_ARB = 0x88CE
    const val GL_MATRIX15_ARB = 0x88CF
    const val GL_MATRIX16_ARB = 0x88D0
    const val GL_MATRIX17_ARB = 0x88D1
    const val GL_MATRIX18_ARB = 0x88D2
    const val GL_MATRIX19_ARB = 0x88D3
    const val GL_MATRIX20_ARB = 0x88D4
    const val GL_MATRIX21_ARB = 0x88D5
    const val GL_MATRIX22_ARB = 0x88D6
    const val GL_MATRIX23_ARB = 0x88D7
    const val GL_MATRIX24_ARB = 0x88D8
    const val GL_MATRIX25_ARB = 0x88D9
    const val GL_MATRIX26_ARB = 0x88DA
    const val GL_MATRIX27_ARB = 0x88DB
    const val GL_MATRIX28_ARB = 0x88DC
    const val GL_MATRIX29_ARB = 0x88DD
    const val GL_MATRIX30_ARB = 0x88DE
    const val GL_MATRIX31_ARB = 0x88DF
    const val GL_FRAGMENT_SHADER_ARB = 0x8B30
    const val GL_MAX_FRAGMENT_UNIFORM_COMPONENTS_ARB = 0x8B49
    const val GL_FRAGMENT_SHADER_DERIVATIVE_HINT_ARB = 0x8B8B
    const val GL_INDEX = 0x8222
    const val GL_LINES_ADJACENCY_ARB = 0x000A
    const val GL_LINE_STRIP_ADJACENCY_ARB = 0x000B
    const val GL_TRIANGLES_ADJACENCY_ARB = 0x000C
    const val GL_TRIANGLE_STRIP_ADJACENCY_ARB = 0x000D
    const val GL_PROGRAM_POINT_SIZE_ARB = 0x8642
    const val GL_MAX_GEOMETRY_TEXTURE_IMAGE_UNITS_ARB = 0x8C29
    const val GL_FRAMEBUFFER_ATTACHMENT_LAYERED_ARB = 0x8DA7
    const val GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS_ARB = 0x8DA8
    const val GL_FRAMEBUFFER_INCOMPLETE_LAYER_COUNT_ARB = 0x8DA9
    const val GL_GEOMETRY_SHADER_ARB = 0x8DD9
    const val GL_GEOMETRY_VERTICES_OUT_ARB = 0x8DDA
    const val GL_GEOMETRY_INPUT_TYPE_ARB = 0x8DDB
    const val GL_GEOMETRY_OUTPUT_TYPE_ARB = 0x8DDC
    const val GL_MAX_GEOMETRY_VARYING_COMPONENTS_ARB = 0x8DDD
    const val GL_MAX_VERTEX_VARYING_COMPONENTS_ARB = 0x8DDE
    const val GL_MAX_GEOMETRY_UNIFORM_COMPONENTS_ARB = 0x8DDF
    const val GL_MAX_GEOMETRY_OUTPUT_VERTICES_ARB = 0x8DE0
    const val GL_MAX_GEOMETRY_TOTAL_OUTPUT_COMPONENTS_ARB = 0x8DE1
    const val GL_SHADER_BINARY_FORMAT_SPIR_V_ARB = 0x9551
    const val GL_SPIR_V_BINARY_ARB = 0x9552
    const val GL_INT64_ARB = 0x140E
    const val GL_INT64_VEC2_ARB = 0x8FE9
    const val GL_INT64_VEC3_ARB = 0x8FEA
    const val GL_INT64_VEC4_ARB = 0x8FEB
    const val GL_UNSIGNED_INT64_VEC2_ARB = 0x8FF5
    const val GL_UNSIGNED_INT64_VEC3_ARB = 0x8FF6
    const val GL_UNSIGNED_INT64_VEC4_ARB = 0x8FF7
    const val GL_HALF_FLOAT_ARB = 0x140B
    const val GL_CONVOLUTION_BORDER_MODE = 0x8013
    const val GL_CONVOLUTION_FILTER_SCALE = 0x8014
    const val GL_CONVOLUTION_FILTER_BIAS = 0x8015
    const val GL_REDUCE = 0x8016
    const val GL_CONVOLUTION_FORMAT = 0x8017
    const val GL_CONVOLUTION_WIDTH = 0x8018
    const val GL_CONVOLUTION_HEIGHT = 0x8019
    const val GL_MAX_CONVOLUTION_WIDTH = 0x801A
    const val GL_MAX_CONVOLUTION_HEIGHT = 0x801B
    const val GL_POST_CONVOLUTION_RED_SCALE = 0x801C
    const val GL_POST_CONVOLUTION_GREEN_SCALE = 0x801D
    const val GL_POST_CONVOLUTION_BLUE_SCALE = 0x801E
    const val GL_POST_CONVOLUTION_ALPHA_SCALE = 0x801F
    const val GL_POST_CONVOLUTION_RED_BIAS = 0x8020
    const val GL_POST_CONVOLUTION_GREEN_BIAS = 0x8021
    const val GL_POST_CONVOLUTION_BLUE_BIAS = 0x8022
    const val GL_POST_CONVOLUTION_ALPHA_BIAS = 0x8023
    const val GL_HISTOGRAM_WIDTH = 0x8026
    const val GL_HISTOGRAM_FORMAT = 0x8027
    const val GL_HISTOGRAM_RED_SIZE = 0x8028
    const val GL_HISTOGRAM_GREEN_SIZE = 0x8029
    const val GL_HISTOGRAM_BLUE_SIZE = 0x802A
    const val GL_HISTOGRAM_ALPHA_SIZE = 0x802B
    const val GL_HISTOGRAM_LUMINANCE_SIZE = 0x802C
    const val GL_HISTOGRAM_SINK = 0x802D
    const val GL_MINMAX_FORMAT = 0x802F
    const val GL_MINMAX_SINK = 0x8030
    const val GL_TABLE_TOO_LARGE = 0x8031
    const val GL_COLOR_MATRIX = 0x80B1
    const val GL_COLOR_MATRIX_STACK_DEPTH = 0x80B2
    const val GL_MAX_COLOR_MATRIX_STACK_DEPTH = 0x80B3
    const val GL_POST_COLOR_MATRIX_RED_SCALE = 0x80B4
    const val GL_POST_COLOR_MATRIX_GREEN_SCALE = 0x80B5
    const val GL_POST_COLOR_MATRIX_BLUE_SCALE = 0x80B6
    const val GL_POST_COLOR_MATRIX_ALPHA_SCALE = 0x80B7
    const val GL_POST_COLOR_MATRIX_RED_BIAS = 0x80B8
    const val GL_POST_COLOR_MATRIX_GREEN_BIAS = 0x80B9
    const val GL_POST_COLOR_MATRIX_BLUE_BIAS = 0x80BA
    const val GL_POST_COLOR_MATRIX_ALPHA_BIAS = 0x80BB
    const val GL_COLOR_TABLE_SCALE = 0x80D6
    const val GL_COLOR_TABLE_BIAS = 0x80D7
    const val GL_COLOR_TABLE_FORMAT = 0x80D8
    const val GL_COLOR_TABLE_WIDTH = 0x80D9
    const val GL_COLOR_TABLE_RED_SIZE = 0x80DA
    const val GL_COLOR_TABLE_GREEN_SIZE = 0x80DB
    const val GL_COLOR_TABLE_BLUE_SIZE = 0x80DC
    const val GL_COLOR_TABLE_ALPHA_SIZE = 0x80DD
    const val GL_COLOR_TABLE_LUMINANCE_SIZE = 0x80DE
    const val GL_COLOR_TABLE_INTENSITY_SIZE = 0x80DF
    const val GL_CONSTANT_BORDER = 0x8151
    const val GL_REPLICATE_BORDER = 0x8153
    const val GL_CONVOLUTION_BORDER_COLOR = 0x8154
    const val GL_PARAMETER_BUFFER_ARB = 0x80EE
    const val GL_PARAMETER_BUFFER_BINDING_ARB = 0x80EF
    const val GL_VERTEX_ATTRIB_ARRAY_DIVISOR_ARB = 0x88FE
    const val GL_SRGB_DECODE_ARB = 0x8299
    const val GL_VIEW_CLASS_EAC_R11 = 0x9383
    const val GL_VIEW_CLASS_EAC_RG11 = 0x9384
    const val GL_VIEW_CLASS_ETC2_RGB = 0x9385
    const val GL_VIEW_CLASS_ETC2_RGBA = 0x9386
    const val GL_VIEW_CLASS_ETC2_EAC_RGBA = 0x9387
    const val GL_VIEW_CLASS_ASTC_4x4_RGBA = 0x9388
    const val GL_VIEW_CLASS_ASTC_5x4_RGBA = 0x9389
    const val GL_VIEW_CLASS_ASTC_5x5_RGBA = 0x938A
    const val GL_VIEW_CLASS_ASTC_6x5_RGBA = 0x938B
    const val GL_VIEW_CLASS_ASTC_6x6_RGBA = 0x938C
    const val GL_VIEW_CLASS_ASTC_8x5_RGBA = 0x938D
    const val GL_VIEW_CLASS_ASTC_8x6_RGBA = 0x938E
    const val GL_VIEW_CLASS_ASTC_8x8_RGBA = 0x938F
    const val GL_VIEW_CLASS_ASTC_10x5_RGBA = 0x9390
    const val GL_VIEW_CLASS_ASTC_10x6_RGBA = 0x9391
    const val GL_VIEW_CLASS_ASTC_10x8_RGBA = 0x9392
    const val GL_VIEW_CLASS_ASTC_10x10_RGBA = 0x9393
    const val GL_VIEW_CLASS_ASTC_12x10_RGBA = 0x9394
    const val GL_VIEW_CLASS_ASTC_12x12_RGBA = 0x9395
    const val GL_MATRIX_PALETTE_ARB = 0x8840
    const val GL_MAX_MATRIX_PALETTE_STACK_DEPTH_ARB = 0x8841
    const val GL_MAX_PALETTE_MATRICES_ARB = 0x8842
    const val GL_CURRENT_PALETTE_MATRIX_ARB = 0x8843
    const val GL_MATRIX_INDEX_ARRAY_ARB = 0x8844
    const val GL_CURRENT_MATRIX_INDEX_ARB = 0x8845
    const val GL_MATRIX_INDEX_ARRAY_SIZE_ARB = 0x8846
    const val GL_MATRIX_INDEX_ARRAY_TYPE_ARB = 0x8847
    const val GL_MATRIX_INDEX_ARRAY_STRIDE_ARB = 0x8848
    const val GL_MATRIX_INDEX_ARRAY_POINTER_ARB = 0x8849
    const val GL_MULTISAMPLE_ARB = 0x809D
    const val GL_SAMPLE_ALPHA_TO_COVERAGE_ARB = 0x809E
    const val GL_SAMPLE_ALPHA_TO_ONE_ARB = 0x809F
    const val GL_SAMPLE_COVERAGE_ARB = 0x80A0
    const val GL_SAMPLE_BUFFERS_ARB = 0x80A8
    const val GL_SAMPLES_ARB = 0x80A9
    const val GL_SAMPLE_COVERAGE_VALUE_ARB = 0x80AA
    const val GL_SAMPLE_COVERAGE_INVERT_ARB = 0x80AB
    const val GL_MULTISAMPLE_BIT_ARB = 0x20000000
    const val GL_TEXTURE0_ARB = 0x84C0
    const val GL_TEXTURE1_ARB = 0x84C1
    const val GL_TEXTURE2_ARB = 0x84C2
    const val GL_TEXTURE3_ARB = 0x84C3
    const val GL_TEXTURE4_ARB = 0x84C4
    const val GL_TEXTURE5_ARB = 0x84C5
    const val GL_TEXTURE6_ARB = 0x84C6
    const val GL_TEXTURE7_ARB = 0x84C7
    const val GL_TEXTURE8_ARB = 0x84C8
    const val GL_TEXTURE9_ARB = 0x84C9
    const val GL_TEXTURE10_ARB = 0x84CA
    const val GL_TEXTURE11_ARB = 0x84CB
    const val GL_TEXTURE12_ARB = 0x84CC
    const val GL_TEXTURE13_ARB = 0x84CD
    const val GL_TEXTURE14_ARB = 0x84CE
    const val GL_TEXTURE15_ARB = 0x84CF
    const val GL_TEXTURE16_ARB = 0x84D0
    const val GL_TEXTURE17_ARB = 0x84D1
    const val GL_TEXTURE18_ARB = 0x84D2
    const val GL_TEXTURE19_ARB = 0x84D3
    const val GL_TEXTURE20_ARB = 0x84D4
    const val GL_TEXTURE21_ARB = 0x84D5
    const val GL_TEXTURE22_ARB = 0x84D6
    const val GL_TEXTURE23_ARB = 0x84D7
    const val GL_TEXTURE24_ARB = 0x84D8
    const val GL_TEXTURE25_ARB = 0x84D9
    const val GL_TEXTURE26_ARB = 0x84DA
    const val GL_TEXTURE27_ARB = 0x84DB
    const val GL_TEXTURE28_ARB = 0x84DC
    const val GL_TEXTURE29_ARB = 0x84DD
    const val GL_TEXTURE30_ARB = 0x84DE
    const val GL_TEXTURE31_ARB = 0x84DF
    const val GL_ACTIVE_TEXTURE_ARB = 0x84E0
    const val GL_CLIENT_ACTIVE_TEXTURE_ARB = 0x84E1
    const val GL_MAX_TEXTURE_UNITS_ARB = 0x84E2
    const val GL_QUERY_COUNTER_BITS_ARB = 0x8864
    const val GL_CURRENT_QUERY_ARB = 0x8865
    const val GL_QUERY_RESULT_ARB = 0x8866
    const val GL_QUERY_RESULT_AVAILABLE_ARB = 0x8867
    const val GL_SAMPLES_PASSED_ARB = 0x8914
    const val GL_MAX_SHADER_COMPILER_THREADS_ARB = 0x91B0
    const val GL_COMPLETION_STATUS_ARB = 0x91B1
    const val GL_VERTICES_SUBMITTED_ARB = 0x82EE
    const val GL_PRIMITIVES_SUBMITTED_ARB = 0x82EF
    const val GL_VERTEX_SHADER_INVOCATIONS_ARB = 0x82F0
    const val GL_TESS_CONTROL_SHADER_PATCHES_ARB = 0x82F1
    const val GL_TESS_EVALUATION_SHADER_INVOCATIONS_ARB = 0x82F2
    const val GL_GEOMETRY_SHADER_PRIMITIVES_EMITTED_ARB = 0x82F3
    const val GL_FRAGMENT_SHADER_INVOCATIONS_ARB = 0x82F4
    const val GL_COMPUTE_SHADER_INVOCATIONS_ARB = 0x82F5
    const val GL_CLIPPING_INPUT_PRIMITIVES_ARB = 0x82F6
    const val GL_CLIPPING_OUTPUT_PRIMITIVES_ARB = 0x82F7
    const val GL_PIXEL_PACK_BUFFER_ARB = 0x88EB
    const val GL_PIXEL_UNPACK_BUFFER_ARB = 0x88EC
    const val GL_PIXEL_PACK_BUFFER_BINDING_ARB = 0x88ED
    const val GL_PIXEL_UNPACK_BUFFER_BINDING_ARB = 0x88EF
    const val GL_POINT_SIZE_MIN_ARB = 0x8126
    const val GL_POINT_SIZE_MAX_ARB = 0x8127
    const val GL_POINT_FADE_THRESHOLD_SIZE_ARB = 0x8128
    const val GL_POINT_DISTANCE_ATTENUATION_ARB = 0x8129
    const val GL_POINT_SPRITE_ARB = 0x8861
    const val GL_COORD_REPLACE_ARB = 0x8862
    const val GL_CONTEXT_FLAG_ROBUST_ACCESS_BIT_ARB = 0x00000004
    const val GL_LOSE_CONTEXT_ON_RESET_ARB = 0x8252
    const val GL_GUILTY_CONTEXT_RESET_ARB = 0x8253
    const val GL_INNOCENT_CONTEXT_RESET_ARB = 0x8254
    const val GL_UNKNOWN_CONTEXT_RESET_ARB = 0x8255
    const val GL_RESET_NOTIFICATION_STRATEGY_ARB = 0x8256
    const val GL_NO_RESET_NOTIFICATION_ARB = 0x8261
    const val GL_SAMPLE_LOCATION_SUBPIXEL_BITS_ARB = 0x933D
    const val GL_SAMPLE_LOCATION_PIXEL_GRID_WIDTH_ARB = 0x933E
    const val GL_SAMPLE_LOCATION_PIXEL_GRID_HEIGHT_ARB = 0x933F
    const val GL_PROGRAMMABLE_SAMPLE_LOCATION_TABLE_SIZE_ARB = 0x9340
    const val GL_SAMPLE_LOCATION_ARB = 0x8E50
    const val GL_PROGRAMMABLE_SAMPLE_LOCATION_ARB = 0x9341
    const val GL_FRAMEBUFFER_PROGRAMMABLE_SAMPLE_LOCATIONS_ARB = 0x9342
    const val GL_FRAMEBUFFER_SAMPLE_LOCATION_PIXEL_GRID_ARB = 0x9343
    const val GL_SAMPLE_SHADING_ARB = 0x8C36
    const val GL_MIN_SAMPLE_SHADING_VALUE_ARB = 0x8C37
    const val GL_PROGRAM_OBJECT_ARB = 0x8B40
    const val GL_SHADER_OBJECT_ARB = 0x8B48
    const val GL_OBJECT_TYPE_ARB = 0x8B4E
    const val GL_OBJECT_SUBTYPE_ARB = 0x8B4F
    const val GL_FLOAT_VEC2_ARB = 0x8B50
    const val GL_FLOAT_VEC3_ARB = 0x8B51
    const val GL_FLOAT_VEC4_ARB = 0x8B52
    const val GL_INT_VEC2_ARB = 0x8B53
    const val GL_INT_VEC3_ARB = 0x8B54
    const val GL_INT_VEC4_ARB = 0x8B55
    const val GL_BOOL_ARB = 0x8B56
    const val GL_BOOL_VEC2_ARB = 0x8B57
    const val GL_BOOL_VEC3_ARB = 0x8B58
    const val GL_BOOL_VEC4_ARB = 0x8B59
    const val GL_FLOAT_MAT2_ARB = 0x8B5A
    const val GL_FLOAT_MAT3_ARB = 0x8B5B
    const val GL_FLOAT_MAT4_ARB = 0x8B5C
    const val GL_SAMPLER_1D_ARB = 0x8B5D
    const val GL_SAMPLER_2D_ARB = 0x8B5E
    const val GL_SAMPLER_3D_ARB = 0x8B5F
    const val GL_SAMPLER_CUBE_ARB = 0x8B60
    const val GL_SAMPLER_1D_SHADOW_ARB = 0x8B61
    const val GL_SAMPLER_2D_SHADOW_ARB = 0x8B62
    const val GL_SAMPLER_2D_RECT_ARB = 0x8B63
    const val GL_SAMPLER_2D_RECT_SHADOW_ARB = 0x8B64
    const val GL_OBJECT_DELETE_STATUS_ARB = 0x8B80
    const val GL_OBJECT_COMPILE_STATUS_ARB = 0x8B81
    const val GL_OBJECT_LINK_STATUS_ARB = 0x8B82
    const val GL_OBJECT_VALIDATE_STATUS_ARB = 0x8B83
    const val GL_OBJECT_INFO_LOG_LENGTH_ARB = 0x8B84
    const val GL_OBJECT_ATTACHED_OBJECTS_ARB = 0x8B85
    const val GL_OBJECT_ACTIVE_UNIFORMS_ARB = 0x8B86
    const val GL_OBJECT_ACTIVE_UNIFORM_MAX_LENGTH_ARB = 0x8B87
    const val GL_OBJECT_SHADER_SOURCE_LENGTH_ARB = 0x8B88
    const val GL_SHADING_LANGUAGE_VERSION_ARB = 0x8B8C
    const val GL_SHADER_INCLUDE_ARB = 0x8DAE
    const val GL_NAMED_STRING_LENGTH_ARB = 0x8DE9
    const val GL_NAMED_STRING_TYPE_ARB = 0x8DEA
    const val GL_TEXTURE_COMPARE_MODE_ARB = 0x884C
    const val GL_TEXTURE_COMPARE_FUNC_ARB = 0x884D
    const val GL_COMPARE_R_TO_TEXTURE_ARB = 0x884E
    const val GL_TEXTURE_COMPARE_FAIL_VALUE_ARB = 0x80BF
    const val GL_SPARSE_STORAGE_BIT_ARB = 0x0400
    const val GL_SPARSE_BUFFER_PAGE_SIZE_ARB = 0x82F8
    const val GL_TEXTURE_SPARSE_ARB = 0x91A6
    const val GL_VIRTUAL_PAGE_SIZE_INDEX_ARB = 0x91A7
    const val GL_NUM_SPARSE_LEVELS_ARB = 0x91AA
    const val GL_NUM_VIRTUAL_PAGE_SIZES_ARB = 0x91A8
    const val GL_VIRTUAL_PAGE_SIZE_X_ARB = 0x9195
    const val GL_VIRTUAL_PAGE_SIZE_Y_ARB = 0x9196
    const val GL_VIRTUAL_PAGE_SIZE_Z_ARB = 0x9197
    const val GL_MAX_SPARSE_TEXTURE_SIZE_ARB = 0x9198
    const val GL_MAX_SPARSE_3D_TEXTURE_SIZE_ARB = 0x9199
    const val GL_MAX_SPARSE_ARRAY_TEXTURE_LAYERS_ARB = 0x919A
    const val GL_SPARSE_TEXTURE_FULL_ARRAY_CUBE_MIPMAPS_ARB = 0x91A9
    const val GL_CLAMP_TO_BORDER_ARB = 0x812D
    const val GL_TEXTURE_BUFFER_ARB = 0x8C2A
    const val GL_MAX_TEXTURE_BUFFER_SIZE_ARB = 0x8C2B
    const val GL_TEXTURE_BINDING_BUFFER_ARB = 0x8C2C
    const val GL_TEXTURE_BUFFER_DATA_STORE_BINDING_ARB = 0x8C2D
    const val GL_TEXTURE_BUFFER_FORMAT_ARB = 0x8C2E
    const val GL_COMPRESSED_ALPHA_ARB = 0x84E9
    const val GL_COMPRESSED_LUMINANCE_ARB = 0x84EA
    const val GL_COMPRESSED_LUMINANCE_ALPHA_ARB = 0x84EB
    const val GL_COMPRESSED_INTENSITY_ARB = 0x84EC
    const val GL_COMPRESSED_RGB_ARB = 0x84ED
    const val GL_COMPRESSED_RGBA_ARB = 0x84EE
    const val GL_TEXTURE_COMPRESSION_HINT_ARB = 0x84EF
    const val GL_TEXTURE_COMPRESSED_IMAGE_SIZE_ARB = 0x86A0
    const val GL_TEXTURE_COMPRESSED_ARB = 0x86A1
    const val GL_NUM_COMPRESSED_TEXTURE_FORMATS_ARB = 0x86A2
    const val GL_COMPRESSED_TEXTURE_FORMATS_ARB = 0x86A3
    const val GL_COMPRESSED_RGBA_BPTC_UNORM_ARB = 0x8E8C
    const val GL_COMPRESSED_SRGB_ALPHA_BPTC_UNORM_ARB = 0x8E8D
    const val GL_COMPRESSED_RGB_BPTC_SIGNED_FLOAT_ARB = 0x8E8E
    const val GL_COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT_ARB = 0x8E8F
    const val GL_NORMAL_MAP_ARB = 0x8511
    const val GL_REFLECTION_MAP_ARB = 0x8512
    const val GL_TEXTURE_CUBE_MAP_ARB = 0x8513
    const val GL_TEXTURE_BINDING_CUBE_MAP_ARB = 0x8514
    const val GL_TEXTURE_CUBE_MAP_POSITIVE_X_ARB = 0x8515
    const val GL_TEXTURE_CUBE_MAP_NEGATIVE_X_ARB = 0x8516
    const val GL_TEXTURE_CUBE_MAP_POSITIVE_Y_ARB = 0x8517
    const val GL_TEXTURE_CUBE_MAP_NEGATIVE_Y_ARB = 0x8518
    const val GL_TEXTURE_CUBE_MAP_POSITIVE_Z_ARB = 0x8519
    const val GL_TEXTURE_CUBE_MAP_NEGATIVE_Z_ARB = 0x851A
    const val GL_PROXY_TEXTURE_CUBE_MAP_ARB = 0x851B
    const val GL_MAX_CUBE_MAP_TEXTURE_SIZE_ARB = 0x851C
    const val GL_TEXTURE_CUBE_MAP_ARRAY_ARB = 0x9009
    const val GL_TEXTURE_BINDING_CUBE_MAP_ARRAY_ARB = 0x900A
    const val GL_PROXY_TEXTURE_CUBE_MAP_ARRAY_ARB = 0x900B
    const val GL_SAMPLER_CUBE_MAP_ARRAY_ARB = 0x900C
    const val GL_SAMPLER_CUBE_MAP_ARRAY_SHADOW_ARB = 0x900D
    const val GL_INT_SAMPLER_CUBE_MAP_ARRAY_ARB = 0x900E
    const val GL_UNSIGNED_INT_SAMPLER_CUBE_MAP_ARRAY_ARB = 0x900F
    const val GL_COMBINE_ARB = 0x8570
    const val GL_COMBINE_RGB_ARB = 0x8571
    const val GL_COMBINE_ALPHA_ARB = 0x8572
    const val GL_SOURCE0_RGB_ARB = 0x8580
    const val GL_SOURCE1_RGB_ARB = 0x8581
    const val GL_SOURCE2_RGB_ARB = 0x8582
    const val GL_SOURCE0_ALPHA_ARB = 0x8588
    const val GL_SOURCE1_ALPHA_ARB = 0x8589
    const val GL_SOURCE2_ALPHA_ARB = 0x858A
    const val GL_OPERAND0_RGB_ARB = 0x8590
    const val GL_OPERAND1_RGB_ARB = 0x8591
    const val GL_OPERAND2_RGB_ARB = 0x8592
    const val GL_OPERAND0_ALPHA_ARB = 0x8598
    const val GL_OPERAND1_ALPHA_ARB = 0x8599
    const val GL_OPERAND2_ALPHA_ARB = 0x859A
    const val GL_RGB_SCALE_ARB = 0x8573
    const val GL_ADD_SIGNED_ARB = 0x8574
    const val GL_INTERPOLATE_ARB = 0x8575
    const val GL_SUBTRACT_ARB = 0x84E7
    const val GL_CONSTANT_ARB = 0x8576
    const val GL_PRIMARY_COLOR_ARB = 0x8577
    const val GL_PREVIOUS_ARB = 0x8578
    const val GL_DOT3_RGB_ARB = 0x86AE
    const val GL_DOT3_RGBA_ARB = 0x86AF
    const val GL_TEXTURE_REDUCTION_MODE_ARB = 0x9366
    const val GL_WEIGHTED_AVERAGE_ARB = 0x9367
    const val GL_TEXTURE_RED_TYPE_ARB = 0x8C10
    const val GL_TEXTURE_GREEN_TYPE_ARB = 0x8C11
    const val GL_TEXTURE_BLUE_TYPE_ARB = 0x8C12
    const val GL_TEXTURE_ALPHA_TYPE_ARB = 0x8C13
    const val GL_TEXTURE_LUMINANCE_TYPE_ARB = 0x8C14
    const val GL_TEXTURE_INTENSITY_TYPE_ARB = 0x8C15
    const val GL_TEXTURE_DEPTH_TYPE_ARB = 0x8C16
    const val GL_UNSIGNED_NORMALIZED_ARB = 0x8C17
    const val GL_RGBA32F_ARB = 0x8814
    const val GL_RGB32F_ARB = 0x8815
    const val GL_ALPHA32F_ARB = 0x8816
    const val GL_INTENSITY32F_ARB = 0x8817
    const val GL_LUMINANCE32F_ARB = 0x8818
    const val GL_LUMINANCE_ALPHA32F_ARB = 0x8819
    const val GL_RGBA16F_ARB = 0x881A
    const val GL_RGB16F_ARB = 0x881B
    const val GL_ALPHA16F_ARB = 0x881C
    const val GL_INTENSITY16F_ARB = 0x881D
    const val GL_LUMINANCE16F_ARB = 0x881E
    const val GL_LUMINANCE_ALPHA16F_ARB = 0x881F
    const val GL_MIN_PROGRAM_TEXTURE_GATHER_OFFSET_ARB = 0x8E5E
    const val GL_MAX_PROGRAM_TEXTURE_GATHER_OFFSET_ARB = 0x8E5F
    const val GL_MAX_PROGRAM_TEXTURE_GATHER_COMPONENTS_ARB = 0x8F9F
    const val GL_MIRRORED_REPEAT_ARB = 0x8370
    const val GL_TEXTURE_RECTANGLE_ARB = 0x84F5
    const val GL_TEXTURE_BINDING_RECTANGLE_ARB = 0x84F6
    const val GL_PROXY_TEXTURE_RECTANGLE_ARB = 0x84F7
    const val GL_MAX_RECTANGLE_TEXTURE_SIZE_ARB = 0x84F8
    const val GL_TRANSFORM_FEEDBACK_OVERFLOW_ARB = 0x82EC
    const val GL_TRANSFORM_FEEDBACK_STREAM_OVERFLOW_ARB = 0x82ED
    const val GL_TRANSPOSE_MODELVIEW_MATRIX_ARB = 0x84E3
    const val GL_TRANSPOSE_PROJECTION_MATRIX_ARB = 0x84E4
    const val GL_TRANSPOSE_TEXTURE_MATRIX_ARB = 0x84E5
    const val GL_TRANSPOSE_COLOR_MATRIX_ARB = 0x84E6
    const val GL_MAX_VERTEX_UNITS_ARB = 0x86A4
    const val GL_ACTIVE_VERTEX_UNITS_ARB = 0x86A5
    const val GL_WEIGHT_SUM_UNITY_ARB = 0x86A6
    const val GL_VERTEX_BLEND_ARB = 0x86A7
    const val GL_CURRENT_WEIGHT_ARB = 0x86A8
    const val GL_WEIGHT_ARRAY_TYPE_ARB = 0x86A9
    const val GL_WEIGHT_ARRAY_STRIDE_ARB = 0x86AA
    const val GL_WEIGHT_ARRAY_SIZE_ARB = 0x86AB
    const val GL_WEIGHT_ARRAY_POINTER_ARB = 0x86AC
    const val GL_WEIGHT_ARRAY_ARB = 0x86AD
    const val GL_MODELVIEW0_ARB = 0x1700
    const val GL_MODELVIEW1_ARB = 0x850A
    const val GL_MODELVIEW2_ARB = 0x8722
    const val GL_MODELVIEW3_ARB = 0x8723
    const val GL_MODELVIEW4_ARB = 0x8724
    const val GL_MODELVIEW5_ARB = 0x8725
    const val GL_MODELVIEW6_ARB = 0x8726
    const val GL_MODELVIEW7_ARB = 0x8727
    const val GL_MODELVIEW8_ARB = 0x8728
    const val GL_MODELVIEW9_ARB = 0x8729
    const val GL_MODELVIEW10_ARB = 0x872A
    const val GL_MODELVIEW11_ARB = 0x872B
    const val GL_MODELVIEW12_ARB = 0x872C
    const val GL_MODELVIEW13_ARB = 0x872D
    const val GL_MODELVIEW14_ARB = 0x872E
    const val GL_MODELVIEW15_ARB = 0x872F
    const val GL_MODELVIEW16_ARB = 0x8730
    const val GL_MODELVIEW17_ARB = 0x8731
    const val GL_MODELVIEW18_ARB = 0x8732
    const val GL_MODELVIEW19_ARB = 0x8733
    const val GL_MODELVIEW20_ARB = 0x8734
    const val GL_MODELVIEW21_ARB = 0x8735
    const val GL_MODELVIEW22_ARB = 0x8736
    const val GL_MODELVIEW23_ARB = 0x8737
    const val GL_MODELVIEW24_ARB = 0x8738
    const val GL_MODELVIEW25_ARB = 0x8739
    const val GL_MODELVIEW26_ARB = 0x873A
    const val GL_MODELVIEW27_ARB = 0x873B
    const val GL_MODELVIEW28_ARB = 0x873C
    const val GL_MODELVIEW29_ARB = 0x873D
    const val GL_MODELVIEW30_ARB = 0x873E
    const val GL_MODELVIEW31_ARB = 0x873F
    const val GL_BUFFER_SIZE_ARB = 0x8764
    const val GL_BUFFER_USAGE_ARB = 0x8765
    const val GL_ARRAY_BUFFER_ARB = 0x8892
    const val GL_ELEMENT_ARRAY_BUFFER_ARB = 0x8893
    const val GL_ARRAY_BUFFER_BINDING_ARB = 0x8894
    const val GL_ELEMENT_ARRAY_BUFFER_BINDING_ARB = 0x8895
    const val GL_VERTEX_ARRAY_BUFFER_BINDING_ARB = 0x8896
    const val GL_NORMAL_ARRAY_BUFFER_BINDING_ARB = 0x8897
    const val GL_COLOR_ARRAY_BUFFER_BINDING_ARB = 0x8898
    const val GL_INDEX_ARRAY_BUFFER_BINDING_ARB = 0x8899
    const val GL_TEXTURE_COORD_ARRAY_BUFFER_BINDING_ARB = 0x889A
    const val GL_EDGE_FLAG_ARRAY_BUFFER_BINDING_ARB = 0x889B
    const val GL_SECONDARY_COLOR_ARRAY_BUFFER_BINDING_ARB = 0x889C
    const val GL_FOG_COORDINATE_ARRAY_BUFFER_BINDING_ARB = 0x889D
    const val GL_WEIGHT_ARRAY_BUFFER_BINDING_ARB = 0x889E
    const val GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING_ARB = 0x889F
    const val GL_READ_ONLY_ARB = 0x88B8
    const val GL_WRITE_ONLY_ARB = 0x88B9
    const val GL_READ_WRITE_ARB = 0x88BA
    const val GL_BUFFER_ACCESS_ARB = 0x88BB
    const val GL_BUFFER_MAPPED_ARB = 0x88BC
    const val GL_BUFFER_MAP_POINTER_ARB = 0x88BD
    const val GL_STREAM_DRAW_ARB = 0x88E0
    const val GL_STREAM_READ_ARB = 0x88E1
    const val GL_STREAM_COPY_ARB = 0x88E2
    const val GL_STATIC_DRAW_ARB = 0x88E4
    const val GL_STATIC_READ_ARB = 0x88E5
    const val GL_STATIC_COPY_ARB = 0x88E6
    const val GL_DYNAMIC_DRAW_ARB = 0x88E8
    const val GL_DYNAMIC_READ_ARB = 0x88E9
    const val GL_DYNAMIC_COPY_ARB = 0x88EA
    const val GL_COLOR_SUM_ARB = 0x8458
    const val GL_VERTEX_PROGRAM_ARB = 0x8620
    const val GL_VERTEX_ATTRIB_ARRAY_ENABLED_ARB = 0x8622
    const val GL_VERTEX_ATTRIB_ARRAY_SIZE_ARB = 0x8623
    const val GL_VERTEX_ATTRIB_ARRAY_STRIDE_ARB = 0x8624
    const val GL_VERTEX_ATTRIB_ARRAY_TYPE_ARB = 0x8625
    const val GL_CURRENT_VERTEX_ATTRIB_ARB = 0x8626
    const val GL_VERTEX_PROGRAM_POINT_SIZE_ARB = 0x8642
    const val GL_VERTEX_PROGRAM_TWO_SIDE_ARB = 0x8643
    const val GL_VERTEX_ATTRIB_ARRAY_POINTER_ARB = 0x8645
    const val GL_MAX_VERTEX_ATTRIBS_ARB = 0x8869
    const val GL_VERTEX_ATTRIB_ARRAY_NORMALIZED_ARB = 0x886A
    const val GL_PROGRAM_ADDRESS_REGISTERS_ARB = 0x88B0
    const val GL_MAX_PROGRAM_ADDRESS_REGISTERS_ARB = 0x88B1
    const val GL_PROGRAM_NATIVE_ADDRESS_REGISTERS_ARB = 0x88B2
    const val GL_MAX_PROGRAM_NATIVE_ADDRESS_REGISTERS_ARB = 0x88B3
    const val GL_VERTEX_SHADER_ARB = 0x8B31
    const val GL_MAX_VERTEX_UNIFORM_COMPONENTS_ARB = 0x8B4A
    const val GL_MAX_VARYING_FLOATS_ARB = 0x8B4B
    const val GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS_ARB = 0x8B4C
    const val GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS_ARB = 0x8B4D
    const val GL_OBJECT_ACTIVE_ATTRIBUTES_ARB = 0x8B89
    const val GL_OBJECT_ACTIVE_ATTRIBUTE_MAX_LENGTH_ARB = 0x8B8A
    const val GL_MAX_DRAW_BUFFERS_ATI = 0x8824
    const val GL_DRAW_BUFFER0_ATI = 0x8825
    const val GL_DRAW_BUFFER1_ATI = 0x8826
    const val GL_DRAW_BUFFER2_ATI = 0x8827
    const val GL_DRAW_BUFFER3_ATI = 0x8828
    const val GL_DRAW_BUFFER4_ATI = 0x8829
    const val GL_DRAW_BUFFER5_ATI = 0x882A
    const val GL_DRAW_BUFFER6_ATI = 0x882B
    const val GL_DRAW_BUFFER7_ATI = 0x882C
    const val GL_DRAW_BUFFER8_ATI = 0x882D
    const val GL_DRAW_BUFFER9_ATI = 0x882E
    const val GL_DRAW_BUFFER10_ATI = 0x882F
    const val GL_DRAW_BUFFER11_ATI = 0x8830
    const val GL_DRAW_BUFFER12_ATI = 0x8831
    const val GL_DRAW_BUFFER13_ATI = 0x8832
    const val GL_DRAW_BUFFER14_ATI = 0x8833
    const val GL_DRAW_BUFFER15_ATI = 0x8834
    const val GL_ELEMENT_ARRAY_ATI = 0x8768
    const val GL_ELEMENT_ARRAY_TYPE_ATI = 0x8769
    const val GL_ELEMENT_ARRAY_POINTER_ATI = 0x876A
    const val GL_BUMP_ROT_MATRIX_ATI = 0x8775
    const val GL_BUMP_ROT_MATRIX_SIZE_ATI = 0x8776
    const val GL_BUMP_NUM_TEX_UNITS_ATI = 0x8777
    const val GL_BUMP_TEX_UNITS_ATI = 0x8778
    const val GL_DUDV_ATI = 0x8779
    const val GL_DU8DV8_ATI = 0x877A
    const val GL_BUMP_ENVMAP_ATI = 0x877B
    const val GL_BUMP_TARGET_ATI = 0x877C
    const val GL_FRAGMENT_SHADER_ATI = 0x8920
    const val GL_REG_0_ATI = 0x8921
    const val GL_REG_1_ATI = 0x8922
    const val GL_REG_2_ATI = 0x8923
    const val GL_REG_3_ATI = 0x8924
    const val GL_REG_4_ATI = 0x8925
    const val GL_REG_5_ATI = 0x8926
    const val GL_REG_6_ATI = 0x8927
    const val GL_REG_7_ATI = 0x8928
    const val GL_REG_8_ATI = 0x8929
    const val GL_REG_9_ATI = 0x892A
    const val GL_REG_10_ATI = 0x892B
    const val GL_REG_11_ATI = 0x892C
    const val GL_REG_12_ATI = 0x892D
    const val GL_REG_13_ATI = 0x892E
    const val GL_REG_14_ATI = 0x892F
    const val GL_REG_15_ATI = 0x8930
    const val GL_REG_16_ATI = 0x8931
    const val GL_REG_17_ATI = 0x8932
    const val GL_REG_18_ATI = 0x8933
    const val GL_REG_19_ATI = 0x8934
    const val GL_REG_20_ATI = 0x8935
    const val GL_REG_21_ATI = 0x8936
    const val GL_REG_22_ATI = 0x8937
    const val GL_REG_23_ATI = 0x8938
    const val GL_REG_24_ATI = 0x8939
    const val GL_REG_25_ATI = 0x893A
    const val GL_REG_26_ATI = 0x893B
    const val GL_REG_27_ATI = 0x893C
    const val GL_REG_28_ATI = 0x893D
    const val GL_REG_29_ATI = 0x893E
    const val GL_REG_30_ATI = 0x893F
    const val GL_REG_31_ATI = 0x8940
    const val GL_CON_0_ATI = 0x8941
    const val GL_CON_1_ATI = 0x8942
    const val GL_CON_2_ATI = 0x8943
    const val GL_CON_3_ATI = 0x8944
    const val GL_CON_4_ATI = 0x8945
    const val GL_CON_5_ATI = 0x8946
    const val GL_CON_6_ATI = 0x8947
    const val GL_CON_7_ATI = 0x8948
    const val GL_CON_8_ATI = 0x8949
    const val GL_CON_9_ATI = 0x894A
    const val GL_CON_10_ATI = 0x894B
    const val GL_CON_11_ATI = 0x894C
    const val GL_CON_12_ATI = 0x894D
    const val GL_CON_13_ATI = 0x894E
    const val GL_CON_14_ATI = 0x894F
    const val GL_CON_15_ATI = 0x8950
    const val GL_CON_16_ATI = 0x8951
    const val GL_CON_17_ATI = 0x8952
    const val GL_CON_18_ATI = 0x8953
    const val GL_CON_19_ATI = 0x8954
    const val GL_CON_20_ATI = 0x8955
    const val GL_CON_21_ATI = 0x8956
    const val GL_CON_22_ATI = 0x8957
    const val GL_CON_23_ATI = 0x8958
    const val GL_CON_24_ATI = 0x8959
    const val GL_CON_25_ATI = 0x895A
    const val GL_CON_26_ATI = 0x895B
    const val GL_CON_27_ATI = 0x895C
    const val GL_CON_28_ATI = 0x895D
    const val GL_CON_29_ATI = 0x895E
    const val GL_CON_30_ATI = 0x895F
    const val GL_CON_31_ATI = 0x8960
    const val GL_MOV_ATI = 0x8961
    const val GL_ADD_ATI = 0x8963
    const val GL_MUL_ATI = 0x8964
    const val GL_SUB_ATI = 0x8965
    const val GL_DOT3_ATI = 0x8966
    const val GL_DOT4_ATI = 0x8967
    const val GL_MAD_ATI = 0x8968
    const val GL_LERP_ATI = 0x8969
    const val GL_CND_ATI = 0x896A
    const val GL_CND0_ATI = 0x896B
    const val GL_DOT2_ADD_ATI = 0x896C
    const val GL_SECONDARY_INTERPOLATOR_ATI = 0x896D
    const val GL_NUM_FRAGMENT_REGISTERS_ATI = 0x896E
    const val GL_NUM_FRAGMENT_CONSTANTS_ATI = 0x896F
    const val GL_NUM_PASSES_ATI = 0x8970
    const val GL_NUM_INSTRUCTIONS_PER_PASS_ATI = 0x8971
    const val GL_NUM_INSTRUCTIONS_TOTAL_ATI = 0x8972
    const val GL_NUM_INPUT_INTERPOLATOR_COMPONENTS_ATI = 0x8973
    const val GL_NUM_LOOPBACK_COMPONENTS_ATI = 0x8974
    const val GL_COLOR_ALPHA_PAIRING_ATI = 0x8975
    const val GL_SWIZZLE_STR_ATI = 0x8976
    const val GL_SWIZZLE_STQ_ATI = 0x8977
    const val GL_SWIZZLE_STR_DR_ATI = 0x8978
    const val GL_SWIZZLE_STQ_DQ_ATI = 0x8979
    const val GL_SWIZZLE_STRQ_ATI = 0x897A
    const val GL_SWIZZLE_STRQ_DQ_ATI = 0x897B
    const val GL_RED_BIT_ATI = 0x00000001
    const val GL_GREEN_BIT_ATI = 0x00000002
    const val GL_BLUE_BIT_ATI = 0x00000004
    const val GL_2X_BIT_ATI = 0x00000001
    const val GL_4X_BIT_ATI = 0x00000002
    const val GL_8X_BIT_ATI = 0x00000004
    const val GL_HALF_BIT_ATI = 0x00000008
    const val GL_QUARTER_BIT_ATI = 0x00000010
    const val GL_EIGHTH_BIT_ATI = 0x00000020
    const val GL_SATURATE_BIT_ATI = 0x00000040
    const val GL_COMP_BIT_ATI = 0x00000002
    const val GL_NEGATE_BIT_ATI = 0x00000004
    const val GL_BIAS_BIT_ATI = 0x00000008
    const val GL_VBO_FREE_MEMORY_ATI = 0x87FB
    const val GL_TEXTURE_FREE_MEMORY_ATI = 0x87FC
    const val GL_RENDERBUFFER_FREE_MEMORY_ATI = 0x87FD
    const val GL_RGBA_FLOAT_MODE_ATI = 0x8820
    const val GL_COLOR_CLEAR_UNCLAMPED_VALUE_ATI = 0x8835
    const val GL_PN_TRIANGLES_ATI = 0x87F0
    const val GL_MAX_PN_TRIANGLES_TESSELATION_LEVEL_ATI = 0x87F1
    const val GL_PN_TRIANGLES_POINT_MODE_ATI = 0x87F2
    const val GL_PN_TRIANGLES_NORMAL_MODE_ATI = 0x87F3
    const val GL_PN_TRIANGLES_TESSELATION_LEVEL_ATI = 0x87F4
    const val GL_PN_TRIANGLES_POINT_MODE_LINEAR_ATI = 0x87F5
    const val GL_PN_TRIANGLES_POINT_MODE_CUBIC_ATI = 0x87F6
    const val GL_PN_TRIANGLES_NORMAL_MODE_LINEAR_ATI = 0x87F7
    const val GL_PN_TRIANGLES_NORMAL_MODE_QUADRATIC_ATI = 0x87F8
    const val GL_STENCIL_BACK_FUNC_ATI = 0x8800
    const val GL_STENCIL_BACK_FAIL_ATI = 0x8801
    const val GL_STENCIL_BACK_PASS_DEPTH_FAIL_ATI = 0x8802
    const val GL_STENCIL_BACK_PASS_DEPTH_PASS_ATI = 0x8803
    const val GL_TEXT_FRAGMENT_SHADER_ATI = 0x8200
    const val GL_MODULATE_ADD_ATI = 0x8744
    const val GL_MODULATE_SIGNED_ADD_ATI = 0x8745
    const val GL_MODULATE_SUBTRACT_ATI = 0x8746
    const val GL_RGBA_FLOAT32_ATI = 0x8814
    const val GL_RGB_FLOAT32_ATI = 0x8815
    const val GL_ALPHA_FLOAT32_ATI = 0x8816
    const val GL_INTENSITY_FLOAT32_ATI = 0x8817
    const val GL_LUMINANCE_FLOAT32_ATI = 0x8818
    const val GL_LUMINANCE_ALPHA_FLOAT32_ATI = 0x8819
    const val GL_RGBA_FLOAT16_ATI = 0x881A
    const val GL_RGB_FLOAT16_ATI = 0x881B
    const val GL_ALPHA_FLOAT16_ATI = 0x881C
    const val GL_INTENSITY_FLOAT16_ATI = 0x881D
    const val GL_LUMINANCE_FLOAT16_ATI = 0x881E
    const val GL_LUMINANCE_ALPHA_FLOAT16_ATI = 0x881F
    const val GL_MIRROR_CLAMP_ATI = 0x8742
    const val GL_MIRROR_CLAMP_TO_EDGE_ATI = 0x8743
    const val GL_STATIC_ATI = 0x8760
    const val GL_DYNAMIC_ATI = 0x8761
    const val GL_PRESERVE_ATI = 0x8762
    const val GL_DISCARD_ATI = 0x8763
    const val GL_OBJECT_BUFFER_SIZE_ATI = 0x8764
    const val GL_OBJECT_BUFFER_USAGE_ATI = 0x8765
    const val GL_ARRAY_OBJECT_BUFFER_ATI = 0x8766
    const val GL_ARRAY_OBJECT_OFFSET_ATI = 0x8767
    const val GL_MAX_VERTEX_STREAMS_ATI = 0x876B
    const val GL_VERTEX_STREAM0_ATI = 0x876C
    const val GL_VERTEX_STREAM1_ATI = 0x876D
    const val GL_VERTEX_STREAM2_ATI = 0x876E
    const val GL_VERTEX_STREAM3_ATI = 0x876F
    const val GL_VERTEX_STREAM4_ATI = 0x8770
    const val GL_VERTEX_STREAM5_ATI = 0x8771
    const val GL_VERTEX_STREAM6_ATI = 0x8772
    const val GL_VERTEX_STREAM7_ATI = 0x8773
    const val GL_VERTEX_SOURCE_ATI = 0x8774
    const val GL_422_EXT = 0x80CC
    const val GL_422_REV_EXT = 0x80CD
    const val GL_422_AVERAGE_EXT = 0x80CE
    const val GL_422_REV_AVERAGE_EXT = 0x80CF
    const val GL_ABGR_EXT = 0x8000
    const val GL_BGR_EXT = 0x80E0
    const val GL_BGRA_EXT = 0x80E1
    const val GL_MAX_VERTEX_BINDABLE_UNIFORMS_EXT = 0x8DE2
    const val GL_MAX_FRAGMENT_BINDABLE_UNIFORMS_EXT = 0x8DE3
    const val GL_MAX_GEOMETRY_BINDABLE_UNIFORMS_EXT = 0x8DE4
    const val GL_MAX_BINDABLE_UNIFORM_SIZE_EXT = 0x8DED
    const val GL_UNIFORM_BUFFER_EXT = 0x8DEE
    const val GL_UNIFORM_BUFFER_BINDING_EXT = 0x8DEF
    const val GL_CONSTANT_COLOR_EXT = 0x8001
    const val GL_ONE_MINUS_CONSTANT_COLOR_EXT = 0x8002
    const val GL_CONSTANT_ALPHA_EXT = 0x8003
    const val GL_ONE_MINUS_CONSTANT_ALPHA_EXT = 0x8004
    const val GL_BLEND_COLOR_EXT = 0x8005
    const val GL_BLEND_EQUATION_RGB_EXT = 0x8009
    const val GL_BLEND_EQUATION_ALPHA_EXT = 0x883D
    const val GL_BLEND_DST_RGB_EXT = 0x80C8
    const val GL_BLEND_SRC_RGB_EXT = 0x80C9
    const val GL_BLEND_DST_ALPHA_EXT = 0x80CA
    const val GL_BLEND_SRC_ALPHA_EXT = 0x80CB
    const val GL_MIN_EXT = 0x8007
    const val GL_MAX_EXT = 0x8008
    const val GL_FUNC_ADD_EXT = 0x8006
    const val GL_BLEND_EQUATION_EXT = 0x8009
    const val GL_FUNC_SUBTRACT_EXT = 0x800A
    const val GL_FUNC_REVERSE_SUBTRACT_EXT = 0x800B
    const val GL_CLIP_VOLUME_CLIPPING_HINT_EXT = 0x80F0
    const val GL_CMYK_EXT = 0x800C
    const val GL_CMYKA_EXT = 0x800D
    const val GL_PACK_CMYK_HINT_EXT = 0x800E
    const val GL_UNPACK_CMYK_HINT_EXT = 0x800F
    const val GL_ARRAY_ELEMENT_LOCK_FIRST_EXT = 0x81A8
    const val GL_ARRAY_ELEMENT_LOCK_COUNT_EXT = 0x81A9
    const val GL_CONVOLUTION_1D_EXT = 0x8010
    const val GL_CONVOLUTION_2D_EXT = 0x8011
    const val GL_SEPARABLE_2D_EXT = 0x8012
    const val GL_CONVOLUTION_BORDER_MODE_EXT = 0x8013
    const val GL_CONVOLUTION_FILTER_SCALE_EXT = 0x8014
    const val GL_CONVOLUTION_FILTER_BIAS_EXT = 0x8015
    const val GL_REDUCE_EXT = 0x8016
    const val GL_CONVOLUTION_FORMAT_EXT = 0x8017
    const val GL_CONVOLUTION_WIDTH_EXT = 0x8018
    const val GL_CONVOLUTION_HEIGHT_EXT = 0x8019
    const val GL_MAX_CONVOLUTION_WIDTH_EXT = 0x801A
    const val GL_MAX_CONVOLUTION_HEIGHT_EXT = 0x801B
    const val GL_POST_CONVOLUTION_RED_SCALE_EXT = 0x801C
    const val GL_POST_CONVOLUTION_GREEN_SCALE_EXT = 0x801D
    const val GL_POST_CONVOLUTION_BLUE_SCALE_EXT = 0x801E
    const val GL_POST_CONVOLUTION_ALPHA_SCALE_EXT = 0x801F
    const val GL_POST_CONVOLUTION_RED_BIAS_EXT = 0x8020
    const val GL_POST_CONVOLUTION_GREEN_BIAS_EXT = 0x8021
    const val GL_POST_CONVOLUTION_BLUE_BIAS_EXT = 0x8022
    const val GL_POST_CONVOLUTION_ALPHA_BIAS_EXT = 0x8023
    const val GL_TANGENT_ARRAY_EXT = 0x8439
    const val GL_BINORMAL_ARRAY_EXT = 0x843A
    const val GL_CURRENT_TANGENT_EXT = 0x843B
    const val GL_CURRENT_BINORMAL_EXT = 0x843C
    const val GL_TANGENT_ARRAY_TYPE_EXT = 0x843E
    const val GL_TANGENT_ARRAY_STRIDE_EXT = 0x843F
    const val GL_BINORMAL_ARRAY_TYPE_EXT = 0x8440
    const val GL_BINORMAL_ARRAY_STRIDE_EXT = 0x8441
    const val GL_TANGENT_ARRAY_POINTER_EXT = 0x8442
    const val GL_BINORMAL_ARRAY_POINTER_EXT = 0x8443
    const val GL_MAP1_TANGENT_EXT = 0x8444
    const val GL_MAP2_TANGENT_EXT = 0x8445
    const val GL_MAP1_BINORMAL_EXT = 0x8446
    const val GL_MAP2_BINORMAL_EXT = 0x8447
    const val GL_CULL_VERTEX_EXT = 0x81AA
    const val GL_CULL_VERTEX_EYE_POSITION_EXT = 0x81AB
    const val GL_CULL_VERTEX_OBJECT_POSITION_EXT = 0x81AC
    const val GL_PROGRAM_PIPELINE_OBJECT_EXT = 0x8A4F
    const val GL_PROGRAM_OBJECT_EXT = 0x8B40
    const val GL_SHADER_OBJECT_EXT = 0x8B48
    const val GL_BUFFER_OBJECT_EXT = 0x9151
    const val GL_QUERY_OBJECT_EXT = 0x9153
    const val GL_VERTEX_ARRAY_OBJECT_EXT = 0x9154
    const val GL_DEPTH_BOUNDS_TEST_EXT = 0x8890
    const val GL_DEPTH_BOUNDS_EXT = 0x8891
    const val GL_PROGRAM_MATRIX_EXT = 0x8E2D
    const val GL_TRANSPOSE_PROGRAM_MATRIX_EXT = 0x8E2E
    const val GL_PROGRAM_MATRIX_STACK_DEPTH_EXT = 0x8E2F
    const val GL_MAX_ELEMENTS_VERTICES_EXT = 0x80E8
    const val GL_MAX_ELEMENTS_INDICES_EXT = 0x80E9
    const val GL_FOG_COORDINATE_SOURCE_EXT = 0x8450
    const val GL_FOG_COORDINATE_EXT = 0x8451
    const val GL_FRAGMENT_DEPTH_EXT = 0x8452
    const val GL_CURRENT_FOG_COORDINATE_EXT = 0x8453
    const val GL_FOG_COORDINATE_ARRAY_TYPE_EXT = 0x8454
    const val GL_FOG_COORDINATE_ARRAY_STRIDE_EXT = 0x8455
    const val GL_FOG_COORDINATE_ARRAY_POINTER_EXT = 0x8456
    const val GL_FOG_COORDINATE_ARRAY_EXT = 0x8457
    const val GL_READ_FRAMEBUFFER_EXT = 0x8CA8
    const val GL_DRAW_FRAMEBUFFER_EXT = 0x8CA9
    const val GL_DRAW_FRAMEBUFFER_BINDING_EXT = 0x8CA6
    const val GL_READ_FRAMEBUFFER_BINDING_EXT = 0x8CAA
    const val GL_RENDERBUFFER_SAMPLES_EXT = 0x8CAB
    const val GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE_EXT = 0x8D56
    const val GL_MAX_SAMPLES_EXT = 0x8D57
    const val GL_SCALED_RESOLVE_FASTEST_EXT = 0x90BA
    const val GL_SCALED_RESOLVE_NICEST_EXT = 0x90BB
    const val GL_INVALID_FRAMEBUFFER_OPERATION_EXT = 0x0506
    const val GL_MAX_RENDERBUFFER_SIZE_EXT = 0x84E8
    const val GL_FRAMEBUFFER_BINDING_EXT = 0x8CA6
    const val GL_RENDERBUFFER_BINDING_EXT = 0x8CA7
    const val GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE_EXT = 0x8CD0
    const val GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME_EXT = 0x8CD1
    const val GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL_EXT = 0x8CD2
    const val GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE_EXT = 0x8CD3
    const val GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_3D_ZOFFSET_EXT = 0x8CD4
    const val GL_FRAMEBUFFER_COMPLETE_EXT = 0x8CD5
    const val GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT = 0x8CD6
    const val GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT = 0x8CD7
    const val GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT = 0x8CD9
    const val GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT = 0x8CDA
    const val GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT = 0x8CDB
    const val GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT = 0x8CDC
    const val GL_FRAMEBUFFER_UNSUPPORTED_EXT = 0x8CDD
    const val GL_MAX_COLOR_ATTACHMENTS_EXT = 0x8CDF
    const val GL_COLOR_ATTACHMENT0_EXT = 0x8CE0
    const val GL_COLOR_ATTACHMENT1_EXT = 0x8CE1
    const val GL_COLOR_ATTACHMENT2_EXT = 0x8CE2
    const val GL_COLOR_ATTACHMENT3_EXT = 0x8CE3
    const val GL_COLOR_ATTACHMENT4_EXT = 0x8CE4
    const val GL_COLOR_ATTACHMENT5_EXT = 0x8CE5
    const val GL_COLOR_ATTACHMENT6_EXT = 0x8CE6
    const val GL_COLOR_ATTACHMENT7_EXT = 0x8CE7
    const val GL_COLOR_ATTACHMENT8_EXT = 0x8CE8
    const val GL_COLOR_ATTACHMENT9_EXT = 0x8CE9
    const val GL_COLOR_ATTACHMENT10_EXT = 0x8CEA
    const val GL_COLOR_ATTACHMENT11_EXT = 0x8CEB
    const val GL_COLOR_ATTACHMENT12_EXT = 0x8CEC
    const val GL_COLOR_ATTACHMENT13_EXT = 0x8CED
    const val GL_COLOR_ATTACHMENT14_EXT = 0x8CEE
    const val GL_COLOR_ATTACHMENT15_EXT = 0x8CEF
    const val GL_DEPTH_ATTACHMENT_EXT = 0x8D00
    const val GL_STENCIL_ATTACHMENT_EXT = 0x8D20
    const val GL_FRAMEBUFFER_EXT = 0x8D40
    const val GL_RENDERBUFFER_EXT = 0x8D41
    const val GL_RENDERBUFFER_WIDTH_EXT = 0x8D42
    const val GL_RENDERBUFFER_HEIGHT_EXT = 0x8D43
    const val GL_RENDERBUFFER_INTERNAL_FORMAT_EXT = 0x8D44
    const val GL_STENCIL_INDEX1_EXT = 0x8D46
    const val GL_STENCIL_INDEX4_EXT = 0x8D47
    const val GL_STENCIL_INDEX8_EXT = 0x8D48
    const val GL_STENCIL_INDEX16_EXT = 0x8D49
    const val GL_RENDERBUFFER_RED_SIZE_EXT = 0x8D50
    const val GL_RENDERBUFFER_GREEN_SIZE_EXT = 0x8D51
    const val GL_RENDERBUFFER_BLUE_SIZE_EXT = 0x8D52
    const val GL_RENDERBUFFER_ALPHA_SIZE_EXT = 0x8D53
    const val GL_RENDERBUFFER_DEPTH_SIZE_EXT = 0x8D54
    const val GL_RENDERBUFFER_STENCIL_SIZE_EXT = 0x8D55
    const val GL_FRAMEBUFFER_SRGB_EXT = 0x8DB9
    const val GL_FRAMEBUFFER_SRGB_CAPABLE_EXT = 0x8DBA
    const val GL_GEOMETRY_SHADER_EXT = 0x8DD9
    const val GL_GEOMETRY_VERTICES_OUT_EXT = 0x8DDA
    const val GL_GEOMETRY_INPUT_TYPE_EXT = 0x8DDB
    const val GL_GEOMETRY_OUTPUT_TYPE_EXT = 0x8DDC
    const val GL_MAX_GEOMETRY_TEXTURE_IMAGE_UNITS_EXT = 0x8C29
    const val GL_MAX_GEOMETRY_VARYING_COMPONENTS_EXT = 0x8DDD
    const val GL_MAX_VERTEX_VARYING_COMPONENTS_EXT = 0x8DDE
    const val GL_MAX_VARYING_COMPONENTS_EXT = 0x8B4B
    const val GL_MAX_GEOMETRY_UNIFORM_COMPONENTS_EXT = 0x8DDF
    const val GL_MAX_GEOMETRY_OUTPUT_VERTICES_EXT = 0x8DE0
    const val GL_MAX_GEOMETRY_TOTAL_OUTPUT_COMPONENTS_EXT = 0x8DE1
    const val GL_LINES_ADJACENCY_EXT = 0x000A
    const val GL_LINE_STRIP_ADJACENCY_EXT = 0x000B
    const val GL_TRIANGLES_ADJACENCY_EXT = 0x000C
    const val GL_TRIANGLE_STRIP_ADJACENCY_EXT = 0x000D
    const val GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS_EXT = 0x8DA8
    const val GL_FRAMEBUFFER_INCOMPLETE_LAYER_COUNT_EXT = 0x8DA9
    const val GL_FRAMEBUFFER_ATTACHMENT_LAYERED_EXT = 0x8DA7
    const val GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LAYER_EXT = 0x8CD4
    const val GL_PROGRAM_POINT_SIZE_EXT = 0x8642
    const val GL_SAMPLER_1D_ARRAY_EXT = 0x8DC0
    const val GL_SAMPLER_2D_ARRAY_EXT = 0x8DC1
    const val GL_SAMPLER_BUFFER_EXT = 0x8DC2
    const val GL_SAMPLER_1D_ARRAY_SHADOW_EXT = 0x8DC3
    const val GL_SAMPLER_2D_ARRAY_SHADOW_EXT = 0x8DC4
    const val GL_SAMPLER_CUBE_SHADOW_EXT = 0x8DC5
    const val GL_UNSIGNED_INT_VEC2_EXT = 0x8DC6
    const val GL_UNSIGNED_INT_VEC3_EXT = 0x8DC7
    const val GL_UNSIGNED_INT_VEC4_EXT = 0x8DC8
    const val GL_INT_SAMPLER_1D_EXT = 0x8DC9
    const val GL_INT_SAMPLER_2D_EXT = 0x8DCA
    const val GL_INT_SAMPLER_3D_EXT = 0x8DCB
    const val GL_INT_SAMPLER_CUBE_EXT = 0x8DCC
    const val GL_INT_SAMPLER_2D_RECT_EXT = 0x8DCD
    const val GL_INT_SAMPLER_1D_ARRAY_EXT = 0x8DCE
    const val GL_INT_SAMPLER_2D_ARRAY_EXT = 0x8DCF
    const val GL_INT_SAMPLER_BUFFER_EXT = 0x8DD0
    const val GL_UNSIGNED_INT_SAMPLER_1D_EXT = 0x8DD1
    const val GL_UNSIGNED_INT_SAMPLER_2D_EXT = 0x8DD2
    const val GL_UNSIGNED_INT_SAMPLER_3D_EXT = 0x8DD3
    const val GL_UNSIGNED_INT_SAMPLER_CUBE_EXT = 0x8DD4
    const val GL_UNSIGNED_INT_SAMPLER_2D_RECT_EXT = 0x8DD5
    const val GL_UNSIGNED_INT_SAMPLER_1D_ARRAY_EXT = 0x8DD6
    const val GL_UNSIGNED_INT_SAMPLER_2D_ARRAY_EXT = 0x8DD7
    const val GL_UNSIGNED_INT_SAMPLER_BUFFER_EXT = 0x8DD8
    const val GL_MIN_PROGRAM_TEXEL_OFFSET_EXT = 0x8904
    const val GL_MAX_PROGRAM_TEXEL_OFFSET_EXT = 0x8905
    const val GL_VERTEX_ATTRIB_ARRAY_INTEGER_EXT = 0x88FD
    const val GL_HISTOGRAM_EXT = 0x8024
    const val GL_PROXY_HISTOGRAM_EXT = 0x8025
    const val GL_HISTOGRAM_WIDTH_EXT = 0x8026
    const val GL_HISTOGRAM_FORMAT_EXT = 0x8027
    const val GL_HISTOGRAM_RED_SIZE_EXT = 0x8028
    const val GL_HISTOGRAM_GREEN_SIZE_EXT = 0x8029
    const val GL_HISTOGRAM_BLUE_SIZE_EXT = 0x802A
    const val GL_HISTOGRAM_ALPHA_SIZE_EXT = 0x802B
    const val GL_HISTOGRAM_LUMINANCE_SIZE_EXT = 0x802C
    const val GL_HISTOGRAM_SINK_EXT = 0x802D
    const val GL_MINMAX_EXT = 0x802E
    const val GL_MINMAX_FORMAT_EXT = 0x802F
    const val GL_MINMAX_SINK_EXT = 0x8030
    const val GL_TABLE_TOO_LARGE_EXT = 0x8031
    const val GL_IUI_V2F_EXT = 0x81AD
    const val GL_IUI_V3F_EXT = 0x81AE
    const val GL_IUI_N3F_V2F_EXT = 0x81AF
    const val GL_IUI_N3F_V3F_EXT = 0x81B0
    const val GL_T2F_IUI_V2F_EXT = 0x81B1
    const val GL_T2F_IUI_V3F_EXT = 0x81B2
    const val GL_T2F_IUI_N3F_V2F_EXT = 0x81B3
    const val GL_T2F_IUI_N3F_V3F_EXT = 0x81B4
    const val GL_INDEX_TEST_EXT = 0x81B5
    const val GL_INDEX_TEST_FUNC_EXT = 0x81B6
    const val GL_INDEX_TEST_REF_EXT = 0x81B7
    const val GL_INDEX_MATERIAL_EXT = 0x81B8
    const val GL_INDEX_MATERIAL_PARAMETER_EXT = 0x81B9
    const val GL_INDEX_MATERIAL_FACE_EXT = 0x81BA
    const val GL_FRAGMENT_MATERIAL_EXT = 0x8349
    const val GL_FRAGMENT_NORMAL_EXT = 0x834A
    const val GL_FRAGMENT_COLOR_EXT = 0x834C
    const val GL_ATTENUATION_EXT = 0x834D
    const val GL_SHADOW_ATTENUATION_EXT = 0x834E
    const val GL_TEXTURE_APPLICATION_MODE_EXT = 0x834F
    const val GL_TEXTURE_LIGHT_EXT = 0x8350
    const val GL_TEXTURE_MATERIAL_FACE_EXT = 0x8351
    const val GL_TEXTURE_MATERIAL_PARAMETER_EXT = 0x8352
    const val GL_TEXTURE_TILING_EXT = 0x9580
    const val GL_DEDICATED_MEMORY_OBJECT_EXT = 0x9581
    const val GL_PROTECTED_MEMORY_OBJECT_EXT = 0x959B
    const val GL_NUM_TILING_TYPES_EXT = 0x9582
    const val GL_TILING_TYPES_EXT = 0x9583
    const val GL_OPTIMAL_TILING_EXT = 0x9584
    const val GL_LINEAR_TILING_EXT = 0x9585
    const val GL_NUM_DEVICE_UUIDS_EXT = 0x9596
    const val GL_DEVICE_UUID_EXT = 0x9597
    const val GL_DRIVER_UUID_EXT = 0x9598
    const val GL_UUID_SIZE_EXT = 16
    const val GL_HANDLE_TYPE_OPAQUE_FD_EXT = 0x9586
    const val GL_HANDLE_TYPE_OPAQUE_WIN32_EXT = 0x9587
    const val GL_HANDLE_TYPE_OPAQUE_WIN32_KMT_EXT = 0x9588
    const val GL_DEVICE_LUID_EXT = 0x9599
    const val GL_DEVICE_NODE_MASK_EXT = 0x959A
    const val GL_LUID_SIZE_EXT = 8
    const val GL_HANDLE_TYPE_D3D12_TILEPOOL_EXT = 0x9589
    const val GL_HANDLE_TYPE_D3D12_RESOURCE_EXT = 0x958A
    const val GL_HANDLE_TYPE_D3D11_IMAGE_EXT = 0x958B
    const val GL_HANDLE_TYPE_D3D11_IMAGE_KMT_EXT = 0x958C
    const val GL_MULTISAMPLE_EXT = 0x809D
    const val GL_SAMPLE_ALPHA_TO_MASK_EXT = 0x809E
    const val GL_SAMPLE_ALPHA_TO_ONE_EXT = 0x809F
    const val GL_SAMPLE_MASK_EXT = 0x80A0
    const val GL_1PASS_EXT = 0x80A1
    const val GL_2PASS_0_EXT = 0x80A2
    const val GL_2PASS_1_EXT = 0x80A3
    const val GL_4PASS_0_EXT = 0x80A4
    const val GL_4PASS_1_EXT = 0x80A5
    const val GL_4PASS_2_EXT = 0x80A6
    const val GL_4PASS_3_EXT = 0x80A7
    const val GL_SAMPLE_BUFFERS_EXT = 0x80A8
    const val GL_SAMPLES_EXT = 0x80A9
    const val GL_SAMPLE_MASK_VALUE_EXT = 0x80AA
    const val GL_SAMPLE_MASK_INVERT_EXT = 0x80AB
    const val GL_SAMPLE_PATTERN_EXT = 0x80AC
    const val GL_MULTISAMPLE_BIT_EXT = 0x20000000
    const val GL_DEPTH_STENCIL_EXT = 0x84F9
    const val GL_UNSIGNED_INT_24_8_EXT = 0x84FA
    const val GL_DEPTH24_STENCIL8_EXT = 0x88F0
    const val GL_TEXTURE_STENCIL_SIZE_EXT = 0x88F1
    const val GL_R11F_G11F_B10F_EXT = 0x8C3A
    const val GL_UNSIGNED_INT_10F_11F_11F_REV_EXT = 0x8C3B
    const val GL_RGBA_SIGNED_COMPONENTS_EXT = 0x8C3C
    const val GL_UNSIGNED_BYTE_3_3_2_EXT = 0x8032
    const val GL_UNSIGNED_SHORT_4_4_4_4_EXT = 0x8033
    const val GL_UNSIGNED_SHORT_5_5_5_1_EXT = 0x8034
    const val GL_UNSIGNED_INT_8_8_8_8_EXT = 0x8035
    const val GL_UNSIGNED_INT_10_10_10_2_EXT = 0x8036
    const val GL_COLOR_INDEX1_EXT = 0x80E2
    const val GL_COLOR_INDEX2_EXT = 0x80E3
    const val GL_COLOR_INDEX4_EXT = 0x80E4
    const val GL_COLOR_INDEX8_EXT = 0x80E5
    const val GL_COLOR_INDEX12_EXT = 0x80E6
    const val GL_COLOR_INDEX16_EXT = 0x80E7
    const val GL_TEXTURE_INDEX_SIZE_EXT = 0x80ED
    const val GL_PIXEL_PACK_BUFFER_EXT = 0x88EB
    const val GL_PIXEL_UNPACK_BUFFER_EXT = 0x88EC
    const val GL_PIXEL_PACK_BUFFER_BINDING_EXT = 0x88ED
    const val GL_PIXEL_UNPACK_BUFFER_BINDING_EXT = 0x88EF
    const val GL_PIXEL_TRANSFORM_2D_EXT = 0x8330
    const val GL_PIXEL_MAG_FILTER_EXT = 0x8331
    const val GL_PIXEL_MIN_FILTER_EXT = 0x8332
    const val GL_PIXEL_CUBIC_WEIGHT_EXT = 0x8333
    const val GL_CUBIC_EXT = 0x8334
    const val GL_AVERAGE_EXT = 0x8335
    const val GL_PIXEL_TRANSFORM_2D_STACK_DEPTH_EXT = 0x8336
    const val GL_MAX_PIXEL_TRANSFORM_2D_STACK_DEPTH_EXT = 0x8337
    const val GL_PIXEL_TRANSFORM_2D_MATRIX_EXT = 0x8338
    const val GL_POINT_SIZE_MIN_EXT = 0x8126
    const val GL_POINT_SIZE_MAX_EXT = 0x8127
    const val GL_POINT_FADE_THRESHOLD_SIZE_EXT = 0x8128
    const val GL_DISTANCE_ATTENUATION_EXT = 0x8129
    const val GL_POLYGON_OFFSET_EXT = 0x8037
    const val GL_POLYGON_OFFSET_FACTOR_EXT = 0x8038
    const val GL_POLYGON_OFFSET_BIAS_EXT = 0x8039
    const val GL_POLYGON_OFFSET_CLAMP_EXT = 0x8E1B
    const val GL_QUADS_FOLLOW_PROVOKING_VERTEX_CONVENTION_EXT = 0x8E4C
    const val GL_FIRST_VERTEX_CONVENTION_EXT = 0x8E4D
    const val GL_LAST_VERTEX_CONVENTION_EXT = 0x8E4E
    const val GL_PROVOKING_VERTEX_EXT = 0x8E4F
    const val GL_RASTER_MULTISAMPLE_EXT = 0x9327
    const val GL_RASTER_SAMPLES_EXT = 0x9328
    const val GL_MAX_RASTER_SAMPLES_EXT = 0x9329
    const val GL_RASTER_FIXED_SAMPLE_LOCATIONS_EXT = 0x932A
    const val GL_MULTISAMPLE_RASTERIZATION_ALLOWED_EXT = 0x932B
    const val GL_EFFECTIVE_RASTER_SAMPLES_EXT = 0x932C
    const val GL_RESCALE_NORMAL_EXT = 0x803A
    const val GL_COLOR_SUM_EXT = 0x8458
    const val GL_CURRENT_SECONDARY_COLOR_EXT = 0x8459
    const val GL_SECONDARY_COLOR_ARRAY_SIZE_EXT = 0x845A
    const val GL_SECONDARY_COLOR_ARRAY_TYPE_EXT = 0x845B
    const val GL_SECONDARY_COLOR_ARRAY_STRIDE_EXT = 0x845C
    const val GL_SECONDARY_COLOR_ARRAY_POINTER_EXT = 0x845D
    const val GL_SECONDARY_COLOR_ARRAY_EXT = 0x845E
    const val GL_LAYOUT_GENERAL_EXT = 0x958D
    const val GL_LAYOUT_COLOR_ATTACHMENT_EXT = 0x958E
    const val GL_LAYOUT_DEPTH_STENCIL_ATTACHMENT_EXT = 0x958F
    const val GL_LAYOUT_DEPTH_STENCIL_READ_ONLY_EXT = 0x9590
    const val GL_LAYOUT_SHADER_READ_ONLY_EXT = 0x9591
    const val GL_LAYOUT_TRANSFER_SRC_EXT = 0x9592
    const val GL_LAYOUT_TRANSFER_DST_EXT = 0x9593
    const val GL_LAYOUT_DEPTH_READ_ONLY_STENCIL_ATTACHMENT_EXT = 0x9530
    const val GL_LAYOUT_DEPTH_ATTACHMENT_STENCIL_READ_ONLY_EXT = 0x9531
    const val GL_HANDLE_TYPE_D3D12_FENCE_EXT = 0x9594
    const val GL_D3D12_FENCE_VALUE_EXT = 0x9595
    const val GL_ACTIVE_PROGRAM_EXT = 0x8B8D
    const val GL_VERTEX_SHADER_BIT_EXT = 0x00000001
    const val GL_FRAGMENT_SHADER_BIT_EXT = 0x00000002
    const val GL_ALL_SHADER_BITS_EXT = 0xFFFFFFFF
    const val GL_PROGRAM_SEPARABLE_EXT = 0x8258
    const val GL_PROGRAM_PIPELINE_BINDING_EXT = 0x825A
    const val GL_LIGHT_MODEL_COLOR_CONTROL_EXT = 0x81F8
    const val GL_SINGLE_COLOR_EXT = 0x81F9
    const val GL_SEPARATE_SPECULAR_COLOR_EXT = 0x81FA
    const val GL_FRAGMENT_SHADER_DISCARDS_SAMPLES_EXT = 0x8A52
    const val GL_MAX_IMAGE_UNITS_EXT = 0x8F38
    const val GL_MAX_COMBINED_IMAGE_UNITS_AND_FRAGMENT_OUTPUTS_EXT = 0x8F39
    const val GL_IMAGE_BINDING_NAME_EXT = 0x8F3A
    const val GL_IMAGE_BINDING_LEVEL_EXT = 0x8F3B
    const val GL_IMAGE_BINDING_LAYERED_EXT = 0x8F3C
    const val GL_IMAGE_BINDING_LAYER_EXT = 0x8F3D
    const val GL_IMAGE_BINDING_ACCESS_EXT = 0x8F3E
    const val GL_IMAGE_1D_EXT = 0x904C
    const val GL_IMAGE_2D_EXT = 0x904D
    const val GL_IMAGE_3D_EXT = 0x904E
    const val GL_IMAGE_2D_RECT_EXT = 0x904F
    const val GL_IMAGE_CUBE_EXT = 0x9050
    const val GL_IMAGE_BUFFER_EXT = 0x9051
    const val GL_IMAGE_1D_ARRAY_EXT = 0x9052
    const val GL_IMAGE_2D_ARRAY_EXT = 0x9053
    const val GL_IMAGE_CUBE_MAP_ARRAY_EXT = 0x9054
    const val GL_IMAGE_2D_MULTISAMPLE_EXT = 0x9055
    const val GL_IMAGE_2D_MULTISAMPLE_ARRAY_EXT = 0x9056
    const val GL_INT_IMAGE_1D_EXT = 0x9057
    const val GL_INT_IMAGE_2D_EXT = 0x9058
    const val GL_INT_IMAGE_3D_EXT = 0x9059
    const val GL_INT_IMAGE_2D_RECT_EXT = 0x905A
    const val GL_INT_IMAGE_CUBE_EXT = 0x905B
    const val GL_INT_IMAGE_BUFFER_EXT = 0x905C
    const val GL_INT_IMAGE_1D_ARRAY_EXT = 0x905D
    const val GL_INT_IMAGE_2D_ARRAY_EXT = 0x905E
    const val GL_INT_IMAGE_CUBE_MAP_ARRAY_EXT = 0x905F
    const val GL_INT_IMAGE_2D_MULTISAMPLE_EXT = 0x9060
    const val GL_INT_IMAGE_2D_MULTISAMPLE_ARRAY_EXT = 0x9061
    const val GL_UNSIGNED_INT_IMAGE_1D_EXT = 0x9062
    const val GL_UNSIGNED_INT_IMAGE_2D_EXT = 0x9063
    const val GL_UNSIGNED_INT_IMAGE_3D_EXT = 0x9064
    const val GL_UNSIGNED_INT_IMAGE_2D_RECT_EXT = 0x9065
    const val GL_UNSIGNED_INT_IMAGE_CUBE_EXT = 0x9066
    const val GL_UNSIGNED_INT_IMAGE_BUFFER_EXT = 0x9067
    const val GL_UNSIGNED_INT_IMAGE_1D_ARRAY_EXT = 0x9068
    const val GL_UNSIGNED_INT_IMAGE_2D_ARRAY_EXT = 0x9069
    const val GL_UNSIGNED_INT_IMAGE_CUBE_MAP_ARRAY_EXT = 0x906A
    const val GL_UNSIGNED_INT_IMAGE_2D_MULTISAMPLE_EXT = 0x906B
    const val GL_UNSIGNED_INT_IMAGE_2D_MULTISAMPLE_ARRAY_EXT = 0x906C
    const val GL_MAX_IMAGE_SAMPLES_EXT = 0x906D
    const val GL_IMAGE_BINDING_FORMAT_EXT = 0x906E
    const val GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT_EXT = 0x00000001
    const val GL_ELEMENT_ARRAY_BARRIER_BIT_EXT = 0x00000002
    const val GL_UNIFORM_BARRIER_BIT_EXT = 0x00000004
    const val GL_TEXTURE_FETCH_BARRIER_BIT_EXT = 0x00000008
    const val GL_SHADER_IMAGE_ACCESS_BARRIER_BIT_EXT = 0x00000020
    const val GL_COMMAND_BARRIER_BIT_EXT = 0x00000040
    const val GL_PIXEL_BUFFER_BARRIER_BIT_EXT = 0x00000080
    const val GL_TEXTURE_UPDATE_BARRIER_BIT_EXT = 0x00000100
    const val GL_BUFFER_UPDATE_BARRIER_BIT_EXT = 0x00000200
    const val GL_FRAMEBUFFER_BARRIER_BIT_EXT = 0x00000400
    const val GL_TRANSFORM_FEEDBACK_BARRIER_BIT_EXT = 0x00000800
    const val GL_ATOMIC_COUNTER_BARRIER_BIT_EXT = 0x00001000
    const val GL_ALL_BARRIER_BITS_EXT = 0xFFFFFFFF
    const val GL_SHARED_TEXTURE_PALETTE_EXT = 0x81FB
    const val GL_STENCIL_TAG_BITS_EXT = 0x88F2
    const val GL_STENCIL_CLEAR_TAG_VALUE_EXT = 0x88F3
    const val GL_STENCIL_TEST_TWO_SIDE_EXT = 0x8910
    const val GL_ACTIVE_STENCIL_FACE_EXT = 0x8911
    const val GL_INCR_WRAP_EXT = 0x8507
    const val GL_DECR_WRAP_EXT = 0x8508
    const val GL_ALPHA4_EXT = 0x803B
    const val GL_ALPHA8_EXT = 0x803C
    const val GL_ALPHA12_EXT = 0x803D
    const val GL_ALPHA16_EXT = 0x803E
    const val GL_LUMINANCE4_EXT = 0x803F
    const val GL_LUMINANCE8_EXT = 0x8040
    const val GL_LUMINANCE12_EXT = 0x8041
    const val GL_LUMINANCE16_EXT = 0x8042
    const val GL_LUMINANCE4_ALPHA4_EXT = 0x8043
    const val GL_LUMINANCE6_ALPHA2_EXT = 0x8044
    const val GL_LUMINANCE8_ALPHA8_EXT = 0x8045
    const val GL_LUMINANCE12_ALPHA4_EXT = 0x8046
    const val GL_LUMINANCE12_ALPHA12_EXT = 0x8047
    const val GL_LUMINANCE16_ALPHA16_EXT = 0x8048
    const val GL_INTENSITY_EXT = 0x8049
    const val GL_INTENSITY4_EXT = 0x804A
    const val GL_INTENSITY8_EXT = 0x804B
    const val GL_INTENSITY12_EXT = 0x804C
    const val GL_INTENSITY16_EXT = 0x804D
    const val GL_RGB2_EXT = 0x804E
    const val GL_RGB4_EXT = 0x804F
    const val GL_RGB5_EXT = 0x8050
    const val GL_RGB8_EXT = 0x8051
    const val GL_RGB10_EXT = 0x8052
    const val GL_RGB12_EXT = 0x8053
    const val GL_RGB16_EXT = 0x8054
    const val GL_RGBA2_EXT = 0x8055
    const val GL_RGBA4_EXT = 0x8056
    const val GL_RGB5_A1_EXT = 0x8057
    const val GL_RGBA8_EXT = 0x8058
    const val GL_RGB10_A2_EXT = 0x8059
    const val GL_RGBA12_EXT = 0x805A
    const val GL_RGBA16_EXT = 0x805B
    const val GL_TEXTURE_RED_SIZE_EXT = 0x805C
    const val GL_TEXTURE_GREEN_SIZE_EXT = 0x805D
    const val GL_TEXTURE_BLUE_SIZE_EXT = 0x805E
    const val GL_TEXTURE_ALPHA_SIZE_EXT = 0x805F
    const val GL_TEXTURE_LUMINANCE_SIZE_EXT = 0x8060
    const val GL_TEXTURE_INTENSITY_SIZE_EXT = 0x8061
    const val GL_REPLACE_EXT = 0x8062
    const val GL_PROXY_TEXTURE_1D_EXT = 0x8063
    const val GL_PROXY_TEXTURE_2D_EXT = 0x8064
    const val GL_TEXTURE_TOO_LARGE_EXT = 0x8065
    const val GL_PACK_SKIP_IMAGES_EXT = 0x806B
    const val GL_PACK_IMAGE_HEIGHT_EXT = 0x806C
    const val GL_UNPACK_SKIP_IMAGES_EXT = 0x806D
    const val GL_UNPACK_IMAGE_HEIGHT_EXT = 0x806E
    const val GL_TEXTURE_3D_EXT = 0x806F
    const val GL_PROXY_TEXTURE_3D_EXT = 0x8070
    const val GL_TEXTURE_DEPTH_EXT = 0x8071
    const val GL_TEXTURE_WRAP_R_EXT = 0x8072
    const val GL_MAX_3D_TEXTURE_SIZE_EXT = 0x8073
    const val GL_TEXTURE_1D_ARRAY_EXT = 0x8C18
    const val GL_PROXY_TEXTURE_1D_ARRAY_EXT = 0x8C19
    const val GL_TEXTURE_2D_ARRAY_EXT = 0x8C1A
    const val GL_PROXY_TEXTURE_2D_ARRAY_EXT = 0x8C1B
    const val GL_TEXTURE_BINDING_1D_ARRAY_EXT = 0x8C1C
    const val GL_TEXTURE_BINDING_2D_ARRAY_EXT = 0x8C1D
    const val GL_MAX_ARRAY_TEXTURE_LAYERS_EXT = 0x88FF
    const val GL_COMPARE_REF_DEPTH_TO_TEXTURE_EXT = 0x884E
    const val GL_TEXTURE_BUFFER_EXT = 0x8C2A
    const val GL_MAX_TEXTURE_BUFFER_SIZE_EXT = 0x8C2B
    const val GL_TEXTURE_BINDING_BUFFER_EXT = 0x8C2C
    const val GL_TEXTURE_BUFFER_DATA_STORE_BINDING_EXT = 0x8C2D
    const val GL_TEXTURE_BUFFER_FORMAT_EXT = 0x8C2E
    const val GL_COMPRESSED_LUMINANCE_LATC1_EXT = 0x8C70
    const val GL_COMPRESSED_SIGNED_LUMINANCE_LATC1_EXT = 0x8C71
    const val GL_COMPRESSED_LUMINANCE_ALPHA_LATC2_EXT = 0x8C72
    const val GL_COMPRESSED_SIGNED_LUMINANCE_ALPHA_LATC2_EXT = 0x8C73
    const val GL_COMPRESSED_RED_RGTC1_EXT = 0x8DBB
    const val GL_COMPRESSED_SIGNED_RED_RGTC1_EXT = 0x8DBC
    const val GL_COMPRESSED_RED_GREEN_RGTC2_EXT = 0x8DBD
    const val GL_COMPRESSED_SIGNED_RED_GREEN_RGTC2_EXT = 0x8DBE
    const val GL_COMPRESSED_RGB_S3TC_DXT1_EXT = 0x83F0
    const val GL_COMPRESSED_RGBA_S3TC_DXT1_EXT = 0x83F1
    const val GL_COMPRESSED_RGBA_S3TC_DXT3_EXT = 0x83F2
    const val GL_COMPRESSED_RGBA_S3TC_DXT5_EXT = 0x83F3
    const val GL_NORMAL_MAP_EXT = 0x8511
    const val GL_REFLECTION_MAP_EXT = 0x8512
    const val GL_TEXTURE_CUBE_MAP_EXT = 0x8513
    const val GL_TEXTURE_BINDING_CUBE_MAP_EXT = 0x8514
    const val GL_TEXTURE_CUBE_MAP_POSITIVE_X_EXT = 0x8515
    const val GL_TEXTURE_CUBE_MAP_NEGATIVE_X_EXT = 0x8516
    const val GL_TEXTURE_CUBE_MAP_POSITIVE_Y_EXT = 0x8517
    const val GL_TEXTURE_CUBE_MAP_NEGATIVE_Y_EXT = 0x8518
    const val GL_TEXTURE_CUBE_MAP_POSITIVE_Z_EXT = 0x8519
    const val GL_TEXTURE_CUBE_MAP_NEGATIVE_Z_EXT = 0x851A
    const val GL_PROXY_TEXTURE_CUBE_MAP_EXT = 0x851B
    const val GL_MAX_CUBE_MAP_TEXTURE_SIZE_EXT = 0x851C
    const val GL_COMBINE_EXT = 0x8570
    const val GL_COMBINE_RGB_EXT = 0x8571
    const val GL_COMBINE_ALPHA_EXT = 0x8572
    const val GL_RGB_SCALE_EXT = 0x8573
    const val GL_ADD_SIGNED_EXT = 0x8574
    const val GL_INTERPOLATE_EXT = 0x8575
    const val GL_CONSTANT_EXT = 0x8576
    const val GL_PRIMARY_COLOR_EXT = 0x8577
    const val GL_PREVIOUS_EXT = 0x8578
    const val GL_SOURCE0_RGB_EXT = 0x8580
    const val GL_SOURCE1_RGB_EXT = 0x8581
    const val GL_SOURCE2_RGB_EXT = 0x8582
    const val GL_SOURCE0_ALPHA_EXT = 0x8588
    const val GL_SOURCE1_ALPHA_EXT = 0x8589
    const val GL_SOURCE2_ALPHA_EXT = 0x858A
    const val GL_OPERAND0_RGB_EXT = 0x8590
    const val GL_OPERAND1_RGB_EXT = 0x8591
    const val GL_OPERAND2_RGB_EXT = 0x8592
    const val GL_OPERAND0_ALPHA_EXT = 0x8598
    const val GL_OPERAND1_ALPHA_EXT = 0x8599
    const val GL_OPERAND2_ALPHA_EXT = 0x859A
    const val GL_DOT3_RGB_EXT = 0x8740
    const val GL_DOT3_RGBA_EXT = 0x8741
    const val GL_TEXTURE_MAX_ANISOTROPY_EXT = 0x84FE
    const val GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT = 0x84FF
    const val GL_TEXTURE_REDUCTION_MODE_EXT = 0x9366
    const val GL_WEIGHTED_AVERAGE_EXT = 0x9367
    const val GL_RGBA32UI_EXT = 0x8D70
    const val GL_RGB32UI_EXT = 0x8D71
    const val GL_ALPHA32UI_EXT = 0x8D72
    const val GL_INTENSITY32UI_EXT = 0x8D73
    const val GL_LUMINANCE32UI_EXT = 0x8D74
    const val GL_LUMINANCE_ALPHA32UI_EXT = 0x8D75
    const val GL_RGBA16UI_EXT = 0x8D76
    const val GL_RGB16UI_EXT = 0x8D77
    const val GL_ALPHA16UI_EXT = 0x8D78
    const val GL_INTENSITY16UI_EXT = 0x8D79
    const val GL_LUMINANCE16UI_EXT = 0x8D7A
    const val GL_LUMINANCE_ALPHA16UI_EXT = 0x8D7B
    const val GL_RGBA8UI_EXT = 0x8D7C
    const val GL_RGB8UI_EXT = 0x8D7D
    const val GL_ALPHA8UI_EXT = 0x8D7E
    const val GL_INTENSITY8UI_EXT = 0x8D7F
    const val GL_LUMINANCE8UI_EXT = 0x8D80
    const val GL_LUMINANCE_ALPHA8UI_EXT = 0x8D81
    const val GL_RGBA32I_EXT = 0x8D82
    const val GL_RGB32I_EXT = 0x8D83
    const val GL_ALPHA32I_EXT = 0x8D84
    const val GL_INTENSITY32I_EXT = 0x8D85
    const val GL_LUMINANCE32I_EXT = 0x8D86
    const val GL_LUMINANCE_ALPHA32I_EXT = 0x8D87
    const val GL_RGBA16I_EXT = 0x8D88
    const val GL_RGB16I_EXT = 0x8D89
    const val GL_ALPHA16I_EXT = 0x8D8A
    const val GL_INTENSITY16I_EXT = 0x8D8B
    const val GL_LUMINANCE16I_EXT = 0x8D8C
    const val GL_LUMINANCE_ALPHA16I_EXT = 0x8D8D
    const val GL_RGBA8I_EXT = 0x8D8E
    const val GL_RGB8I_EXT = 0x8D8F
    const val GL_ALPHA8I_EXT = 0x8D90
    const val GL_INTENSITY8I_EXT = 0x8D91
    const val GL_LUMINANCE8I_EXT = 0x8D92
    const val GL_LUMINANCE_ALPHA8I_EXT = 0x8D93
    const val GL_RED_INTEGER_EXT = 0x8D94
    const val GL_GREEN_INTEGER_EXT = 0x8D95
    const val GL_BLUE_INTEGER_EXT = 0x8D96
    const val GL_ALPHA_INTEGER_EXT = 0x8D97
    const val GL_RGB_INTEGER_EXT = 0x8D98
    const val GL_RGBA_INTEGER_EXT = 0x8D99
    const val GL_BGR_INTEGER_EXT = 0x8D9A
    const val GL_BGRA_INTEGER_EXT = 0x8D9B
    const val GL_LUMINANCE_INTEGER_EXT = 0x8D9C
    const val GL_LUMINANCE_ALPHA_INTEGER_EXT = 0x8D9D
    const val GL_RGBA_INTEGER_MODE_EXT = 0x8D9E
    const val GL_MAX_TEXTURE_LOD_BIAS_EXT = 0x84FD
    const val GL_TEXTURE_FILTER_CONTROL_EXT = 0x8500
    const val GL_TEXTURE_LOD_BIAS_EXT = 0x8501
    const val GL_MIRROR_CLAMP_EXT = 0x8742
    const val GL_MIRROR_CLAMP_TO_EDGE_EXT = 0x8743
    const val GL_MIRROR_CLAMP_TO_BORDER_EXT = 0x8912
    const val GL_TEXTURE_PRIORITY_EXT = 0x8066
    const val GL_TEXTURE_RESIDENT_EXT = 0x8067
    const val GL_TEXTURE_1D_BINDING_EXT = 0x8068
    const val GL_TEXTURE_2D_BINDING_EXT = 0x8069
    const val GL_TEXTURE_3D_BINDING_EXT = 0x806A
    const val GL_PERTURB_EXT = 0x85AE
    const val GL_TEXTURE_NORMAL_EXT = 0x85AF
    const val GL_SRGB_EXT = 0x8C40
    const val GL_SRGB8_EXT = 0x8C41
    const val GL_SRGB_ALPHA_EXT = 0x8C42
    const val GL_SRGB8_ALPHA8_EXT = 0x8C43
    const val GL_SLUMINANCE_ALPHA_EXT = 0x8C44
    const val GL_SLUMINANCE8_ALPHA8_EXT = 0x8C45
    const val GL_SLUMINANCE_EXT = 0x8C46
    const val GL_SLUMINANCE8_EXT = 0x8C47
    const val GL_COMPRESSED_SRGB_EXT = 0x8C48
    const val GL_COMPRESSED_SRGB_ALPHA_EXT = 0x8C49
    const val GL_COMPRESSED_SLUMINANCE_EXT = 0x8C4A
    const val GL_COMPRESSED_SLUMINANCE_ALPHA_EXT = 0x8C4B
    const val GL_COMPRESSED_SRGB_S3TC_DXT1_EXT = 0x8C4C
    const val GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT1_EXT = 0x8C4D
    const val GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT3_EXT = 0x8C4E
    const val GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT5_EXT = 0x8C4F
    const val GL_SR8_EXT = 0x8FBD
    const val GL_SRG8_EXT = 0x8FBE
    const val GL_TEXTURE_SRGB_DECODE_EXT = 0x8A48
    const val GL_DECODE_EXT = 0x8A49
    const val GL_SKIP_DECODE_EXT = 0x8A4A
    const val GL_RGB9_E5_EXT = 0x8C3D
    const val GL_UNSIGNED_INT_5_9_9_9_REV_EXT = 0x8C3E
    const val GL_TEXTURE_SHARED_SIZE_EXT = 0x8C3F
    const val GL_ALPHA_SNORM = 0x9010
    const val GL_LUMINANCE_SNORM = 0x9011
    const val GL_LUMINANCE_ALPHA_SNORM = 0x9012
    const val GL_INTENSITY_SNORM = 0x9013
    const val GL_ALPHA8_SNORM = 0x9014
    const val GL_LUMINANCE8_SNORM = 0x9015
    const val GL_LUMINANCE8_ALPHA8_SNORM = 0x9016
    const val GL_INTENSITY8_SNORM = 0x9017
    const val GL_ALPHA16_SNORM = 0x9018
    const val GL_LUMINANCE16_SNORM = 0x9019
    const val GL_LUMINANCE16_ALPHA16_SNORM = 0x901A
    const val GL_INTENSITY16_SNORM = 0x901B
    const val GL_RED_SNORM = 0x8F90
    const val GL_RG_SNORM = 0x8F91
    const val GL_RGB_SNORM = 0x8F92
    const val GL_RGBA_SNORM = 0x8F93
    const val GL_TEXTURE_IMMUTABLE_FORMAT_EXT = 0x912F
    const val GL_RGBA32F_EXT = 0x8814
    const val GL_RGB32F_EXT = 0x8815
    const val GL_ALPHA32F_EXT = 0x8816
    const val GL_LUMINANCE32F_EXT = 0x8818
    const val GL_LUMINANCE_ALPHA32F_EXT = 0x8819
    const val GL_RGBA16F_EXT = 0x881A
    const val GL_RGB16F_EXT = 0x881B
    const val GL_ALPHA16F_EXT = 0x881C
    const val GL_LUMINANCE16F_EXT = 0x881E
    const val GL_LUMINANCE_ALPHA16F_EXT = 0x881F
    const val GL_BGRA8_EXT = 0x93A1
    const val GL_R8_EXT = 0x8229
    const val GL_RG8_EXT = 0x822B
    const val GL_R32F_EXT = 0x822E
    const val GL_RG32F_EXT = 0x8230
    const val GL_R16F_EXT = 0x822D
    const val GL_RG16F_EXT = 0x822F
    const val GL_TEXTURE_SWIZZLE_R_EXT = 0x8E42
    const val GL_TEXTURE_SWIZZLE_G_EXT = 0x8E43
    const val GL_TEXTURE_SWIZZLE_B_EXT = 0x8E44
    const val GL_TEXTURE_SWIZZLE_A_EXT = 0x8E45
    const val GL_TEXTURE_SWIZZLE_RGBA_EXT = 0x8E46
    const val GL_TIME_ELAPSED_EXT = 0x88BF
    const val GL_TRANSFORM_FEEDBACK_BUFFER_EXT = 0x8C8E
    const val GL_TRANSFORM_FEEDBACK_BUFFER_START_EXT = 0x8C84
    const val GL_TRANSFORM_FEEDBACK_BUFFER_SIZE_EXT = 0x8C85
    const val GL_TRANSFORM_FEEDBACK_BUFFER_BINDING_EXT = 0x8C8F
    const val GL_INTERLEAVED_ATTRIBS_EXT = 0x8C8C
    const val GL_SEPARATE_ATTRIBS_EXT = 0x8C8D
    const val GL_PRIMITIVES_GENERATED_EXT = 0x8C87
    const val GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN_EXT = 0x8C88
    const val GL_RASTERIZER_DISCARD_EXT = 0x8C89
    const val GL_MAX_TRANSFORM_FEEDBACK_INTERLEAVED_COMPONENTS_EXT = 0x8C8A
    const val GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_ATTRIBS_EXT = 0x8C8B
    const val GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_COMPONENTS_EXT = 0x8C80
    const val GL_TRANSFORM_FEEDBACK_VARYINGS_EXT = 0x8C83
    const val GL_TRANSFORM_FEEDBACK_BUFFER_MODE_EXT = 0x8C7F
    const val GL_TRANSFORM_FEEDBACK_VARYING_MAX_LENGTH_EXT = 0x8C76
    const val GL_VERTEX_ARRAY_EXT = 0x8074
    const val GL_NORMAL_ARRAY_EXT = 0x8075
    const val GL_COLOR_ARRAY_EXT = 0x8076
    const val GL_INDEX_ARRAY_EXT = 0x8077
    const val GL_TEXTURE_COORD_ARRAY_EXT = 0x8078
    const val GL_EDGE_FLAG_ARRAY_EXT = 0x8079
    const val GL_VERTEX_ARRAY_SIZE_EXT = 0x807A
    const val GL_VERTEX_ARRAY_TYPE_EXT = 0x807B
    const val GL_VERTEX_ARRAY_STRIDE_EXT = 0x807C
    const val GL_VERTEX_ARRAY_COUNT_EXT = 0x807D
    const val GL_NORMAL_ARRAY_TYPE_EXT = 0x807E
    const val GL_NORMAL_ARRAY_STRIDE_EXT = 0x807F
    const val GL_NORMAL_ARRAY_COUNT_EXT = 0x8080
    const val GL_COLOR_ARRAY_SIZE_EXT = 0x8081
    const val GL_COLOR_ARRAY_TYPE_EXT = 0x8082
    const val GL_COLOR_ARRAY_STRIDE_EXT = 0x8083
    const val GL_COLOR_ARRAY_COUNT_EXT = 0x8084
    const val GL_INDEX_ARRAY_TYPE_EXT = 0x8085
    const val GL_INDEX_ARRAY_STRIDE_EXT = 0x8086
    const val GL_INDEX_ARRAY_COUNT_EXT = 0x8087
    const val GL_TEXTURE_COORD_ARRAY_SIZE_EXT = 0x8088
    const val GL_TEXTURE_COORD_ARRAY_TYPE_EXT = 0x8089
    const val GL_TEXTURE_COORD_ARRAY_STRIDE_EXT = 0x808A
    const val GL_TEXTURE_COORD_ARRAY_COUNT_EXT = 0x808B
    const val GL_EDGE_FLAG_ARRAY_STRIDE_EXT = 0x808C
    const val GL_EDGE_FLAG_ARRAY_COUNT_EXT = 0x808D
    const val GL_VERTEX_ARRAY_POINTER_EXT = 0x808E
    const val GL_NORMAL_ARRAY_POINTER_EXT = 0x808F
    const val GL_COLOR_ARRAY_POINTER_EXT = 0x8090
    const val GL_INDEX_ARRAY_POINTER_EXT = 0x8091
    const val GL_TEXTURE_COORD_ARRAY_POINTER_EXT = 0x8092
    const val GL_EDGE_FLAG_ARRAY_POINTER_EXT = 0x8093
    const val GL_DOUBLE_VEC2_EXT = 0x8FFC
    const val GL_DOUBLE_VEC3_EXT = 0x8FFD
    const val GL_DOUBLE_VEC4_EXT = 0x8FFE
    const val GL_DOUBLE_MAT2_EXT = 0x8F46
    const val GL_DOUBLE_MAT3_EXT = 0x8F47
    const val GL_DOUBLE_MAT4_EXT = 0x8F48
    const val GL_DOUBLE_MAT2x3_EXT = 0x8F49
    const val GL_DOUBLE_MAT2x4_EXT = 0x8F4A
    const val GL_DOUBLE_MAT3x2_EXT = 0x8F4B
    const val GL_DOUBLE_MAT3x4_EXT = 0x8F4C
    const val GL_DOUBLE_MAT4x2_EXT = 0x8F4D
    const val GL_DOUBLE_MAT4x3_EXT = 0x8F4E
    const val GL_VERTEX_SHADER_EXT = 0x8780
    const val GL_VERTEX_SHADER_BINDING_EXT = 0x8781
    const val GL_OP_INDEX_EXT = 0x8782
    const val GL_OP_NEGATE_EXT = 0x8783
    const val GL_OP_DOT3_EXT = 0x8784
    const val GL_OP_DOT4_EXT = 0x8785
    const val GL_OP_MUL_EXT = 0x8786
    const val GL_OP_ADD_EXT = 0x8787
    const val GL_OP_MADD_EXT = 0x8788
    const val GL_OP_FRAC_EXT = 0x8789
    const val GL_OP_MAX_EXT = 0x878A
    const val GL_OP_MIN_EXT = 0x878B
    const val GL_OP_SET_GE_EXT = 0x878C
    const val GL_OP_SET_LT_EXT = 0x878D
    const val GL_OP_CLAMP_EXT = 0x878E
    const val GL_OP_FLOOR_EXT = 0x878F
    const val GL_OP_ROUND_EXT = 0x8790
    const val GL_OP_EXP_BASE_2_EXT = 0x8791
    const val GL_OP_LOG_BASE_2_EXT = 0x8792
    const val GL_OP_POWER_EXT = 0x8793
    const val GL_OP_RECIP_EXT = 0x8794
    const val GL_OP_RECIP_SQRT_EXT = 0x8795
    const val GL_OP_SUB_EXT = 0x8796
    const val GL_OP_CROSS_PRODUCT_EXT = 0x8797
    const val GL_OP_MULTIPLY_MATRIX_EXT = 0x8798
    const val GL_OP_MOV_EXT = 0x8799
    const val GL_OUTPUT_VERTEX_EXT = 0x879A
    const val GL_OUTPUT_COLOR0_EXT = 0x879B
    const val GL_OUTPUT_COLOR1_EXT = 0x879C
    const val GL_OUTPUT_TEXTURE_COORD0_EXT = 0x879D
    const val GL_OUTPUT_TEXTURE_COORD1_EXT = 0x879E
    const val GL_OUTPUT_TEXTURE_COORD2_EXT = 0x879F
    const val GL_OUTPUT_TEXTURE_COORD3_EXT = 0x87A0
    const val GL_OUTPUT_TEXTURE_COORD4_EXT = 0x87A1
    const val GL_OUTPUT_TEXTURE_COORD5_EXT = 0x87A2
    const val GL_OUTPUT_TEXTURE_COORD6_EXT = 0x87A3
    const val GL_OUTPUT_TEXTURE_COORD7_EXT = 0x87A4
    const val GL_OUTPUT_TEXTURE_COORD8_EXT = 0x87A5
    const val GL_OUTPUT_TEXTURE_COORD9_EXT = 0x87A6
    const val GL_OUTPUT_TEXTURE_COORD10_EXT = 0x87A7
    const val GL_OUTPUT_TEXTURE_COORD11_EXT = 0x87A8
    const val GL_OUTPUT_TEXTURE_COORD12_EXT = 0x87A9
    const val GL_OUTPUT_TEXTURE_COORD13_EXT = 0x87AA
    const val GL_OUTPUT_TEXTURE_COORD14_EXT = 0x87AB
    const val GL_OUTPUT_TEXTURE_COORD15_EXT = 0x87AC
    const val GL_OUTPUT_TEXTURE_COORD16_EXT = 0x87AD
    const val GL_OUTPUT_TEXTURE_COORD17_EXT = 0x87AE
    const val GL_OUTPUT_TEXTURE_COORD18_EXT = 0x87AF
    const val GL_OUTPUT_TEXTURE_COORD19_EXT = 0x87B0
    const val GL_OUTPUT_TEXTURE_COORD20_EXT = 0x87B1
    const val GL_OUTPUT_TEXTURE_COORD21_EXT = 0x87B2
    const val GL_OUTPUT_TEXTURE_COORD22_EXT = 0x87B3
    const val GL_OUTPUT_TEXTURE_COORD23_EXT = 0x87B4
    const val GL_OUTPUT_TEXTURE_COORD24_EXT = 0x87B5
    const val GL_OUTPUT_TEXTURE_COORD25_EXT = 0x87B6
    const val GL_OUTPUT_TEXTURE_COORD26_EXT = 0x87B7
    const val GL_OUTPUT_TEXTURE_COORD27_EXT = 0x87B8
    const val GL_OUTPUT_TEXTURE_COORD28_EXT = 0x87B9
    const val GL_OUTPUT_TEXTURE_COORD29_EXT = 0x87BA
    const val GL_OUTPUT_TEXTURE_COORD30_EXT = 0x87BB
    const val GL_OUTPUT_TEXTURE_COORD31_EXT = 0x87BC
    const val GL_OUTPUT_FOG_EXT = 0x87BD
    const val GL_SCALAR_EXT = 0x87BE
    const val GL_VECTOR_EXT = 0x87BF
    const val GL_MATRIX_EXT = 0x87C0
    const val GL_VARIANT_EXT = 0x87C1
    const val GL_INVARIANT_EXT = 0x87C2
    const val GL_LOCAL_CONSTANT_EXT = 0x87C3
    const val GL_LOCAL_EXT = 0x87C4
    const val GL_MAX_VERTEX_SHADER_INSTRUCTIONS_EXT = 0x87C5
    const val GL_MAX_VERTEX_SHADER_VARIANTS_EXT = 0x87C6
    const val GL_MAX_VERTEX_SHADER_INVARIANTS_EXT = 0x87C7
    const val GL_MAX_VERTEX_SHADER_LOCAL_CONSTANTS_EXT = 0x87C8
    const val GL_MAX_VERTEX_SHADER_LOCALS_EXT = 0x87C9
    const val GL_MAX_OPTIMIZED_VERTEX_SHADER_INSTRUCTIONS_EXT = 0x87CA
    const val GL_MAX_OPTIMIZED_VERTEX_SHADER_VARIANTS_EXT = 0x87CB
    const val GL_MAX_OPTIMIZED_VERTEX_SHADER_LOCAL_CONSTANTS_EXT = 0x87CC
    const val GL_MAX_OPTIMIZED_VERTEX_SHADER_INVARIANTS_EXT = 0x87CD
    const val GL_MAX_OPTIMIZED_VERTEX_SHADER_LOCALS_EXT = 0x87CE
    const val GL_VERTEX_SHADER_INSTRUCTIONS_EXT = 0x87CF
    const val GL_VERTEX_SHADER_VARIANTS_EXT = 0x87D0
    const val GL_VERTEX_SHADER_INVARIANTS_EXT = 0x87D1
    const val GL_VERTEX_SHADER_LOCAL_CONSTANTS_EXT = 0x87D2
    const val GL_VERTEX_SHADER_LOCALS_EXT = 0x87D3
    const val GL_VERTEX_SHADER_OPTIMIZED_EXT = 0x87D4
    const val GL_X_EXT = 0x87D5
    const val GL_Y_EXT = 0x87D6
    const val GL_Z_EXT = 0x87D7
    const val GL_W_EXT = 0x87D8
    const val GL_NEGATIVE_X_EXT = 0x87D9
    const val GL_NEGATIVE_Y_EXT = 0x87DA
    const val GL_NEGATIVE_Z_EXT = 0x87DB
    const val GL_NEGATIVE_W_EXT = 0x87DC
    const val GL_ZERO_EXT = 0x87DD
    const val GL_ONE_EXT = 0x87DE
    const val GL_NEGATIVE_ONE_EXT = 0x87DF
    const val GL_NORMALIZED_RANGE_EXT = 0x87E0
    const val GL_FULL_RANGE_EXT = 0x87E1
    const val GL_CURRENT_VERTEX_EXT = 0x87E2
    const val GL_MVP_MATRIX_EXT = 0x87E3
    const val GL_VARIANT_VALUE_EXT = 0x87E4
    const val GL_VARIANT_DATATYPE_EXT = 0x87E5
    const val GL_VARIANT_ARRAY_STRIDE_EXT = 0x87E6
    const val GL_VARIANT_ARRAY_TYPE_EXT = 0x87E7
    const val GL_VARIANT_ARRAY_EXT = 0x87E8
    const val GL_VARIANT_ARRAY_POINTER_EXT = 0x87E9
    const val GL_INVARIANT_VALUE_EXT = 0x87EA
    const val GL_INVARIANT_DATATYPE_EXT = 0x87EB
    const val GL_LOCAL_CONSTANT_VALUE_EXT = 0x87EC
    const val GL_LOCAL_CONSTANT_DATATYPE_EXT = 0x87ED
    const val GL_MODELVIEW0_STACK_DEPTH_EXT = 0x0BA3
    const val GL_MODELVIEW1_STACK_DEPTH_EXT = 0x8502
    const val GL_MODELVIEW0_MATRIX_EXT = 0x0BA6
    const val GL_MODELVIEW1_MATRIX_EXT = 0x8506
    const val GL_VERTEX_WEIGHTING_EXT = 0x8509
    const val GL_MODELVIEW0_EXT = 0x1700
    const val GL_MODELVIEW1_EXT = 0x850A
    const val GL_CURRENT_VERTEX_WEIGHT_EXT = 0x850B
    const val GL_VERTEX_WEIGHT_ARRAY_EXT = 0x850C
    const val GL_VERTEX_WEIGHT_ARRAY_SIZE_EXT = 0x850D
    const val GL_VERTEX_WEIGHT_ARRAY_TYPE_EXT = 0x850E
    const val GL_VERTEX_WEIGHT_ARRAY_STRIDE_EXT = 0x850F
    const val GL_VERTEX_WEIGHT_ARRAY_POINTER_EXT = 0x8510
    const val GL_INCLUSIVE_EXT = 0x8F10
    const val GL_EXCLUSIVE_EXT = 0x8F11
    const val GL_WINDOW_RECTANGLE_EXT = 0x8F12
    const val GL_WINDOW_RECTANGLE_MODE_EXT = 0x8F13
    const val GL_MAX_WINDOW_RECTANGLES_EXT = 0x8F14
    const val GL_NUM_WINDOW_RECTANGLES_EXT = 0x8F15
    const val GL_SYNC_X11_FENCE_EXT = 0x90E1
    const val GL_IGNORE_BORDER_HP = 0x8150
    const val GL_CONSTANT_BORDER_HP = 0x8151
    const val GL_REPLICATE_BORDER_HP = 0x8153
    const val GL_CONVOLUTION_BORDER_COLOR_HP = 0x8154
    const val GL_IMAGE_SCALE_X_HP = 0x8155
    const val GL_IMAGE_SCALE_Y_HP = 0x8156
    const val GL_IMAGE_TRANSLATE_X_HP = 0x8157
    const val GL_IMAGE_TRANSLATE_Y_HP = 0x8158
    const val GL_IMAGE_ROTATE_ANGLE_HP = 0x8159
    const val GL_IMAGE_ROTATE_ORIGIN_X_HP = 0x815A
    const val GL_IMAGE_ROTATE_ORIGIN_Y_HP = 0x815B
    const val GL_IMAGE_MAG_FILTER_HP = 0x815C
    const val GL_IMAGE_MIN_FILTER_HP = 0x815D
    const val GL_IMAGE_CUBIC_WEIGHT_HP = 0x815E
    const val GL_CUBIC_HP = 0x815F
    const val GL_AVERAGE_HP = 0x8160
    const val GL_IMAGE_TRANSFORM_2D_HP = 0x8161
    const val GL_POST_IMAGE_TRANSFORM_COLOR_TABLE_HP = 0x8162
    const val GL_PROXY_POST_IMAGE_TRANSFORM_COLOR_TABLE_HP = 0x8163
    const val GL_OCCLUSION_TEST_HP = 0x8165
    const val GL_OCCLUSION_TEST_RESULT_HP = 0x8166
    const val GL_TEXTURE_LIGHTING_MODE_HP = 0x8167
    const val GL_TEXTURE_POST_SPECULAR_HP = 0x8168
    const val GL_TEXTURE_PRE_SPECULAR_HP = 0x8169
    const val GL_CULL_VERTEX_IBM = 103050
    const val GL_RASTER_POSITION_UNCLIPPED_IBM = 0x19262
    const val GL_ALL_STATIC_DATA_IBM = 103060
    const val GL_STATIC_VERTEX_ARRAY_IBM = 103061
    const val GL_MIRRORED_REPEAT_IBM = 0x8370
    const val GL_VERTEX_ARRAY_LIST_IBM = 103070
    const val GL_NORMAL_ARRAY_LIST_IBM = 103071
    const val GL_COLOR_ARRAY_LIST_IBM = 103072
    const val GL_INDEX_ARRAY_LIST_IBM = 103073
    const val GL_TEXTURE_COORD_ARRAY_LIST_IBM = 103074
    const val GL_EDGE_FLAG_ARRAY_LIST_IBM = 103075
    const val GL_FOG_COORDINATE_ARRAY_LIST_IBM = 103076
    const val GL_SECONDARY_COLOR_ARRAY_LIST_IBM = 103077
    const val GL_VERTEX_ARRAY_LIST_STRIDE_IBM = 103080
    const val GL_NORMAL_ARRAY_LIST_STRIDE_IBM = 103081
    const val GL_COLOR_ARRAY_LIST_STRIDE_IBM = 103082
    const val GL_INDEX_ARRAY_LIST_STRIDE_IBM = 103083
    const val GL_TEXTURE_COORD_ARRAY_LIST_STRIDE_IBM = 103084
    const val GL_EDGE_FLAG_ARRAY_LIST_STRIDE_IBM = 103085
    const val GL_FOG_COORDINATE_ARRAY_LIST_STRIDE_IBM = 103086
    const val GL_SECONDARY_COLOR_ARRAY_LIST_STRIDE_IBM = 103087
    const val GL_RED_MIN_CLAMP_INGR = 0x8560
    const val GL_GREEN_MIN_CLAMP_INGR = 0x8561
    const val GL_BLUE_MIN_CLAMP_INGR = 0x8562
    const val GL_ALPHA_MIN_CLAMP_INGR = 0x8563
    const val GL_RED_MAX_CLAMP_INGR = 0x8564
    const val GL_GREEN_MAX_CLAMP_INGR = 0x8565
    const val GL_BLUE_MAX_CLAMP_INGR = 0x8566
    const val GL_ALPHA_MAX_CLAMP_INGR = 0x8567
    const val GL_INTERLACE_READ_INGR = 0x8568
    const val GL_BLACKHOLE_RENDER_INTEL = 0x83FC
    const val GL_CONSERVATIVE_RASTERIZATION_INTEL = 0x83FE
    const val GL_TEXTURE_MEMORY_LAYOUT_INTEL = 0x83FF
    const val GL_LAYOUT_DEFAULT_INTEL = 0
    const val GL_LAYOUT_LINEAR_INTEL = 1
    const val GL_LAYOUT_LINEAR_CPU_CACHED_INTEL = 2
    const val GL_PARALLEL_ARRAYS_INTEL = 0x83F4
    const val GL_VERTEX_ARRAY_PARALLEL_POINTERS_INTEL = 0x83F5
    const val GL_NORMAL_ARRAY_PARALLEL_POINTERS_INTEL = 0x83F6
    const val GL_COLOR_ARRAY_PARALLEL_POINTERS_INTEL = 0x83F7
    const val GL_TEXTURE_COORD_ARRAY_PARALLEL_POINTERS_INTEL = 0x83F8
    const val GL_PERFQUERY_SINGLE_CONTEXT_INTEL = 0x00000000
    const val GL_PERFQUERY_GLOBAL_CONTEXT_INTEL = 0x00000001
    const val GL_PERFQUERY_WAIT_INTEL = 0x83FB
    const val GL_PERFQUERY_FLUSH_INTEL = 0x83FA
    const val GL_PERFQUERY_DONOT_FLUSH_INTEL = 0x83F9
    const val GL_PERFQUERY_COUNTER_EVENT_INTEL = 0x94F0
    const val GL_PERFQUERY_COUNTER_DURATION_NORM_INTEL = 0x94F1
    const val GL_PERFQUERY_COUNTER_DURATION_RAW_INTEL = 0x94F2
    const val GL_PERFQUERY_COUNTER_THROUGHPUT_INTEL = 0x94F3
    const val GL_PERFQUERY_COUNTER_RAW_INTEL = 0x94F4
    const val GL_PERFQUERY_COUNTER_TIMESTAMP_INTEL = 0x94F5
    const val GL_PERFQUERY_COUNTER_DATA_UINT32_INTEL = 0x94F8
    const val GL_PERFQUERY_COUNTER_DATA_UINT64_INTEL = 0x94F9
    const val GL_PERFQUERY_COUNTER_DATA_FLOAT_INTEL = 0x94FA
    const val GL_PERFQUERY_COUNTER_DATA_DOUBLE_INTEL = 0x94FB
    const val GL_PERFQUERY_COUNTER_DATA_BOOL32_INTEL = 0x94FC
    const val GL_PERFQUERY_QUERY_NAME_LENGTH_MAX_INTEL = 0x94FD
    const val GL_PERFQUERY_COUNTER_NAME_LENGTH_MAX_INTEL = 0x94FE
    const val GL_PERFQUERY_COUNTER_DESC_LENGTH_MAX_INTEL = 0x94FF
    const val GL_PERFQUERY_GPA_EXTENDED_COUNTERS_INTEL = 0x9500
    const val GL_MULTIPLY_KHR = 0x9294
    const val GL_SCREEN_KHR = 0x9295
    const val GL_OVERLAY_KHR = 0x9296
    const val GL_DARKEN_KHR = 0x9297
    const val GL_LIGHTEN_KHR = 0x9298
    const val GL_COLORDODGE_KHR = 0x9299
    const val GL_COLORBURN_KHR = 0x929A
    const val GL_HARDLIGHT_KHR = 0x929B
    const val GL_SOFTLIGHT_KHR = 0x929C
    const val GL_DIFFERENCE_KHR = 0x929E
    const val GL_EXCLUSION_KHR = 0x92A0
    const val GL_HSL_HUE_KHR = 0x92AD
    const val GL_HSL_SATURATION_KHR = 0x92AE
    const val GL_HSL_COLOR_KHR = 0x92AF
    const val GL_HSL_LUMINOSITY_KHR = 0x92B0
    const val GL_BLEND_ADVANCED_COHERENT_KHR = 0x9285
    const val GL_CONTEXT_RELEASE_BEHAVIOR_KHR = 0x82FB
    const val GL_CONTEXT_RELEASE_BEHAVIOR_FLUSH_KHR = 0x82FC
    const val GL_DEBUG_OUTPUT_SYNCHRONOUS_KHR = 0x8242
    const val GL_DEBUG_NEXT_LOGGED_MESSAGE_LENGTH_KHR = 0x8243
    const val GL_DEBUG_CALLBACK_FUNCTION_KHR = 0x8244
    const val GL_DEBUG_CALLBACK_USER_PARAM_KHR = 0x8245
    const val GL_DEBUG_SOURCE_API_KHR = 0x8246
    const val GL_DEBUG_SOURCE_WINDOW_SYSTEM_KHR = 0x8247
    const val GL_DEBUG_SOURCE_SHADER_COMPILER_KHR = 0x8248
    const val GL_DEBUG_SOURCE_THIRD_PARTY_KHR = 0x8249
    const val GL_DEBUG_SOURCE_APPLICATION_KHR = 0x824A
    const val GL_DEBUG_SOURCE_OTHER_KHR = 0x824B
    const val GL_DEBUG_TYPE_ERROR_KHR = 0x824C
    const val GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR_KHR = 0x824D
    const val GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR_KHR = 0x824E
    const val GL_DEBUG_TYPE_PORTABILITY_KHR = 0x824F
    const val GL_DEBUG_TYPE_PERFORMANCE_KHR = 0x8250
    const val GL_DEBUG_TYPE_OTHER_KHR = 0x8251
    const val GL_DEBUG_TYPE_MARKER_KHR = 0x8268
    const val GL_DEBUG_TYPE_PUSH_GROUP_KHR = 0x8269
    const val GL_DEBUG_TYPE_POP_GROUP_KHR = 0x826A
    const val GL_DEBUG_SEVERITY_NOTIFICATION_KHR = 0x826B
    const val GL_MAX_DEBUG_GROUP_STACK_DEPTH_KHR = 0x826C
    const val GL_DEBUG_GROUP_STACK_DEPTH_KHR = 0x826D
    const val GL_BUFFER_KHR = 0x82E0
    const val GL_SHADER_KHR = 0x82E1
    const val GL_PROGRAM_KHR = 0x82E2
    const val GL_VERTEX_ARRAY_KHR = 0x8074
    const val GL_QUERY_KHR = 0x82E3
    const val GL_PROGRAM_PIPELINE_KHR = 0x82E4
    const val GL_SAMPLER_KHR = 0x82E6
    const val GL_MAX_LABEL_LENGTH_KHR = 0x82E8
    const val GL_MAX_DEBUG_MESSAGE_LENGTH_KHR = 0x9143
    const val GL_MAX_DEBUG_LOGGED_MESSAGES_KHR = 0x9144
    const val GL_DEBUG_LOGGED_MESSAGES_KHR = 0x9145
    const val GL_DEBUG_SEVERITY_HIGH_KHR = 0x9146
    const val GL_DEBUG_SEVERITY_MEDIUM_KHR = 0x9147
    const val GL_DEBUG_SEVERITY_LOW_KHR = 0x9148
    const val GL_DEBUG_OUTPUT_KHR = 0x92E0
    const val GL_CONTEXT_FLAG_DEBUG_BIT_KHR = 0x00000002
    const val GL_STACK_OVERFLOW_KHR = 0x0503
    const val GL_STACK_UNDERFLOW_KHR = 0x0504
    const val GL_CONTEXT_FLAG_NO_ERROR_BIT_KHR = 0x00000008
    const val GL_MAX_SHADER_COMPILER_THREADS_KHR = 0x91B0
    const val GL_COMPLETION_STATUS_KHR = 0x91B1
    const val GL_CONTEXT_ROBUST_ACCESS = 0x90F3
    const val GL_CONTEXT_ROBUST_ACCESS_KHR = 0x90F3
    const val GL_LOSE_CONTEXT_ON_RESET_KHR = 0x8252
    const val GL_GUILTY_CONTEXT_RESET_KHR = 0x8253
    const val GL_INNOCENT_CONTEXT_RESET_KHR = 0x8254
    const val GL_UNKNOWN_CONTEXT_RESET_KHR = 0x8255
    const val GL_RESET_NOTIFICATION_STRATEGY_KHR = 0x8256
    const val GL_NO_RESET_NOTIFICATION_KHR = 0x8261
    const val GL_CONTEXT_LOST_KHR = 0x0507
    const val GL_SUBGROUP_SIZE_KHR = 0x9532
    const val GL_SUBGROUP_SUPPORTED_STAGES_KHR = 0x9533
    const val GL_SUBGROUP_SUPPORTED_FEATURES_KHR = 0x9534
    const val GL_SUBGROUP_QUAD_ALL_STAGES_KHR = 0x9535
    const val GL_SUBGROUP_FEATURE_BASIC_BIT_KHR = 0x00000001
    const val GL_SUBGROUP_FEATURE_VOTE_BIT_KHR = 0x00000002
    const val GL_SUBGROUP_FEATURE_ARITHMETIC_BIT_KHR = 0x00000004
    const val GL_SUBGROUP_FEATURE_BALLOT_BIT_KHR = 0x00000008
    const val GL_SUBGROUP_FEATURE_SHUFFLE_BIT_KHR = 0x00000010
    const val GL_SUBGROUP_FEATURE_SHUFFLE_RELATIVE_BIT_KHR = 0x00000020
    const val GL_SUBGROUP_FEATURE_CLUSTERED_BIT_KHR = 0x00000040
    const val GL_SUBGROUP_FEATURE_QUAD_BIT_KHR = 0x00000080
    const val GL_COMPRESSED_RGBA_ASTC_4x4_KHR = 0x93B0
    const val GL_COMPRESSED_RGBA_ASTC_5x4_KHR = 0x93B1
    const val GL_COMPRESSED_RGBA_ASTC_5x5_KHR = 0x93B2
    const val GL_COMPRESSED_RGBA_ASTC_6x5_KHR = 0x93B3
    const val GL_COMPRESSED_RGBA_ASTC_6x6_KHR = 0x93B4
    const val GL_COMPRESSED_RGBA_ASTC_8x5_KHR = 0x93B5
    const val GL_COMPRESSED_RGBA_ASTC_8x6_KHR = 0x93B6
    const val GL_COMPRESSED_RGBA_ASTC_8x8_KHR = 0x93B7
    const val GL_COMPRESSED_RGBA_ASTC_10x5_KHR = 0x93B8
    const val GL_COMPRESSED_RGBA_ASTC_10x6_KHR = 0x93B9
    const val GL_COMPRESSED_RGBA_ASTC_10x8_KHR = 0x93BA
    const val GL_COMPRESSED_RGBA_ASTC_10x10_KHR = 0x93BB
    const val GL_COMPRESSED_RGBA_ASTC_12x10_KHR = 0x93BC
    const val GL_COMPRESSED_RGBA_ASTC_12x12_KHR = 0x93BD
    const val GL_COMPRESSED_SRGB8_ALPHA8_ASTC_4x4_KHR = 0x93D0
    const val GL_COMPRESSED_SRGB8_ALPHA8_ASTC_5x4_KHR = 0x93D1
    const val GL_COMPRESSED_SRGB8_ALPHA8_ASTC_5x5_KHR = 0x93D2
    const val GL_COMPRESSED_SRGB8_ALPHA8_ASTC_6x5_KHR = 0x93D3
    const val GL_COMPRESSED_SRGB8_ALPHA8_ASTC_6x6_KHR = 0x93D4
    const val GL_COMPRESSED_SRGB8_ALPHA8_ASTC_8x5_KHR = 0x93D5
    const val GL_COMPRESSED_SRGB8_ALPHA8_ASTC_8x6_KHR = 0x93D6
    const val GL_COMPRESSED_SRGB8_ALPHA8_ASTC_8x8_KHR = 0x93D7
    const val GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x5_KHR = 0x93D8
    const val GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x6_KHR = 0x93D9
    const val GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x8_KHR = 0x93DA
    const val GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x10_KHR = 0x93DB
    const val GL_COMPRESSED_SRGB8_ALPHA8_ASTC_12x10_KHR = 0x93DC
    const val GL_COMPRESSED_SRGB8_ALPHA8_ASTC_12x12_KHR = 0x93DD
    const val GL_TEXTURE_1D_STACK_MESAX = 0x8759
    const val GL_TEXTURE_2D_STACK_MESAX = 0x875A
    const val GL_PROXY_TEXTURE_1D_STACK_MESAX = 0x875B
    const val GL_PROXY_TEXTURE_2D_STACK_MESAX = 0x875C
    const val GL_TEXTURE_1D_STACK_BINDING_MESAX = 0x875D
    const val GL_TEXTURE_2D_STACK_BINDING_MESAX = 0x875E
    const val GL_FRAMEBUFFER_FLIP_X_MESA = 0x8BBC
    const val GL_FRAMEBUFFER_FLIP_Y_MESA = 0x8BBB
    const val GL_FRAMEBUFFER_SWAP_XY_MESA = 0x8BBD
    const val GL_PACK_INVERT_MESA = 0x8758
    const val GL_PROGRAM_BINARY_FORMAT_MESA = 0x875F
    const val GL_TILE_RASTER_ORDER_FIXED_MESA = 0x8BB8
    const val GL_TILE_RASTER_ORDER_INCREASING_X_MESA = 0x8BB9
    const val GL_TILE_RASTER_ORDER_INCREASING_Y_MESA = 0x8BBA
    const val GL_UNSIGNED_SHORT_8_8_MESA = 0x85BA
    const val GL_UNSIGNED_SHORT_8_8_REV_MESA = 0x85BB
    const val GL_YCBCR_MESA = 0x8757
    const val GL_GPU_MEMORY_INFO_DEDICATED_VIDMEM_NVX = 0x9047
    const val GL_GPU_MEMORY_INFO_TOTAL_AVAILABLE_MEMORY_NVX = 0x9048
    const val GL_GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX = 0x9049
    const val GL_GPU_MEMORY_INFO_EVICTION_COUNT_NVX = 0x904A
    const val GL_GPU_MEMORY_INFO_EVICTED_MEMORY_NVX = 0x904B
    const val GL_UPLOAD_GPU_MASK_NVX = 0x954A
    const val GL_LGPU_SEPARATE_STORAGE_BIT_NVX = 0x0800
    const val GL_MAX_LGPU_GPUS_NVX = 0x92BA
    const val GL_ALPHA_TO_COVERAGE_DITHER_DEFAULT_NV = 0x934D
    const val GL_ALPHA_TO_COVERAGE_DITHER_ENABLE_NV = 0x934E
    const val GL_ALPHA_TO_COVERAGE_DITHER_DISABLE_NV = 0x934F
    const val GL_ALPHA_TO_COVERAGE_DITHER_MODE_NV = 0x92BF
    const val GL_BLEND_OVERLAP_NV = 0x9281
    const val GL_BLEND_PREMULTIPLIED_SRC_NV = 0x9280
    const val GL_BLUE_NV = 0x1905
    const val GL_COLORBURN_NV = 0x929A
    const val GL_COLORDODGE_NV = 0x9299
    const val GL_CONJOINT_NV = 0x9284
    const val GL_CONTRAST_NV = 0x92A1
    const val GL_DARKEN_NV = 0x9297
    const val GL_DIFFERENCE_NV = 0x929E
    const val GL_DISJOINT_NV = 0x9283
    const val GL_DST_ATOP_NV = 0x928F
    const val GL_DST_IN_NV = 0x928B
    const val GL_DST_NV = 0x9287
    const val GL_DST_OUT_NV = 0x928D
    const val GL_DST_OVER_NV = 0x9289
    const val GL_EXCLUSION_NV = 0x92A0
    const val GL_GREEN_NV = 0x1904
    const val GL_HARDLIGHT_NV = 0x929B
    const val GL_HARDMIX_NV = 0x92A9
    const val GL_HSL_COLOR_NV = 0x92AF
    const val GL_HSL_HUE_NV = 0x92AD
    const val GL_HSL_LUMINOSITY_NV = 0x92B0
    const val GL_HSL_SATURATION_NV = 0x92AE
    const val GL_INVERT_OVG_NV = 0x92B4
    const val GL_INVERT_RGB_NV = 0x92A3
    const val GL_LIGHTEN_NV = 0x9298
    const val GL_LINEARBURN_NV = 0x92A5
    const val GL_LINEARDODGE_NV = 0x92A4
    const val GL_LINEARLIGHT_NV = 0x92A7
    const val GL_MINUS_CLAMPED_NV = 0x92B3
    const val GL_MINUS_NV = 0x929F
    const val GL_MULTIPLY_NV = 0x9294
    const val GL_OVERLAY_NV = 0x9296
    const val GL_PINLIGHT_NV = 0x92A8
    const val GL_PLUS_CLAMPED_ALPHA_NV = 0x92B2
    const val GL_PLUS_CLAMPED_NV = 0x92B1
    const val GL_PLUS_DARKER_NV = 0x9292
    const val GL_PLUS_NV = 0x9291
    const val GL_RED_NV = 0x1903
    const val GL_SCREEN_NV = 0x9295
    const val GL_SOFTLIGHT_NV = 0x929C
    const val GL_SRC_ATOP_NV = 0x928E
    const val GL_SRC_IN_NV = 0x928A
    const val GL_SRC_NV = 0x9286
    const val GL_SRC_OUT_NV = 0x928C
    const val GL_SRC_OVER_NV = 0x9288
    const val GL_UNCORRELATED_NV = 0x9282
    const val GL_VIVIDLIGHT_NV = 0x92A6
    const val GL_XOR_NV = 0x1506
    const val GL_BLEND_ADVANCED_COHERENT_NV = 0x9285
    const val GL_VIEWPORT_POSITION_W_SCALE_NV = 0x937C
    const val GL_VIEWPORT_POSITION_W_SCALE_X_COEFF_NV = 0x937D
    const val GL_VIEWPORT_POSITION_W_SCALE_Y_COEFF_NV = 0x937E
    const val GL_TERMINATE_SEQUENCE_COMMAND_NV = 0x0000
    const val GL_NOP_COMMAND_NV = 0x0001
    const val GL_DRAW_ELEMENTS_COMMAND_NV = 0x0002
    const val GL_DRAW_ARRAYS_COMMAND_NV = 0x0003
    const val GL_DRAW_ELEMENTS_STRIP_COMMAND_NV = 0x0004
    const val GL_DRAW_ARRAYS_STRIP_COMMAND_NV = 0x0005
    const val GL_DRAW_ELEMENTS_INSTANCED_COMMAND_NV = 0x0006
    const val GL_DRAW_ARRAYS_INSTANCED_COMMAND_NV = 0x0007
    const val GL_ELEMENT_ADDRESS_COMMAND_NV = 0x0008
    const val GL_ATTRIBUTE_ADDRESS_COMMAND_NV = 0x0009
    const val GL_UNIFORM_ADDRESS_COMMAND_NV = 0x000A
    const val GL_BLEND_COLOR_COMMAND_NV = 0x000B
    const val GL_STENCIL_REF_COMMAND_NV = 0x000C
    const val GL_LINE_WIDTH_COMMAND_NV = 0x000D
    const val GL_POLYGON_OFFSET_COMMAND_NV = 0x000E
    const val GL_ALPHA_REF_COMMAND_NV = 0x000F
    const val GL_VIEWPORT_COMMAND_NV = 0x0010
    const val GL_SCISSOR_COMMAND_NV = 0x0011
    const val GL_FRONT_FACE_COMMAND_NV = 0x0012
    const val GL_COMPUTE_PROGRAM_NV = 0x90FB
    const val GL_COMPUTE_PROGRAM_PARAMETER_BUFFER_NV = 0x90FC
    const val GL_QUERY_WAIT_NV = 0x8E13
    const val GL_QUERY_NO_WAIT_NV = 0x8E14
    const val GL_QUERY_BY_REGION_WAIT_NV = 0x8E15
    const val GL_QUERY_BY_REGION_NO_WAIT_NV = 0x8E16
    const val GL_CONSERVATIVE_RASTERIZATION_NV = 0x9346
    const val GL_SUBPIXEL_PRECISION_BIAS_X_BITS_NV = 0x9347
    const val GL_SUBPIXEL_PRECISION_BIAS_Y_BITS_NV = 0x9348
    const val GL_MAX_SUBPIXEL_PRECISION_BIAS_BITS_NV = 0x9349
    const val GL_CONSERVATIVE_RASTER_DILATE_NV = 0x9379
    const val GL_CONSERVATIVE_RASTER_DILATE_RANGE_NV = 0x937A
    const val GL_CONSERVATIVE_RASTER_DILATE_GRANULARITY_NV = 0x937B
    const val GL_CONSERVATIVE_RASTER_MODE_PRE_SNAP_NV = 0x9550
    const val GL_CONSERVATIVE_RASTER_MODE_NV = 0x954D
    const val GL_CONSERVATIVE_RASTER_MODE_POST_SNAP_NV = 0x954E
    const val GL_CONSERVATIVE_RASTER_MODE_PRE_SNAP_TRIANGLES_NV = 0x954F
    const val GL_DEPTH_STENCIL_TO_RGBA_NV = 0x886E
    const val GL_DEPTH_STENCIL_TO_BGRA_NV = 0x886F
    const val GL_MAX_DEEP_3D_TEXTURE_WIDTH_HEIGHT_NV = 0x90D0
    const val GL_MAX_DEEP_3D_TEXTURE_DEPTH_NV = 0x90D1
    const val GL_DEPTH_COMPONENT32F_NV = 0x8DAB
    const val GL_DEPTH32F_STENCIL8_NV = 0x8DAC
    const val GL_FLOAT_32_UNSIGNED_INT_24_8_REV_NV = 0x8DAD
    const val GL_DEPTH_BUFFER_FLOAT_MODE_NV = 0x8DAF
    const val GL_DEPTH_CLAMP_NV = 0x864F
    const val GL_EVAL_2D_NV = 0x86C0
    const val GL_EVAL_TRIANGULAR_2D_NV = 0x86C1
    const val GL_MAP_TESSELLATION_NV = 0x86C2
    const val GL_MAP_ATTRIB_U_ORDER_NV = 0x86C3
    const val GL_MAP_ATTRIB_V_ORDER_NV = 0x86C4
    const val GL_EVAL_FRACTIONAL_TESSELLATION_NV = 0x86C5
    const val GL_EVAL_VERTEX_ATTRIB0_NV = 0x86C6
    const val GL_EVAL_VERTEX_ATTRIB1_NV = 0x86C7
    const val GL_EVAL_VERTEX_ATTRIB2_NV = 0x86C8
    const val GL_EVAL_VERTEX_ATTRIB3_NV = 0x86C9
    const val GL_EVAL_VERTEX_ATTRIB4_NV = 0x86CA
    const val GL_EVAL_VERTEX_ATTRIB5_NV = 0x86CB
    const val GL_EVAL_VERTEX_ATTRIB6_NV = 0x86CC
    const val GL_EVAL_VERTEX_ATTRIB7_NV = 0x86CD
    const val GL_EVAL_VERTEX_ATTRIB8_NV = 0x86CE
    const val GL_EVAL_VERTEX_ATTRIB9_NV = 0x86CF
    const val GL_EVAL_VERTEX_ATTRIB10_NV = 0x86D0
    const val GL_EVAL_VERTEX_ATTRIB11_NV = 0x86D1
    const val GL_EVAL_VERTEX_ATTRIB12_NV = 0x86D2
    const val GL_EVAL_VERTEX_ATTRIB13_NV = 0x86D3
    const val GL_EVAL_VERTEX_ATTRIB14_NV = 0x86D4
    const val GL_EVAL_VERTEX_ATTRIB15_NV = 0x86D5
    const val GL_MAX_MAP_TESSELLATION_NV = 0x86D6
    const val GL_MAX_RATIONAL_EVAL_ORDER_NV = 0x86D7
    const val GL_SAMPLE_POSITION_NV = 0x8E50
    const val GL_SAMPLE_MASK_NV = 0x8E51
    const val GL_SAMPLE_MASK_VALUE_NV = 0x8E52
    const val GL_TEXTURE_BINDING_RENDERBUFFER_NV = 0x8E53
    const val GL_TEXTURE_RENDERBUFFER_DATA_STORE_BINDING_NV = 0x8E54
    const val GL_TEXTURE_RENDERBUFFER_NV = 0x8E55
    const val GL_SAMPLER_RENDERBUFFER_NV = 0x8E56
    const val GL_INT_SAMPLER_RENDERBUFFER_NV = 0x8E57
    const val GL_UNSIGNED_INT_SAMPLER_RENDERBUFFER_NV = 0x8E58
    const val GL_MAX_SAMPLE_MASK_WORDS_NV = 0x8E59
    const val GL_ALL_COMPLETED_NV = 0x84F2
    const val GL_FENCE_STATUS_NV = 0x84F3
    const val GL_FENCE_CONDITION_NV = 0x84F4
    const val GL_FILL_RECTANGLE_NV = 0x933C
    const val GL_FLOAT_R_NV = 0x8880
    const val GL_FLOAT_RG_NV = 0x8881
    const val GL_FLOAT_RGB_NV = 0x8882
    const val GL_FLOAT_RGBA_NV = 0x8883
    const val GL_FLOAT_R16_NV = 0x8884
    const val GL_FLOAT_R32_NV = 0x8885
    const val GL_FLOAT_RG16_NV = 0x8886
    const val GL_FLOAT_RG32_NV = 0x8887
    const val GL_FLOAT_RGB16_NV = 0x8888
    const val GL_FLOAT_RGB32_NV = 0x8889
    const val GL_FLOAT_RGBA16_NV = 0x888A
    const val GL_FLOAT_RGBA32_NV = 0x888B
    const val GL_TEXTURE_FLOAT_COMPONENTS_NV = 0x888C
    const val GL_FLOAT_CLEAR_COLOR_VALUE_NV = 0x888D
    const val GL_FLOAT_RGBA_MODE_NV = 0x888E
    const val GL_FOG_DISTANCE_MODE_NV = 0x855A
    const val GL_EYE_RADIAL_NV = 0x855B
    const val GL_EYE_PLANE_ABSOLUTE_NV = 0x855C
    const val GL_EYE_PLANE = 0x2502
    const val GL_FRAGMENT_COVERAGE_TO_COLOR_NV = 0x92DD
    const val GL_FRAGMENT_COVERAGE_COLOR_NV = 0x92DE
    const val GL_MAX_FRAGMENT_PROGRAM_LOCAL_PARAMETERS_NV = 0x8868
    const val GL_FRAGMENT_PROGRAM_NV = 0x8870
    const val GL_MAX_TEXTURE_COORDS_NV = 0x8871
    const val GL_MAX_TEXTURE_IMAGE_UNITS_NV = 0x8872
    const val GL_FRAGMENT_PROGRAM_BINDING_NV = 0x8873
    const val GL_PROGRAM_ERROR_STRING_NV = 0x8874
    const val GL_MAX_PROGRAM_EXEC_INSTRUCTIONS_NV = 0x88F4
    const val GL_MAX_PROGRAM_CALL_DEPTH_NV = 0x88F5
    const val GL_MAX_PROGRAM_IF_DEPTH_NV = 0x88F6
    const val GL_MAX_PROGRAM_LOOP_DEPTH_NV = 0x88F7
    const val GL_MAX_PROGRAM_LOOP_COUNT_NV = 0x88F8
    const val GL_COVERAGE_MODULATION_TABLE_NV = 0x9331
    const val GL_COLOR_SAMPLES_NV = 0x8E20
    const val GL_DEPTH_SAMPLES_NV = 0x932D
    const val GL_STENCIL_SAMPLES_NV = 0x932E
    const val GL_MIXED_DEPTH_SAMPLES_SUPPORTED_NV = 0x932F
    const val GL_MIXED_STENCIL_SAMPLES_SUPPORTED_NV = 0x9330
    const val GL_COVERAGE_MODULATION_NV = 0x9332
    const val GL_COVERAGE_MODULATION_TABLE_SIZE_NV = 0x9333
    const val GL_RENDERBUFFER_COVERAGE_SAMPLES_NV = 0x8CAB
    const val GL_RENDERBUFFER_COLOR_SAMPLES_NV = 0x8E10
    const val GL_MAX_MULTISAMPLE_COVERAGE_MODES_NV = 0x8E11
    const val GL_MULTISAMPLE_COVERAGE_MODES_NV = 0x8E12
    const val GL_GEOMETRY_PROGRAM_NV = 0x8C26
    const val GL_MAX_PROGRAM_OUTPUT_VERTICES_NV = 0x8C27
    const val GL_MAX_PROGRAM_TOTAL_OUTPUT_COMPONENTS_NV = 0x8C28
    const val GL_PER_GPU_STORAGE_BIT_NV = 0x0800
    const val GL_MULTICAST_GPUS_NV = 0x92BA
    const val GL_RENDER_GPU_MASK_NV = 0x9558
    const val GL_PER_GPU_STORAGE_NV = 0x9548
    const val GL_MULTICAST_PROGRAMMABLE_SAMPLE_LOCATION_NV = 0x9549
    const val GL_MIN_PROGRAM_TEXEL_OFFSET_NV = 0x8904
    const val GL_MAX_PROGRAM_TEXEL_OFFSET_NV = 0x8905
    const val GL_PROGRAM_ATTRIB_COMPONENTS_NV = 0x8906
    const val GL_PROGRAM_RESULT_COMPONENTS_NV = 0x8907
    const val GL_MAX_PROGRAM_ATTRIB_COMPONENTS_NV = 0x8908
    const val GL_MAX_PROGRAM_RESULT_COMPONENTS_NV = 0x8909
    const val GL_MAX_PROGRAM_GENERIC_ATTRIBS_NV = 0x8DA5
    const val GL_MAX_PROGRAM_GENERIC_RESULTS_NV = 0x8DA6
    const val GL_MAX_GEOMETRY_PROGRAM_INVOCATIONS_NV = 0x8E5A
    const val GL_MIN_FRAGMENT_INTERPOLATION_OFFSET_NV = 0x8E5B
    const val GL_MAX_FRAGMENT_INTERPOLATION_OFFSET_NV = 0x8E5C
    const val GL_FRAGMENT_PROGRAM_INTERPOLATION_OFFSET_BITS_NV = 0x8E5D
    const val GL_MIN_PROGRAM_TEXTURE_GATHER_OFFSET_NV = 0x8E5E
    const val GL_MAX_PROGRAM_TEXTURE_GATHER_OFFSET_NV = 0x8E5F
    const val GL_MAX_PROGRAM_SUBROUTINE_PARAMETERS_NV = 0x8F44
    const val GL_MAX_PROGRAM_SUBROUTINE_NUM_NV = 0x8F45
    const val GL_HALF_FLOAT_NV = 0x140B
    const val GL_MULTISAMPLES_NV = 0x9371
    const val GL_SUPERSAMPLE_SCALE_X_NV = 0x9372
    const val GL_SUPERSAMPLE_SCALE_Y_NV = 0x9373
    const val GL_CONFORMANT_NV = 0x9374
    const val GL_MAX_SHININESS_NV = 0x8504
    const val GL_MAX_SPOT_EXPONENT_NV = 0x8505
    const val GL_ATTACHED_MEMORY_OBJECT_NV = 0x95A4
    const val GL_ATTACHED_MEMORY_OFFSET_NV = 0x95A5
    const val GL_MEMORY_ATTACHABLE_ALIGNMENT_NV = 0x95A6
    const val GL_MEMORY_ATTACHABLE_SIZE_NV = 0x95A7
    const val GL_MEMORY_ATTACHABLE_NV = 0x95A8
    const val GL_DETACHED_MEMORY_INCARNATION_NV = 0x95A9
    const val GL_DETACHED_TEXTURES_NV = 0x95AA
    const val GL_DETACHED_BUFFERS_NV = 0x95AB
    const val GL_MAX_DETACHED_TEXTURES_NV = 0x95AC
    const val GL_MAX_DETACHED_BUFFERS_NV = 0x95AD
    const val GL_MESH_SHADER_NV = 0x9559
    const val GL_TASK_SHADER_NV = 0x955A
    const val GL_MAX_MESH_UNIFORM_BLOCKS_NV = 0x8E60
    const val GL_MAX_MESH_TEXTURE_IMAGE_UNITS_NV = 0x8E61
    const val GL_MAX_MESH_IMAGE_UNIFORMS_NV = 0x8E62
    const val GL_MAX_MESH_UNIFORM_COMPONENTS_NV = 0x8E63
    const val GL_MAX_MESH_ATOMIC_COUNTER_BUFFERS_NV = 0x8E64
    const val GL_MAX_MESH_ATOMIC_COUNTERS_NV = 0x8E65
    const val GL_MAX_MESH_SHADER_STORAGE_BLOCKS_NV = 0x8E66
    const val GL_MAX_COMBINED_MESH_UNIFORM_COMPONENTS_NV = 0x8E67
    const val GL_MAX_TASK_UNIFORM_BLOCKS_NV = 0x8E68
    const val GL_MAX_TASK_TEXTURE_IMAGE_UNITS_NV = 0x8E69
    const val GL_MAX_TASK_IMAGE_UNIFORMS_NV = 0x8E6A
    const val GL_MAX_TASK_UNIFORM_COMPONENTS_NV = 0x8E6B
    const val GL_MAX_TASK_ATOMIC_COUNTER_BUFFERS_NV = 0x8E6C
    const val GL_MAX_TASK_ATOMIC_COUNTERS_NV = 0x8E6D
    const val GL_MAX_TASK_SHADER_STORAGE_BLOCKS_NV = 0x8E6E
    const val GL_MAX_COMBINED_TASK_UNIFORM_COMPONENTS_NV = 0x8E6F
    const val GL_MAX_MESH_WORK_GROUP_INVOCATIONS_NV = 0x95A2
    const val GL_MAX_TASK_WORK_GROUP_INVOCATIONS_NV = 0x95A3
    const val GL_MAX_MESH_TOTAL_MEMORY_SIZE_NV = 0x9536
    const val GL_MAX_TASK_TOTAL_MEMORY_SIZE_NV = 0x9537
    const val GL_MAX_MESH_OUTPUT_VERTICES_NV = 0x9538
    const val GL_MAX_MESH_OUTPUT_PRIMITIVES_NV = 0x9539
    const val GL_MAX_TASK_OUTPUT_COUNT_NV = 0x953A
    const val GL_MAX_DRAW_MESH_TASKS_COUNT_NV = 0x953D
    const val GL_MAX_MESH_VIEWS_NV = 0x9557
    const val GL_MESH_OUTPUT_PER_VERTEX_GRANULARITY_NV = 0x92DF
    const val GL_MESH_OUTPUT_PER_PRIMITIVE_GRANULARITY_NV = 0x9543
    const val GL_MAX_MESH_WORK_GROUP_SIZE_NV = 0x953B
    const val GL_MAX_TASK_WORK_GROUP_SIZE_NV = 0x953C
    const val GL_MESH_WORK_GROUP_SIZE_NV = 0x953E
    const val GL_TASK_WORK_GROUP_SIZE_NV = 0x953F
    const val GL_MESH_VERTICES_OUT_NV = 0x9579
    const val GL_MESH_PRIMITIVES_OUT_NV = 0x957A
    const val GL_MESH_OUTPUT_TYPE_NV = 0x957B
    const val GL_UNIFORM_BLOCK_REFERENCED_BY_MESH_SHADER_NV = 0x959C
    const val GL_UNIFORM_BLOCK_REFERENCED_BY_TASK_SHADER_NV = 0x959D
    const val GL_REFERENCED_BY_MESH_SHADER_NV = 0x95A0
    const val GL_REFERENCED_BY_TASK_SHADER_NV = 0x95A1
    const val GL_MESH_SHADER_BIT_NV = 0x00000040
    const val GL_TASK_SHADER_BIT_NV = 0x00000080
    const val GL_MESH_SUBROUTINE_NV = 0x957C
    const val GL_TASK_SUBROUTINE_NV = 0x957D
    const val GL_MESH_SUBROUTINE_UNIFORM_NV = 0x957E
    const val GL_TASK_SUBROUTINE_UNIFORM_NV = 0x957F
    const val GL_ATOMIC_COUNTER_BUFFER_REFERENCED_BY_MESH_SHADER_NV = 0x959E
    const val GL_ATOMIC_COUNTER_BUFFER_REFERENCED_BY_TASK_SHADER_NV = 0x959F
    const val GL_MULTISAMPLE_FILTER_HINT_NV = 0x8534
    const val GL_PIXEL_COUNTER_BITS_NV = 0x8864
    const val GL_CURRENT_OCCLUSION_QUERY_ID_NV = 0x8865
    const val GL_PIXEL_COUNT_NV = 0x8866
    const val GL_PIXEL_COUNT_AVAILABLE_NV = 0x8867
    const val GL_DEPTH_STENCIL_NV = 0x84F9
    const val GL_UNSIGNED_INT_24_8_NV = 0x84FA
    const val GL_MAX_PROGRAM_PARAMETER_BUFFER_BINDINGS_NV = 0x8DA0
    const val GL_MAX_PROGRAM_PARAMETER_BUFFER_SIZE_NV = 0x8DA1
    const val GL_VERTEX_PROGRAM_PARAMETER_BUFFER_NV = 0x8DA2
    const val GL_GEOMETRY_PROGRAM_PARAMETER_BUFFER_NV = 0x8DA3
    const val GL_FRAGMENT_PROGRAM_PARAMETER_BUFFER_NV = 0x8DA4
    const val GL_PATH_FORMAT_SVG_NV = 0x9070
    const val GL_PATH_FORMAT_PS_NV = 0x9071
    const val GL_STANDARD_FONT_NAME_NV = 0x9072
    const val GL_SYSTEM_FONT_NAME_NV = 0x9073
    const val GL_FILE_NAME_NV = 0x9074
    const val GL_PATH_STROKE_WIDTH_NV = 0x9075
    const val GL_PATH_END_CAPS_NV = 0x9076
    const val GL_PATH_INITIAL_END_CAP_NV = 0x9077
    const val GL_PATH_TERMINAL_END_CAP_NV = 0x9078
    const val GL_PATH_JOIN_STYLE_NV = 0x9079
    const val GL_PATH_MITER_LIMIT_NV = 0x907A
    const val GL_PATH_DASH_CAPS_NV = 0x907B
    const val GL_PATH_INITIAL_DASH_CAP_NV = 0x907C
    const val GL_PATH_TERMINAL_DASH_CAP_NV = 0x907D
    const val GL_PATH_DASH_OFFSET_NV = 0x907E
    const val GL_PATH_CLIENT_LENGTH_NV = 0x907F
    const val GL_PATH_FILL_MODE_NV = 0x9080
    const val GL_PATH_FILL_MASK_NV = 0x9081
    const val GL_PATH_FILL_COVER_MODE_NV = 0x9082
    const val GL_PATH_STROKE_COVER_MODE_NV = 0x9083
    const val GL_PATH_STROKE_MASK_NV = 0x9084
    const val GL_COUNT_UP_NV = 0x9088
    const val GL_COUNT_DOWN_NV = 0x9089
    const val GL_PATH_OBJECT_BOUNDING_BOX_NV = 0x908A
    const val GL_CONVEX_HULL_NV = 0x908B
    const val GL_BOUNDING_BOX_NV = 0x908D
    const val GL_TRANSLATE_X_NV = 0x908E
    const val GL_TRANSLATE_Y_NV = 0x908F
    const val GL_TRANSLATE_2D_NV = 0x9090
    const val GL_TRANSLATE_3D_NV = 0x9091
    const val GL_AFFINE_2D_NV = 0x9092
    const val GL_AFFINE_3D_NV = 0x9094
    const val GL_TRANSPOSE_AFFINE_2D_NV = 0x9096
    const val GL_TRANSPOSE_AFFINE_3D_NV = 0x9098
    const val GL_UTF8_NV = 0x909A
    const val GL_UTF16_NV = 0x909B
    const val GL_BOUNDING_BOX_OF_BOUNDING_BOXES_NV = 0x909C
    const val GL_PATH_COMMAND_COUNT_NV = 0x909D
    const val GL_PATH_COORD_COUNT_NV = 0x909E
    const val GL_PATH_DASH_ARRAY_COUNT_NV = 0x909F
    const val GL_PATH_COMPUTED_LENGTH_NV = 0x90A0
    const val GL_PATH_FILL_BOUNDING_BOX_NV = 0x90A1
    const val GL_PATH_STROKE_BOUNDING_BOX_NV = 0x90A2
    const val GL_SQUARE_NV = 0x90A3
    const val GL_ROUND_NV = 0x90A4
    const val GL_TRIANGULAR_NV = 0x90A5
    const val GL_BEVEL_NV = 0x90A6
    const val GL_MITER_REVERT_NV = 0x90A7
    const val GL_MITER_TRUNCATE_NV = 0x90A8
    const val GL_SKIP_MISSING_GLYPH_NV = 0x90A9
    const val GL_USE_MISSING_GLYPH_NV = 0x90AA
    const val GL_PATH_ERROR_POSITION_NV = 0x90AB
    const val GL_ACCUM_ADJACENT_PAIRS_NV = 0x90AD
    const val GL_ADJACENT_PAIRS_NV = 0x90AE
    const val GL_FIRST_TO_REST_NV = 0x90AF
    const val GL_PATH_GEN_MODE_NV = 0x90B0
    const val GL_PATH_GEN_COEFF_NV = 0x90B1
    const val GL_PATH_GEN_COMPONENTS_NV = 0x90B3
    const val GL_PATH_STENCIL_FUNC_NV = 0x90B7
    const val GL_PATH_STENCIL_REF_NV = 0x90B8
    const val GL_PATH_STENCIL_VALUE_MASK_NV = 0x90B9
    const val GL_PATH_STENCIL_DEPTH_OFFSET_FACTOR_NV = 0x90BD
    const val GL_PATH_STENCIL_DEPTH_OFFSET_UNITS_NV = 0x90BE
    const val GL_PATH_COVER_DEPTH_FUNC_NV = 0x90BF
    const val GL_PATH_DASH_OFFSET_RESET_NV = 0x90B4
    const val GL_MOVE_TO_RESETS_NV = 0x90B5
    const val GL_MOVE_TO_CONTINUES_NV = 0x90B6
    const val GL_CLOSE_PATH_NV = 0x00
    const val GL_MOVE_TO_NV = 0x02
    const val GL_RELATIVE_MOVE_TO_NV = 0x03
    const val GL_LINE_TO_NV = 0x04
    const val GL_RELATIVE_LINE_TO_NV = 0x05
    const val GL_HORIZONTAL_LINE_TO_NV = 0x06
    const val GL_RELATIVE_HORIZONTAL_LINE_TO_NV = 0x07
    const val GL_VERTICAL_LINE_TO_NV = 0x08
    const val GL_RELATIVE_VERTICAL_LINE_TO_NV = 0x09
    const val GL_QUADRATIC_CURVE_TO_NV = 0x0A
    const val GL_RELATIVE_QUADRATIC_CURVE_TO_NV = 0x0B
    const val GL_CUBIC_CURVE_TO_NV = 0x0C
    const val GL_RELATIVE_CUBIC_CURVE_TO_NV = 0x0D
    const val GL_SMOOTH_QUADRATIC_CURVE_TO_NV = 0x0E
    const val GL_RELATIVE_SMOOTH_QUADRATIC_CURVE_TO_NV = 0x0F
    const val GL_SMOOTH_CUBIC_CURVE_TO_NV = 0x10
    const val GL_RELATIVE_SMOOTH_CUBIC_CURVE_TO_NV = 0x11
    const val GL_SMALL_CCW_ARC_TO_NV = 0x12
    const val GL_RELATIVE_SMALL_CCW_ARC_TO_NV = 0x13
    const val GL_SMALL_CW_ARC_TO_NV = 0x14
    const val GL_RELATIVE_SMALL_CW_ARC_TO_NV = 0x15
    const val GL_LARGE_CCW_ARC_TO_NV = 0x16
    const val GL_RELATIVE_LARGE_CCW_ARC_TO_NV = 0x17
    const val GL_LARGE_CW_ARC_TO_NV = 0x18
    const val GL_RELATIVE_LARGE_CW_ARC_TO_NV = 0x19
    const val GL_RESTART_PATH_NV = 0xF0
    const val GL_DUP_FIRST_CUBIC_CURVE_TO_NV = 0xF2
    const val GL_DUP_LAST_CUBIC_CURVE_TO_NV = 0xF4
    const val GL_RECT_NV = 0xF6
    const val GL_CIRCULAR_CCW_ARC_TO_NV = 0xF8
    const val GL_CIRCULAR_CW_ARC_TO_NV = 0xFA
    const val GL_CIRCULAR_TANGENT_ARC_TO_NV = 0xFC
    const val GL_ARC_TO_NV = 0xFE
    const val GL_RELATIVE_ARC_TO_NV = 0xFF
    const val GL_BOLD_BIT_NV = 0x01
    const val GL_ITALIC_BIT_NV = 0x02
    const val GL_GLYPH_WIDTH_BIT_NV = 0x01
    const val GL_GLYPH_HEIGHT_BIT_NV = 0x02
    const val GL_GLYPH_HORIZONTAL_BEARING_X_BIT_NV = 0x04
    const val GL_GLYPH_HORIZONTAL_BEARING_Y_BIT_NV = 0x08
    const val GL_GLYPH_HORIZONTAL_BEARING_ADVANCE_BIT_NV = 0x10
    const val GL_GLYPH_VERTICAL_BEARING_X_BIT_NV = 0x20
    const val GL_GLYPH_VERTICAL_BEARING_Y_BIT_NV = 0x40
    const val GL_GLYPH_VERTICAL_BEARING_ADVANCE_BIT_NV = 0x80
    const val GL_GLYPH_HAS_KERNING_BIT_NV = 0x100
    const val GL_FONT_X_MIN_BOUNDS_BIT_NV = 0x00010000
    const val GL_FONT_Y_MIN_BOUNDS_BIT_NV = 0x00020000
    const val GL_FONT_X_MAX_BOUNDS_BIT_NV = 0x00040000
    const val GL_FONT_Y_MAX_BOUNDS_BIT_NV = 0x00080000
    const val GL_FONT_UNITS_PER_EM_BIT_NV = 0x00100000
    const val GL_FONT_ASCENDER_BIT_NV = 0x00200000
    const val GL_FONT_DESCENDER_BIT_NV = 0x00400000
    const val GL_FONT_HEIGHT_BIT_NV = 0x00800000
    const val GL_FONT_MAX_ADVANCE_WIDTH_BIT_NV = 0x01000000
    const val GL_FONT_MAX_ADVANCE_HEIGHT_BIT_NV = 0x02000000
    const val GL_FONT_UNDERLINE_POSITION_BIT_NV = 0x04000000
    const val GL_FONT_UNDERLINE_THICKNESS_BIT_NV = 0x08000000
    const val GL_FONT_HAS_KERNING_BIT_NV = 0x10000000
    const val GL_ROUNDED_RECT_NV = 0xE8
    const val GL_RELATIVE_ROUNDED_RECT_NV = 0xE9
    const val GL_ROUNDED_RECT2_NV = 0xEA
    const val GL_RELATIVE_ROUNDED_RECT2_NV = 0xEB
    const val GL_ROUNDED_RECT4_NV = 0xEC
    const val GL_RELATIVE_ROUNDED_RECT4_NV = 0xED
    const val GL_ROUNDED_RECT8_NV = 0xEE
    const val GL_RELATIVE_ROUNDED_RECT8_NV = 0xEF
    const val GL_RELATIVE_RECT_NV = 0xF7
    const val GL_FONT_GLYPHS_AVAILABLE_NV = 0x9368
    const val GL_FONT_TARGET_UNAVAILABLE_NV = 0x9369
    const val GL_FONT_UNAVAILABLE_NV = 0x936A
    const val GL_FONT_UNINTELLIGIBLE_NV = 0x936B
    const val GL_CONIC_CURVE_TO_NV = 0x1A
    const val GL_RELATIVE_CONIC_CURVE_TO_NV = 0x1B
    const val GL_FONT_NUM_GLYPH_INDICES_BIT_NV = 0x20000000
    const val GL_STANDARD_FONT_FORMAT_NV = 0x936C
    const val GL_2_BYTES_NV = 0x1407
    const val GL_3_BYTES_NV = 0x1408
    const val GL_4_BYTES_NV = 0x1409
    const val GL_EYE_LINEAR_NV = 0x2400
    const val GL_OBJECT_LINEAR_NV = 0x2401
    const val GL_CONSTANT_NV = 0x8576
    const val GL_PATH_FOG_GEN_MODE_NV = 0x90AC
    const val GL_PRIMARY_COLOR = 0x8577
    const val GL_PRIMARY_COLOR_NV = 0x852C
    const val GL_SECONDARY_COLOR_NV = 0x852D
    const val GL_PATH_GEN_COLOR_FORMAT_NV = 0x90B2
    const val GL_PATH_PROJECTION_NV = 0x1701
    const val GL_PATH_MODELVIEW_NV = 0x1700
    const val GL_PATH_MODELVIEW_STACK_DEPTH_NV = 0x0BA3
    const val GL_PATH_MODELVIEW_MATRIX_NV = 0x0BA6
    const val GL_PATH_MAX_MODELVIEW_STACK_DEPTH_NV = 0x0D36
    const val GL_PATH_TRANSPOSE_MODELVIEW_MATRIX_NV = 0x84E3
    const val GL_PATH_PROJECTION_STACK_DEPTH_NV = 0x0BA4
    const val GL_PATH_PROJECTION_MATRIX_NV = 0x0BA7
    const val GL_PATH_MAX_PROJECTION_STACK_DEPTH_NV = 0x0D38
    const val GL_PATH_TRANSPOSE_PROJECTION_MATRIX_NV = 0x84E4
    const val GL_FRAGMENT_INPUT_NV = 0x936D
    const val GL_SHARED_EDGE_NV = 0xC0
    const val GL_WRITE_PIXEL_DATA_RANGE_NV = 0x8878
    const val GL_READ_PIXEL_DATA_RANGE_NV = 0x8879
    const val GL_WRITE_PIXEL_DATA_RANGE_LENGTH_NV = 0x887A
    const val GL_READ_PIXEL_DATA_RANGE_LENGTH_NV = 0x887B
    const val GL_WRITE_PIXEL_DATA_RANGE_POINTER_NV = 0x887C
    const val GL_READ_PIXEL_DATA_RANGE_POINTER_NV = 0x887D
    const val GL_POINT_SPRITE_NV = 0x8861
    const val GL_COORD_REPLACE_NV = 0x8862
    const val GL_POINT_SPRITE_R_MODE_NV = 0x8863
    const val GL_FRAME_NV = 0x8E26
    const val GL_FIELDS_NV = 0x8E27
    const val GL_CURRENT_TIME_NV = 0x8E28
    const val GL_NUM_FILL_STREAMS_NV = 0x8E29
    const val GL_PRESENT_TIME_NV = 0x8E2A
    const val GL_PRESENT_DURATION_NV = 0x8E2B
    const val GL_PRIMITIVE_RESTART_NV = 0x8558
    const val GL_PRIMITIVE_RESTART_INDEX_NV = 0x8559
    const val GL_SHADING_RATE_IMAGE_PER_PRIMITIVE_NV = 0x95B1
    const val GL_SHADING_RATE_IMAGE_PALETTE_COUNT_NV = 0x95B2
    const val GL_QUERY_RESOURCE_TYPE_VIDMEM_ALLOC_NV = 0x9540
    const val GL_QUERY_RESOURCE_MEMTYPE_VIDMEM_NV = 0x9542
    const val GL_QUERY_RESOURCE_SYS_RESERVED_NV = 0x9544
    const val GL_QUERY_RESOURCE_TEXTURE_NV = 0x9545
    const val GL_QUERY_RESOURCE_RENDERBUFFER_NV = 0x9546
    const val GL_QUERY_RESOURCE_BUFFEROBJECT_NV = 0x9547
    const val GL_REGISTER_COMBINERS_NV = 0x8522
    const val GL_VARIABLE_A_NV = 0x8523
    const val GL_VARIABLE_B_NV = 0x8524
    const val GL_VARIABLE_C_NV = 0x8525
    const val GL_VARIABLE_D_NV = 0x8526
    const val GL_VARIABLE_E_NV = 0x8527
    const val GL_VARIABLE_F_NV = 0x8528
    const val GL_VARIABLE_G_NV = 0x8529
    const val GL_CONSTANT_COLOR0_NV = 0x852A
    const val GL_CONSTANT_COLOR1_NV = 0x852B
    const val GL_SPARE0_NV = 0x852E
    const val GL_SPARE1_NV = 0x852F
    const val GL_DISCARD_NV = 0x8530
    const val GL_E_TIMES_F_NV = 0x8531
    const val GL_SPARE0_PLUS_SECONDARY_COLOR_NV = 0x8532
    const val GL_UNSIGNED_IDENTITY_NV = 0x8536
    const val GL_UNSIGNED_INVERT_NV = 0x8537
    const val GL_EXPAND_NORMAL_NV = 0x8538
    const val GL_EXPAND_NEGATE_NV = 0x8539
    const val GL_HALF_BIAS_NORMAL_NV = 0x853A
    const val GL_HALF_BIAS_NEGATE_NV = 0x853B
    const val GL_SIGNED_IDENTITY_NV = 0x853C
    const val GL_SIGNED_NEGATE_NV = 0x853D
    const val GL_SCALE_BY_TWO_NV = 0x853E
    const val GL_SCALE_BY_FOUR_NV = 0x853F
    const val GL_SCALE_BY_ONE_HALF_NV = 0x8540
    const val GL_BIAS_BY_NEGATIVE_ONE_HALF_NV = 0x8541
    const val GL_COMBINER_INPUT_NV = 0x8542
    const val GL_COMBINER_MAPPING_NV = 0x8543
    const val GL_COMBINER_COMPONENT_USAGE_NV = 0x8544
    const val GL_COMBINER_AB_DOT_PRODUCT_NV = 0x8545
    const val GL_COMBINER_CD_DOT_PRODUCT_NV = 0x8546
    const val GL_COMBINER_MUX_SUM_NV = 0x8547
    const val GL_COMBINER_SCALE_NV = 0x8548
    const val GL_COMBINER_BIAS_NV = 0x8549
    const val GL_COMBINER_AB_OUTPUT_NV = 0x854A
    const val GL_COMBINER_CD_OUTPUT_NV = 0x854B
    const val GL_COMBINER_SUM_OUTPUT_NV = 0x854C
    const val GL_MAX_GENERAL_COMBINERS_NV = 0x854D
    const val GL_NUM_GENERAL_COMBINERS_NV = 0x854E
    const val GL_COLOR_SUM_CLAMP_NV = 0x854F
    const val GL_COMBINER0_NV = 0x8550
    const val GL_COMBINER1_NV = 0x8551
    const val GL_COMBINER2_NV = 0x8552
    const val GL_COMBINER3_NV = 0x8553
    const val GL_COMBINER4_NV = 0x8554
    const val GL_COMBINER5_NV = 0x8555
    const val GL_COMBINER6_NV = 0x8556
    const val GL_COMBINER7_NV = 0x8557
    const val GL_FOG = 0x0B60
    const val GL_PER_STAGE_CONSTANTS_NV = 0x8535
    const val GL_REPRESENTATIVE_FRAGMENT_TEST_NV = 0x937F
    const val GL_PURGED_CONTEXT_RESET_NV = 0x92BB
    const val GL_SAMPLE_LOCATION_SUBPIXEL_BITS_NV = 0x933D
    const val GL_SAMPLE_LOCATION_PIXEL_GRID_WIDTH_NV = 0x933E
    const val GL_SAMPLE_LOCATION_PIXEL_GRID_HEIGHT_NV = 0x933F
    const val GL_PROGRAMMABLE_SAMPLE_LOCATION_TABLE_SIZE_NV = 0x9340
    const val GL_SAMPLE_LOCATION_NV = 0x8E50
    const val GL_PROGRAMMABLE_SAMPLE_LOCATION_NV = 0x9341
    const val GL_FRAMEBUFFER_PROGRAMMABLE_SAMPLE_LOCATIONS_NV = 0x9342
    const val GL_FRAMEBUFFER_SAMPLE_LOCATION_PIXEL_GRID_NV = 0x9343
    const val GL_SCISSOR_TEST_EXCLUSIVE_NV = 0x9555
    const val GL_SCISSOR_BOX_EXCLUSIVE_NV = 0x9556
    const val GL_BUFFER_GPU_ADDRESS_NV = 0x8F1D
    const val GL_GPU_ADDRESS_NV = 0x8F34
    const val GL_MAX_SHADER_BUFFER_ADDRESS_NV = 0x8F35
    const val GL_SHADER_GLOBAL_ACCESS_BARRIER_BIT_NV = 0x00000010
    const val GL_SUBGROUP_FEATURE_PARTITIONED_BIT_NV = 0x00000100
    const val GL_WARP_SIZE_NV = 0x9339
    const val GL_WARPS_PER_SM_NV = 0x933A
    const val GL_SM_COUNT_NV = 0x933B
    const val GL_SHADING_RATE_IMAGE_NV = 0x9563
    const val GL_SHADING_RATE_NO_INVOCATIONS_NV = 0x9564
    const val GL_SHADING_RATE_1_INVOCATION_PER_PIXEL_NV = 0x9565
    const val GL_SHADING_RATE_1_INVOCATION_PER_1X2_PIXELS_NV = 0x9566
    const val GL_SHADING_RATE_1_INVOCATION_PER_2X1_PIXELS_NV = 0x9567
    const val GL_SHADING_RATE_1_INVOCATION_PER_2X2_PIXELS_NV = 0x9568
    const val GL_SHADING_RATE_1_INVOCATION_PER_2X4_PIXELS_NV = 0x9569
    const val GL_SHADING_RATE_1_INVOCATION_PER_4X2_PIXELS_NV = 0x956A
    const val GL_SHADING_RATE_1_INVOCATION_PER_4X4_PIXELS_NV = 0x956B
    const val GL_SHADING_RATE_2_INVOCATIONS_PER_PIXEL_NV = 0x956C
    const val GL_SHADING_RATE_4_INVOCATIONS_PER_PIXEL_NV = 0x956D
    const val GL_SHADING_RATE_8_INVOCATIONS_PER_PIXEL_NV = 0x956E
    const val GL_SHADING_RATE_16_INVOCATIONS_PER_PIXEL_NV = 0x956F
    const val GL_SHADING_RATE_IMAGE_BINDING_NV = 0x955B
    const val GL_SHADING_RATE_IMAGE_TEXEL_WIDTH_NV = 0x955C
    const val GL_SHADING_RATE_IMAGE_TEXEL_HEIGHT_NV = 0x955D
    const val GL_SHADING_RATE_IMAGE_PALETTE_SIZE_NV = 0x955E
    const val GL_MAX_COARSE_FRAGMENT_SAMPLES_NV = 0x955F
    const val GL_SHADING_RATE_SAMPLE_ORDER_DEFAULT_NV = 0x95AE
    const val GL_SHADING_RATE_SAMPLE_ORDER_PIXEL_MAJOR_NV = 0x95AF
    const val GL_SHADING_RATE_SAMPLE_ORDER_SAMPLE_MAJOR_NV = 0x95B0
    const val GL_MAX_PROGRAM_PATCH_ATTRIBS_NV = 0x86D8
    const val GL_TESS_CONTROL_PROGRAM_NV = 0x891E
    const val GL_TESS_EVALUATION_PROGRAM_NV = 0x891F
    const val GL_TESS_CONTROL_PROGRAM_PARAMETER_BUFFER_NV = 0x8C74
    const val GL_TESS_EVALUATION_PROGRAM_PARAMETER_BUFFER_NV = 0x8C75
    const val GL_EMBOSS_LIGHT_NV = 0x855D
    const val GL_EMBOSS_CONSTANT_NV = 0x855E
    const val GL_EMBOSS_MAP_NV = 0x855F
    const val GL_NORMAL_MAP_NV = 0x8511
    const val GL_REFLECTION_MAP_NV = 0x8512
    const val GL_COMBINE4_NV = 0x8503
    const val GL_SOURCE3_RGB_NV = 0x8583
    const val GL_SOURCE3_ALPHA_NV = 0x858B
    const val GL_OPERAND3_RGB_NV = 0x8593
    const val GL_OPERAND3_ALPHA_NV = 0x859B
    const val GL_TEXTURE_UNSIGNED_REMAP_MODE_NV = 0x888F
    const val GL_TEXTURE_COVERAGE_SAMPLES_NV = 0x9045
    const val GL_TEXTURE_COLOR_SAMPLES_NV = 0x9046
    const val GL_TEXTURE_RECTANGLE_NV = 0x84F5
    const val GL_TEXTURE_BINDING_RECTANGLE_NV = 0x84F6
    const val GL_PROXY_TEXTURE_RECTANGLE_NV = 0x84F7
    const val GL_MAX_RECTANGLE_TEXTURE_SIZE_NV = 0x84F8
    const val GL_OFFSET_TEXTURE_RECTANGLE_NV = 0x864C
    const val GL_OFFSET_TEXTURE_RECTANGLE_SCALE_NV = 0x864D
    const val GL_DOT_PRODUCT_TEXTURE_RECTANGLE_NV = 0x864E
    const val GL_RGBA_UNSIGNED_DOT_PRODUCT_MAPPING_NV = 0x86D9
    const val GL_UNSIGNED_INT_S8_S8_8_8_NV = 0x86DA
    const val GL_UNSIGNED_INT_8_8_S8_S8_REV_NV = 0x86DB
    const val GL_DSDT_MAG_INTENSITY_NV = 0x86DC
    const val GL_SHADER_CONSISTENT_NV = 0x86DD
    const val GL_TEXTURE_SHADER_NV = 0x86DE
    const val GL_SHADER_OPERATION_NV = 0x86DF
    const val GL_CULL_MODES_NV = 0x86E0
    const val GL_OFFSET_TEXTURE_MATRIX_NV = 0x86E1
    const val GL_OFFSET_TEXTURE_SCALE_NV = 0x86E2
    const val GL_OFFSET_TEXTURE_BIAS_NV = 0x86E3
    const val GL_OFFSET_TEXTURE_2D_MATRIX_NV = 0x86E1
    const val GL_OFFSET_TEXTURE_2D_SCALE_NV = 0x86E2
    const val GL_OFFSET_TEXTURE_2D_BIAS_NV = 0x86E3
    const val GL_PREVIOUS_TEXTURE_INPUT_NV = 0x86E4
    const val GL_CONST_EYE_NV = 0x86E5
    const val GL_PASS_THROUGH_NV = 0x86E6
    const val GL_CULL_FRAGMENT_NV = 0x86E7
    const val GL_OFFSET_TEXTURE_2D_NV = 0x86E8
    const val GL_DEPENDENT_AR_TEXTURE_2D_NV = 0x86E9
    const val GL_DEPENDENT_GB_TEXTURE_2D_NV = 0x86EA
    const val GL_DOT_PRODUCT_NV = 0x86EC
    const val GL_DOT_PRODUCT_DEPTH_REPLACE_NV = 0x86ED
    const val GL_DOT_PRODUCT_TEXTURE_2D_NV = 0x86EE
    const val GL_DOT_PRODUCT_TEXTURE_CUBE_MAP_NV = 0x86F0
    const val GL_DOT_PRODUCT_DIFFUSE_CUBE_MAP_NV = 0x86F1
    const val GL_DOT_PRODUCT_REFLECT_CUBE_MAP_NV = 0x86F2
    const val GL_DOT_PRODUCT_CONST_EYE_REFLECT_CUBE_MAP_NV = 0x86F3
    const val GL_HILO_NV = 0x86F4
    const val GL_DSDT_NV = 0x86F5
    const val GL_DSDT_MAG_NV = 0x86F6
    const val GL_DSDT_MAG_VIB_NV = 0x86F7
    const val GL_HILO16_NV = 0x86F8
    const val GL_SIGNED_HILO_NV = 0x86F9
    const val GL_SIGNED_HILO16_NV = 0x86FA
    const val GL_SIGNED_RGBA_NV = 0x86FB
    const val GL_SIGNED_RGBA8_NV = 0x86FC
    const val GL_SIGNED_RGB_NV = 0x86FE
    const val GL_SIGNED_RGB8_NV = 0x86FF
    const val GL_SIGNED_LUMINANCE_NV = 0x8701
    const val GL_SIGNED_LUMINANCE8_NV = 0x8702
    const val GL_SIGNED_LUMINANCE_ALPHA_NV = 0x8703
    const val GL_SIGNED_LUMINANCE8_ALPHA8_NV = 0x8704
    const val GL_SIGNED_ALPHA_NV = 0x8705
    const val GL_SIGNED_ALPHA8_NV = 0x8706
    const val GL_SIGNED_INTENSITY_NV = 0x8707
    const val GL_SIGNED_INTENSITY8_NV = 0x8708
    const val GL_DSDT8_NV = 0x8709
    const val GL_DSDT8_MAG8_NV = 0x870A
    const val GL_DSDT8_MAG8_INTENSITY8_NV = 0x870B
    const val GL_SIGNED_RGB_UNSIGNED_ALPHA_NV = 0x870C
    const val GL_SIGNED_RGB8_UNSIGNED_ALPHA8_NV = 0x870D
    const val GL_HI_SCALE_NV = 0x870E
    const val GL_LO_SCALE_NV = 0x870F
    const val GL_DS_SCALE_NV = 0x8710
    const val GL_DT_SCALE_NV = 0x8711
    const val GL_MAGNITUDE_SCALE_NV = 0x8712
    const val GL_VIBRANCE_SCALE_NV = 0x8713
    const val GL_HI_BIAS_NV = 0x8714
    const val GL_LO_BIAS_NV = 0x8715
    const val GL_DS_BIAS_NV = 0x8716
    const val GL_DT_BIAS_NV = 0x8717
    const val GL_MAGNITUDE_BIAS_NV = 0x8718
    const val GL_VIBRANCE_BIAS_NV = 0x8719
    const val GL_TEXTURE_BORDER_VALUES_NV = 0x871A
    const val GL_TEXTURE_HI_SIZE_NV = 0x871B
    const val GL_TEXTURE_LO_SIZE_NV = 0x871C
    const val GL_TEXTURE_DS_SIZE_NV = 0x871D
    const val GL_TEXTURE_DT_SIZE_NV = 0x871E
    const val GL_TEXTURE_MAG_SIZE_NV = 0x871F
    const val GL_DOT_PRODUCT_TEXTURE_3D_NV = 0x86EF
    const val GL_OFFSET_PROJECTIVE_TEXTURE_2D_NV = 0x8850
    const val GL_OFFSET_PROJECTIVE_TEXTURE_2D_SCALE_NV = 0x8851
    const val GL_OFFSET_PROJECTIVE_TEXTURE_RECTANGLE_NV = 0x8852
    const val GL_OFFSET_PROJECTIVE_TEXTURE_RECTANGLE_SCALE_NV = 0x8853
    const val GL_OFFSET_HILO_TEXTURE_2D_NV = 0x8854
    const val GL_OFFSET_HILO_TEXTURE_RECTANGLE_NV = 0x8855
    const val GL_OFFSET_HILO_PROJECTIVE_TEXTURE_2D_NV = 0x8856
    const val GL_OFFSET_HILO_PROJECTIVE_TEXTURE_RECTANGLE_NV = 0x8857
    const val GL_DEPENDENT_HILO_TEXTURE_2D_NV = 0x8858
    const val GL_DEPENDENT_RGB_TEXTURE_3D_NV = 0x8859
    const val GL_DEPENDENT_RGB_TEXTURE_CUBE_MAP_NV = 0x885A
    const val GL_DOT_PRODUCT_PASS_THROUGH_NV = 0x885B
    const val GL_DOT_PRODUCT_TEXTURE_1D_NV = 0x885C
    const val GL_DOT_PRODUCT_AFFINE_DEPTH_REPLACE_NV = 0x885D
    const val GL_HILO8_NV = 0x885E
    const val GL_SIGNED_HILO8_NV = 0x885F
    const val GL_FORCE_BLUE_TO_ONE_NV = 0x8860
    const val GL_TIMELINE_SEMAPHORE_VALUE_NV = 0x9595
    const val GL_SEMAPHORE_TYPE_NV = 0x95B3
    const val GL_SEMAPHORE_TYPE_BINARY_NV = 0x95B4
    const val GL_SEMAPHORE_TYPE_TIMELINE_NV = 0x95B5
    const val GL_MAX_TIMELINE_SEMAPHORE_VALUE_DIFFERENCE_NV = 0x95B6
    const val GL_BACK_PRIMARY_COLOR_NV = 0x8C77
    const val GL_BACK_SECONDARY_COLOR_NV = 0x8C78
    const val GL_TEXTURE_COORD_NV = 0x8C79
    const val GL_CLIP_DISTANCE_NV = 0x8C7A
    const val GL_VERTEX_ID_NV = 0x8C7B
    const val GL_PRIMITIVE_ID_NV = 0x8C7C
    const val GL_GENERIC_ATTRIB_NV = 0x8C7D
    const val GL_TRANSFORM_FEEDBACK_ATTRIBS_NV = 0x8C7E
    const val GL_TRANSFORM_FEEDBACK_BUFFER_MODE_NV = 0x8C7F
    const val GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_COMPONENTS_NV = 0x8C80
    const val GL_ACTIVE_VARYINGS_NV = 0x8C81
    const val GL_ACTIVE_VARYING_MAX_LENGTH_NV = 0x8C82
    const val GL_TRANSFORM_FEEDBACK_VARYINGS_NV = 0x8C83
    const val GL_TRANSFORM_FEEDBACK_BUFFER_START_NV = 0x8C84
    const val GL_TRANSFORM_FEEDBACK_BUFFER_SIZE_NV = 0x8C85
    const val GL_TRANSFORM_FEEDBACK_RECORD_NV = 0x8C86
    const val GL_PRIMITIVES_GENERATED_NV = 0x8C87
    const val GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN_NV = 0x8C88
    const val GL_RASTERIZER_DISCARD_NV = 0x8C89
    const val GL_MAX_TRANSFORM_FEEDBACK_INTERLEAVED_COMPONENTS_NV = 0x8C8A
    const val GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_ATTRIBS_NV = 0x8C8B
    const val GL_INTERLEAVED_ATTRIBS_NV = 0x8C8C
    const val GL_SEPARATE_ATTRIBS_NV = 0x8C8D
    const val GL_TRANSFORM_FEEDBACK_BUFFER_NV = 0x8C8E
    const val GL_TRANSFORM_FEEDBACK_BUFFER_BINDING_NV = 0x8C8F
    const val GL_LAYER_NV = 0x8DAA
    const val GL_NEXT_BUFFER_NV = -2
    const val GL_SKIP_COMPONENTS4_NV = -3
    const val GL_SKIP_COMPONENTS3_NV = -4
    const val GL_SKIP_COMPONENTS2_NV = -5
    const val GL_SKIP_COMPONENTS1_NV = -6
    const val GL_TRANSFORM_FEEDBACK_NV = 0x8E22
    const val GL_TRANSFORM_FEEDBACK_BUFFER_PAUSED_NV = 0x8E23
    const val GL_TRANSFORM_FEEDBACK_BUFFER_ACTIVE_NV = 0x8E24
    const val GL_TRANSFORM_FEEDBACK_BINDING_NV = 0x8E25
    const val GL_UNIFORM_BUFFER_UNIFIED_NV = 0x936E
    const val GL_UNIFORM_BUFFER_ADDRESS_NV = 0x936F
    const val GL_UNIFORM_BUFFER_LENGTH_NV = 0x9370
    const val GL_SURFACE_STATE_NV = 0x86EB
    const val GL_SURFACE_REGISTERED_NV = 0x86FD
    const val GL_SURFACE_MAPPED_NV = 0x8700
    const val GL_WRITE_DISCARD_NV = 0x88BE
    const val GL_VERTEX_ARRAY_RANGE_NV = 0x851D
    const val GL_VERTEX_ARRAY_RANGE_LENGTH_NV = 0x851E
    const val GL_VERTEX_ARRAY_RANGE_VALID_NV = 0x851F
    const val GL_MAX_VERTEX_ARRAY_RANGE_ELEMENT_NV = 0x8520
    const val GL_VERTEX_ARRAY_RANGE_POINTER_NV = 0x8521
    const val GL_VERTEX_ARRAY_RANGE_WITHOUT_FLUSH_NV = 0x8533
    const val GL_VERTEX_ATTRIB_ARRAY_UNIFIED_NV = 0x8F1E
    const val GL_ELEMENT_ARRAY_UNIFIED_NV = 0x8F1F
    const val GL_VERTEX_ATTRIB_ARRAY_ADDRESS_NV = 0x8F20
    const val GL_VERTEX_ARRAY_ADDRESS_NV = 0x8F21
    const val GL_NORMAL_ARRAY_ADDRESS_NV = 0x8F22
    const val GL_COLOR_ARRAY_ADDRESS_NV = 0x8F23
    const val GL_INDEX_ARRAY_ADDRESS_NV = 0x8F24
    const val GL_TEXTURE_COORD_ARRAY_ADDRESS_NV = 0x8F25
    const val GL_EDGE_FLAG_ARRAY_ADDRESS_NV = 0x8F26
    const val GL_SECONDARY_COLOR_ARRAY_ADDRESS_NV = 0x8F27
    const val GL_FOG_COORD_ARRAY_ADDRESS_NV = 0x8F28
    const val GL_ELEMENT_ARRAY_ADDRESS_NV = 0x8F29
    const val GL_VERTEX_ATTRIB_ARRAY_LENGTH_NV = 0x8F2A
    const val GL_VERTEX_ARRAY_LENGTH_NV = 0x8F2B
    const val GL_NORMAL_ARRAY_LENGTH_NV = 0x8F2C
    const val GL_COLOR_ARRAY_LENGTH_NV = 0x8F2D
    const val GL_INDEX_ARRAY_LENGTH_NV = 0x8F2E
    const val GL_TEXTURE_COORD_ARRAY_LENGTH_NV = 0x8F2F
    const val GL_EDGE_FLAG_ARRAY_LENGTH_NV = 0x8F30
    const val GL_SECONDARY_COLOR_ARRAY_LENGTH_NV = 0x8F31
    const val GL_FOG_COORD_ARRAY_LENGTH_NV = 0x8F32
    const val GL_ELEMENT_ARRAY_LENGTH_NV = 0x8F33
    const val GL_DRAW_INDIRECT_UNIFIED_NV = 0x8F40
    const val GL_DRAW_INDIRECT_ADDRESS_NV = 0x8F41
    const val GL_DRAW_INDIRECT_LENGTH_NV = 0x8F42
    const val GL_VERTEX_PROGRAM_NV = 0x8620
    const val GL_VERTEX_STATE_PROGRAM_NV = 0x8621
    const val GL_ATTRIB_ARRAY_SIZE_NV = 0x8623
    const val GL_ATTRIB_ARRAY_STRIDE_NV = 0x8624
    const val GL_ATTRIB_ARRAY_TYPE_NV = 0x8625
    const val GL_CURRENT_ATTRIB_NV = 0x8626
    const val GL_PROGRAM_LENGTH_NV = 0x8627
    const val GL_PROGRAM_STRING_NV = 0x8628
    const val GL_MODELVIEW_PROJECTION_NV = 0x8629
    const val GL_IDENTITY_NV = 0x862A
    const val GL_INVERSE_NV = 0x862B
    const val GL_TRANSPOSE_NV = 0x862C
    const val GL_INVERSE_TRANSPOSE_NV = 0x862D
    const val GL_MAX_TRACK_MATRIX_STACK_DEPTH_NV = 0x862E
    const val GL_MAX_TRACK_MATRICES_NV = 0x862F
    const val GL_MATRIX0_NV = 0x8630
    const val GL_MATRIX1_NV = 0x8631
    const val GL_MATRIX2_NV = 0x8632
    const val GL_MATRIX3_NV = 0x8633
    const val GL_MATRIX4_NV = 0x8634
    const val GL_MATRIX5_NV = 0x8635
    const val GL_MATRIX6_NV = 0x8636
    const val GL_MATRIX7_NV = 0x8637
    const val GL_CURRENT_MATRIX_STACK_DEPTH_NV = 0x8640
    const val GL_CURRENT_MATRIX_NV = 0x8641
    const val GL_VERTEX_PROGRAM_POINT_SIZE_NV = 0x8642
    const val GL_VERTEX_PROGRAM_TWO_SIDE_NV = 0x8643
    const val GL_PROGRAM_PARAMETER_NV = 0x8644
    const val GL_ATTRIB_ARRAY_POINTER_NV = 0x8645
    const val GL_PROGRAM_TARGET_NV = 0x8646
    const val GL_PROGRAM_RESIDENT_NV = 0x8647
    const val GL_TRACK_MATRIX_NV = 0x8648
    const val GL_TRACK_MATRIX_TRANSFORM_NV = 0x8649
    const val GL_VERTEX_PROGRAM_BINDING_NV = 0x864A
    const val GL_PROGRAM_ERROR_POSITION_NV = 0x864B
    const val GL_VERTEX_ATTRIB_ARRAY0_NV = 0x8650
    const val GL_VERTEX_ATTRIB_ARRAY1_NV = 0x8651
    const val GL_VERTEX_ATTRIB_ARRAY2_NV = 0x8652
    const val GL_VERTEX_ATTRIB_ARRAY3_NV = 0x8653
    const val GL_VERTEX_ATTRIB_ARRAY4_NV = 0x8654
    const val GL_VERTEX_ATTRIB_ARRAY5_NV = 0x8655
    const val GL_VERTEX_ATTRIB_ARRAY6_NV = 0x8656
    const val GL_VERTEX_ATTRIB_ARRAY7_NV = 0x8657
    const val GL_VERTEX_ATTRIB_ARRAY8_NV = 0x8658
    const val GL_VERTEX_ATTRIB_ARRAY9_NV = 0x8659
    const val GL_VERTEX_ATTRIB_ARRAY10_NV = 0x865A
    const val GL_VERTEX_ATTRIB_ARRAY11_NV = 0x865B
    const val GL_VERTEX_ATTRIB_ARRAY12_NV = 0x865C
    const val GL_VERTEX_ATTRIB_ARRAY13_NV = 0x865D
    const val GL_VERTEX_ATTRIB_ARRAY14_NV = 0x865E
    const val GL_VERTEX_ATTRIB_ARRAY15_NV = 0x865F
    const val GL_MAP1_VERTEX_ATTRIB0_4_NV = 0x8660
    const val GL_MAP1_VERTEX_ATTRIB1_4_NV = 0x8661
    const val GL_MAP1_VERTEX_ATTRIB2_4_NV = 0x8662
    const val GL_MAP1_VERTEX_ATTRIB3_4_NV = 0x8663
    const val GL_MAP1_VERTEX_ATTRIB4_4_NV = 0x8664
    const val GL_MAP1_VERTEX_ATTRIB5_4_NV = 0x8665
    const val GL_MAP1_VERTEX_ATTRIB6_4_NV = 0x8666
    const val GL_MAP1_VERTEX_ATTRIB7_4_NV = 0x8667
    const val GL_MAP1_VERTEX_ATTRIB8_4_NV = 0x8668
    const val GL_MAP1_VERTEX_ATTRIB9_4_NV = 0x8669
    const val GL_MAP1_VERTEX_ATTRIB10_4_NV = 0x866A
    const val GL_MAP1_VERTEX_ATTRIB11_4_NV = 0x866B
    const val GL_MAP1_VERTEX_ATTRIB12_4_NV = 0x866C
    const val GL_MAP1_VERTEX_ATTRIB13_4_NV = 0x866D
    const val GL_MAP1_VERTEX_ATTRIB14_4_NV = 0x866E
    const val GL_MAP1_VERTEX_ATTRIB15_4_NV = 0x866F
    const val GL_MAP2_VERTEX_ATTRIB0_4_NV = 0x8670
    const val GL_MAP2_VERTEX_ATTRIB1_4_NV = 0x8671
    const val GL_MAP2_VERTEX_ATTRIB2_4_NV = 0x8672
    const val GL_MAP2_VERTEX_ATTRIB3_4_NV = 0x8673
    const val GL_MAP2_VERTEX_ATTRIB4_4_NV = 0x8674
    const val GL_MAP2_VERTEX_ATTRIB5_4_NV = 0x8675
    const val GL_MAP2_VERTEX_ATTRIB6_4_NV = 0x8676
    const val GL_MAP2_VERTEX_ATTRIB7_4_NV = 0x8677
    const val GL_MAP2_VERTEX_ATTRIB8_4_NV = 0x8678
    const val GL_MAP2_VERTEX_ATTRIB9_4_NV = 0x8679
    const val GL_MAP2_VERTEX_ATTRIB10_4_NV = 0x867A
    const val GL_MAP2_VERTEX_ATTRIB11_4_NV = 0x867B
    const val GL_MAP2_VERTEX_ATTRIB12_4_NV = 0x867C
    const val GL_MAP2_VERTEX_ATTRIB13_4_NV = 0x867D
    const val GL_MAP2_VERTEX_ATTRIB14_4_NV = 0x867E
    const val GL_MAP2_VERTEX_ATTRIB15_4_NV = 0x867F
    const val GL_VERTEX_ATTRIB_ARRAY_INTEGER_NV = 0x88FD
    const val GL_VIDEO_BUFFER_NV = 0x9020
    const val GL_VIDEO_BUFFER_BINDING_NV = 0x9021
    const val GL_FIELD_UPPER_NV = 0x9022
    const val GL_FIELD_LOWER_NV = 0x9023
    const val GL_NUM_VIDEO_CAPTURE_STREAMS_NV = 0x9024
    const val GL_NEXT_VIDEO_CAPTURE_BUFFER_STATUS_NV = 0x9025
    const val GL_VIDEO_CAPTURE_TO_422_SUPPORTED_NV = 0x9026
    const val GL_LAST_VIDEO_CAPTURE_STATUS_NV = 0x9027
    const val GL_VIDEO_BUFFER_PITCH_NV = 0x9028
    const val GL_VIDEO_COLOR_CONVERSION_MATRIX_NV = 0x9029
    const val GL_VIDEO_COLOR_CONVERSION_MAX_NV = 0x902A
    const val GL_VIDEO_COLOR_CONVERSION_MIN_NV = 0x902B
    const val GL_VIDEO_COLOR_CONVERSION_OFFSET_NV = 0x902C
    const val GL_VIDEO_BUFFER_INTERNAL_FORMAT_NV = 0x902D
    const val GL_PARTIAL_SUCCESS_NV = 0x902E
    const val GL_SUCCESS_NV = 0x902F
    const val GL_FAILURE_NV = 0x9030
    const val GL_YCBYCR8_422_NV = 0x9031
    const val GL_YCBAYCR8A_4224_NV = 0x9032
    const val GL_Z6Y10Z6CB10Z6Y10Z6CR10_422_NV = 0x9033
    const val GL_Z6Y10Z6CB10Z6A10Z6Y10Z6CR10Z6A10_4224_NV = 0x9034
    const val GL_Z4Y12Z4CB12Z4Y12Z4CR12_422_NV = 0x9035
    const val GL_Z4Y12Z4CB12Z4A12Z4Y12Z4CR12Z4A12_4224_NV = 0x9036
    const val GL_Z4Y12Z4CB12Z4CR12_444_NV = 0x9037
    const val GL_VIDEO_CAPTURE_FRAME_WIDTH_NV = 0x9038
    const val GL_VIDEO_CAPTURE_FRAME_HEIGHT_NV = 0x9039
    const val GL_VIDEO_CAPTURE_FIELD_UPPER_HEIGHT_NV = 0x903A
    const val GL_VIDEO_CAPTURE_FIELD_LOWER_HEIGHT_NV = 0x903B
    const val GL_VIDEO_CAPTURE_SURFACE_ORIGIN_NV = 0x903C
    const val GL_VIEWPORT_SWIZZLE_POSITIVE_X_NV = 0x9350
    const val GL_VIEWPORT_SWIZZLE_NEGATIVE_X_NV = 0x9351
    const val GL_VIEWPORT_SWIZZLE_POSITIVE_Y_NV = 0x9352
    const val GL_VIEWPORT_SWIZZLE_NEGATIVE_Y_NV = 0x9353
    const val GL_VIEWPORT_SWIZZLE_POSITIVE_Z_NV = 0x9354
    const val GL_VIEWPORT_SWIZZLE_NEGATIVE_Z_NV = 0x9355
    const val GL_VIEWPORT_SWIZZLE_POSITIVE_W_NV = 0x9356
    const val GL_VIEWPORT_SWIZZLE_NEGATIVE_W_NV = 0x9357
    const val GL_VIEWPORT_SWIZZLE_X_NV = 0x9358
    const val GL_VIEWPORT_SWIZZLE_Y_NV = 0x9359
    const val GL_VIEWPORT_SWIZZLE_Z_NV = 0x935A
    const val GL_VIEWPORT_SWIZZLE_W_NV = 0x935B
    const val GL_PALETTE4_RGB8_OES = 0x8B90
    const val GL_PALETTE4_RGBA8_OES = 0x8B91
    const val GL_PALETTE4_R5_G6_B5_OES = 0x8B92
    const val GL_PALETTE4_RGBA4_OES = 0x8B93
    const val GL_PALETTE4_RGB5_A1_OES = 0x8B94
    const val GL_PALETTE8_RGB8_OES = 0x8B95
    const val GL_PALETTE8_RGBA8_OES = 0x8B96
    const val GL_PALETTE8_R5_G6_B5_OES = 0x8B97
    const val GL_PALETTE8_RGBA4_OES = 0x8B98
    const val GL_PALETTE8_RGB5_A1_OES = 0x8B99
    const val GL_FIXED_OES = 0x140C
    const val GL_IMPLEMENTATION_COLOR_READ_TYPE_OES = 0x8B9A
    const val GL_IMPLEMENTATION_COLOR_READ_FORMAT_OES = 0x8B9B
    const val GL_INTERLACE_OML = 0x8980
    const val GL_INTERLACE_READ_OML = 0x8981
    const val GL_PACK_RESAMPLE_OML = 0x8984
    const val GL_UNPACK_RESAMPLE_OML = 0x8985
    const val GL_RESAMPLE_REPLICATE_OML = 0x8986
    const val GL_RESAMPLE_ZERO_FILL_OML = 0x8987
    const val GL_RESAMPLE_AVERAGE_OML = 0x8988
    const val GL_RESAMPLE_DECIMATE_OML = 0x8989
    const val GL_FORMAT_SUBSAMPLE_24_24_OML = 0x8982
    const val GL_FORMAT_SUBSAMPLE_244_244_OML = 0x8983
    const val GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_NUM_VIEWS_OVR = 0x9630
    const val GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_BASE_VIEW_INDEX_OVR = 0x9632
    const val GL_MAX_VIEWS_OVR = 0x9631
    const val GL_FRAMEBUFFER_INCOMPLETE_VIEW_TARGETS_OVR = 0x9633
    const val GL_PREFER_DOUBLEBUFFER_HINT_PGI = 0x1A1F8
    const val GL_CONSERVE_MEMORY_HINT_PGI = 0x1A1FD
    const val GL_RECLAIM_MEMORY_HINT_PGI = 0x1A1FE
    const val GL_NATIVE_GRAPHICS_HANDLE_PGI = 0x1A202
    const val GL_NATIVE_GRAPHICS_BEGIN_HINT_PGI = 0x1A203
    const val GL_NATIVE_GRAPHICS_END_HINT_PGI = 0x1A204
    const val GL_ALWAYS_FAST_HINT_PGI = 0x1A20C
    const val GL_ALWAYS_SOFT_HINT_PGI = 0x1A20D
    const val GL_ALLOW_DRAW_OBJ_HINT_PGI = 0x1A20E
    const val GL_ALLOW_DRAW_WIN_HINT_PGI = 0x1A20F
    const val GL_ALLOW_DRAW_FRG_HINT_PGI = 0x1A210
    const val GL_ALLOW_DRAW_MEM_HINT_PGI = 0x1A211
    const val GL_STRICT_DEPTHFUNC_HINT_PGI = 0x1A216
    const val GL_STRICT_LIGHTING_HINT_PGI = 0x1A217
    const val GL_STRICT_SCISSOR_HINT_PGI = 0x1A218
    const val GL_FULL_STIPPLE_HINT_PGI = 0x1A219
    const val GL_CLIP_NEAR_HINT_PGI = 0x1A220
    const val GL_CLIP_FAR_HINT_PGI = 0x1A221
    const val GL_WIDE_LINE_HINT_PGI = 0x1A222
    const val GL_BACK_NORMALS_HINT_PGI = 0x1A223
    const val GL_VERTEX_DATA_HINT_PGI = 0x1A22A
    const val GL_VERTEX_CONSISTENT_HINT_PGI = 0x1A22B
    const val GL_MATERIAL_SIDE_HINT_PGI = 0x1A22C
    const val GL_MAX_VERTEX_HINT_PGI = 0x1A22D
    const val GL_COLOR3_BIT_PGI = 0x00010000
    const val GL_COLOR4_BIT_PGI = 0x00020000
    const val GL_EDGEFLAG_BIT_PGI = 0x00040000
    const val GL_INDEX_BIT_PGI = 0x00080000
    const val GL_MAT_AMBIENT_BIT_PGI = 0x00100000
    const val GL_MAT_AMBIENT_AND_DIFFUSE_BIT_PGI = 0x00200000
    const val GL_MAT_DIFFUSE_BIT_PGI = 0x00400000
    const val GL_MAT_EMISSION_BIT_PGI = 0x00800000
    const val GL_MAT_COLOR_INDEXES_BIT_PGI = 0x01000000
    const val GL_MAT_SHININESS_BIT_PGI = 0x02000000
    const val GL_MAT_SPECULAR_BIT_PGI = 0x04000000
    const val GL_NORMAL_BIT_PGI = 0x08000000
    const val GL_TEXCOORD1_BIT_PGI = 0x10000000
    const val GL_TEXCOORD2_BIT_PGI = 0x20000000
    const val GL_TEXCOORD3_BIT_PGI = 0x40000000
    const val GL_TEXCOORD4_BIT_PGI = 0x80000000
    const val GL_VERTEX23_BIT_PGI = 0x00000004
    const val GL_VERTEX4_BIT_PGI = 0x00000008
    const val GL_SCREEN_COORDINATES_REND = 0x8490
    const val GL_INVERTED_SCREEN_W_REND = 0x8491
    const val GL_RGB_S3TC = 0x83A0
    const val GL_RGB4_S3TC = 0x83A1
    const val GL_RGBA_S3TC = 0x83A2
    const val GL_RGBA4_S3TC = 0x83A3
    const val GL_RGBA_DXT5_S3TC = 0x83A4
    const val GL_RGBA4_DXT5_S3TC = 0x83A5
    const val GL_DETAIL_TEXTURE_2D_SGIS = 0x8095
    const val GL_DETAIL_TEXTURE_2D_BINDING_SGIS = 0x8096
    const val GL_LINEAR_DETAIL_SGIS = 0x8097
    const val GL_LINEAR_DETAIL_ALPHA_SGIS = 0x8098
    const val GL_LINEAR_DETAIL_COLOR_SGIS = 0x8099
    const val GL_DETAIL_TEXTURE_LEVEL_SGIS = 0x809A
    const val GL_DETAIL_TEXTURE_MODE_SGIS = 0x809B
    const val GL_DETAIL_TEXTURE_FUNC_POINTS_SGIS = 0x809C
    const val GL_FOG_FUNC_SGIS = 0x812A
    const val GL_FOG_FUNC_POINTS_SGIS = 0x812B
    const val GL_MAX_FOG_FUNC_POINTS_SGIS = 0x812C
    const val GL_GENERATE_MIPMAP_SGIS = 0x8191
    const val GL_GENERATE_MIPMAP_HINT_SGIS = 0x8192
    const val GL_MULTISAMPLE_SGIS = 0x809D
    const val GL_SAMPLE_ALPHA_TO_MASK_SGIS = 0x809E
    const val GL_SAMPLE_ALPHA_TO_ONE_SGIS = 0x809F
    const val GL_SAMPLE_MASK_SGIS = 0x80A0
    const val GL_1PASS_SGIS = 0x80A1
    const val GL_2PASS_0_SGIS = 0x80A2
    const val GL_2PASS_1_SGIS = 0x80A3
    const val GL_4PASS_0_SGIS = 0x80A4
    const val GL_4PASS_1_SGIS = 0x80A5
    const val GL_4PASS_2_SGIS = 0x80A6
    const val GL_4PASS_3_SGIS = 0x80A7
    const val GL_SAMPLE_BUFFERS_SGIS = 0x80A8
    const val GL_SAMPLES_SGIS = 0x80A9
    const val GL_SAMPLE_MASK_VALUE_SGIS = 0x80AA
    const val GL_SAMPLE_MASK_INVERT_SGIS = 0x80AB
    const val GL_SAMPLE_PATTERN_SGIS = 0x80AC
    const val GL_PIXEL_TEXTURE_SGIS = 0x8353
    const val GL_PIXEL_FRAGMENT_RGB_SOURCE_SGIS = 0x8354
    const val GL_PIXEL_FRAGMENT_ALPHA_SOURCE_SGIS = 0x8355
    const val GL_PIXEL_GROUP_COLOR_SGIS = 0x8356
    const val GL_EYE_DISTANCE_TO_POINT_SGIS = 0x81F0
    const val GL_OBJECT_DISTANCE_TO_POINT_SGIS = 0x81F1
    const val GL_EYE_DISTANCE_TO_LINE_SGIS = 0x81F2
    const val GL_OBJECT_DISTANCE_TO_LINE_SGIS = 0x81F3
    const val GL_EYE_POINT_SGIS = 0x81F4
    const val GL_OBJECT_POINT_SGIS = 0x81F5
    const val GL_EYE_LINE_SGIS = 0x81F6
    const val GL_OBJECT_LINE_SGIS = 0x81F7
    const val GL_POINT_SIZE_MIN_SGIS = 0x8126
    const val GL_POINT_SIZE_MAX_SGIS = 0x8127
    const val GL_POINT_FADE_THRESHOLD_SIZE_SGIS = 0x8128
    const val GL_DISTANCE_ATTENUATION_SGIS = 0x8129
    const val GL_LINEAR_SHARPEN_SGIS = 0x80AD
    const val GL_LINEAR_SHARPEN_ALPHA_SGIS = 0x80AE
    const val GL_LINEAR_SHARPEN_COLOR_SGIS = 0x80AF
    const val GL_SHARPEN_TEXTURE_FUNC_POINTS_SGIS = 0x80B0
    const val GL_PACK_SKIP_VOLUMES_SGIS = 0x8130
    const val GL_PACK_IMAGE_DEPTH_SGIS = 0x8131
    const val GL_UNPACK_SKIP_VOLUMES_SGIS = 0x8132
    const val GL_UNPACK_IMAGE_DEPTH_SGIS = 0x8133
    const val GL_TEXTURE_4D_SGIS = 0x8134
    const val GL_PROXY_TEXTURE_4D_SGIS = 0x8135
    const val GL_TEXTURE_4DSIZE_SGIS = 0x8136
    const val GL_TEXTURE_WRAP_Q_SGIS = 0x8137
    const val GL_MAX_4D_TEXTURE_SIZE_SGIS = 0x8138
    const val GL_TEXTURE_4D_BINDING_SGIS = 0x814F
    const val GL_CLAMP_TO_BORDER_SGIS = 0x812D
    const val GL_TEXTURE_COLOR_WRITEMASK_SGIS = 0x81EF
    const val GL_CLAMP_TO_EDGE_SGIS = 0x812F
    const val GL_FILTER4_SGIS = 0x8146
    const val GL_TEXTURE_FILTER4_SIZE_SGIS = 0x8147
    const val GL_TEXTURE_MIN_LOD_SGIS = 0x813A
    const val GL_TEXTURE_MAX_LOD_SGIS = 0x813B
    const val GL_TEXTURE_BASE_LEVEL_SGIS = 0x813C
    const val GL_TEXTURE_MAX_LEVEL_SGIS = 0x813D
    const val GL_DUAL_ALPHA4_SGIS = 0x8110
    const val GL_DUAL_ALPHA8_SGIS = 0x8111
    const val GL_DUAL_ALPHA12_SGIS = 0x8112
    const val GL_DUAL_ALPHA16_SGIS = 0x8113
    const val GL_DUAL_LUMINANCE4_SGIS = 0x8114
    const val GL_DUAL_LUMINANCE8_SGIS = 0x8115
    const val GL_DUAL_LUMINANCE12_SGIS = 0x8116
    const val GL_DUAL_LUMINANCE16_SGIS = 0x8117
    const val GL_DUAL_INTENSITY4_SGIS = 0x8118
    const val GL_DUAL_INTENSITY8_SGIS = 0x8119
    const val GL_DUAL_INTENSITY12_SGIS = 0x811A
    const val GL_DUAL_INTENSITY16_SGIS = 0x811B
    const val GL_DUAL_LUMINANCE_ALPHA4_SGIS = 0x811C
    const val GL_DUAL_LUMINANCE_ALPHA8_SGIS = 0x811D
    const val GL_QUAD_ALPHA4_SGIS = 0x811E
    const val GL_QUAD_ALPHA8_SGIS = 0x811F
    const val GL_QUAD_LUMINANCE4_SGIS = 0x8120
    const val GL_QUAD_LUMINANCE8_SGIS = 0x8121
    const val GL_QUAD_INTENSITY4_SGIS = 0x8122
    const val GL_QUAD_INTENSITY8_SGIS = 0x8123
    const val GL_DUAL_TEXTURE_SELECT_SGIS = 0x8124
    const val GL_QUAD_TEXTURE_SELECT_SGIS = 0x8125
    const val GL_ASYNC_MARKER_SGIX = 0x8329
    const val GL_ASYNC_HISTOGRAM_SGIX = 0x832C
    const val GL_MAX_ASYNC_HISTOGRAM_SGIX = 0x832D
    const val GL_ASYNC_TEX_IMAGE_SGIX = 0x835C
    const val GL_ASYNC_DRAW_PIXELS_SGIX = 0x835D
    const val GL_ASYNC_READ_PIXELS_SGIX = 0x835E
    const val GL_MAX_ASYNC_TEX_IMAGE_SGIX = 0x835F
    const val GL_MAX_ASYNC_DRAW_PIXELS_SGIX = 0x8360
    const val GL_MAX_ASYNC_READ_PIXELS_SGIX = 0x8361
    const val GL_ALPHA_MIN_SGIX = 0x8320
    const val GL_ALPHA_MAX_SGIX = 0x8321
    const val GL_CALLIGRAPHIC_FRAGMENT_SGIX = 0x8183
    const val GL_LINEAR_CLIPMAP_LINEAR_SGIX = 0x8170
    const val GL_TEXTURE_CLIPMAP_CENTER_SGIX = 0x8171
    const val GL_TEXTURE_CLIPMAP_FRAME_SGIX = 0x8172
    const val GL_TEXTURE_CLIPMAP_OFFSET_SGIX = 0x8173
    const val GL_TEXTURE_CLIPMAP_VIRTUAL_DEPTH_SGIX = 0x8174
    const val GL_TEXTURE_CLIPMAP_LOD_OFFSET_SGIX = 0x8175
    const val GL_TEXTURE_CLIPMAP_DEPTH_SGIX = 0x8176
    const val GL_MAX_CLIPMAP_DEPTH_SGIX = 0x8177
    const val GL_MAX_CLIPMAP_VIRTUAL_DEPTH_SGIX = 0x8178
    const val GL_NEAREST_CLIPMAP_NEAREST_SGIX = 0x844D
    const val GL_NEAREST_CLIPMAP_LINEAR_SGIX = 0x844E
    const val GL_LINEAR_CLIPMAP_NEAREST_SGIX = 0x844F
    const val GL_CONVOLUTION_HINT_SGIX = 0x8316
    const val GL_DEPTH_COMPONENT16_SGIX = 0x81A5
    const val GL_DEPTH_COMPONENT24_SGIX = 0x81A6
    const val GL_DEPTH_COMPONENT32_SGIX = 0x81A7
    const val GL_FOG_OFFSET_SGIX = 0x8198
    const val GL_FOG_OFFSET_VALUE_SGIX = 0x8199
    const val GL_FRAGMENT_LIGHTING_SGIX = 0x8400
    const val GL_FRAGMENT_COLOR_MATERIAL_SGIX = 0x8401
    const val GL_FRAGMENT_COLOR_MATERIAL_FACE_SGIX = 0x8402
    const val GL_FRAGMENT_COLOR_MATERIAL_PARAMETER_SGIX = 0x8403
    const val GL_MAX_FRAGMENT_LIGHTS_SGIX = 0x8404
    const val GL_MAX_ACTIVE_LIGHTS_SGIX = 0x8405
    const val GL_CURRENT_RASTER_NORMAL_SGIX = 0x8406
    const val GL_LIGHT_ENV_MODE_SGIX = 0x8407
    const val GL_FRAGMENT_LIGHT_MODEL_LOCAL_VIEWER_SGIX = 0x8408
    const val GL_FRAGMENT_LIGHT_MODEL_TWO_SIDE_SGIX = 0x8409
    const val GL_FRAGMENT_LIGHT_MODEL_AMBIENT_SGIX = 0x840A
    const val GL_FRAGMENT_LIGHT_MODEL_NORMAL_INTERPOLATION_SGIX = 0x840B
    const val GL_FRAGMENT_LIGHT0_SGIX = 0x840C
    const val GL_FRAGMENT_LIGHT1_SGIX = 0x840D
    const val GL_FRAGMENT_LIGHT2_SGIX = 0x840E
    const val GL_FRAGMENT_LIGHT3_SGIX = 0x840F
    const val GL_FRAGMENT_LIGHT4_SGIX = 0x8410
    const val GL_FRAGMENT_LIGHT5_SGIX = 0x8411
    const val GL_FRAGMENT_LIGHT6_SGIX = 0x8412
    const val GL_FRAGMENT_LIGHT7_SGIX = 0x8413
    const val GL_FRAMEZOOM_SGIX = 0x818B
    const val GL_FRAMEZOOM_FACTOR_SGIX = 0x818C
    const val GL_MAX_FRAMEZOOM_FACTOR_SGIX = 0x818D
    const val GL_INSTRUMENT_BUFFER_POINTER_SGIX = 0x8180
    const val GL_INSTRUMENT_MEASUREMENTS_SGIX = 0x8181
    const val GL_INTERLACE_SGIX = 0x8094
    const val GL_IR_INSTRUMENT1_SGIX = 0x817F
    const val GL_LIST_PRIORITY_SGIX = 0x8182
    const val GL_PIXEL_TEX_GEN_SGIX = 0x8139
    const val GL_PIXEL_TEX_GEN_MODE_SGIX = 0x832B
    const val GL_PIXEL_TILE_BEST_ALIGNMENT_SGIX = 0x813E
    const val GL_PIXEL_TILE_CACHE_INCREMENT_SGIX = 0x813F
    const val GL_PIXEL_TILE_WIDTH_SGIX = 0x8140
    const val GL_PIXEL_TILE_HEIGHT_SGIX = 0x8141
    const val GL_PIXEL_TILE_GRID_WIDTH_SGIX = 0x8142
    const val GL_PIXEL_TILE_GRID_HEIGHT_SGIX = 0x8143
    const val GL_PIXEL_TILE_GRID_DEPTH_SGIX = 0x8144
    const val GL_PIXEL_TILE_CACHE_SIZE_SGIX = 0x8145
    const val GL_TEXTURE_DEFORMATION_BIT_SGIX = 0x00000001
    const val GL_GEOMETRY_DEFORMATION_BIT_SGIX = 0x00000002
    const val GL_GEOMETRY_DEFORMATION_SGIX = 0x8194
    const val GL_TEXTURE_DEFORMATION_SGIX = 0x8195
    const val GL_DEFORMATIONS_MASK_SGIX = 0x8196
    const val GL_MAX_DEFORMATION_ORDER_SGIX = 0x8197
    const val GL_REFERENCE_PLANE_SGIX = 0x817D
    const val GL_REFERENCE_PLANE_EQUATION_SGIX = 0x817E
    const val GL_PACK_RESAMPLE_SGIX = 0x842E
    const val GL_UNPACK_RESAMPLE_SGIX = 0x842F
    const val GL_RESAMPLE_REPLICATE_SGIX = 0x8433
    const val GL_RESAMPLE_ZERO_FILL_SGIX = 0x8434
    const val GL_RESAMPLE_DECIMATE_SGIX = 0x8430
    const val GL_SCALEBIAS_HINT_SGIX = 0x8322
    const val GL_TEXTURE_COMPARE_SGIX = 0x819A
    const val GL_TEXTURE_COMPARE_OPERATOR_SGIX = 0x819B
    const val GL_TEXTURE_LEQUAL_R_SGIX = 0x819C
    const val GL_TEXTURE_GEQUAL_R_SGIX = 0x819D
    const val GL_SHADOW_AMBIENT_SGIX = 0x80BF
    const val GL_SPRITE_SGIX = 0x8148
    const val GL_SPRITE_MODE_SGIX = 0x8149
    const val GL_SPRITE_AXIS_SGIX = 0x814A
    const val GL_SPRITE_TRANSLATION_SGIX = 0x814B
    const val GL_SPRITE_AXIAL_SGIX = 0x814C
    const val GL_SPRITE_OBJECT_ALIGNED_SGIX = 0x814D
    const val GL_SPRITE_EYE_ALIGNED_SGIX = 0x814E
    const val GL_PACK_SUBSAMPLE_RATE_SGIX = 0x85A0
    const val GL_UNPACK_SUBSAMPLE_RATE_SGIX = 0x85A1
    const val GL_PIXEL_SUBSAMPLE_4444_SGIX = 0x85A2
    const val GL_PIXEL_SUBSAMPLE_2424_SGIX = 0x85A3
    const val GL_PIXEL_SUBSAMPLE_4242_SGIX = 0x85A4
    const val GL_TEXTURE_ENV_BIAS_SGIX = 0x80BE
    const val GL_TEXTURE_MAX_CLAMP_S_SGIX = 0x8369
    const val GL_TEXTURE_MAX_CLAMP_T_SGIX = 0x836A
    const val GL_TEXTURE_MAX_CLAMP_R_SGIX = 0x836B
    const val GL_TEXTURE_LOD_BIAS_S_SGIX = 0x818E
    const val GL_TEXTURE_LOD_BIAS_T_SGIX = 0x818F
    const val GL_TEXTURE_LOD_BIAS_R_SGIX = 0x8190
    const val GL_TEXTURE_MULTI_BUFFER_HINT_SGIX = 0x812E
    const val GL_POST_TEXTURE_FILTER_BIAS_SGIX = 0x8179
    const val GL_POST_TEXTURE_FILTER_SCALE_SGIX = 0x817A
    const val GL_POST_TEXTURE_FILTER_BIAS_RANGE_SGIX = 0x817B
    const val GL_POST_TEXTURE_FILTER_SCALE_RANGE_SGIX = 0x817C
    const val GL_VERTEX_PRECLIP_SGIX = 0x83EE
    const val GL_VERTEX_PRECLIP_HINT_SGIX = 0x83EF
    const val GL_YCRCB_422_SGIX = 0x81BB
    const val GL_YCRCB_444_SGIX = 0x81BC
    const val GL_YCRCB_SGIX = 0x8318
    const val GL_YCRCBA_SGIX = 0x8319
    const val GL_COLOR_MATRIX_SGI = 0x80B1
    const val GL_COLOR_MATRIX_STACK_DEPTH_SGI = 0x80B2
    const val GL_MAX_COLOR_MATRIX_STACK_DEPTH_SGI = 0x80B3
    const val GL_POST_COLOR_MATRIX_RED_SCALE_SGI = 0x80B4
    const val GL_POST_COLOR_MATRIX_GREEN_SCALE_SGI = 0x80B5
    const val GL_POST_COLOR_MATRIX_BLUE_SCALE_SGI = 0x80B6
    const val GL_POST_COLOR_MATRIX_ALPHA_SCALE_SGI = 0x80B7
    const val GL_POST_COLOR_MATRIX_RED_BIAS_SGI = 0x80B8
    const val GL_POST_COLOR_MATRIX_GREEN_BIAS_SGI = 0x80B9
    const val GL_POST_COLOR_MATRIX_BLUE_BIAS_SGI = 0x80BA
    const val GL_POST_COLOR_MATRIX_ALPHA_BIAS_SGI = 0x80BB
    const val GL_COLOR_TABLE_SGI = 0x80D0
    const val GL_POST_CONVOLUTION_COLOR_TABLE_SGI = 0x80D1
    const val GL_POST_COLOR_MATRIX_COLOR_TABLE_SGI = 0x80D2
    const val GL_PROXY_COLOR_TABLE_SGI = 0x80D3
    const val GL_PROXY_POST_CONVOLUTION_COLOR_TABLE_SGI = 0x80D4
    const val GL_PROXY_POST_COLOR_MATRIX_COLOR_TABLE_SGI = 0x80D5
    const val GL_COLOR_TABLE_SCALE_SGI = 0x80D6
    const val GL_COLOR_TABLE_BIAS_SGI = 0x80D7
    const val GL_COLOR_TABLE_FORMAT_SGI = 0x80D8
    const val GL_COLOR_TABLE_WIDTH_SGI = 0x80D9
    const val GL_COLOR_TABLE_RED_SIZE_SGI = 0x80DA
    const val GL_COLOR_TABLE_GREEN_SIZE_SGI = 0x80DB
    const val GL_COLOR_TABLE_BLUE_SIZE_SGI = 0x80DC
    const val GL_COLOR_TABLE_ALPHA_SIZE_SGI = 0x80DD
    const val GL_COLOR_TABLE_LUMINANCE_SIZE_SGI = 0x80DE
    const val GL_COLOR_TABLE_INTENSITY_SIZE_SGI = 0x80DF
    const val GL_TEXTURE_COLOR_TABLE_SGI = 0x80BC
    const val GL_PROXY_TEXTURE_COLOR_TABLE_SGI = 0x80BD
    const val GL_UNPACK_CONSTANT_DATA_SUNX = 0x81D5
    const val GL_TEXTURE_CONSTANT_DATA_SUNX = 0x81D6
    const val GL_WRAP_BORDER_SUN = 0x81D4
    const val GL_GLOBAL_ALPHA_SUN = 0x81D9
    const val GL_GLOBAL_ALPHA_FACTOR_SUN = 0x81DA
    const val GL_QUAD_MESH_SUN = 0x8614
    const val GL_TRIANGLE_MESH_SUN = 0x8615
    const val GL_SLICE_ACCUM_SUN = 0x85CC
    const val GL_RESTART_SUN = 0x0001
    const val GL_REPLACE_MIDDLE_SUN = 0x0002
    const val GL_REPLACE_OLDEST_SUN = 0x0003
    const val GL_TRIANGLE_LIST_SUN = 0x81D7
    const val GL_REPLACEMENT_CODE_SUN = 0x81D8
    const val GL_REPLACEMENT_CODE_ARRAY_SUN = 0x85C0
    const val GL_REPLACEMENT_CODE_ARRAY_TYPE_SUN = 0x85C1
    const val GL_REPLACEMENT_CODE_ARRAY_STRIDE_SUN = 0x85C2
    const val GL_REPLACEMENT_CODE_ARRAY_POINTER_SUN = 0x85C3
    const val GL_R1UI_V3F_SUN = 0x85C4
    const val GL_R1UI_C4UB_V3F_SUN = 0x85C5
    const val GL_R1UI_C3F_V3F_SUN = 0x85C6
    const val GL_R1UI_N3F_V3F_SUN = 0x85C7
    const val GL_R1UI_C4F_N3F_V3F_SUN = 0x85C8
    const val GL_R1UI_T2F_V3F_SUN = 0x85C9
    const val GL_R1UI_T2F_N3F_V3F_SUN = 0x85CA
    const val GL_R1UI_T2F_C4F_N3F_V3F_SUN = 0x85CB
    const val GL_PHONG_WIN = 0x80EA
    const val GL_PHONG_HINT_WIN = 0x80EB
    const val GL_FOG_SPECULAR_TEXTURE_WIN = 0x80EC
}

val glContextAccess = accessor<_, GLContext.Static>()

@Named("org/lwjgl/opengl/GLContext", "org/lwjgl/opengl/GL")
interface GLContext {
    interface Static : StaticAccessor<GLContext> {
        val capabilities: GLCapabilities
    }

    companion object : Static by glContextAccess.static()
}

val glCapabilitiesAccess = accessor<_, GLCapabilities.Static>()

@Named("org/lwjgl/opengl/GLCapabilities", "org/lwjgl/opengl/ContextCapabilities")
interface GLCapabilities {
    val GL_AMD_blend_minmax_factor: Boolean
    val GL_AMD_conservative_depth: Boolean
    val GL_AMD_debug_output: Boolean
    val GL_AMD_depth_clamp_separate: Boolean
    val GL_AMD_draw_buffers_blend: Boolean
    val GL_AMD_interleaved_elements: Boolean
    val GL_AMD_multi_draw_indirect: Boolean
    val GL_AMD_name_gen_delete: Boolean
    val GL_AMD_performance_monitor: Boolean
    val GL_AMD_pinned_memory: Boolean
    val GL_AMD_query_buffer_object: Boolean
    val GL_AMD_sample_positions: Boolean
    val GL_AMD_seamless_cubemap_per_texture: Boolean
    val GL_AMD_shader_atomic_counter_ops: Boolean
    val GL_AMD_shader_stencil_export: Boolean
    val GL_AMD_shader_trinary_minmax: Boolean
    val GL_AMD_sparse_texture: Boolean
    val GL_AMD_stencil_operation_extended: Boolean
    val GL_AMD_texture_texture4: Boolean
    val GL_AMD_transform_feedback3_lines_triangles: Boolean
    val GL_AMD_vertex_shader_layer: Boolean
    val GL_AMD_vertex_shader_tessellator: Boolean
    val GL_AMD_vertex_shader_viewport_index: Boolean
    val GL_APPLE_aux_depth_stencil: Boolean
    val GL_APPLE_client_storage: Boolean
    val GL_APPLE_element_array: Boolean
    val GL_APPLE_fence: Boolean
    val GL_APPLE_float_pixels: Boolean
    val GL_APPLE_flush_buffer_range: Boolean
    val GL_APPLE_object_purgeable: Boolean
    val GL_APPLE_packed_pixels: Boolean
    val GL_APPLE_rgb_422: Boolean
    val GL_APPLE_row_bytes: Boolean
    val GL_APPLE_texture_range: Boolean
    val GL_APPLE_vertex_array_object: Boolean
    val GL_APPLE_vertex_array_range: Boolean
    val GL_APPLE_vertex_program_evaluators: Boolean
    val GL_APPLE_ycbcr_422: Boolean
    val GL_ARB_arrays_of_arrays: Boolean
    val GL_ARB_base_instance: Boolean
    val GL_ARB_bindless_texture: Boolean
    val GL_ARB_blend_func_extended: Boolean
    val GL_ARB_buffer_storage: Boolean
    val GL_ARB_cl_event: Boolean
    val GL_ARB_clear_buffer_object: Boolean
    val GL_ARB_clear_texture: Boolean
    val GL_ARB_clip_control: Boolean
    val GL_ARB_color_buffer_float: Boolean
    val GL_ARB_compatibility: Boolean
    val GL_ARB_compressed_texture_pixel_storage: Boolean
    val GL_ARB_compute_shader: Boolean
    val GL_ARB_compute_variable_group_size: Boolean
    val GL_ARB_conditional_render_inverted: Boolean
    val GL_ARB_conservative_depth: Boolean
    val GL_ARB_copy_buffer: Boolean
    val GL_ARB_copy_image: Boolean
    val GL_ARB_cull_distance: Boolean
    val GL_ARB_debug_output: Boolean
    val GL_ARB_depth_buffer_float: Boolean
    val GL_ARB_depth_clamp: Boolean
    val GL_ARB_depth_texture: Boolean
    val GL_ARB_derivative_control: Boolean
    val GL_ARB_direct_state_access: Boolean
    val GL_ARB_draw_buffers: Boolean
    val GL_ARB_draw_buffers_blend: Boolean
    val GL_ARB_draw_elements_base_vertex: Boolean
    val GL_ARB_draw_indirect: Boolean
    val GL_ARB_draw_instanced: Boolean
    val GL_ARB_enhanced_layouts: Boolean
    val GL_ARB_ES2_compatibility: Boolean
    val GL_ARB_ES3_1_compatibility: Boolean
    val GL_ARB_ES3_compatibility: Boolean
    val GL_ARB_explicit_attrib_location: Boolean
    val GL_ARB_explicit_uniform_location: Boolean
    val GL_ARB_fragment_coord_conventions: Boolean
    val GL_ARB_fragment_layer_viewport: Boolean
    val GL_ARB_fragment_program: Boolean
    val GL_ARB_fragment_program_shadow: Boolean
    val GL_ARB_fragment_shader: Boolean
    val GL_ARB_framebuffer_no_attachments: Boolean
    val GL_ARB_framebuffer_object: Boolean
    val GL_ARB_framebuffer_sRGB: Boolean
    val GL_ARB_geometry_shader4: Boolean
    val GL_ARB_get_program_binary: Boolean
    val GL_ARB_get_texture_sub_image: Boolean
    val GL_ARB_gpu_shader_fp64: Boolean
    val GL_ARB_gpu_shader5: Boolean
    val GL_ARB_half_float_pixel: Boolean
    val GL_ARB_half_float_vertex: Boolean
    val GL_ARB_imaging: Boolean
    val GL_ARB_indirect_parameters: Boolean
    val GL_ARB_instanced_arrays: Boolean
    val GL_ARB_internalformat_query: Boolean
    val GL_ARB_internalformat_query2: Boolean
    val GL_ARB_invalidate_subdata: Boolean
    val GL_ARB_map_buffer_alignment: Boolean
    val GL_ARB_map_buffer_range: Boolean
    val GL_ARB_matrix_palette: Boolean
    val GL_ARB_multi_bind: Boolean
    val GL_ARB_multi_draw_indirect: Boolean
    val GL_ARB_multisample: Boolean
    val GL_ARB_multitexture: Boolean
    val GL_ARB_occlusion_query: Boolean
    val GL_ARB_occlusion_query2: Boolean
    val GL_ARB_pipeline_statistics_query: Boolean
    val GL_ARB_pixel_buffer_object: Boolean
    val GL_ARB_point_parameters: Boolean
    val GL_ARB_point_sprite: Boolean
    val GL_ARB_program_interface_query: Boolean
    val GL_ARB_provoking_vertex: Boolean
    val GL_ARB_query_buffer_object: Boolean
    val GL_ARB_robust_buffer_access_behavior: Boolean
    val GL_ARB_robustness: Boolean
    val GL_ARB_robustness_isolation: Boolean
    val GL_ARB_sample_shading: Boolean
    val GL_ARB_sampler_objects: Boolean
    val GL_ARB_seamless_cube_map: Boolean
    val GL_ARB_seamless_cubemap_per_texture: Boolean
    val GL_ARB_separate_shader_objects: Boolean
    val GL_ARB_shader_atomic_counters: Boolean
    val GL_ARB_shader_bit_encoding: Boolean
    val GL_ARB_shader_draw_parameters: Boolean
    val GL_ARB_shader_group_vote: Boolean
    val GL_ARB_shader_image_load_store: Boolean
    val GL_ARB_shader_image_size: Boolean
    val GL_ARB_shader_objects: Boolean
    val GL_ARB_shader_precision: Boolean
    val GL_ARB_shader_stencil_export: Boolean
    val GL_ARB_shader_storage_buffer_object: Boolean
    val GL_ARB_shader_subroutine: Boolean
    val GL_ARB_shader_texture_image_samples: Boolean
    val GL_ARB_shader_texture_lod: Boolean
    val GL_ARB_shading_language_100: Boolean
    val GL_ARB_shading_language_420pack: Boolean
    val GL_ARB_shading_language_include: Boolean
    val GL_ARB_shading_language_packing: Boolean
    val GL_ARB_shadow: Boolean
    val GL_ARB_shadow_ambient: Boolean
    val GL_ARB_sparse_buffer: Boolean
    val GL_ARB_sparse_texture: Boolean
    val GL_ARB_stencil_texturing: Boolean
    val GL_ARB_sync: Boolean
    val GL_ARB_tessellation_shader: Boolean
    val GL_ARB_texture_barrier: Boolean
    val GL_ARB_texture_border_clamp: Boolean
    val GL_ARB_texture_buffer_object: Boolean
    val GL_ARB_texture_buffer_object_rgb32: Boolean
    val GL_ARB_texture_buffer_range: Boolean
    val GL_ARB_texture_compression: Boolean
    val GL_ARB_texture_compression_bptc: Boolean
    val GL_ARB_texture_compression_rgtc: Boolean
    val GL_ARB_texture_cube_map: Boolean
    val GL_ARB_texture_cube_map_array: Boolean
    val GL_ARB_texture_env_add: Boolean
    val GL_ARB_texture_env_combine: Boolean
    val GL_ARB_texture_env_crossbar: Boolean
    val GL_ARB_texture_env_dot3: Boolean
    val GL_ARB_texture_float: Boolean
    val GL_ARB_texture_gather: Boolean
    val GL_ARB_texture_mirror_clamp_to_edge: Boolean
    val GL_ARB_texture_mirrored_repeat: Boolean
    val GL_ARB_texture_multisample: Boolean
    val GL_ARB_texture_non_power_of_two: Boolean
    val GL_ARB_texture_query_levels: Boolean
    val GL_ARB_texture_query_lod: Boolean
    val GL_ARB_texture_rectangle: Boolean
    val GL_ARB_texture_rg: Boolean
    val GL_ARB_texture_rgb10_a2ui: Boolean
    val GL_ARB_texture_stencil8: Boolean
    val GL_ARB_texture_storage: Boolean
    val GL_ARB_texture_storage_multisample: Boolean
    val GL_ARB_texture_swizzle: Boolean
    val GL_ARB_texture_view: Boolean
    val GL_ARB_timer_query: Boolean
    val GL_ARB_transform_feedback_instanced: Boolean
    val GL_ARB_transform_feedback_overflow_query: Boolean
    val GL_ARB_transform_feedback2: Boolean
    val GL_ARB_transform_feedback3: Boolean
    val GL_ARB_transpose_matrix: Boolean
    val GL_ARB_uniform_buffer_object: Boolean
    val GL_ARB_vertex_array_bgra: Boolean
    val GL_ARB_vertex_array_object: Boolean
    val GL_ARB_vertex_attrib_64bit: Boolean
    val GL_ARB_vertex_attrib_binding: Boolean
    val GL_ARB_vertex_blend: Boolean
    val GL_ARB_vertex_buffer_object: Boolean
    val GL_ARB_vertex_program: Boolean
    val GL_ARB_vertex_shader: Boolean
    val GL_ARB_vertex_type_10f_11f_11f_rev: Boolean
    val GL_ARB_vertex_type_2_10_10_10_rev: Boolean
    val GL_ARB_viewport_array: Boolean
    val GL_ARB_window_pos: Boolean
    val GL_ATI_draw_buffers: Boolean
    val GL_ATI_element_array: Boolean
    val GL_ATI_envmap_bumpmap: Boolean
    val GL_ATI_fragment_shader: Boolean
    val GL_ATI_map_object_buffer: Boolean
    val GL_ATI_meminfo: Boolean
    val GL_ATI_pn_triangles: Boolean
    val GL_ATI_separate_stencil: Boolean
    val GL_ATI_shader_texture_lod: Boolean
    val GL_ATI_text_fragment_shader: Boolean
    val GL_ATI_texture_compression_3dc: Boolean
    val GL_ATI_texture_env_combine3: Boolean
    val GL_ATI_texture_float: Boolean
    val GL_ATI_texture_mirror_once: Boolean
    val GL_ATI_vertex_array_object: Boolean
    val GL_ATI_vertex_attrib_array_object: Boolean
    val GL_ATI_vertex_streams: Boolean
    val GL_EXT_abgr: Boolean
    val GL_EXT_bgra: Boolean
    val GL_EXT_bindable_uniform: Boolean
    val GL_EXT_blend_color: Boolean
    val GL_EXT_blend_equation_separate: Boolean
    val GL_EXT_blend_func_separate: Boolean
    val GL_EXT_blend_minmax: Boolean
    val GL_EXT_blend_subtract: Boolean
    val GL_EXT_Cg_shader: Boolean
    val GL_EXT_compiled_vertex_array: Boolean
    val GL_EXT_depth_bounds_test: Boolean
    val GL_EXT_direct_state_access: Boolean
    val GL_EXT_draw_buffers2: Boolean
    val GL_EXT_draw_instanced: Boolean
    val GL_EXT_draw_range_elements: Boolean
    val GL_EXT_fog_coord: Boolean
    val GL_EXT_framebuffer_blit: Boolean
    val GL_EXT_framebuffer_multisample: Boolean
    val GL_EXT_framebuffer_multisample_blit_scaled: Boolean
    val GL_EXT_framebuffer_object: Boolean
    val GL_EXT_framebuffer_sRGB: Boolean
    val GL_EXT_geometry_shader4: Boolean
    val GL_EXT_gpu_program_parameters: Boolean
    val GL_EXT_gpu_shader4: Boolean
    val GL_EXT_multi_draw_arrays: Boolean
    val GL_EXT_packed_depth_stencil: Boolean
    val GL_EXT_packed_float: Boolean
    val GL_EXT_packed_pixels: Boolean
    val GL_EXT_paletted_texture: Boolean
    val GL_EXT_pixel_buffer_object: Boolean
    val GL_EXT_point_parameters: Boolean
    val GL_EXT_provoking_vertex: Boolean
    val GL_EXT_rescale_normal: Boolean
    val GL_EXT_secondary_color: Boolean
    val GL_EXT_separate_shader_objects: Boolean
    val GL_EXT_separate_specular_color: Boolean
    val GL_EXT_shader_image_load_store: Boolean
    val GL_EXT_shadow_funcs: Boolean
    val GL_EXT_shared_texture_palette: Boolean
    val GL_EXT_stencil_clear_tag: Boolean
    val GL_EXT_stencil_two_side: Boolean
    val GL_EXT_stencil_wrap: Boolean
    val GL_EXT_texture_3d: Boolean
    val GL_EXT_texture_array: Boolean
    val GL_EXT_texture_buffer_object: Boolean
    val GL_EXT_texture_compression_latc: Boolean
    val GL_EXT_texture_compression_rgtc: Boolean
    val GL_EXT_texture_compression_s3tc: Boolean
    val GL_EXT_texture_env_combine: Boolean
    val GL_EXT_texture_env_dot3: Boolean
    val GL_EXT_texture_filter_anisotropic: Boolean
    val GL_EXT_texture_integer: Boolean
    val GL_EXT_texture_lod_bias: Boolean
    val GL_EXT_texture_mirror_clamp: Boolean
    val GL_EXT_texture_rectangle: Boolean
    val GL_EXT_texture_shared_exponent: Boolean
    val GL_EXT_texture_snorm: Boolean
    val GL_EXT_texture_sRGB: Boolean
    val GL_EXT_texture_sRGB_decode: Boolean
    val GL_EXT_texture_swizzle: Boolean
    val GL_EXT_timer_query: Boolean
    val GL_EXT_transform_feedback: Boolean
    val GL_EXT_vertex_array_bgra: Boolean
    val GL_EXT_vertex_attrib_64bit: Boolean
    val GL_EXT_vertex_shader: Boolean
    val GL_EXT_vertex_weighting: Boolean
    val GL_GREMEDY_frame_terminator: Boolean
    val GL_GREMEDY_string_marker: Boolean
    val GL_HP_occlusion_test: Boolean
    val GL_IBM_rasterpos_clip: Boolean
    val GL_INTEL_map_texture: Boolean
    val GL_KHR_context_flush_control: Boolean
    val GL_KHR_debug: Boolean
    val GL_KHR_robust_buffer_access_behavior: Boolean
    val GL_KHR_robustness: Boolean
    val GL_KHR_texture_compression_astc_ldr: Boolean
    val GL_NV_bindless_multi_draw_indirect: Boolean
    val GL_NV_bindless_texture: Boolean
    val GL_NV_blend_equation_advanced: Boolean
    val GL_NV_blend_square: Boolean
    val GL_NV_compute_program5: Boolean
    val GL_NV_conditional_render: Boolean
    val GL_NV_copy_depth_to_color: Boolean
    val GL_NV_copy_image: Boolean
    val GL_NV_deep_texture3D: Boolean
    val GL_NV_depth_buffer_float: Boolean
    val GL_NV_depth_clamp: Boolean
    val GL_NV_draw_texture: Boolean
    val GL_NV_evaluators: Boolean
    val GL_NV_explicit_multisample: Boolean
    val GL_NV_fence: Boolean
    val GL_NV_float_buffer: Boolean
    val GL_NV_fog_distance: Boolean
    val GL_NV_fragment_program: Boolean
    val GL_NV_fragment_program_option: Boolean
    val GL_NV_fragment_program2: Boolean
    val GL_NV_fragment_program4: Boolean
    val GL_NV_framebuffer_multisample_coverage: Boolean
    val GL_NV_geometry_program4: Boolean
    val GL_NV_geometry_shader4: Boolean
    val GL_NV_gpu_program4: Boolean
    val GL_NV_gpu_program5: Boolean
    val GL_NV_gpu_program5_mem_extended: Boolean
    val GL_NV_gpu_shader5: Boolean
    val GL_NV_half_float: Boolean
    val GL_NV_light_max_exponent: Boolean
    val GL_NV_multisample_coverage: Boolean
    val GL_NV_multisample_filter_hint: Boolean
    val GL_NV_occlusion_query: Boolean
    val GL_NV_packed_depth_stencil: Boolean
    val GL_NV_parameter_buffer_object: Boolean
    val GL_NV_parameter_buffer_object2: Boolean
    val GL_NV_path_rendering: Boolean
    val GL_NV_pixel_data_range: Boolean
    val GL_NV_point_sprite: Boolean
    val GL_NV_present_video: Boolean
    val GL_NV_primitive_restart: Boolean
    val GL_NV_register_combiners: Boolean
    val GL_NV_register_combiners2: Boolean
    val GL_NV_shader_atomic_counters: Boolean
    val GL_NV_shader_atomic_float: Boolean
    val GL_NV_shader_buffer_load: Boolean
    val GL_NV_shader_buffer_store: Boolean
    val GL_NV_shader_storage_buffer_object: Boolean
    val GL_NV_tessellation_program5: Boolean
    val GL_NV_texgen_reflection: Boolean
    val GL_NV_texture_barrier: Boolean
    val GL_NV_texture_compression_vtc: Boolean
    val GL_NV_texture_env_combine4: Boolean
    val GL_NV_texture_expand_normal: Boolean
    val GL_NV_texture_multisample: Boolean
    val GL_NV_texture_rectangle: Boolean
    val GL_NV_texture_shader: Boolean
    val GL_NV_texture_shader2: Boolean
    val GL_NV_texture_shader3: Boolean
    val GL_NV_transform_feedback: Boolean
    val GL_NV_transform_feedback2: Boolean
    val GL_NV_vertex_array_range: Boolean
    val GL_NV_vertex_array_range2: Boolean
    val GL_NV_vertex_attrib_integer_64bit: Boolean
    val GL_NV_vertex_buffer_unified_memory: Boolean
    val GL_NV_vertex_program: Boolean
    val GL_NV_vertex_program1_1: Boolean
    val GL_NV_vertex_program2: Boolean
    val GL_NV_vertex_program2_option: Boolean
    val GL_NV_vertex_program3: Boolean
    val GL_NV_vertex_program4: Boolean
    val GL_NV_video_capture: Boolean
    val GL_NVX_gpu_memory_info: Boolean
    val GL_SGIS_generate_mipmap: Boolean
    val GL_SGIS_texture_lod: Boolean
    val GL_SUN_slice_accum: Boolean
    val OpenGL11: Boolean
    val OpenGL12: Boolean
    val OpenGL13: Boolean
    val OpenGL14: Boolean
    val OpenGL15: Boolean
    val OpenGL20: Boolean
    val OpenGL21: Boolean
    val OpenGL30: Boolean
    val OpenGL31: Boolean
    val OpenGL32: Boolean
    val OpenGL33: Boolean
    val OpenGL40: Boolean
    val OpenGL41: Boolean
    val OpenGL42: Boolean
    val OpenGL43: Boolean
    val OpenGL44: Boolean
    val OpenGL45: Boolean

    interface Static : StaticAccessor<GLCapabilities>
    companion object : Static by glCapabilitiesAccess.static()
}