package com.example.anatomyviewer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.anatomyviewer.ar.AnatomyViewerFragment
import com.example.anatomyviewer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val TAG: String = MainActivity::class.java.toString()

    private lateinit var binding: ActivityMainBinding

    lateinit var arFragment: AnatomyViewerFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as AnatomyViewerFragment
        binding.resetButton.setOnClickListener {
            // Reset ar world
        }
    }
}
