package com.example.anatomyviewer.ar.quiz

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.example.anatomyviewer.ar.ui.ArViewModel
import com.google.ar.core.AugmentedImage
import com.google.ar.core.Frame
import com.google.ar.core.Pose
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.DpToMetersViewSizer
import com.google.ar.sceneform.rendering.ViewRenderable
import javax.inject.Inject

class QuizManager @Inject constructor(
    private val viewModel: ArViewModel,
    private val arSceneView: ArSceneView,
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    private val TAG = QuizManager::class.java.toString()

    private var quizCardNode: Node? = null
    val quizData: QuizData = QuizData()

    fun onUpdate(frame: Frame){
        updateCardNodeForFrame(frame, quizCardNode)
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

    fun createQuizForImage(image: AugmentedImage) {
        val id = image.name
        when (id) {
            viewModel.IMAGE_1_NAME -> { QuizData.QuizType.MODEL1 }
            viewModel.IMAGE_2_NAME -> { QuizData.QuizType.MODEL2 }
            else -> { null }
        }?.let {
            quizData.makeNewQuiz(it)
        }

        val quizNode = Node()
        quizNode.setParent(arSceneView.scene)
        quizNode.worldPosition = Vector3(0f,-0f,-0f)

        //If camera pos available instead place quiz in front of camera
        arSceneView.arFrame?.camera?.let {
            val translationPose = Pose.makeTranslation(0f, 0f,-2f)
            val quizPose = it.pose.compose(translationPose).extractTranslation()
            quizNode.worldPosition= Vector3(quizPose.tx(), -1f ,quizPose.tz())
        }


        val quizCardView = QuizCardView(context)
        quizCardView.setQuizData(quizData, lifecycleOwner)

        val dpm = 350 //Default 250 dpm
        ViewRenderable.builder().setView(context, quizCardView).build()
            .thenAccept { viewRenderable ->
                viewRenderable.setSizer { DpToMetersViewSizer(dpm).getSize(viewRenderable.view) }

                viewRenderable.isShadowCaster = false
                viewRenderable.isShadowReceiver = false

                quizNode.renderable = viewRenderable
                quizCardNode = quizNode
            }
    }

}