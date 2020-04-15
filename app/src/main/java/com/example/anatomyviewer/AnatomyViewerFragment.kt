package com.example.anatomyviewer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.DpToMetersViewSizer
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.math.Quaternion
import java.io.IOException
import java.lang.IllegalArgumentException


open class AnatomyViewerFragment : ArFragment() {

    private val TAG = AnatomyViewerFragment::class.java.toString()

    // ViewModel containing data and business logic
    private lateinit var viewModel: ARViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  super.onCreateView(inflater, container, savedInstanceState)

        //Instantiate ViewModel
        viewModel = ViewModelProvider(this).get(ARViewModel::class.java)
        viewModel.setup(arSceneView,requireContext(),transformationSystem,viewLifecycleOwner)
        //Hides the scan floor gif
        this.planeDiscoveryController.hide()
        this.planeDiscoveryController.setInstructionView(null)

        //Hides the plane renderering
        this.arSceneView.planeRenderer.isVisible = false
        return view
    }

    override fun getSessionConfiguration(session: Session): Config {
        val config = super.getSessionConfiguration(session)

        // Lightning settings
        config.lightEstimationMode = Config.LightEstimationMode.AMBIENT_INTENSITY
        config.focusMode = Config.FocusMode.AUTO

        // Setup AugmentedImageDatabase
        if (!viewModel.setupAugmentedImageDatabase(requireContext(), config, session)) {
            Toast.makeText(requireContext(), "Could not setup image database", Toast.LENGTH_LONG).show()
        }

        return config
    }

    fun resetSession(){
        viewModel.reset(this.requireContext())
    }

    override fun onUpdate(frameTime: FrameTime?) {
        super.onUpdate(frameTime)
        val frame = this.arSceneView.arFrame ?: return
        viewModel.onUpdate(frame)
    }

}