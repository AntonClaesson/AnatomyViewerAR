package com.example.anatomyviewer.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.anatomyviewer.R
import com.example.anatomyviewer.ar.AnatomyViewerFragment
import com.example.anatomyviewer.ar.interfaces.ArFragmentResetListener
import com.example.anatomyviewer.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), ArFragmentResetListener, OpenArFragmentListener {

    private val TAG: String = MainActivity::class.java.toString()

    private lateinit var binding: ActivityMainBinding

    private lateinit var mainActivityViewModel: MainActivityViewModel

    private val arFragment: AnatomyViewerFragment by lazy { anatomy_viewer_fragment as AnatomyViewerFragment }
    private val menu: FrameLayout by lazy { start_menu_layout as FrameLayout }
    private val navController: NavController by lazy { findNavController(R.id.myNavHostFragment)}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataBinding()
        setupViewModel()
        setupArWorld()
        setupMenu()
    }

    private fun setupDataBinding(){
        binding = DataBindingUtil.setContentView(this,
            R.layout.activity_main
        )
        binding.lifecycleOwner = this
    }

    private fun setupViewModel(){
        mainActivityViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        binding.mainActivityViewModel = mainActivityViewModel
    }

    private fun setupArWorld(){
        arFragment.resetListener = this
    }

    private fun setupMenu(){
        menu.
        menu.visibility = View.VISIBLE
    }

    override fun openArFragmentRequest() {
       menu.visibility = View.GONE
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
