package com.example.anatomyviewer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

open class AnatomyViewerFragment : ArFragment() {

    // ViewModel containing data and business logic
    private lateinit var viewModel: ARViewModel

    // The node attached to the tracked AugmentedImage anchor
    private var modelAnchorNode: AnchorNode? = null

    // The node to which the model is attached
    private var modelNode: TransformableNode? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  super.onCreateView(inflater, container, savedInstanceState)

        //Instantiate ViewModel
        viewModel = ViewModelProvider(this).get(ARViewModel::class.java)

        //Hides the scan floor gif
        this.planeDiscoveryController.hide()
        this.planeDiscoveryController.setInstructionView(null)

        //Hides the plane renderering
        this.arSceneView.planeRenderer.isVisible = true

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
        viewModel.reset()

        if (modelAnchorNode != null) {
            val anchorNode = modelAnchorNode!!
            if (anchorNode.anchor != null) {
                anchorNode.anchor!!.detach()
            }
            this.arSceneView.scene.removeChild(modelAnchorNode)
        }

        Toast.makeText(this.requireContext(), "Scanning for new image", Toast.LENGTH_SHORT).show()
    }

    override fun onUpdate(frameTime: FrameTime?) {
        super.onUpdate(frameTime)
        val frame = this.arSceneView.arFrame ?: return
        // If tracking is ok, we proceed
        if (trackingStateOK(frame)) {
            viewModel.updateTrackedImageForFrame(frame)
            if (viewModel.shouldUpdate3DModel) {
                viewModel.shouldUpdate3DModel = false
                createModelForTrackedImage()
                Toast.makeText(this.requireContext(), "Changed to tracking: "+ viewModel.currentlyTrackedImage!!.name, Toast.LENGTH_LONG).show()
                Log.i(TAG, "Tracking "+viewModel.currentlyTrackedImage!!.name)
            }
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


    // Adds the 3D model corresponding to the tracked image to the scene.
    private fun createModelForTrackedImage() {
        val trackedImage = viewModel.currentlyTrackedImage ?: return

        val model = if (trackedImage.name == "earth.jpg") R.raw.bone else R.raw.dino

        // Load model from file
        ModelRenderable.builder().setSource(this.requireContext(), model).build().thenAccept { renderable ->
            renderable.isShadowCaster = true
            renderable.isShadowReceiver = false

            if (viewModel.dynamicTrackingEnabled) {
                val anchor = trackedImage.createAnchor(trackedImage.centerPose)
                modelAnchorNode = AnchorNode(anchor).apply {
                    setParent(this@AnatomyViewerFragment.arSceneView.scene)
                }

            } else {
                modelAnchorNode = AnchorNode().apply {
                    val pos = trackedImage.centerPose
                    this.worldPosition = Vector3(pos.tx(), pos.ty(), pos.tz())
                    setParent(this@AnatomyViewerFragment.arSceneView.scene)
                }
            }

            val node = TransformableNode(this.transformationSystem)

            node.rotationController.isEnabled = true
            node.scaleController.isEnabled = true

            modelNode = node
            node.setParent(modelAnchorNode)
            node.renderable = renderable
            node.select()
        }
            .exceptionally { throwable ->
                Log.e(TAG, "Could not create ModelRenderable", throwable)
                return@exceptionally null
            }
    }

    companion object {
        private val TAG: String = AnatomyViewerFragment::class.java.simpleName
    }

}