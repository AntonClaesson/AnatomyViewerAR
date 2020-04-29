package com.example.anatomyviewer.ar


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.ar.core.AugmentedImage
import com.google.ar.core.Frame
import javax.inject.Inject

class ImageTracker @Inject constructor() {

    var trackNewImages: Boolean = true

    private val _currentlyTrackedImage = MutableLiveData<AugmentedImage?>(null)
    var currentlyTrackedImage: LiveData<AugmentedImage?> = _currentlyTrackedImage

    fun onUpdate(frame: Frame){
        updateTrackedImageForFrame(frame)
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
            if (currentlyTrackedImage.value == augmentedImage) return

            // Disable tracking of new images until user input requests otherwise
            trackNewImages = false

            // Update tracked image
            _currentlyTrackedImage.value = augmentedImage
        }
    }

}