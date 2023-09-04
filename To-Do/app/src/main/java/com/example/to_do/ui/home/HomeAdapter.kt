package com.example.to_do.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.to_do.R
import com.example.to_do.data.domain.Task
import com.example.to_do.databinding.ItemTaskBinding
import com.example.to_do.util.TaskDiffUtil
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HomeAdapter(private var list : List<Task>, private val interactionInterface: TaskInteractionInterface) : RecyclerView.Adapter<HomeAdapter.TaskHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task , parent , false)
        return TaskHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: TaskHolder, position: Int) {
        var currentTask = list[position]
        val currentTime = LocalDateTime.now()

        val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy, h:mm a")
        val dueDateFormatted = currentTask.dueDate.format(formatter)

        holder.binding.apply {
            taskCompletedCheckbox.text = currentTask.title
            taskCompletedCheckbox.isChecked = currentTask.completed == 1
            taskCompletedCheckbox.paint.isStrikeThruText = currentTask.completed == 1
            taskDescription.text = currentTask.description
            if(currentTask.important == 1)
            {
                importantLottie.visibility = View.VISIBLE
            }
            else
            {
                importantLottie.visibility = View.GONE
            }
            val currentTime = LocalDateTime.now()
            if (currentTask.dueDate.isBefore(currentTime) && currentTask.completed == 0) {
                missedLottie.visibility = View.VISIBLE
                taskCompletedCheckbox.isEnabled = false
            }
            else
            {
                missedLottie.visibility = View.INVISIBLE

            }
            dueDateTime.text = dueDateFormatted
            deleteButton.setOnClickListener { interactionInterface.deleteTask(currentTask) }
            root.setOnClickListener { interactionInterface.editeTask(currentTask) }
            taskCompletedCheckbox.setOnCheckedChangeListener { compoundButton, isChecked ->
                val newState = if (isChecked) 1 else 0
                taskCompletedCheckbox.paint.isStrikeThruText = isChecked
                interactionInterface.changeState(compoundButton,newState ,currentTask)
            }
        }
    }

    fun setData(newList : List<Task>)
    {
        val diffResult = DiffUtil.calculateDiff(TaskDiffUtil(list , newList))
        list = newList
        diffResult.dispatchUpdatesTo(this)
    }

    class TaskHolder(view : View): RecyclerView.ViewHolder(view){
        val binding = ItemTaskBinding.bind(view)
    }
}