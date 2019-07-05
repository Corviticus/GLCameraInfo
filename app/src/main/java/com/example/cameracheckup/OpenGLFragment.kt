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
