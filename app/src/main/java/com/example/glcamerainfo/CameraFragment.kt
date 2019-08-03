package com.example.glcamerainfo

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Point
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import com.example.glcamerainfo.CameraInfo.Companion.SUPPORT_LEVEL_FULL
import com.example.glcamerainfo.CameraInfo.Companion.SUPPORT_LEVEL_LEGACY
import com.example.glcamerainfo.CameraInfo.Companion.SUPPORT_LEVEL_LIMITED
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.camera_fragment_layout.*
import kotlinx.coroutines.*
import org.jetbrains.anko.textColor
import org.jetbrains.anko.windowManager
import kotlin.coroutines.CoroutineContext

class CameraFragment : Fragment(), CoroutineScope {

    /** Run all co-routines on Main  */
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    // helper class to query the camera capabilities
    private var mCameraInfo: CameraInfo? = null

    // the model for this fragment
    private var mCameraViewModel: CameraViewModel? = null

    private var mCameraPermissionsFragment: CameraPermissionsFragment? = null
    private val isRationaleCamFragmentShown: Boolean
        get() = mCameraPermissionsFragment != null
                && (mCameraPermissionsFragment?.isVisible
            ?: false)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.camera_fragment_layout, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // get the device display size
        val displaySize = Point()
        context?.windowManager?.defaultDisplay?.getRealSize(displaySize)

        // get instance of our view model
        mCameraViewModel = ViewModelProviders.of(this).get(CameraViewModel::class.java)

        // bind the UI to the CameraViewModel
        bindViewModelObservers()

        // create instance of the CameraInfo class
        mCameraInfo = activity?.let { CameraInfo(it, mCameraViewModel as CameraViewModel) }

        // ask user for camera permissions and nag if necessary
        if (context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) } != PackageManager.PERMISSION_GRANTED) {
            // do we need to explain the need for the camera permissions
            activity?.also {
                if (ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)) {
                    if (mCameraPermissionsFragment == null) {
                        mCameraPermissionsFragment = CameraPermissionsFragment.newInstance("Camera Permissions Rationale")
                    }
                    if (!isRationaleCamFragmentShown) {
                        val fragmentManager = (activity as FragmentActivity).supportFragmentManager
                        mCameraPermissionsFragment?.show(fragmentManager, getString(R.string.permissions_title_camera))

                        // nag the user until they grant permissions
                        fragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
                            override fun onFragmentViewDestroyed(fragManager: FragmentManager, fragment: Fragment) {
                                super.onFragmentViewDestroyed(fragManager, fragment)
                                fragmentManager.unregisterFragmentLifecycleCallbacks(this)
                                requestPermissions(arrayOf(Manifest.permission.CAMERA), CameraInfo.REQUEST_CAMERA_PERMISSIONS)
                            }
                        }, false)
                    }
                } else {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), CameraInfo.REQUEST_CAMERA_PERMISSIONS)
                }
            }
        } else {
            // and finally, open the camera
            mCameraInfo?.openCamera()
        }

        // use custom layout for the toolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar_camera)
        val customActionBar = (activity as AppCompatActivity).supportActionBar
        customActionBar?.also {
            it.setDisplayShowHomeEnabled(false)
            it.setDisplayHomeAsUpEnabled(false)
            it.setDisplayShowCustomEnabled(true)
            it.setDisplayShowTitleEnabled(false)
            it.title = null
        }

        // use the overflow menu
        setHasOptionsMenu(true)

        launch(coroutineContext) {
            // get camera hardware support level and display the opening paragraph
            val supportedHardware = mCameraInfo?.getCameraHardwareSupport()
            val hardwareSupportString = "Your camera offers $supportedHardware Level hardware support. " +
                    "This support level was determined by the manufacturer of this device and cannot be changed. "
            camera_version_text_view.text = hardwareSupportString

            // add more info about the support level
            when (supportedHardware) {
                SUPPORT_LEVEL_LEGACY -> version_info_text_view.text = context?.getString(R.string.legacy_support_string)
                SUPPORT_LEVEL_LIMITED -> version_info_text_view.text = context?.getString(R.string.limited_support_string)
                SUPPORT_LEVEL_FULL -> version_info_text_view.text = context?.getString(R.string.full_support_string)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mCameraInfo?.closeCamera()
        coroutineContext.cancelChildren()
        this@CameraFragment.clearFindViewByIdCache()
    }

    // this will allow the app to continue after the user accepts the permissions request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        when (requestCode) {
            CameraInfo.REQUEST_CAMERA_PERMISSIONS -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    mCameraInfo?.openCamera()
                } else {
                    if (mCameraPermissionsFragment == null) {
                        mCameraPermissionsFragment = CameraPermissionsFragment.newInstance("Camera Permissions Rationale")
                    }
                    if (!isRationaleCamFragmentShown) {
                        val fragmentManager = (activity as FragmentActivity).supportFragmentManager
                        mCameraPermissionsFragment?.show(fragmentManager, getString(R.string.permissions_title_camera))
                    }
                }
                return
            }
        }
    }

    /**
     * Create an observer for each view model value. Used for automatically updating the UI as the values change.
     */
    private fun bindViewModelObservers() {
        // exposure compensation value change observer
        mCameraViewModel?.evCompValues?.observe(this, Observer { compRange ->
            launch(coroutineContext) {
                compRange?.let {
                    val rangeString = "${it.lower} to ${it.upper}"
                    val valueString = "\u003C $rangeString \u003E"
                    ev_comp_range_textview.text = valueString
                    mCameraViewModel?.supportsEVComp?.value = true
                }
            }
        })

        // iso value change observer
        mCameraViewModel?.isoValues?.observe(this, Observer { isoRange ->
            launch(coroutineContext) {
                isoRange?.let {
                    val rangeString = "${it.lower} to ${it.upper}"
                    val valueString = "\u003C $rangeString \u003E"
                    iso_range_textview.text = valueString
                    mCameraViewModel?.supportsISO?.value = true
                }
            }
        })

        // exposure value change observer
        mCameraViewModel?.exposureValues?.observe(this, Observer { evRange ->
            launch(coroutineContext) {
                evRange?.let {
                    val rangeString = "${it.lower} to ${it.upper}"
                    val valueString = "\u003C $rangeString \u003E"
                    ev_range_textview.text = valueString
                    mCameraViewModel?.supportsEV?.value = true
                }
            }
        })

        // EV Compensation capability observer
        mCameraViewModel?.supportsEVComp?.observe(this, Observer {
            launch(coroutineContext) {
                when (it) {
                    true -> {
                        ev_comp_supported_textview.also { textView ->
                            textView.text = getString(R.string.yes)
                            context?.let { textView.textColor = ContextCompat.getColor(it, R.color.green) }
                        }
                    }
                    false -> {
                        ev_comp_supported_textview.also { textView ->
                            textView.text = getString(R.string.no)
                            context?.let { textView.textColor = ContextCompat.getColor(it, R.color.red) }
                        }
                    }
                }
            }
        })

        // ISO value capability observer
        mCameraViewModel?.supportsISO?.observe(this, Observer {
            launch(coroutineContext) {
                when (it) {
                    true -> {
                        iso_supported_textview.also { textView ->
                            textView.text = getString(R.string.yes)
                            context?.let { textView.textColor = ContextCompat.getColor(it, R.color.green) }
                        }
                    }
                    false -> {
                        iso_supported_textview.also { textView ->
                            textView.text = getString(R.string.no)
                            context?.let { textView.textColor = ContextCompat.getColor(it, R.color.red) }
                        }
                    }
                }
            }
        })

        // Exposure Value capability observer
        mCameraViewModel?.supportsEV?.observe(this, Observer {
            launch(coroutineContext) {
                when (it) {
                    true -> {
                        exposure_supported_textview.also { textView ->
                            textView.text = getString(R.string.yes)
                            context?.let { textView.textColor = ContextCompat.getColor(it, R.color.green) }
                        }
                    }
                    false -> {
                        exposure_supported_textview.also { textView ->
                            textView.text = getString(R.string.no)
                            context?.let { textView.textColor = ContextCompat.getColor(it, R.color.red) }
                        }
                    }
                }
            }
        })

        // focus lock capability observer
        mCameraViewModel?.supportsFocusLock?.observe(this, Observer {
            launch(coroutineContext) {
                when (it) {
                    true -> {
                        focus_lock_supported_textview.also { textView ->
                            textView.text = getString(R.string.yes)
                            context?.let { textView.textColor = ContextCompat.getColor(it, R.color.green) }
                        }
                    }
                    false -> {
                        focus_lock_supported_textview.also { textView ->
                            textView.text = getString(R.string.no)
                            context?.let { textView.textColor = ContextCompat.getColor(it, R.color.red) }
                        }
                    }
                }
            }
        })
    }
}

