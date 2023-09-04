package com.example.to_do.util

import com.example.to_do.data.domain.Task

interface TaskAddedListener {
    fun onTaskAdded(task: Task)
}