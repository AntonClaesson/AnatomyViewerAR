package com.example.anatomyviewer.ar
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.google.ar.core.AugmentedImage
import com.google.ar.core.Frame
import javax.inject.Inject

class ArOrganizer @Inject constructor(
    private val lifecycleOwner: LifecycleOwner) {

    private enum class State{
        SEARCHING_IMAGE,
        FOUND_IMAGE
    }

    @Inject lateinit var imageTracker: ImageTracker
    @Inject lateinit var modelManager: ModelManager
    @Inject lateinit var quizManager: QuizManager

    private var state: State = State.SEARCHING_IMAGE

    fun start(){
        observeImageTracking()
    }

    fun onUpdate(frame: Frame){
        when(state) {
            State.SEARCHING_IMAGE -> {
                imageTracker.onUpdate(frame)
            }
            State.FOUND_IMAGE -> {
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
        modelManager.createModelForTrackedImage(trackedImage)
        quizManager.createQuizForImage(trackedImage)
        state = State.FOUND_IMAGE
    }
}