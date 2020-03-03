package com.example.anatomyviewer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.ar.sceneform.ux.ArFragment
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.anatomyviewer.databinding.ActivityMainBinding
import com.google.ar.core.*
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.TransformableNode


private val TAG: String = MainActivity::class.java.simpleName

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var arFragment: ArFragment

    private lateinit var modelRenderable: ModelRenderable
    private var modelAnchorNode: AnchorNode? = null
    private var modelNode: TransformableNode? = null

    // Enables tracking of dynamic images. Should be set to true if the tracked image is able to move.
    val dynamicTrackingEnabled: Boolean = true

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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // TODO: Can this be converted with data binding?
        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment
        arFragment.arSceneView.scene.addOnUpdateListener(::onUpdate)

        binding.resetButton.setOnClickListener {
            trackNewImages = true
            currentlyTrackedImage = null

            if (modelAnchorNode != null) {
                val anchorNode = modelAnchorNode!!
                if (anchorNode.anchor != null) {
                    anchorNode.anchor!!.detach()
                }
                arFragment.arSceneView.scene.removeChild(modelAnchorNode)
            }

            Toast.makeText(arFragment.requireContext(), "Scanning for new image", Toast.LENGTH_SHORT).show() }
    }




    /// Called once per frame right before the scene is updated.
    private fun onUpdate(frameTime: FrameTime){
        val frame = arFragment.arSceneView.arFrame ?: return
        // If tracking is ok, we proceed
        if (trackingStateOK(frame)) {
            val updatedTrackedImage = updateTrackedImageForFrame(frame) ?: return
            Toast.makeText(arFragment.requireContext(), "Changed to tracking: "+ updatedTrackedImage.name, Toast.LENGTH_LONG).show()
        } else {
            handleBadTracking()
        }
    }

    // Checks whether tracking is active or not.
    private fun trackingStateOK(frame: Frame): Boolean {
        if (frame.camera.trackingState == TrackingState.TRACKING) return true
        return false
    }

    private fun handleBadTracking() {
        //TODO: Handle bad tracking
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

        val model = if (trackedImage.name == "earth.jpg") R.raw.bone else R.raw.dino

        // Load model from file
        ModelRenderable.builder().setSource(arFragment.requireContext(), model).build().thenAccept { renderable ->
            modelRenderable = renderable
            renderable.isShadowCaster = true
            renderable.isShadowReceiver = false

            if (dynamicTrackingEnabled) {
                val anchor = trackedImage.createAnchor(trackedImage.centerPose)
                modelAnchorNode = AnchorNode(anchor).apply {
                    setParent(arFragment.arSceneView.scene)
                }

            } else {
               modelAnchorNode = AnchorNode().apply {
                   val pos = trackedImage.centerPose
                   this.worldPosition = Vector3(pos.tx(), pos.ty(), pos.tz())
                   setParent(arFragment.arSceneView.scene)
               }
            }
            val node = TransformableNode(arFragment.transformationSystem)

            node.rotationController.isEnabled = true
            node.scaleController.isEnabled = true

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
}
