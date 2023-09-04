package com.example.to_do.data

import com.example.to_do.data.domain.Task
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date

object DataManager {

    private var tasksList = mutableListOf<Task>()
    val tasks : List<Task>
        get() = tasksList.toMutableList()

    fun remove(task: Task)
    {
        tasksList.remove(task)
    }
    fun add(task: Task)
    {
        tasksList.add(task)
    }
    fun setTasks(tasks : MutableList<Task>)
    {
        tasksList = tasks
    }
    fun editeTask(task: Task , title : String , description : String)
    {
        task.title = title
        task.description = description
    }
    fun getImportant() : MutableList<Task>
    {
        var important = mutableListOf<Task>()
        for (task in tasksList)
        {
            if (task.important == 1)
            {
                important.add(task)
            }
        }
        return important
    }

    fun getCompleted() : MutableList<Task>
    {
        var completed = mutableListOf<Task>()
        for (task in tasksList)
        {
            if (task.completed == 1)
            {
                completed.add(task)
            }
        }
        return completed
    }
    fun getLate(): MutableList<Task> {
        val late = mutableListOf<Task>()
        val currentTime = LocalDateTime.now()

        for (task in tasksList) {
            if (task.dueDate.isBefore(currentTime) && task.completed == 0) {
                late.add(task)
            }
        }
        return late
    }
}