package com.example.anatomyviewer.quiz

class Quiz(val questions: MutableList<Question>) {

    var quizFinished = false

    private var score: Int = 0
    private var time: String = ""

    private var currentQuestionIndex =  0

    fun getCurrentQuestion(): Question? {
        if (quizFinished) {
            return null
        }
        return questions[currentQuestionIndex]
    }

    fun nextQuestion(): Question? {
        if (currentQuestionIndex+1 == questions.count()) {
            quizFinished = true
            return null
        }
        currentQuestionIndex = currentQuestionIndex + 1
        return questions[currentQuestionIndex]
    }

    fun guessOption(guess: Int) {
        if (guess == getCurrentQuestion()?.correctOption) {
            score = score + 1
        }
    }

    fun getScore(): Int {
        return score
    }

    fun getTime(): String {
        return time
    }
}