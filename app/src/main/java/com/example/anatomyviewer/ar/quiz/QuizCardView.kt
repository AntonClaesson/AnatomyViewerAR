package com.example.anatomyviewer.ar.quiz

import android.content.Context
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

    fun setQuizData(quizData: QuizData, lifecycleOwner: LifecycleOwner){
        binding.quizData = quizData
        binding.lifecycleOwner = lifecycleOwner
    }

}