package com.example.to_do.ui.home

import android.widget.CompoundButton
import com.example.to_do.data.domain.Task

interface TaskInteractionInterface {
    fun deleteTask(task: Task)
    fun editeTask(task: Task)
    fun changeState(compoundButton: CompoundButton, isChecked : Int,task: Task)
}