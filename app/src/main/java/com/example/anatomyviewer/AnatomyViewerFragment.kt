package com.example.anatomyviewer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.filament.Box
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.DpToMetersViewSizer
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.info_card.*

open class AnatomyViewerFragment : ArFragment() {

    // ViewModel containing data and business logic
    private lateinit var viewModel: ARViewModel

    private var modelAnchorNode: AnchorNode? = null     // A node attached to the tracked AugmentedImage anchor
    private var modelNode: TransformableNode? = null    // A node to which the model is attached


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  super.onCreateView(inflater, container, savedInstanceState)

        //Instantiate ViewModel
        viewModel = ViewModelProvider(this).get(ARViewModel::class.java)

        //Hides the scan floor gif
        this.planeDiscoveryController.hide()
        this.planeDiscoveryController.setInstructionView(null)

        //Hides the plane renderering
        this.arSceneView.planeRenderer.isVisible = false

        //Setup observers
        viewModel.trackedImageUpdated.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandledOrReturnNull()?.let { image ->
                Toast.makeText( this.requireContext(), "Changed to tracking: " + image.name, Toast.LENGTH_LONG).show()
                createModelForTrackedImage()
            }
        })

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

        val id = trackedImage.name
        var model = R.raw.dino //Default fallback model
        when(id){
            viewModel.IMAGE_1_NAME -> {model = R.raw.bone }
            viewModel.IMAGE_2_NAME -> {model = R.raw.dino}
        }

        // Load model from file
        ModelRenderable.builder().setSource(this.requireContext(), model).build().thenAccept { renderable ->
            renderable.isShadowCaster = true
            renderable.isShadowReceiver = false

            val anchor = trackedImage.createAnchor(trackedImage.centerPose)
            modelAnchorNode = AnchorNode(anchor).apply {
                setParent(this@AnatomyViewerFragment.arSceneView.scene)
            }

            createViewRenderableForNode(modelAnchorNode!!)

            // Finish loading of model
            val node = TransformableNode(this.transformationSystem)
            node.rotationController.isEnabled = true
            node.scaleController.isEnabled = true
            node.translationController.isEnabled = false
//            node.scaleController.maxScale =node.scaleController.maxScale*1.0f

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

    private fun createViewRenderableForNode(parent: Node){

        val id = viewModel.currentlyTrackedImage?.name  ?: return

        var title = "Not available"
        var description = "Not available"

        when (id) {
            viewModel.IMAGE_1_NAME -> { title = "Skeleton"; description = "This is the skeleton of a human torso"}
            viewModel.IMAGE_2_NAME -> { title = "Torso"; description = "This is the skeleton, heart and kidneys of a human torso"}
        }

        val dpm = 500 //Default 250 dpm

        ViewRenderable.builder().setView(requireContext(), R.layout.info_card).build().thenAccept { viewRenderable ->

            viewRenderable.setSizer { DpToMetersViewSizer(dpm).getSize(viewRenderable.view) }
            val titleTextView = viewRenderable.view.findViewById<TextView>(R.id.title)
            titleTextView.text = title
            val descriptionTextView = viewRenderable.view.findViewById<TextView>(R.id.description)
            descriptionTextView.text = description

            viewRenderable.isShadowCaster = false
            viewRenderable.isShadowReceiver = false

            val viewNode = Node()
            viewNode.setParent(parent)
            viewNode.localPosition = Vector3(0f,0.2f,0f)
            viewNode.renderable = viewRenderable
        }

    }

    companion object {
        private val TAG: String = AnatomyViewerFragment::class.java.simpleName
    }

}