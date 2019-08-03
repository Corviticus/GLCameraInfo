package com.example.glcamerainfo

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.layout_fragment_about.view.*

/**
 * A class for displaying a Fragment containing information about this app
 */
class AboutFragment : androidx.fragment.app.DialogFragment() {

    companion object {

        private var title: String = ""

        /**
         * Factory method to create a new instance of
         * this fragment using the provided parameter
         *
         * @param title The title to display on the top of fragment
         * @return A new instance of fragment AboutFragment.
         */
        @JvmStatic
        fun newInstance(title: String): AboutFragment {

            this.title = title

            return AboutFragment()
        }
    }

    // two web views to display some html
    private var infoWebView: WebView? = null
    private var changeListWebView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val rootView = inflater.inflate(R.layout.layout_fragment_about, container, false)

        // Setup a toolbar for this fragment
        val toolbar = rootView.aboutDialogToolbar
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left_24dp)
        toolbar.setNavigationOnClickListener { dismiss() }
        toolbar.title = "About"

        infoWebView = rootView.findViewById(R.id.versionWebView)
        infoWebView?.loadUrl("file:///android_asset/changelog.html")
        infoWebView?.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
        infoWebView?.isScrollbarFadingEnabled = false
        infoWebView?.setBackgroundColor(Color.WHITE)

        changeListWebView = rootView.findViewById(R.id.changeNoticeWebView)
        changeListWebView?.loadUrl("file:///android_asset/licenses_credits.html")
        changeListWebView?.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
        changeListWebView?.isScrollbarFadingEnabled = false
        changeListWebView?.setBackgroundColor(Color.WHITE)

        // Inflate the layout for this fragment
        return rootView
    }

    override fun onResume() {
        super.onResume()
        val params = dialog?.window?.attributes
        params?.width = ConstraintLayout.LayoutParams.MATCH_PARENT
        params?.height = ConstraintLayout.LayoutParams.MATCH_PARENT
        dialog?.window?.attributes = params as android.view.WindowManager.LayoutParams
    }

    override fun onDestroyView() {

        if (infoWebView != null) {
            infoWebView?.clearCache(true)
            infoWebView?.destroy()
            infoWebView = null
        }

        if (changeListWebView != null) {
            changeListWebView?.clearCache(true)
            changeListWebView?.destroy()
            changeListWebView = null
        }

        super.onDestroyView()
        this@AboutFragment.clearFindViewByIdCache()
    }
}
