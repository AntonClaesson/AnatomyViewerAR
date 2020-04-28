package com.example.anatomyviewer.ar.quiz

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

    fun getCurrentQuestionIndex(): Int {
        if (quizFinished) {
            return -1
        }
        return currentQuestionIndex
    }

    fun nextQuestion() {
        if (currentQuestionIndex+1 == questions.count()) {
            quizFinished = true
        }
        currentQuestionIndex = currentQuestionIndex + 1
    }

    fun guessOption(guess: Int, question: Question): Boolean {
        return guess == question.correctOption
    }

    fun addScore(value: Int){
        score = score + value
    }

    fun getScore(): Int {
        return score
    }

    fun getTime(): String {
        return time
    }
}