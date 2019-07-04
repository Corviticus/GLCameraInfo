package com.example.cameracheckup

import android.opengl.*
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import timber.log.Timber
import android.opengl.GLSurfaceView
import kotlinx.android.synthetic.main.open_gl_fragment_layout.*
import android.opengl.GLES30
import android.opengl.Matrix.multiplyMM
import android.opengl.Matrix.setRotateM
import android.opengl.Matrix.setLookAtM
import android.opengl.Matrix.frustumM
import android.os.SystemClock
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class OpenGLFragment : Fragment() {

    private lateinit var viewModel: OpenGlViewModel

    private var mDisplay: EGLDisplay? = null
    private var mEGLExtensions: String = ""

    private var mGLSurfaceView: GLSurfaceView? = null
    private var mGLRenderer: GLRenderer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.open_gl_fragment_layout, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(OpenGlViewModel::class.java)

        mGLRenderer = GLRenderer()

        mGLSurfaceView = gl_surface_view
        mGLSurfaceView?.setEGLContextClientVersion(3) // do this BEFORE setRenderer()
        mGLSurfaceView?.setRenderer(mGLRenderer)

        mGLSurfaceView?.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        mDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        mDisplay?.let { mEGLExtensions = EGL14.eglQueryString(it, EGL14.EGL_EXTENSIONS) }
        Timber.d("OpenGL ES version: $mEGLExtensions")

        val version = GLES30.glGetString(GLES30.GL_VERSION)
        if (version != null) {
            Timber.d("OpenGL ES version: $version")
        }
    }

    override fun onResume() {
        super.onResume()
        mGLSurfaceView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mGLSurfaceView?.onPause()
    }

}

/**
 *
 *
 */
class GLRenderer: GLSurfaceView.Renderer {

    private var mSquare: Square? = null
    private var mTriangle: Triangle? = null

    // the "Model View Projection Matrix"
    private val mMVPMatrix = FloatArray(16)
    private val mProjectionMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val mRotationMatrix = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

        // Set the background frame color
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        mTriangle = Triangle()
        mSquare = Square()
    }

    override fun onSurfaceChanged(gl: GL10, w: Int, h: Int) {

        // Adjust the viewport based on geometry changes such as screen rotation
        GLES30.glViewport(0, 0, w, h)

        // this projection matrix is applied to object coordinates in the onDrawFrame() method
        val ratio = w.toFloat() / h
        frustumM(mProjectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }

    override fun onDrawFrame(gl: GL10) {

        val scratch = FloatArray(16)

        // Draw background color
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        // Set the camera position (View matrix)
        setLookAtM(mViewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        // Calculate the projection and view transformation
        multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)

        // Draw square
        mSquare?.draw(mMVPMatrix)

        // Create a constant rotation for the triangle
         val time = SystemClock.uptimeMillis() % 4000L
         val angle = 0.090f * (time)
        setRotateM(mRotationMatrix, 0, angle, 0f, 0f, 1.0f)

        // Combine the rotation matrix with the projection and camera view
        // NOTE: the mMVPMatrix factor *must be first* in order for the matrix multiplication product to be correct
        multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0)

        // Draw triangle
        mTriangle?.draw(scratch)
    }

    /**
     * Utility method for compiling a OpenGL shader.
     *
     * **Note:** When developing shaders, use the checkGlError()
     * method to debug shader coding errors.
     *
     * @param type - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    fun loadShader(type: Int, shaderCode: String): Int {

        // create a vertex shader type (GLES30.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES30.GL_FRAGMENT_SHADER)
        val shader = GLES30.glCreateShader(type)

        // add the source code to the shader and compile it
        GLES30.glShaderSource(shader, shaderCode)
        GLES30.glCompileShader(shader)

        return shader
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES30.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    fun checkGlError(glOperation: String) {
        while (GLES30.glGetError() != GLES30.GL_NO_ERROR) {
            Timber.e( "$glOperation: glError ${GLES30.glGetError()}")
            throw RuntimeException("$glOperation: glError ${GLES30.glGetError()}")
        }
    }

}