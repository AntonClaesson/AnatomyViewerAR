package com.example.anatomyviewer.ar

import androidx.lifecycle.ViewModel

class ARViewModel: ViewModel() {
    private val TAG: String = ARViewModel::class.java.toString()

    //Image data
    val IMAGE_1_NAME: String = "hand_bone.png"
    val IMAGE_2_NAME: String = "abdomen_no_skin.png"

    //Region LiveData (For future 2D ui)

    //endregion
}

