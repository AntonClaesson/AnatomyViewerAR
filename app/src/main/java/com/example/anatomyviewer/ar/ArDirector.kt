package com.example.anatomyviewer.ar
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.example.anatomyviewer.ar.helpers.UiEvent
import com.example.anatomyviewer.ar.imagetracking.ImageTracker
import com.example.anatomyviewer.ar.model.ModelManager
import com.example.anatomyviewer.ar.quiz.QuizManager
import com.example.anatomyviewer.ar.ui.ArViewModel
import com.google.ar.core.AugmentedImage
import com.google.ar.core.Frame
import javax.inject.Inject

class ArDirector @Inject constructor(
    private val lifecycleOwner: LifecycleOwner,
    private val context: Context,
    private val viewModel: ArViewModel
) {

    private enum class State{
        SEARCHING_IMAGE,
        EXPLORING_MODEL,
        QUIZ_ACTIVE
    }

    @Inject lateinit var imageTracker: ImageTracker
    @Inject lateinit var modelManager: ModelManager
    @Inject lateinit var quizManager: QuizManager

    private var state: State = State.SEARCHING_IMAGE

    fun start(){
        observeImageTracking()
        Toast.makeText(context, "Search image", Toast.LENGTH_LONG).show()
    }

    fun onUpdate(frame: Frame){
        when(state) {
            State.SEARCHING_IMAGE -> { imageTracker.onUpdate(frame) }
            State.EXPLORING_MODEL -> { }
            State.QUIZ_ACTIVE -> { quizManager.onUpdate(frame) }
        }
    }

    private fun observeImageTracking(){
        imageTracker.currentlyTrackedImage.observe(lifecycleOwner, Observer {image ->
            val trackedImage = image ?: return@Observer
            startExploringModelForTrackedImage(trackedImage)
        })
    }

    private fun startExploringModelForTrackedImage(trackedImage: AugmentedImage){
        state = State.EXPLORING_MODEL
        // UI
        viewModel.setStartQuizBtnVisibility(true)
        Toast.makeText(context, "Explore model", Toast.LENGTH_LONG).show()

        // Create model
        modelManager.createModelForTrackedImage(trackedImage)
        modelManager.startExplorationMode()

        // Observe when to start quiz
        viewModel.uiEvents.observe(lifecycleOwner, Observer {
            if(it == UiEvent.START_QUIZ_BUTTON_CLICKED) {
                viewModel.setStartQuizBtnVisibility(false)
                startQuizForImage(trackedImage)
            }
        })
    }

    private fun startQuizForImage(trackedImage: AugmentedImage){
        quizManager.createQuizForImage(trackedImage)
        state = State.QUIZ_ACTIVE
    }
}