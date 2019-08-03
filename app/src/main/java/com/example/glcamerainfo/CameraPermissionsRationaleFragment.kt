package com.example.glcamerainfo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.fragment_camera_permissions_rationale.view.*

/**
 * A fragment used to ask the user for camera permissions
 */
class CameraPermissionsFragment : androidx.fragment.app.DialogFragment() {

    companion object {

        private var title: String = ""

        /**
         * Factory method to create a new instance of
         * this fragment using the provided parameter
         *
         * @param title
         * @return A new instance of fragment LocationPermissionsFragment.
         */
        @JvmStatic
        fun newInstance(title: String): CameraPermissionsFragment {

            this.title = title

            return CameraPermissionsFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_camera_permissions_rationale, container, false)

        // magic to keep app in immersive mode
        dialog?.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        activity?.window?.decorView?.systemUiVisibility?.let {  dialog?.window?.decorView?.systemUiVisibility = it }

        dialog?.setOnShowListener {
            // Clear the not focusable flag from the window
            dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

            // Update the WindowManager with the new attributes (no nicer way I know of to do this)..
            val wm = activity?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.updateViewLayout(dialog?.window?.decorView, dialog?.window?.attributes)
        }

        // Add animations
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation

        // Setup a toolbar for this fragment
        val toolbar = rootView.cameraRationaleToolbar
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left_24dp)
        toolbar.setNavigationOnClickListener { dismiss() }
        toolbar.setTitle(R.string.permissions_title_camera)

        // Inflate the layout for this fragment
        return rootView
    }

    override fun onResume() {
        super.onResume()
        val params = dialog?.window?.attributes
        params?.width = ConstraintLayout.LayoutParams.MATCH_PARENT
        params?.height = ConstraintLayout.LayoutParams.MATCH_PARENT
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }
}
