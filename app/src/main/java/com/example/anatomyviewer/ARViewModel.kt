package com.example.anatomyviewer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.DpToMetersViewSizer
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.TransformationSystem
import java.io.IOException
import java.lang.IllegalArgumentException

class ARViewModel(): ViewModel() {


    //DATA
    val IMAGE_1_NAME: String = "building.jpg"
    val IMAGE_2_NAME: String = "earth.jpg"
    val IMAGE_3_NAME: String = "queen_of_diamonds.jpg"
    val IMAGE_4_NAME: String = "ten_of_spades.jpg"


    // Observers
    val trackedImageUpdated = MutableLiveData<Event<AugmentedImage?>>()

    // Instance variables
    var arSceneView: ArSceneView? = null
    var trackNewImages: Boolean = true
    var currentlyTrackedImage: AugmentedImage? = null
        set(value) {
            field = value
            trackedImageUpdated.value = Event(value)
        }
    private var modelAnchorNode: AnchorNode? = null     // A node attached to the tracked AugmentedImage anchor
    private var modelNode: TransformableNode? = null    // A node to which the model is attached
    //private var infoCardNode: Node? = null
    private var quizCardNode: Node? = null
    private lateinit var context: Context
    private lateinit var transformationSystem: TransformationSystem

    fun setup(arSceneView: ArSceneView, context: Context, transformationSystem: TransformationSystem, lifecycleOwner: LifecycleOwner){
        this.arSceneView = arSceneView
        this.context = context
        this.transformationSystem = transformationSystem

        //Setup observers
        trackedImageUpdated.observe(lifecycleOwner, Observer { event ->
            event?.getContentIfNotHandledOrReturnNull()?.let { image ->
                Toast.makeText( context, "Changed to tracking: " + image.name, Toast.LENGTH_LONG).show()
                createModelForTrackedImage()
                createQuizRenderable()
            }
        })
    }

    fun reset(context: Context){
        currentlyTrackedImage = null
        trackNewImages = true

        if (modelAnchorNode != null) {
            val anchorNode = modelAnchorNode!!
            if (anchorNode.anchor != null) {
                anchorNode.anchor!!.detach()
            }
            this.arSceneView?.scene?.removeChild(modelAnchorNode)
        }

        arSceneView?.scene?.removeChild(quizCardNode)
        quizCardNode?.setParent(null)
        quizCardNode = null
        Toast.makeText(context, "Scanning for new image", Toast.LENGTH_SHORT).show()
    }

    fun onUpdate(frame: Frame) {
        updateTrackedImageForFrame(frame)
       // updateCardNodeForFrame(frame, infoCardNode)
        updateCardNodeForFrame(frame, quizCardNode)
    }

    fun updateTrackedImageForFrame(frame: Frame) {
        // Check if tracking of new images is requested, otherwise return
        if (!trackNewImages) return

        val trackedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage::class.java)

        // Images which are tracked by most recent location
        //val nonFullTrackingImages = trackedAugmentedImages.filter { it.trackingMethod != AugmentedImage.TrackingMethod.FULL_TRACKING }

        // Images which are being tracked by their actual location (in frame)
        val fullTrackingImages = trackedAugmentedImages.filter { it.trackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING }

        // Return if no images are currently fully visible
        if (fullTrackingImages.isEmpty()) return

        // Make first tracked image active
        fullTrackingImages.firstOrNull()?.let { augmentedImage ->
            if (currentlyTrackedImage == augmentedImage) return

            // Disable tracking of new images until user input requests otherwise
            trackNewImages = false

            // Update tracked image
            currentlyTrackedImage = augmentedImage
        }
    }

    private fun updateCardNodeForFrame(frame: Frame, node: Node?){
        val cardNode = node ?: return

        val cameraPosition = Vector3(frame.camera.pose.tx(),frame.camera.pose.ty(),frame.camera.pose.tz())
        val cardPosition = cardNode.worldPosition
        val direction = Vector3.subtract(cameraPosition, cardPosition)
        val lookRotation = Quaternion.lookRotation(direction, Vector3.up())
        cardNode.worldRotation = lookRotation
    }

    // Adds the 3D model corresponding to the tracked image to the scene.
    private fun createModelForTrackedImage() {
        val trackedImage = currentlyTrackedImage ?: return

        val id = trackedImage.name
        var model = R.raw.dino //Default fallback model
        when(id){
            IMAGE_3_NAME -> {model = R.raw.bone }
            IMAGE_4_NAME -> {model = R.raw.bone_with_heart_kidney}
        }

        // Load model from file
        ModelRenderable.builder().setSource(context, model).build().thenAccept { renderable ->
            renderable.isShadowCaster = true
            renderable.isShadowReceiver = false

            val anchor = trackedImage.createAnchor(trackedImage.centerPose)
            modelAnchorNode = AnchorNode(anchor).apply {
                setParent(arSceneView?.scene)
            }

            // Finish loading of model
            val node = TransformableNode(this.transformationSystem)
            node.rotationController.isEnabled = true
            node.scaleController.isEnabled = true
            node.translationController.isEnabled = false
            node.scaleController.maxScale =node.scaleController.maxScale*1.0f
            node.scaleController.minScale = node.scaleController.minScale*0.1f

            node.localPosition = Vector3(0f,0.05f,0f)


            modelNode = node
            node.setParent(modelAnchorNode)
            node.renderable = renderable
            node.select()

           // createViewRenderableForNode(node)

        }
            .exceptionally { throwable ->
                Log.e(TAG, "Could not create ModelRenderable", throwable)
                return@exceptionally null
            }
    }

    private fun createQuizRenderable() {
        val id = currentlyTrackedImage?.name ?: return
        var currentQuiz=1
        when (id) {
            IMAGE_3_NAME -> {currentQuiz=3}
            IMAGE_4_NAME -> {currentQuiz=4}
        }

        val quizNode = Node()
        quizNode.setParent(arSceneView?.scene)

        val dpm = 500 //Default 250 dpm
        ViewRenderable.builder().setView(context, R.layout.quiz_card).build()
            .thenAccept { viewRenderable ->
                viewRenderable.setSizer { DpToMetersViewSizer(dpm).getSize(viewRenderable.view) }
                viewRenderable.isShadowCaster = false
                viewRenderable.isShadowReceiver = false
                quizNode.renderable = viewRenderable
                quizCardNode = quizNode
            }
    }

    private fun createViewRenderableForNode(parent: Node) {
        val id = currentlyTrackedImage?.name ?: return
        var title = "Not available"
        var description = "Not available"
        when (id) {
            IMAGE_3_NAME -> {
                title = "Skeleton"; description = "This is the skeleton of a human torso"
            }
            IMAGE_4_NAME -> {
                title = "Torso"; description =
                    "This is the skeleton, heart and kidneys of a human torso"
            }
        }

        val dpm = 500 //Default 250 dpm
        ViewRenderable.builder().setView(context, R.layout.info_card).build()
            .thenAccept { viewRenderable ->
                viewRenderable.setSizer { DpToMetersViewSizer(dpm).getSize(viewRenderable.view) }
                val titleTextView = viewRenderable.view.findViewById<TextView>(R.id.title)
                titleTextView.text = title
                val descriptionTextView =
                    viewRenderable.view.findViewById<TextView>(R.id.description)
                descriptionTextView.text = description

                viewRenderable.isShadowCaster = false
                viewRenderable.isShadowReceiver = false

                val infoCardNode = Node()
                infoCardNode.setParent(parent)
                infoCardNode.renderable = viewRenderable

                val modelBoundingBox = parent.collisionShape as Box
                val infoCardBoundingBox = infoCardNode.collisionShape as Box

                infoCardNode.localPosition = Vector3(
                    modelBoundingBox.extents.x / 2 + infoCardBoundingBox.extents.x / 2 + 0.05f,
                    0.2f,
                    0f
                )

                infoCardNode.setOnTapListener { hitTestResult, motionEvent ->
                    Toast.makeText(
                        this.context,
                        "Tapped infoCard card!",
                        Toast.LENGTH_LONG
                    ).show()
                }

              //  this.infoCardNode = infoCardNode
            }
    }

    fun setupAugmentedImageDatabase(context: Context, config: Config, session: Session): Boolean {

        fun loadAugmentedImageBitmap(imageName: String): Bitmap = context.assets.open(imageName).use { return BitmapFactory.decodeStream(it) }

        try {
            config.augmentedImageDatabase = AugmentedImageDatabase(session).also { database ->
                database.addImage(IMAGE_1_NAME,loadAugmentedImageBitmap(IMAGE_1_NAME))
                database.addImage(IMAGE_2_NAME,loadAugmentedImageBitmap(IMAGE_2_NAME))
                database.addImage(IMAGE_3_NAME,loadAugmentedImageBitmap(IMAGE_3_NAME))
                database.addImage(IMAGE_4_NAME,loadAugmentedImageBitmap(IMAGE_4_NAME))

            }
            return true
        } catch (e: IllegalArgumentException) {
            Log.e(TAG,"Could not add bitmap to augmented image database", e)
        } catch (e: IOException) {
            Log.e(TAG, "IO exception loading augmented image bitmap", e)
        }
        return false
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "ARViewModel destroyed!")
    }

    companion object {
        private val TAG: String = ARViewModel::class.java.simpleName
    }
}

