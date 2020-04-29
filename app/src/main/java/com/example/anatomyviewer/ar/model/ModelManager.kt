package com.example.anatomyviewer.ar.model

import android.content.Context
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import com.example.anatomyviewer.R
import com.example.anatomyviewer.ar.ui.ArViewModel
import com.google.ar.core.AugmentedImage
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.Material
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.TransformationSystem
import javax.inject.Inject

class ModelManager @Inject constructor(
    private val viewModel: ArViewModel,
    private val arSceneView: ArSceneView,
    private val context: Context,
    private val transformationSystem: TransformationSystem) {

    private val TAG = ModelManager::class.java.toString()

    private var baseModel: BaseModel? = null
    private var defaultMaterials: HashMap<Int, Material> = hashMapOf()
    private var customMaterials: HashMap<Int, Material> = hashMapOf()

    private var explorationMode: Boolean = false

    init {
        createCustomMaterials()
    }

    fun startExplorationMode(){
        viewModel.setSettingsBtnVisibility(true)
    }

    private fun createCustomMaterials(){
        ModelRenderable.builder().setSource(context,
            R.raw.transparent
        ).build().thenAccept { renderable ->
            val transparentMaterial = renderable.material
            customMaterials[R.raw.transparent] = transparentMaterial
        }

        ModelRenderable.builder().setSource(context,
            R.raw.yellow_opaque
        ).build().thenAccept { renderable ->
            val yellowMaterial = renderable.material
            customMaterials[R.raw.yellow_opaque] = yellowMaterial
        }
    }

    // Adds the 3D model corresponding to the tracked image to the scene.
    fun createModelForTrackedImage(image: AugmentedImage?) {
        val trackedImage = image ?: return

        val id = trackedImage.name

        val newBaseModel = BaseModel()
        newBaseModel.modelID = R.raw.hand_bone // default fallback model

        when(id){
            //IMPORTANT: The skin models (or the "outmost/largest" model) should be the base model, since that makes it possible to move the entire node structure
            viewModel.IMAGE_1_NAME -> {newBaseModel.modelID = R.raw.hand_skin }
            viewModel.IMAGE_2_NAME -> {newBaseModel.modelID = R.raw.abdomen_skin }
        }

        buildModel(newBaseModel, trackedImage)

        baseModel = newBaseModel
    }


    private fun buildModel(baseModel: BaseModel, trackedImage: AugmentedImage){
        // First create the base model renderable
        ModelRenderable.builder().setSource(context, baseModel.modelID).build().thenAccept { renderable ->
            renderable.isShadowCaster = false
            renderable.isShadowReceiver = false

            //Save the models default material
            defaultMaterials[baseModel.modelID] = renderable.material

            // Create the anchor attached to the image
            val anchor = trackedImage.createAnchor(trackedImage.centerPose)
            baseModel.modelAnchorNode = AnchorNode(anchor).apply {
                setParent(arSceneView?.scene)
            }

            // Create the base model node and attach it to the model anchor node
            val baseNode = TransformableNode(this.transformationSystem)
            baseNode.setParent(baseModel.modelAnchorNode)

            baseNode.rotationController.isEnabled = true
            baseNode.scaleController.isEnabled = true
            baseNode.translationController.isEnabled = false
            baseNode.scaleController.maxScale = 2.0f
            baseNode.scaleController.minScale = 0.1f

            renderable.material = customMaterials[R.raw.transparent]

            baseModel.baseNode = baseNode
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

                    //Save the materials of the child models
                    defaultMaterials[model] = renderable.material

                    val childModelNode = Node()
                    childModelNode.renderable = renderable
                    childModelNode.setParent(baseNode)
                }
            }
        }
    }

    private fun addChildModelNamesTo(baseModel: BaseModel){
        when(baseModel.modelID){
            R.raw.hand_skin -> {
                baseModel.childModelIDs.add(R.raw.hand_bone)
            }
            R.raw.abdomen_skin -> {
                baseModel.childModelIDs.add(R.raw.abdomen_bone)
                baseModel.childModelIDs.add(R.raw.abdomen_heart)
                baseModel.childModelIDs.add(R.raw.abdomen_kidneys)
            }
        }

    }
}