package com.example.anatomyviewer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment
import java.io.IOException
import java.lang.IllegalArgumentException

open class AnatomyViewerFragment : ArFragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  super.onCreateView(inflater, container, savedInstanceState)

        //Hides the scan floor gif
        this.planeDiscoveryController.hide()
        this.planeDiscoveryController.setInstructionView(null)

        //Hides the plane renderering
        this.arSceneView.planeRenderer.isVisible = true

        return view
    }

    override fun getSessionConfiguration(session: Session): Config {

        fun loadAugmentedImageBitmap(imageName: String): Bitmap =
            requireContext().assets.open(imageName).use { return BitmapFactory.decodeStream(it) }

        fun setupAugmentedImageDatabase(config: Config, session: Session): Boolean {
            try {
                config.augmentedImageDatabase = AugmentedImageDatabase(session).also { database ->
                    database.addImage(IMAGE_1_NAME,loadAugmentedImageBitmap(IMAGE_1_NAME))
                    database.addImage(IMAGE_2_NAME,loadAugmentedImageBitmap(IMAGE_2_NAME))
                }
                return true
            } catch (e: IllegalArgumentException) {
                Log.e(TAG,"Could not add bitmap to augmented image database", e)
            } catch (e: IOException) {
                Log.e(TAG, "IO exception loading augmented image bitmap", e)
            }
            return false
        }

        return super.getSessionConfiguration(session).also { config ->

            config.lightEstimationMode = Config.LightEstimationMode.AMBIENT_INTENSITY
            config.focusMode = Config.FocusMode.AUTO

            if (!setupAugmentedImageDatabase(config, session)) {
                Toast.makeText(requireContext(), "Could not setup image database", Toast.LENGTH_LONG).show()
            }

        }
    }


    companion object {

        private val TAG: String = AnatomyViewerFragment::class.java.simpleName

        private val IMAGE_1_NAME: String = "building.jpg"
        private val IMAGE_2_NAME: String = "earth.jpg"

    }
}