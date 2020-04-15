package com.example.anatomyviewer.quiz

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class QuizData {

    private val TAG = QuizData::class.java.toString()

    enum class QuizType {
        MODEL1, MODEL2, MODEL3, MODEL4
    }

    private var activeQuiz: Quiz? = null

    private var _scoreLabelTxt = MutableLiveData<String>("")
    var scoreLabelText: LiveData<String> = _scoreLabelTxt

    private var _questionNumbText = MutableLiveData<String>("")
    var questionNumbText: LiveData<String> = _questionNumbText

    private var _timeText = MutableLiveData<String>("")
    var timeText: LiveData<String> = _timeText

    private var _questionText = MutableLiveData<String>("")
    var questionText: LiveData<String> = _questionText

    private var _opt1Text = MutableLiveData<String>("")
    var opt1Text: LiveData<String> = _opt1Text

    private var _opt2Text = MutableLiveData<String>("")
    var opt2Text: LiveData<String> = _opt2Text

    private var _opt3Text = MutableLiveData<String>("")
    var opt3Text: LiveData<String> = _opt3Text


    fun makeNewQuiz(quizType: QuizType) {
        activeQuiz = when (quizType) {
            QuizType.MODEL1 -> { createQuiz1() }
            QuizType.MODEL2 -> { createQuiz2() }
            QuizType.MODEL3 -> {null}
            QuizType.MODEL4 -> {null}
        }

        activeQuiz?.let {
           updateQuestionLabelsForQuiz(it)
        }
    }

    fun clickedOption1() {
        Log.d(TAG,"Clicked option 1")
    }

    fun clickedOption2() {
        Log.d(TAG,"Clicked option 2")
    }

    fun clickedOption3() {
        Log.d(TAG,"Clicked option 3")
    }

    fun clickedConfirmed(){
        activeQuiz?.let {
            //Update the question
            it.nextQuestion()
            updateQuestionLabelsForQuiz(it)
        }

    }

    private fun updateQuestionLabelsForQuiz(it: Quiz){
        updateScoreLabel()
        _questionNumbText.value = "Question: ${it.getCurrentQuestionIndex()+1}/${it.questions.count()}"
        _timeText.value = "00:00"

        _questionText.value = it.getCurrentQuestion()?.questionText
        _opt1Text.value = it.getCurrentQuestion()?.opt1Text
        _opt2Text.value = it.getCurrentQuestion()?.opt2Text
        _opt3Text.value = it.getCurrentQuestion()?.opt3Text
    }

    private fun updateScoreLabel(){
        activeQuiz?.let {
            _scoreLabelTxt.value = "Score: ${it.getScore()}"
        }
    }

    private fun createQuiz1(): Quiz {
        val q1 = Question(
            questionText = "What is the kneekap?",
            opt1Text = "opt1",
            opt2Text = "opt2",
            opt3Text = "opt3",
            correctOption = 1
        )

        val q2 = Question(
            questionText = "Where is the elbow found?",
            opt1Text = "1",
            opt2Text = "2",
            opt3Text = "3",
            correctOption = 2
        )

        val questions = mutableListOf<Question>()
        questions.add(q1)
        questions.add(q2)

        return Quiz(questions)
    }

    private fun createQuiz2(): Quiz {
        val q1 = Question(
            questionText = "Question 1",
            opt1Text = "opt1",
            opt2Text = "opt2",
            opt3Text = "opt3",
            correctOption = 1
        )

        val q2 = Question(
            questionText = "Question 2",
            opt1Text = "opt1",
            opt2Text = "opt2",
            opt3Text = "opt3",
            correctOption = 2
        )

        val questions = mutableListOf<Question>()
        questions.add(q1)
        questions.add(q2)

        return Quiz(questions)
    }
}