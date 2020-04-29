package com.example.anatomyviewer.ar.ui

import android.content.Context
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import com.example.anatomyviewer.databinding.ArOverlayBinding

class ArOverlayView(context: Context) : ConstraintLayout(context) {

    private var binding: ArOverlayBinding

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = ArOverlayBinding.inflate(inflater, this, true)
    }

    fun setViewModel(arViewModel: ArViewModel, lifecycleOwner: LifecycleOwner) {
        binding.arViewModel = arViewModel
        binding.lifecycleOwner = lifecycleOwner
    }
}