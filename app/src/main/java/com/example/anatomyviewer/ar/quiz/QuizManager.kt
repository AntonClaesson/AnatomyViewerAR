package com.example.anatomyviewer.ar.quiz

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.example.anatomyviewer.ar.ui.ArViewModel
import com.google.ar.core.AugmentedImage
import com.google.ar.core.Frame
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

    private var quizCardNode: Node? = null
    private val quizData: QuizData = QuizData()

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

    fun createQuizForImage(image: AugmentedImage?) {
        val id = image?.name ?: return
        when (id) {
            viewModel.IMAGE_1_NAME -> { QuizData.QuizType.MODEL1 }
            viewModel.IMAGE_2_NAME -> { QuizData.QuizType.MODEL2 }
            else -> { null }
        }?.let {
            quizData.makeNewQuiz(it)
        }

        val quizNode = Node()
        quizNode.setParent(arSceneView.scene)
        quizNode.localPosition = Vector3(0f,-1f,-2f)

        val quizCardView = QuizCardView(context)
        quizCardView.setQuizData(quizData, lifecycleOwner)

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

}