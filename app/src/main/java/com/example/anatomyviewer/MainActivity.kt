package com.example.anatomyviewer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.ar.sceneform.ux.ArFragment
import android.util.Log
import com.google.ar.core.AugmentedImage
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.sceneform.FrameTime

private val TAG: String = MainActivity::class.java.simpleName

class MainActivity : AppCompatActivity() {

    lateinit var arFragment: ArFragment

    var currentlyTrackedImage: AugmentedImage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment

        //Called whenever user taps an AR plane
        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            Log.d(TAG, "Tapped")

        }
        //Sets the frame update listener
        arFragment.arSceneView.scene.addOnUpdateListener(::onUpdate)
    }


    /// Called once per frame right before the scene is updated.
    private fun onUpdate(frameTime: FrameTime){
        val frame = arFragment.arSceneView.arFrame ?: return

        val updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage::class.java)

        // Images which are tracked by most recent location
        //val nonFullTrackingImages = updatedAugmentedImages.filter { it.trackingMethod != AugmentedImage.TrackingMethod.FULL_TRACKING }

        // Images which are being tracked by their actual location (in frame)
        val fullTrackingImages = updatedAugmentedImages.filter { it.trackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING }

        // Return if no images are currently fully visible
        if (fullTrackingImages.isEmpty()) return

        // Make first tracked image active
        fullTrackingImages.firstOrNull()?.let { augmentedImage ->
            currentlyTrackedImage = augmentedImage
        }

    }

}
