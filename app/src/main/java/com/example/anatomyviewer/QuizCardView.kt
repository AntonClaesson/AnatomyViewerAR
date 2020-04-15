package com.example.anatomyviewer

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.lifecycle.LifecycleOwner
import com.example.anatomyviewer.databinding.ViewQuizCardBinding

class QuizCardView(context: Context): RelativeLayout(context) {

    private var TAG = QuizCardView::class.java.toString()

    private var binding: ViewQuizCardBinding

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = ViewQuizCardBinding.inflate(inflater, this, true)
    }

    fun setViewModel(arvm: ARViewModel, lifecycleOwner: LifecycleOwner){
        Log.d(TAG, "YESSS WORKS")
        binding.viewModel = arvm
        binding.lifecycleOwner = lifecycleOwner
    }

}