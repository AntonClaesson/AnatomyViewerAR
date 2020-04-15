package com.example.anatomyviewer.quiz

data class Question(
    val questionText: String,
    val opt1Text: String,
    val opt2Text: String,
    val opt3Text: String,
    val correctOption: Int
) {}