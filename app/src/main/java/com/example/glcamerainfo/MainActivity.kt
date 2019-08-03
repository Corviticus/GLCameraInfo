package com.example.glcamerainfo

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
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

    // fragment for displaying release info and credits
    private var mAboutFragment: AboutFragment? = null
    private val isAboutFragmentShown: Boolean
        get() = mAboutFragment != null && (mAboutFragment?.isVisible?: false)

    private var mMenu: Menu? = null

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
     * Called to inflate a given [Menu] as specified by it's xml definition
     * @param menu The [Menu] being used
     * @return A [Boolean] True if menu was created or False otherwise
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)

        // save reference to the menu
        mMenu = menu

        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Called to enable or disable various menu items
     * @param menu The [Menu] being used
     * @return A [Boolean] True if the event was handled or False if not
     */
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {

        // default is enabled
        menu.getItem(0).isVisible = true
        menu.getItem(0).isEnabled = true

        return true
    }

    /**
     * Called when an overflow menu item is selected
     * @param menuItem The [MenuItem] that was selected
     * @return A [Boolean] True if the event was handled or False if not
     */
    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {

        when (menuItem.itemId) {
            android.R.id.home -> {
                val homeIntent = Intent(this, MainActivity::class.java)
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(homeIntent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                return true
            }
            R.id.action_about -> {
                if (mAboutFragment == null) {
                    mAboutFragment = AboutFragment.newInstance(getString(R.string.fragment_menu_name_about))
                }
                if (!isAboutFragmentShown) {
                    mAboutFragment?.show(supportFragmentManager, "About Fragment")
                }
                return true
            }
        }

        return super.onOptionsItemSelected(menuItem)
    }

    /**
     * Creates a fragment without adding it to the back stack
     * This preserves the desired navigation behavior (back button does not affect bottom navigation)
     * @param fragment Fragment to be launched
     * @param fragName String containing the fragment's extensionName
     */
    private fun launchFragment(fragment: androidx.fragment.app.Fragment, fragName: String) {

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        //fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
        fragmentTransaction.replace(R.id.main_layout, fragment)
        fragmentTransaction.commit()

        // set the toolbar title
        supportActionBar?.title = null
    }

}
