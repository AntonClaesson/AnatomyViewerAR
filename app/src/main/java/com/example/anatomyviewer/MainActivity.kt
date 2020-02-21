package com.example.anatomyviewer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.ar.sceneform.ux.ArFragment
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.google.ar.core.*
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.core.Anchor
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable



private val TAG: String = MainActivity::class.java.simpleName

class MainActivity : AppCompatActivity() {

    lateinit var arFragment: ArFragment

    private lateinit var modelRenderable: ModelRenderable
    private var modelAnchorNode: AnchorNode? = null
    private var modelNode: TransformableNode? = null

    // Enables tracking of dynamic images. Should be set to true if the tracked image is able to move.
    val dynamicTracking: Boolean = false

    // Enables or disables search of new trackable images
    var trackNewImages: Boolean = true

    // The image currently being tracked by ARCore
    var currentlyTrackedImage: AugmentedImage? = null
        set(value) {
            field = value
            createModelForTrackedImage(value)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment

        // Setup listeners
        arFragment.arSceneView.setOnTouchListener(::onScreenTouch)
        arFragment.arSceneView.scene.addOnUpdateListener(::onUpdate)
    }


    private fun onScreenTouch(view: View, motionEvent: MotionEvent): Boolean {
        trackNewImages = true
        currentlyTrackedImage = null

        if (modelAnchorNode != null) {
            val anchorNode = modelAnchorNode!!
            if (anchorNode.anchor != null) {
                anchorNode.anchor!!.detach()
            }
            arFragment.arSceneView.scene.removeChild(modelAnchorNode)
        }


        Toast.makeText(arFragment.requireContext(), "Scanning for new image", Toast.LENGTH_SHORT).show()
        return true
    }


    /// Called once per frame right before the scene is updated.
    private fun onUpdate(frameTime: FrameTime){
        val frame = arFragment.arSceneView.arFrame ?: return
        // If tracking is ok, we proceed
        if (trackingStateOK(frame)) {
            val updatedTrackedImage = updateTrackedImageForFrame(frame) ?: return
            Toast.makeText(arFragment.requireContext(), "Changed to tracking: "+ updatedTrackedImage.name, Toast.LENGTH_LONG).show()
            updateModelForTrackedImage(updatedTrackedImage)
        } else {
            // TODO: Handle bad tracking state
        }
    }

    // Checks whether tracking is active or not.
    private fun trackingStateOK(frame: Frame): Boolean {
        if (frame.camera.trackingState == TrackingState.TRACKING) return true
        return false
    }

    // Updates the currently tracked image when *trackNewImages* is true and a new tracked image is found.
    // In that case the model corresponding to the updated image is added to the scene.
    // Returns a reference to the updated image, or null if there is no update.
    // This reference is referencing the same instance as the variable *currentlyTrackedImage*
    private fun updateTrackedImageForFrame(frame: Frame): AugmentedImage? {
        val updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage::class.java)

        // Images which are tracked by most recent location
        //val nonFullTrackingImages = updatedAugmentedImages.filter { it.trackingMethod != AugmentedImage.TrackingMethod.FULL_TRACKING }

        // Images which are being tracked by their actual location (in frame)
        val fullTrackingImages = updatedAugmentedImages.filter { it.trackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING }

        // Return if no images are currently fully visible
        if (fullTrackingImages.isEmpty()) return null

        // Make first tracked image active if preconditions are met
        fullTrackingImages.firstOrNull()?.let { augmentedImage ->
            if (currentlyTrackedImage == augmentedImage || !trackNewImages) return null

            // Sets the currently tracked image for which the 3D model will be rendered
            currentlyTrackedImage = augmentedImage

            // Disable tracking of new images until user input requests differently
            trackNewImages = false

            return augmentedImage
        }

        return null
    }

    // Adds the 3D model corresponding to the tracked image to the scene.
    private fun createModelForTrackedImage(image: AugmentedImage?) {
        val trackedImage = image ?: return

        val model = if (trackedImage.name == "earth.jpg") R.raw.lion else R.raw.dino

        // Load model from file
        ModelRenderable.builder().setSource(arFragment.requireContext(), model).build().thenAccept { renderable ->
            modelRenderable = renderable
            renderable.isShadowCaster = true
            renderable.isShadowReceiver = false

            val anchor = trackedImage.createAnchor(trackedImage.centerPose)
            modelAnchorNode = AnchorNode(anchor).apply {
                setParent(arFragment.arSceneView.scene)
            }

            val node = TransformableNode(arFragment.transformationSystem)
            modelNode = node
            node.setParent(modelAnchorNode)
            node.renderable = modelRenderable
            node.select()
        }
            .exceptionally { throwable ->
                Log.e(TAG, "Could not create ModelRenderable", throwable)
                return@exceptionally null
            }

    }



    // Updates location of the 3D model to match that of the tracked image
    private fun updateModelForTrackedImage(image: AugmentedImage) {
        // TODO: Update model location to follow the tracked image?

    }




}
