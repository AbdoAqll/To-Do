package com.example.to_do.data.domain

import java.time.LocalDateTime

data class Task(
    var title : String,
    var description : String,
    var completed : Int ,
    var important : Int,
    var dueDate: LocalDateTime
)
