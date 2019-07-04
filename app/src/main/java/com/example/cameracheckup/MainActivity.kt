package com.example.cameracheckup

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext
import timber.log.Timber

class MainActivity : AppCompatActivity(), CoroutineScope {

    /** Run all co-routines on Main  */
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private var mCurrentFragment: androidx.fragment.app.Fragment? = null

    private var mBottomNavigationView: BottomNavigationView? = null

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_camera -> {
                mCurrentFragment = CameraFragment()
                launchFragment(mCurrentFragment as CameraFragment, "Camera Checkup")

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_opengl -> {
                mCurrentFragment = OpenGLFragment()
                launchFragment(mCurrentFragment as OpenGLFragment, "OpenGL Checkup")
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_opencv -> {
                mCurrentFragment = OpenCVFragment()
                launchFragment(mCurrentFragment as OpenCVFragment, "OpenCV Checkup")
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        mBottomNavigationView = findViewById(R.id.nav_view)
        mBottomNavigationView?.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        mBottomNavigationView?.selectedItemId = R.id.navigation_camera

    }

    /**
     * Creates a fragment without adding it to the back stack
     * This preserves the desired navigation behavior (back button does not affect bottom navigation)
     *
     * @param fragment Fragment to be launched
     * @param fragName String containing the fragment's name
     */
    private fun launchFragment(fragment: androidx.fragment.app.Fragment, fragName: String) {

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        fragmentTransaction.replace(R.id.main_layout, fragment)
        fragmentTransaction.commit()

        // set the toolbar title
        supportActionBar?.title = fragName
    }

}
