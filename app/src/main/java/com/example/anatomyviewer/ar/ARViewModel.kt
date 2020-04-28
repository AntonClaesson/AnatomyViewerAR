package com.example.anatomyviewer.ar

import androidx.lifecycle.ViewModel

class ARViewModel: ViewModel() {
    private val TAG: String = ARViewModel::class.java.toString()

    //Image data
    val IMAGE_1_NAME: String = "queen_of_diamonds.jpg"
    val IMAGE_2_NAME: String = "ten_of_spades.jpg"

    //Region LiveData (For future 2D ui)

    //endregion
}

