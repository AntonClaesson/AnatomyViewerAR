package com.example.anatomyviewer.ar
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.example.anatomyviewer.ar.helpers.UiEvent
import com.google.ar.core.AugmentedImage
import com.google.ar.core.Frame
import javax.inject.Inject

class ArOrganizer @Inject constructor(
    private val lifecycleOwner: LifecycleOwner,
    private val context: Context,
    private val viewModel: ArViewModel) {

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
            State.SEARCHING_IMAGE -> {
                imageTracker.onUpdate(frame)
            }
            State.QUIZ_ACTIVE -> {
                quizManager.onUpdate(frame)
            }
        }
    }

    private fun observeImageTracking(){
        imageTracker.currentlyTrackedImage.observe(lifecycleOwner, Observer {image ->
            val trackedImage = image ?: return@Observer
            handleTrackedImageFound(trackedImage)
        })
    }

    private fun handleTrackedImageFound(trackedImage: AugmentedImage){
        state = State.EXPLORING_MODEL
        modelManager.createModelForTrackedImage(trackedImage)

        viewModel.setStartQuizBtnVisibility(true)

        Toast.makeText(context, "Explore model", Toast.LENGTH_LONG).show()

        viewModel.uiEvents.observe(lifecycleOwner, Observer {
            if(it == UiEvent.START_QUIZ_BUTTON_CLICKED) {
                viewModel.setStartQuizBtnVisibility(false)
                beginQuizForImage(trackedImage)
            }
        })
    }

    private fun beginQuizForImage(trackedImage: AugmentedImage){
        quizManager.createQuizForImage(trackedImage)
        state = State.QUIZ_ACTIVE
    }
}