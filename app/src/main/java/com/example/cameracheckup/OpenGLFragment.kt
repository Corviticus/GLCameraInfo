package com.example.cameracheckup

import android.content.Context
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
import java.io.BufferedReader
import java.io.IOException
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

        // create GL renderer passing in the app context so it can access asset files (shader glsl files)
        mGLRenderer = context?.let { GLRenderer(it) }

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
class GLRenderer(private val ctx: Context) : GLSurfaceView.Renderer {

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

        mTriangle = Triangle(this)
        mSquare = Square(this)
    }

    override fun onSurfaceChanged(gl: GL10, w: Int, h: Int) {

        // Adjust the viewport based on geometry changes such as screen rotation
        GLES30.glViewport(0, 0, w, h)

        // this projection matrix is applied to object coordinates in the onDrawFrame() method
        val ratio = w.toFloat() / h
        frustumM(mProjectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }

    override fun onDrawFrame(gl: GL10) {

        val temp = FloatArray(16)

        // Draw background color
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        // Set the camera position (View matrix)
        setLookAtM(mViewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        // Calculate the projection and view transformation
        multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)

        // Draw square
        mSquare?.draw(mMVPMatrix)

        // Create a constant rotation for the triangle
        val time = SystemClock.uptimeMillis()
        val angle = 0.090f * (time)
        setRotateM(mRotationMatrix, 0, angle*.3.toFloat(), 0f, 0f, 1.0f)

        // Combine the rotation matrix with the projection and camera view
        // NOTE: the mMVPMatrix factor MUST BE FIRST in order for the matrix multiplication product to be correct
        multiplyMM(temp, 0, mMVPMatrix, 0, mRotationMatrix, 0)

        // Draw triangle
        mTriangle?.draw(temp)
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
            Timber.e("$glOperation: glError ${GLES30.glGetError()}")
            throw RuntimeException("$glOperation: glError ${GLES30.glGetError()}")
        }
    }

    /**
     * Creates a new program from the supplied vertex and fragment shaders
     * @param vertexAssetFile Asset file containing the vertex shader code
     * @param fragmentAssetFile Asset file containing the fragment shader code
     * @return A handle to the program, or 0 on failure
     */
    fun createProgram(vertexAssetFile: String, fragmentAssetFile: String): Int {

        val vertexSource = getStringFromFileInAssets(vertexAssetFile)
        val vertexShader = compileShader(GLES30.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) { return 0 }

        val fragmentSource = getStringFromFileInAssets(fragmentAssetFile)
        val fragmentShader = compileShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource)
        if (fragmentShader == 0) { return 0 }

        var program = GLES30.glCreateProgram()
        checkGLError("glCreateProgram")
        if (program == 0) {
            Timber.e("Could not create program from %s, %s", fragmentAssetFile, vertexAssetFile)
        } else {

            GLES30.glAttachShader(program, vertexShader)
            checkGLError("glAttachShader")
            GLES30.glAttachShader(program, fragmentShader)
            checkGLError("glAttachShader")
            GLES30.glLinkProgram(program)

            val linkStatus = IntArray(1)
            GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GLES30.GL_TRUE) {
                Timber.e("Could not link program: %s", GLES30.glGetProgramInfoLog(program))
                GLES30.glDeleteProgram(program)
                program = 0
            }
        }

        return program
    }


    /**
     * Compiles the provided shader shaderSource
     * @return A handle to the shader, or 0 on failure
     */
    private fun compileShader(shaderType: Int, shaderSource: String): Int {

        // create shader and check for errors
        val shader = GLES30.glCreateShader(shaderType)
        checkGLError("glCreateShader type=$shaderType")

        // compile the shader
        GLES30.glShaderSource(shader, shaderSource)
        GLES30.glCompileShader(shader)

        val compiled = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Timber.e("Could not compile shader %d", shaderType)
            Timber.e(GLES30.glGetShaderInfoLog(shader))
            GLES30.glDeleteShader(shader)
            return 0
        }

        return shader
    }

    /**
     * Convert an asset file into a [String]
     * @param filename The asset file name as a [String]
     * @return A [String] built from the shader file found in the assets folder
     */
    private fun getStringFromFileInAssets(filename: String): String {
        try {
            val glAssets = ctx.assets
            val inputStream = glAssets.open(filename)
            val line = inputStream.bufferedReader().use(BufferedReader::readText)
            inputStream.close()

            return line
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    /**
     * Check to see if a GLES error has been raised
     * @param op A [String] representing the OpenGL operation
     */
    private fun checkGLError(op: String) {
        val error = GLES30.glGetError()
        if (error != GLES30.GL_NO_ERROR) {
            val msg = op + ": glError 0x" + Integer.toHexString(error)
            Timber.e(msg)
            throw RuntimeException(msg) // TODO - do something more graceful...
        }
    }
}