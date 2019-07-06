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
import androidx.appcompat.app.AppCompatActivity


class OpenGLFragment : Fragment() {

    private lateinit var viewModel: OpenGlViewModel

    private var mGLSurfaceView: GLSurfaceView? = null
    private var mGLRenderer: GLRenderer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.open_gl_fragment_layout, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // get instance of our view model
        viewModel = ViewModelProviders.of(this).get(OpenGlViewModel::class.java)

        // use custom layout for the toolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar_gl)
        val customActionBar = (activity as AppCompatActivity).supportActionBar
            customActionBar?.also {
                it.setDisplayShowHomeEnabled(false)
                it.setDisplayHomeAsUpEnabled(false)
                it.setDisplayShowCustomEnabled(true)
                it.setDisplayShowTitleEnabled(false)
                it.title = null
            }
        setHasOptionsMenu(true)

        // create GL renderer passing in the app context so it can access asset files (shader glsl files)
        mGLRenderer = context?.let { GLRenderer(it) }

        mGLSurfaceView = gl_surface_view
        mGLSurfaceView?.setEGLContextClientVersion(3) // do this BEFORE setRenderer()
        mGLSurfaceView?.setRenderer(mGLRenderer)

        mGLSurfaceView?.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        bindEGLVersion()
        bindEGLExtensions()

    }

    override fun onResume() {
        super.onResume()
        mGLSurfaceView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mGLSurfaceView?.onPause()
    }

    private fun bindEGLVersion() {

        //

        viewModel.glVersion = EGL14.eglQueryString(EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY), EGL14.EGL_VERSION)
        EGL14.eglQueryString(EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY), GLES30.GL_VERSION)
        Timber.d("EGL14 version: ${viewModel.glVersion}")
        gl_version_text_view.text = viewModel.glVersion ?: ""
    }

    private fun bindEGLExtensions() {

        // get the supported gl extensions and replace spaces with a line feed
        viewModel.glExtensions =
            EGL14.eglQueryString(EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY), EGL14.EGL_EXTENSIONS)
        viewModel.glExtensions = viewModel.glExtensions?.replace("\\s".toRegex(), "\n")
        Timber.d("OpenGL ES Extensions: ${viewModel.glExtensions}")
        gl_extensions_text_view.text = viewModel.glExtensions ?: ""
    }
}
