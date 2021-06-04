package dev.riggaroo.rainbowbox

import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceView

class MainActivity : AppCompatActivity() {

    private var surfaceView: SurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val surfaceView = findViewById<GLSurfaceView>(R.id.surface)

        surfaceView.setEGLContextClientVersion(3)
        surfaceView.setRenderer(RainbowBoxRenderer())
        this.surfaceView = surfaceView
    }

    override fun onPause() {
        super.onPause()
        (surfaceView as GLSurfaceView).onPause()
    }

    override fun onResume() {
        super.onResume()
        (surfaceView as GLSurfaceView).onResume()
    }
}