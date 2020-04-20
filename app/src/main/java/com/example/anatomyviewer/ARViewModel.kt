package com.example.anatomyviewer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.anatomyviewer.Models.BaseModel
import com.example.anatomyviewer.Models.MaterialDefinition
import com.example.anatomyviewer.quiz.QuizCardView
import com.example.anatomyviewer.quiz.QuizData
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.DpToMetersViewSizer
import com.google.ar.sceneform.rendering.Material
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.TransformationSystem
import java.io.IOException
import java.lang.IllegalArgumentException

class ARViewModel(): ViewModel() {


    //DATA
    val IMAGE_1_NAME: String = "queen_of_diamonds.jpg"
    val IMAGE_2_NAME: String = "ten_of_spades.jpg"

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

    private var baseModel: BaseModel? = null

    private var defaultMaterials: MutableSet<MaterialDefinition> = mutableSetOf()
    private var customMaterials: MutableSet<MaterialDefinition> = mutableSetOf()

    //private var infoCardNode: Node? = null
    private var quizCardNode: Node? = null
    var quizData: QuizData? = null

    private lateinit var context: Context
    private lateinit var lifecycleOwner: LifecycleOwner
    private lateinit var transformationSystem: TransformationSystem

    fun setup(arSceneView: ArSceneView, context: Context, transformationSystem: TransformationSystem, lifecycleOwner: LifecycleOwner){
        this.arSceneView = arSceneView
        this.context = context
        this.transformationSystem = transformationSystem
        this.lifecycleOwner = lifecycleOwner

        // Initialize quizdata
        quizData = QuizData()

        // Initialize custom materials
        createCustomMaterials()

        //Setup observers
        trackedImageUpdated.observe(lifecycleOwner, Observer { event ->
            event?.getContentIfNotHandledOrReturnNull()?.let { image ->
                Toast.makeText( context, "Changed to tracking: " + image.name, Toast.LENGTH_LONG).show()
                createModelForTrackedImage()
                createQuiz()
            }
        })
    }

    private fun createCustomMaterials(){
        ModelRenderable.builder().setSource(context, R.raw.transparent).build().thenAccept { renderable ->
            val transparentMaterial = renderable.material
            customMaterials.add(MaterialDefinition(transparentMaterial, R.raw.transparent))
        }
    }

    fun reset(context: Context){
        currentlyTrackedImage = null

        // Remove model and its anchor
        (baseModel?.modelAnchorNode)?.let {
            arSceneView?.scene?.removeChild(it)
            it.anchor?.detach()
        }
        baseModel?.baseNode?.setParent(null)
        baseModel = null

        // Remove quiz
        quizCardNode?.setParent(null)
        quizCardNode = null

        trackNewImages = true
        Toast.makeText(context, "Scanning for new image", Toast.LENGTH_SHORT).show()
    }

    fun onUpdate(frame: Frame) {
        updateTrackedImageForFrame(frame)
        updateCardNodeForFrame(frame, quizCardNode)
}

    private fun updateTrackedImageForFrame(frame: Frame) {
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
        direction.y = 0f
        val lookRotation = Quaternion.lookRotation(direction, Vector3.up())
        cardNode.worldRotation = lookRotation
    }

    // Adds the 3D model corresponding to the tracked image to the scene.
    private fun createModelForTrackedImage() {
        val trackedImage = currentlyTrackedImage ?: return

        val id = trackedImage.name

        var newBaseModel = BaseModel()
        newBaseModel.modelID = R.raw.hand_bone // default fallback model

        when(id){
            IMAGE_1_NAME -> {newBaseModel.modelID = R.raw.hand_skin }
            IMAGE_2_NAME -> {newBaseModel.modelID = R.raw.hand_bone}
        }

        buildModel(newBaseModel, trackedImage)
        baseModel = newBaseModel
    }


    private fun buildModel(baseModel: BaseModel, trackedImage: AugmentedImage){
        // First create the base model renderable
        ModelRenderable.builder().setSource(context, baseModel.modelID).build().thenAccept { renderable ->
            renderable.isShadowCaster = true
            renderable.isShadowReceiver = true

            //Save the models default material
            defaultMaterials.add(MaterialDefinition(renderable.material, baseModel.modelID))

            // Create the anchor attached to the image
            val anchor = trackedImage.createAnchor(trackedImage.centerPose)
            baseModel.modelAnchorNode = AnchorNode(anchor).apply {
                setParent(arSceneView?.scene)
            }

            // Create the base model node and attach it to the model anchor node
            val baseNode = TransformableNode(this.transformationSystem)
            baseNode.rotationController.isEnabled = true
            baseNode.scaleController.isEnabled = true
            baseNode.translationController.isEnabled = false
            baseNode.scaleController.maxScale = 2.0f
            baseNode.scaleController.minScale = 0.8f

            //baseNode.localPosition = Vector3(0f,0.05f,0f)

            baseModel.baseNode = baseNode
            baseNode.setParent(baseModel.modelAnchorNode)
            baseNode.renderable = renderable
            baseNode.select()

            buildChildModels(baseModel)

        }.exceptionally { throwable ->
            Log.e(TAG, "Could not create ModelRenderable", throwable)
            return@exceptionally null
        }
    }


    private fun buildChildModels(baseModel: BaseModel) {
        addChildModelNamesTo(baseModel)
        baseModel.childModelIDs.forEach { model ->
            ModelRenderable.builder().setSource(context, model).build().thenAccept { renderable ->
                (baseModel.baseNode)?.let { baseNode ->
                    val childModelNode = Node()
                    childModelNode.renderable = renderable
                    childModelNode.setParent(baseNode)
                    childModelNode.localPosition = Vector3(0f,0f,0f)

                    //Save the materials of the child models
                    defaultMaterials.add(MaterialDefinition(renderable.material, model))
                }
            }
        }
    }


    private fun addChildModelNamesTo(baseModel: BaseModel){
        when(baseModel.modelID){
            R.raw.hand_skin -> {
                baseModel.childModelIDs.add(R.raw.hand_bone)
            }
        }
    }

    private fun createQuiz() {
        val id = currentlyTrackedImage?.name ?: return
        when (id) {
            IMAGE_1_NAME -> { QuizData.QuizType.MODEL1 }
            IMAGE_2_NAME -> { QuizData.QuizType.MODEL2 }
            else -> { null }
        }?.let {
            quizData?.makeNewQuiz(it)
        }

        val quizNode = Node()
        quizNode.setParent(arSceneView?.scene)
        quizNode.localPosition = Vector3(0f,-1f,-2f)
       // quizNode.worldScale = Vector3(0.1f, 0.1f, 0.1f)

        val quizCardView = QuizCardView(context)
        quizCardView.setViewModel(this, lifecycleOwner)

        val dpm = 250 //Default 250 dpm
        ViewRenderable.builder().setView(context, quizCardView).build()
            .thenAccept { viewRenderable ->
                viewRenderable.setSizer { DpToMetersViewSizer(dpm).getSize(viewRenderable.view) }

                viewRenderable.isShadowCaster = false
                viewRenderable.isShadowReceiver = false

                quizNode.renderable = viewRenderable
                quizCardNode = quizNode
            }
    }


    fun setupAugmentedImageDatabase(context: Context, config: Config, session: Session): Boolean {

        fun loadAugmentedImageBitmap(imageName: String): Bitmap = context.assets.open(imageName).use { return BitmapFactory.decodeStream(it) }

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

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "ARViewModel destroyed!")
    }

    companion object {
        private val TAG: String = ARViewModel::class.java.simpleName
    }
}

