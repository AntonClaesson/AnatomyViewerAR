package com.example.anatomyviewer.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.anatomyviewer.R
import com.example.anatomyviewer.ar.AnatomyViewerFragment
import com.example.anatomyviewer.ar.interfaces.ArFragmentResetListener
import com.example.anatomyviewer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), ArFragmentResetListener {

    private val TAG: String = MainActivity::class.java.toString()

    private lateinit var binding: ActivityMainBinding

    private lateinit var mainActivityViewModel: MainActivityViewModel

    lateinit var arFragment: AnatomyViewerFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivityViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        binding = DataBindingUtil.setContentView(this,
            R.layout.activity_main
        )
        binding.lifecycleOwner = this
        binding.mainActivityViewModel = mainActivityViewModel

        arFragment = supportFragmentManager.findFragmentById(R.id.anatomy_viewer_fragment) as AnatomyViewerFragment
        arFragment.resetListener = this
    }

    override fun resetArFragment() {
        // Pause the old ar experience
        arFragment.arSceneView.pause()
        // Create a new one
        val newArFragment = AnatomyViewerFragment()
        newArFragment.resetListener = this
        supportFragmentManager.beginTransaction().replace(R.id.anatomy_viewer_fragment, newArFragment).commit()
    }
}
