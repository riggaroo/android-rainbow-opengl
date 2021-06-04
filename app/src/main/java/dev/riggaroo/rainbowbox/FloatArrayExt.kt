package dev.riggaroo.rainbowbox

import android.opengl.Matrix

fun FloatArray.setIdentityM(): FloatArray {
    Matrix.setIdentityM(this, 0)
    return this
}

fun FloatArray.translateM(x: Float, y: Float, z: Float = 0.0f): FloatArray {
    Matrix.translateM(this, 0, x, y, z)
    return this
}

fun FloatArray.scaleM(x: Float, y: Float, z: Float = 1.0f): FloatArray {
    Matrix.scaleM(this, 0, x, y, z)
    return this
}