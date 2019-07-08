package com.example.cameracheckup

import android.content.Context
import android.graphics.Point
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.camera_fragment_layout.*
import kotlinx.coroutines.*
import org.jetbrains.anko.textColor
import org.jetbrains.anko.windowManager
import kotlin.coroutines.CoroutineContext


class CameraFragment : Fragment(), CoroutineScope {

    companion object {
        const val SUPPORT_LEVEL_LEGACY = "LEGACY"
        const val SUPPORT_LEVEL_LIMITED = "LIMITED"
        const val SUPPORT_LEVEL_FULL = "FULL"
    }

    /** Run all co-routines on Main  */
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private var mCameraViewModel: CameraViewModel? = null

    private var mCameraCapabilities: String = ""
    private var mFocusCapabilities: String = ""

    private var mCameraInfo: CameraInfo? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.camera_fragment_layout, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // get the device display size
        val displaySize = Point()
        context?.windowManager?.defaultDisplay?.getRealSize(displaySize)

        // get instance of our view model
        mCameraViewModel = ViewModelProviders.of(this).get(CameraViewModel::class.java)


        setViewModelObservers()


        // create instance of the CameraInfo class
        mCameraInfo = activity?.let { CameraInfo(it, mCameraViewModel as CameraViewModel) }


        // and ...  open the camera
        mCameraInfo?.openCamera(displaySize.x, displaySize.y)

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

        setHasOptionsMenu(true)

        launch(coroutineContext) {

            val hardwareSupport = "Your camera offers ${getCameraHardwareSupport()} Level hardware support. " +
                    "This support level was determined by the manufacturer of this device and cannot be changed. "
            camera_version_text_view.text = hardwareSupport

            mCameraCapabilities = getCameraCapabilities()

            when (getCameraHardwareSupport()) {
                SUPPORT_LEVEL_LEGACY -> {
                    version_info_text_view.text = context?.getString(R.string.legacy_support_string)
                }
                SUPPORT_LEVEL_LIMITED -> {
                    version_info_text_view.text = context?.getString(R.string.limited_support_string)
                }
                SUPPORT_LEVEL_FULL -> {
                    version_info_text_view.text = context?.getString(R.string.full_support_string)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mCameraInfo?.closeCamera()
        coroutineContext.cancelChildren()
        this@CameraFragment.clearFindViewByIdCache()
    }

    /**
     *
     * @return
     */
    private fun getCameraHardwareSupport(): String {

        val cameraManager = context?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val characteristics = cameraManager.getCameraCharacteristics("0")

        var supportedHardwareLevel = SUPPORT_LEVEL_LIMITED
        when {
            characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                    == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL ->
                supportedHardwareLevel = SUPPORT_LEVEL_FULL
            characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                    == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY ->
                supportedHardwareLevel = SUPPORT_LEVEL_LEGACY
            characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                    == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED ->
                supportedHardwareLevel = SUPPORT_LEVEL_LIMITED
        }
        return supportedHardwareLevel
    }

    /**
     *
     * @return
     */
    private fun getCameraCapabilities(): String {

        val stringBuilder = StringBuilder()

        val cameraManager = context?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val characteristics = cameraManager.getCameraCharacteristics("0")

        characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)?.also {
            for (i in 0 until it.size) {
                when {
                    it[i] == CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE ->
                        stringBuilder.append("- BACKWARD_COMPATIBLE\n")
                    it[i] == CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_POST_PROCESSING ->
                        stringBuilder.append("- MANUAL_POST_PROCESSING\n")
                    it[i] == CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR ->
                        stringBuilder.append("- MANUAL_SENSOR\n")
                    it[i] == CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW ->
                        stringBuilder.append("- RAW\n")
                }
            }
        }
        return stringBuilder.toString()
    }

    /**
     * An observer for each view model value. Used for updating the UI as the values change
     */
    private fun setViewModelObservers() {
        // exposure compensation value change observer
        mCameraViewModel?.evCompValues?.observe(this, Observer { compRange ->
            launch(coroutineContext) {
                compRange?.let {
                    val rangeString = "${it.lower} to ${it.upper}"
                    val valueString = "\u003C $rangeString \u003E"
                    ev_comp_range_textview.text = valueString
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
                }
            }
        })

        // focus lock capability observer
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

        // focus lock capability observer
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

        // focus lock capability observer
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
