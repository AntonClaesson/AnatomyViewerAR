package com.example.anatomyviewer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.ar.sceneform.ux.ArFragment
import android.util.Log
import com.google.ar.sceneform.FrameTime

private val TAG: String = MainActivity::class.java.simpleName

class MainActivity : AppCompatActivity() {


    lateinit var arFragment: ArFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "Hello World")

        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment

        //Hides the scan floor gif
        arFragment.planeDiscoveryController.hide()
        arFragment.planeDiscoveryController.setInstructionView(null)

        //Hides the plane renderering
        arFragment.arSceneView.planeRenderer.isVisible = false

        //Called whenever user taps an AR plane
        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            Log.d(TAG, "Tapped")

        }
        //Sets the frame update listener
        arFragment.arSceneView.scene.addOnUpdateListener(::onUpdate)
    }


    /// Called once per frame right before Scene is updated.
    private fun onUpdate(frameTime: FrameTime){

    }

}
