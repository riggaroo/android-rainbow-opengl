package dev.riggaroo.rainbowbox

import android.opengl.GLES20
import android.opengl.GLES20.glDisableVertexAttribArray
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glVertexAttribPointer
import android.opengl.GLES30.glBindVertexArray
import android.opengl.GLES30.glDeleteVertexArrays
import android.opengl.GLES30.glGenVertexArrays

private const val UNINITIALIZED = -1

/**
 * Vao = VertexArrayObject - OpenGL object that stores all of the state needed to supply vertex data.
 * https://www.khronos.org/opengl/wiki/Vertex_Specification
 */
class RectOutlineVao {
    private val _vao = IntArray(1)
    private val vao: Int
        get() = _vao[0]
    private val rectOutlineBuffer = RectOutlineBuffer()

    fun vertexCount() = rectOutlineBuffer.vertexCount

    init {
        _vao[0] = UNINITIALIZED
    }

    /**
     * Creates / initializes gl properties if not initialized yet
     */
    private fun ensureInitialized() {
        if (_vao[0] != UNINITIALIZED) {
            return
        }
        glGenVertexArrays(1, _vao, 0)
        glBindVertexArray(vao)
        rectOutlineBuffer.bind()
        val stride = 4 * rectOutlineBuffer.numberValuesPerVertex
        glVertexAttribPointer(0, 2, GLES20.GL_FLOAT, false, stride, 0)
        glVertexAttribPointer(1, 2, GLES20.GL_FLOAT, false, stride, 2 * 4)
        glVertexAttribPointer(2, 1, GLES20.GL_FLOAT, false, stride, 4 * 4)
        glEnableVertexAttribArray(0)
        glEnableVertexAttribArray(1)
        glEnableVertexAttribArray(2)
        glBindVertexArray(0)
    }

    fun bind() {
        ensureInitialized()
        glBindVertexArray(vao)
    }

    fun unbind() {
        glBindVertexArray(0)
        glDisableVertexAttribArray(0)
        glDisableVertexAttribArray(1)
        glDisableVertexAttribArray(2)
        rectOutlineBuffer.unbind()
    }

    fun delete() {
        glDeleteVertexArrays(1, _vao, 0)
        rectOutlineBuffer.delete()
        _vao[0] = UNINITIALIZED
    }
}