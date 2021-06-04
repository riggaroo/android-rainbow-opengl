package dev.riggaroo.rainbowbox

import android.opengl.GLES20
import android.os.SystemClock

class ShaderProgram {

    private var shaderProgram: Int = 0

    private var dashCountUniformLocation = -1
    private var timeOffsetUniformLocation = -1
    private var aspectRatioUniformLocation = -1
    private var strokeWidthUniformLocation = -1
    private var viewProjUniformLocation = -1
    private var modelMatrixUniformLocation = -1

    fun initialize() {
        compileAndLinkShaders()
    }

    /**
     * `bindUniforms` is setting variables on the shader program.
     * A uniform is a "constant" in the Shaders that doesn't change for each pixel,
     * but we still need to set the constants in order for it to function.
     */
    fun bindUniforms(aspectRatio: Float, layerModelMatrix: FloatArray, viewProjMatrix: FloatArray) {
        val perimeter = 50.0f
        val scale = 20.0f
        val dashCount = perimeter / DASH_LENGTH * scale
        GLES20.glUniformMatrix4fv(modelMatrixUniformLocation, 1, false, layerModelMatrix, 0)
        GLES20.glUniformMatrix4fv(viewProjUniformLocation, 1, false, viewProjMatrix, 0)
        GLES20.glUniform1f(strokeWidthUniformLocation, 0.02f)
        GLES20.glUniform1f(aspectRatioUniformLocation, aspectRatio)
        GLES20.glUniform1f(dashCountUniformLocation, dashCount)
        GLES20.glUniform1f(timeOffsetUniformLocation, timeOffset(2f, 25.0f))
    }

    fun bind() {
        GLES20.glUseProgram(shaderProgram)
    }

    fun unbind() {
        GLES20.glUseProgram(0)
    }

    private fun compileShader(shaderType: Int, shaderSource: String): Int {
        val shaderId = GLES20.glCreateShader(shaderType)
        GLES20.glShaderSource(shaderId, shaderSource)
        GLES20.glCompileShader(shaderId)

        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, compileStatus, 0)

        if (compileStatus[0] == 0) {
            val log = GLES20.glGetShaderInfoLog(shaderId)
            GLES20.glDeleteShader(shaderId)
            throw RuntimeException("Failed to compile vertex shader: \n$log")
        }
        return shaderId
    }

    private fun compileAndLinkShaders() {
        // compile both the vertex and fragment shaders
        val vertexShaderId = compileShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
        val fragmentShaderId = compileShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)

        // attach the shaders to the GL Program
        shaderProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(shaderProgram, vertexShaderId)
        GLES20.glAttachShader(shaderProgram, fragmentShaderId)

        GLES20.glLinkProgram(shaderProgram)

        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(shaderProgram, GLES20.GL_LINK_STATUS, linkStatus, 0)

        if (linkStatus[0] == 0) {
            val log = GLES20.glGetProgramInfoLog(shaderProgram)
            GLES20.glDeleteProgram(shaderProgram)
            throw RuntimeException("Failed to link shader program: \n$log")
        }

        // We need to find the location of the uniforms(variables) that we want to substitute into our program
        // These locations will be used in `bindUniforms` function to substitute our variables into the shader.
        dashCountUniformLocation = GLES20.glGetUniformLocation(shaderProgram, "uDashCount")
        modelMatrixUniformLocation = GLES20.glGetUniformLocation(shaderProgram, "uModelMatrix")
        viewProjUniformLocation = GLES20.glGetUniformLocation(shaderProgram, "uViewProjMatrix")
        strokeWidthUniformLocation = GLES20.glGetUniformLocation(shaderProgram, "uStrokeWidth")
        timeOffsetUniformLocation = GLES20.glGetUniformLocation(shaderProgram, "uTimeOffset")
        aspectRatioUniformLocation = GLES20.glGetUniformLocation(shaderProgram, "uAspectRatio")
    }

    // Helper function for the animation
    private fun timeOffset(dashCount: Float, scale: Float): Float {
        // Why 800? It looks good.
        return (SystemClock.uptimeMillis() % (800 * dashCount * scale)) / (800.0f * dashCount * scale)
    }

    companion object {
        private const val DASH_LENGTH = 2.0f
    }
}