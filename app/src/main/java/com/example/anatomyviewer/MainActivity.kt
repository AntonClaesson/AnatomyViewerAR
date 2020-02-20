package com.example.anatomyviewer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.ar.sceneform.ux.ArFragment
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.google.ar.core.AugmentedImage
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.HitResult
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.HitTestResult

private val TAG: String = MainActivity::class.java.simpleName

class MainActivity : AppCompatActivity() {

    lateinit var arFragment: ArFragment

    // Enables or disables search of new trackable images
    var trackNewImages: Boolean = true

    // The image currently being tracked by ARCore
    var currentlyTrackedImage: AugmentedImage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment

        arFragment.arSceneView.setOnTouchListener(::onScreenTouch)

        //Sets the frame update listener
        arFragment.arSceneView.scene.addOnUpdateListener(::onUpdate)
    }


    private fun onScreenTouch(view: View, motionEvent: MotionEvent): Boolean {
        trackNewImages = true
        Toast.makeText(arFragment.requireContext(), "Scanning for new image", Toast.LENGTH_SHORT).show()
        return true
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

        // Make first tracked image active if preconditions are met
        fullTrackingImages.firstOrNull()?.let { augmentedImage ->
            if (currentlyTrackedImage == augmentedImage || !trackNewImages) return

            // Sets the currently tracked image for which the 3D model will be rendered
            currentlyTrackedImage = augmentedImage

            // Disable tracking of new images until user input requests differently
            trackNewImages = false

            Toast.makeText(arFragment.requireContext(), "Changed to tracking: "+ augmentedImage.name, Toast.LENGTH_LONG).show()
        }

    }

}
