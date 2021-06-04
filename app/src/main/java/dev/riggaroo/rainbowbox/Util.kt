package dev.riggaroo.rainbowbox

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Util {
    companion object {
        const val FLOAT_SIZE = 4

        fun createFloatBuffer(values: FloatArray): FloatBuffer {
            val size = values.size * FLOAT_SIZE
            val buffer = ByteBuffer
                .allocateDirect(size)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
            buffer.put(values, 0, values.size)
            buffer.position(0)
            return buffer
        }
    }
}
