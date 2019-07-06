package com.example.cameracheckup

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.camera_fragment_layout.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


class CameraFragment : Fragment(), CoroutineScope {

    companion object {

        fun newInstance() = CameraFragment()

        const val SUPPORT_LEVEL_LEGACY = "LEGACY"
        const val SUPPORT_LEVEL_LIMITED = "LIMITED"
        const val SUPPORT_LEVEL_FULL = "FULL"
    }

    /** Run all co-routines on Main  */
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    private lateinit var viewModel: CameraViewModel

    private var mCameraCapabilities: String = ""
    private var mFocusCapabilities: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.camera_fragment_layout, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // get instance of our view model
        viewModel = ViewModelProviders.of(this).get(CameraViewModel::class.java)

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

        var optionsString = ""

        launch(coroutineContext) {
            mCameraCapabilities = getCameraCapabilities()
            mFocusCapabilities = getFocusCapabilities()

            when (getCameraHardwareSupport()) {

                SUPPORT_LEVEL_LEGACY -> {
                    optionsString = "- Exposure Compensation Adjustment\n" +
                            "- Night Mode\n" +
                            mCameraCapabilities +
                            mFocusCapabilities
                }

                SUPPORT_LEVEL_LIMITED -> {
                    optionsString = "\n" +
                            mCameraCapabilities +
                            mFocusCapabilities
                }

                SUPPORT_LEVEL_FULL -> {
                    optionsString = "- Exposure Compensation Adjustment\n" +
                            "- Sensor Sensitivity (ISO)\n" +
                            "- Shutter Speed (EV)\n" +
                            "- Noise Reduction\n" +
                            mCameraCapabilities +
                            mFocusCapabilities
                }
            }

            val hardwareSupport = "Your camera offers ${getCameraHardwareSupport()} Level hardware support. " +
                    "This support level was determined by the manufacturer of this device and cannot be changed. "

            camera_version_text_view.text = hardwareSupport
        }
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
     *
     * @return
     */
    private fun getFocusCapabilities(): String {

        val stringBuilder = StringBuilder()

        val cameraManager = context?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val characteristics = cameraManager.getCameraCharacteristics("0")

        val minFocalDistance = characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE)
        val hyperFocalDistance = characteristics.get(CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE)

        minFocalDistance?.also {
            stringBuilder.append("Minimum Focus Distance: ")
            stringBuilder.append(minFocalDistance)
            stringBuilder.append("\n")
        }

        hyperFocalDistance?.also {
            stringBuilder.append("Maximum Focus Distance: ")
            stringBuilder.append(hyperFocalDistance)
            stringBuilder.append("\n")
        }

        return stringBuilder.toString()
    }

}
