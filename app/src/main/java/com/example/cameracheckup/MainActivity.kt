package com.example.cameracheckup

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var textMessage: TextView
    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_camera -> {

                textMessage.setText(R.string.title_camera)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_opengl -> {

                textMessage.setText(R.string.title_OpenGL)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_opencv -> {

                textMessage.setText(R.string.title_OpenCV)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        textMessage = findViewById(R.id.message)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }
}
