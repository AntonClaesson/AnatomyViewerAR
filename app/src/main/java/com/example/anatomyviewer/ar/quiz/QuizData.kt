package com.example.anatomyviewer.ar.quiz

import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.anatomyviewer.R

class QuizData: ViewModel() {

    private val TAG = QuizData::class.java.toString()

    enum class QuizType {
        MODEL1, MODEL2
    }

    private var activeQuiz: Quiz? = null

    private val _scoreLabelTxt = MutableLiveData<String>("")
    val scoreLabelText: LiveData<String> = _scoreLabelTxt

    private val _questionNumbText = MutableLiveData<String>("")
    val questionNumbText: LiveData<String> = _questionNumbText

    private val _timeText = MutableLiveData<String>("")
    val timeText: LiveData<String> = _timeText

    private val _questionText = MutableLiveData<String>("")
    val questionText: LiveData<String> = _questionText

    private val _opt1Text = MutableLiveData<String>("")
    val opt1Text: LiveData<String> = _opt1Text

    private val _opt2Text = MutableLiveData<String>("")
    val opt2Text: LiveData<String> = _opt2Text

    private val _opt3Text = MutableLiveData<String>("")
    val opt3Text: LiveData<String> = _opt3Text

    private val _selectedOption = MutableLiveData<Int>(null)
    val selectedOption: LiveData<Int> = _selectedOption

    private val _modelToHighlight = MutableLiveData<Int?>(null)
    val modelToHighLight: LiveData<Int?> = _modelToHighlight

    private val _clearCheck = MutableLiveData(false)
    val clearCheck: LiveData<Boolean> = _clearCheck

    fun makeNewQuiz(quizType: QuizType) {
        activeQuiz = when (quizType) {
            QuizType.MODEL1 -> { createQuiz1() }
            QuizType.MODEL2 -> { createQuiz2() }
        }

        activeQuiz?.let {
           updateLabelsForQuiz(it)
        }
    }



    fun clickedConfirmed(){
        activeQuiz?.let { it ->
            clearChecked()

            // Check the answer and update score
            val question = it.getCurrentQuestion() ?: return
            val guess = selectedOption.value ?: return

            if (it.guessOption(guess, question)) {
                it.addScore(1)
                //TODO: Show some indicator that it was correct
            } else {
                //TODO: Show some indicator that it was wrong
            }
            //Update the question and the labels
            it.nextQuestion()

            //Highlight model part if needed
            it.getCurrentQuestion()?.let { q ->
                if (q.highLightModel != null) {
                    _modelToHighlight.value = q.highLightModel
                } else {
                    _modelToHighlight.value = null
                }
            }

            updateLabelsForQuiz(it)
        }

    }

    fun opt1Clicked(){
        activeQuiz?.let {
            _selectedOption.value = 1
        }
    }

    fun opt2Clicked(){
        activeQuiz?.let {
            _selectedOption.value = 2
        }
    }

    fun opt3Clicked(){
        activeQuiz?.let {
            _selectedOption.value = 3
        }
    }

    private fun clearChecked(){
        _clearCheck.value = true
    }

    private fun updateLabelsForQuiz(it: Quiz){
        updateScoreLabel()

        if (activeQuiz?.quizFinished == true) {

        } else {
            _questionNumbText.value = "Question: ${it.getCurrentQuestionIndex()+1}/${it.questions.count()}"
            _timeText.value = "00:00"

            _questionText.value = it.getCurrentQuestion()?.questionText
            _opt1Text.value = it.getCurrentQuestion()?.opt1Text
            _opt2Text.value = it.getCurrentQuestion()?.opt2Text
            _opt3Text.value = it.getCurrentQuestion()?.opt3Text
        }
    }

    private fun updateScoreLabel(){
        activeQuiz?.let {
            _scoreLabelTxt.value = "Score: ${it.getScore()}"
        }
    }

    private fun createQuiz1(): Quiz {
        val q1 = Question(
            questionText = "How many sections of bone does the thumb have?",
            opt1Text = "2",
            opt2Text = "3",
            opt3Text = "4",
            correctOption = 3
        )

        val q2 = Question(
            questionText = "How many bones are connected to the hand from the arm?",
            opt1Text = "1",
            opt2Text = "2",
            opt3Text = "4",
            correctOption = 2
        )

        val q3 = Question(
            questionText = "How many fingers are there?",
            opt1Text = "4",
            opt2Text = "5",
            opt3Text = "6",
            correctOption = 2
        )

        val questions = mutableListOf<Question>()
        questions.add(q1)
        questions.add(q2)
        questions.add(q3)

        return Quiz(questions)
    }

    private fun createQuiz2(): Quiz {
        val q1 = Question(
            questionText = "Which organs are visible in the model?",
            opt1Text = "Brain and lungs",
            opt2Text = "Heart and liver",
            opt3Text = "Heart and kidneys",
            correctOption = 3
        )

        val q2 = Question(
            questionText = "What is the highlighted organ called?",
            opt1Text = "Kidneys",
            opt2Text = "Heart",
            opt3Text = "Bladder",
            correctOption = 1,
            highLightModel = R.raw.abdomen_kidneys
        )

        val q3 = Question(
            questionText = "What is the purpose of the rib cage?",
            opt1Text = "To make the skeleton cooler",
            opt2Text = "Protecting vital organs",
            opt3Text = "It has no use",
            correctOption = 2
        )

        val q4 = Question(
            questionText = "What is the name of the highlighted organ?",
            opt1Text = "Heart",
            opt2Text = "Stomach",
            opt3Text = "Lungs",
            correctOption = 1,
            highLightModel = R.raw.abdomen_heart
        )

        val questions = mutableListOf<Question>()
        questions.add(q1)
        questions.add(q2)
        questions.add(q3)
        questions.add(q4)

        return Quiz(questions)
    }
}