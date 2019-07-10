package com.example.cameracheckup

import android.opengl.*
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.opengl.GLSurfaceView
import kotlinx.android.synthetic.main.open_gl_fragment_layout.*
import android.opengl.GLES30
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer

class OpenGLFragment : Fragment() {

    private lateinit var glExtensionsViewModel: GLExtensionsModel

    private var extensionsListView: ListView? = null
    private var extensionsAdapter: GLExtensionsAdapter? = null

    private var mGLSurfaceView: GLSurfaceView? = null
    private var mGLRenderer: GLRenderer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.open_gl_fragment_layout, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // get instance of our view model
        glExtensionsViewModel = ViewModelProviders.of(this).get(GLExtensionsModel::class.java)

        // create list view with and adapter to hold the extensions values
        extensionsListView = gl_extensions_list

        // bind the UI to the view model
        glExtensionsViewModel.extensionName.observe(this, Observer { extensionsList ->

            // create the adapter
            if (extensionsAdapter == null) {
                extensionsAdapter = context?.let { GLExtensionsAdapter(it, extensionsList) }
            }

            // add the list to adapter and trigger layout
            extensionsAdapter?.addAll(extensionsList)
            extensionsListView?.adapter = extensionsAdapter
        })

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

        // show the overflow menu
        setHasOptionsMenu(true)

        // create GL renderer passing in the app context so it can access the shader glsl files
        mGLRenderer = context?.let { GLRenderer(it) }

        // set up the GLSurfaceView
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
        glExtensionsViewModel.glVersion.value =
            EGL14.eglQueryString(EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY), EGL14.EGL_VERSION)
        EGL14.eglQueryString(EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY), GLES30.GL_VERSION)
        gl_version_text_view.text = glExtensionsViewModel.glVersion.value ?: ""
    }

    private fun bindEGLExtensions() {

        // get the supported gl extensions and replace spaces with line breaks
        val string = EGL14.eglQueryString(EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY), EGL14.EGL_EXTENSIONS)
        string?.replace("\\s".toRegex(), "\n").also { extension ->

            // parse string on line breaks
            val lines = extension?.lines()

            // fill a temporary list with each extension and pass it to the view model
            val tempList: MutableList<String> = mutableListOf()
            lines?.forEach { tempList.add(it) }
            glExtensionsViewModel.extensionName.value = tempList
        }
    }

}
