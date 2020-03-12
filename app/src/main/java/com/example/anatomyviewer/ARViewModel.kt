package com.example.anatomyviewer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.ar.core.*
import java.io.IOException
import java.lang.IllegalArgumentException

class ARViewModel: ViewModel() {

    val IMAGE_1_NAME: String = "building.jpg"
    val IMAGE_2_NAME: String = "earth.jpg"

    // Observers
    val trackedImageUpdated = MutableLiveData<Event<AugmentedImage?>>()

    // Instance variables
    var trackNewImages: Boolean = true
    var currentlyTrackedImage: AugmentedImage? = null
        set(value) {
            field = value
            trackedImageUpdated.value = Event(value)
        }


    fun reset(){
        currentlyTrackedImage = null
        trackNewImages = true
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


    init {
        Log.i(TAG, "ARViewModel created!")
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "ARViewModel destroyed!")
    }

    companion object {
        private val TAG: String = ARViewModel::class.java.simpleName
    }
}

