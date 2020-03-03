package com.example.anatomyviewer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.TransformableNode
import java.io.IOException
import java.lang.IllegalArgumentException

class ARViewModel: ViewModel() {

    // Enables or disables search of new trackable images
    var trackNewImages: Boolean = true

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

    init {
        Log.i(TAG, "ARViewModel created!")
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "ARViewModel destroyed!")
    }

    companion object {

        private val TAG: String = ARViewModel::class.java.simpleName

        private val IMAGE_1_NAME: String = "building.jpg"
        private val IMAGE_2_NAME: String = "earth.jpg"

    }
}