package com.example.anatomyviewer.ar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.anatomyviewer.ar.di.ArModule
import com.example.anatomyviewer.ar.di.DaggerArComponent
import com.google.ar.core.*
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.ux.ArFragment
import java.io.IOException
import java.lang.IllegalArgumentException
import javax.inject.Inject

open class AnatomyViewerFragment : ArFragment() {

    private val TAG = AnatomyViewerFragment::class.java.toString()

    // ViewModel containing common data and LiveData for ui-updates
    lateinit var viewModel: ARViewModel

    // Object wrapping the logic of the AR session.
    @Inject lateinit var arOrganizer: ArOrganizer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  super.onCreateView(inflater, container, savedInstanceState)

        //Instantiate the ViewModel
        viewModel = ViewModelProvider(this).get(ARViewModel::class.java)

        injectArOrganizer()
        arOrganizer.start()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
    }

    override fun getSessionConfiguration(session: Session): Config {
        val config = super.getSessionConfiguration(session)

        // Lightning settings
        config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
        config.focusMode = Config.FocusMode.AUTO

        // Setup AugmentedImageDatabase
        if (!setupAugmentedImageDatabase(requireContext(), config, session)) {
            Toast.makeText(requireContext(), "Could not setup image database", Toast.LENGTH_LONG).show()
        }

        return config
    }

    override fun onUpdate(frameTime: FrameTime?) {
        super.onUpdate(frameTime)
        val frame = this.arSceneView.arFrame ?: return
        arOrganizer.onUpdate(frame)
    }

    private fun injectArOrganizer() {
        // This makes dagger inject the ArOrganizer, and all its dependencies.
        //Make dagger inject arOrganizer and all of its dependencies
        DaggerArComponent.builder().arModule(
            ArModule(viewModel, arSceneView,transformationSystem, context!!, viewLifecycleOwner!!)
        ).build().inject(this)
    }


    private fun setupUi(){
        this.planeDiscoveryController.hide()
        this.planeDiscoveryController.setInstructionView(null)
        this.arSceneView.planeRenderer.isVisible = false
    }

    private fun setupAugmentedImageDatabase(context: Context, config: Config, session: Session): Boolean {

        fun loadAugmentedImageBitmap(imageName: String): Bitmap = context.assets.open(imageName).use { return BitmapFactory.decodeStream(it) }

        try {
            config.augmentedImageDatabase = AugmentedImageDatabase(session).also { database ->
                database.addImage(viewModel.IMAGE_1_NAME,loadAugmentedImageBitmap(viewModel.IMAGE_1_NAME))
                database.addImage(viewModel.IMAGE_2_NAME,loadAugmentedImageBitmap(viewModel.IMAGE_2_NAME))
            }
            return true
        } catch (e: IllegalArgumentException) {
            Log.e(TAG,"Could not add bitmap to augmented image database", e)
        } catch (e: IOException) {
            Log.e(TAG, "IO exception loading augmented image bitmap", e)
        }
        return false
    }

}