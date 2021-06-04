package dev.riggaroo.rainbowbox

import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Size
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class RainbowBoxRenderer : GLSurfaceView.Renderer {

    // Helper class that wraps the GLSL shaders, binds them etc
    private var shaderProgram: ShaderProgram = ShaderProgram()

    // Helper class containing the vertex construction
    // and binding code to use it with GL.
    private val rectOutlineVao = RectOutlineVao()

    // Matrices
    private val layerModelMatrix = FloatArray(16)
    private val viewProjMatrix = FloatArray(16)

    private var aspectRatio = 1.0f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        shaderProgram.initialize()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // Set up matrices here
        glViewport(0, 0, width, height)
        aspectRatio = width.toFloat() / height
    }

    // The function to draw the contents, run very often, avoid allocations in this function.
    // Similar to onDraw(canvas : Canvas) for CustomViews.
    override fun onDrawFrame(gl: GL10?) {
        // set background to white
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT)

        setupMatrices()

        // bind geometry
        rectOutlineVao.bind()
        // bind shader
        shaderProgram.bind()
        shaderProgram.bindUniforms(aspectRatio, layerModelMatrix, viewProjMatrix)

        // draw box
        glDrawArrays(GL_TRIANGLE_STRIP, 0, rectOutlineVao.vertexCount())

        // unbind shader
        shaderProgram.unbind()
        // unbind geometry
        rectOutlineVao.unbind()
    }

    private fun setupMatrices() {
        Matrix.setIdentityM(layerModelMatrix, 0)

        modelMatrixForBoundingBox(
            Size(1, 1),
            layerModelMatrix
        )

        Matrix.setIdentityM(layerModelMatrix, 0)
        Matrix.scaleM(layerModelMatrix, 0, 0.5f, 0.5f, 1.0f)
        Matrix.setIdentityM(viewProjMatrix, 0)
        Matrix.scaleM(viewProjMatrix, 0, 1.0f, aspectRatio, 1.0f)
    }

    companion object {

        internal fun modelMatrixForBoundingBox(
            layerSize: Size,
            outputModelMatrix: FloatArray
        ) {
            val x = 0f
            val y = 0f
            val scaleX = layerSize.width / 2.0f
            val scaleY = layerSize.height / 2.0f
            outputModelMatrix.setIdentityM()
            outputModelMatrix.translateM(x, y)
            outputModelMatrix.scaleM((scaleX), (scaleY))
        }
    }
}