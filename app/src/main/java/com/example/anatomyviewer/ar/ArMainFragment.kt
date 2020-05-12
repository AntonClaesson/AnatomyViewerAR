package com.example.anatomyviewer.ar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.anatomyviewer.R
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.anatomyviewer.ar.AnatomyViewerFragment
import com.example.anatomyviewer.ar.interfaces.ArFragmentResetListener
import com.example.anatomyviewer.databinding.ActivityMainBinding
import com.example.anatomyviewer.main.MainActivityViewModel
import com.google.android.material.snackbar.Snackbar

/**
 * A simple [Fragment] subclass.
 * Use the [ArMainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ArMainFragment : AppCompatActivity() , ArFragmentResetListener {
    private val TAG: String = com.example.anatomyviewer.main.MainActivity::class.java.toString()

    private lateinit var binding: ActivityMainBinding

    private lateinit var mainActivityViewModel: MainActivityViewModel

    lateinit var arFragment: AnatomyViewerFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivityViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        binding = DataBindingUtil.setContentView(this,
            R.layout.ar_activity_main
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
