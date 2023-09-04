package com.example.to_do.ui.completed

import android.os.Build
import android.text.Editable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.example.to_do.R
import com.example.to_do.base.BaseFragment
import com.example.to_do.data.DataManager
import com.example.to_do.data.TasksDBHelper
import com.example.to_do.data.domain.Task
import com.example.to_do.data.domain.TasksDB
import com.example.to_do.databinding.CompletedFragmentBinding
import com.example.to_do.ui.home.HomeAdapter
import com.example.to_do.ui.home.TaskInteractionInterface
import java.time.LocalDateTime

class CompletedFragment : BaseFragment<CompletedFragmentBinding>() , TaskInteractionInterface {
    lateinit var adapter: HomeAdapter
    private var tasks = DataManager.getCompleted()
    override var bindingInflationFunction: (LayoutInflater, ViewGroup?, Boolean) -> CompletedFragmentBinding = CompletedFragmentBinding :: inflate
    private lateinit var dbHelper: TasksDBHelper

    override fun setup() {
        dbHelper = TasksDBHelper(requireContext())
        readData()
        adapter = HomeAdapter(tasks, this)
        binding.completedRecyclerView.adapter = adapter
        adapter.setData(DataManager.getCompleted())
    }

    override fun addCallBacks() {
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun readData()
    {
        val cursor = dbHelper.readableDatabase.rawQuery("SELECT ${TasksDB.Title}, ${TasksDB.Description},${TasksDB.Completed}, ${TasksDB.Important}, ${TasksDB.DueDate} FROM ${TasksDB.Table_Name}", arrayOf<String>())
        val tasks = mutableListOf<Task>()
        while (cursor.moveToNext())
        {
            val title = cursor.getString(0)
            val description = cursor.getString(1)
            val completed = cursor.getInt(2)
            val important = cursor.getInt(3)
            val dueDateText = cursor.getString(4)
            val dueDate = LocalDateTime.parse(dueDateText)

            tasks.add(Task(title , description , completed , important , dueDate))
        }
        DataManager.setTasks(tasks)
    }

    override fun deleteTask(task: Task) {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Yes") { _, _ ->
                DataManager.remove(task)

                // Delete from the database
                val db = dbHelper.writableDatabase
                val selection = "${TasksDB.Title} = ? AND ${TasksDB.Description} = ?"
                val selectionArgs = arrayOf(task.title, task.description)
                db.delete(TasksDB.Table_Name, selection, selectionArgs)

                // Refresh the adapter
                adapter.setData(DataManager.getCompleted())
            }
            .setNegativeButton("No", null)
            .create()

        alertDialog.show()
    }

    override fun editeTask(task: Task) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edite_task, null)
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())

        val titleEditText = dialogView.findViewById<EditText>(R.id.task_title)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.task_description)

        titleEditText.text = Editable.Factory.getInstance().newEditable(task.title)
        descriptionEditText.text = Editable.Factory.getInstance().newEditable(task.description)

        builder.setView(dialogView)
        val dialog = builder.create()

        dialogView.findViewById<Button>(R.id.save).setOnClickListener {
            val newTitle = titleEditText.text.toString()
            val newDescription = descriptionEditText.text.toString()

            // Update task in the database
            val db = dbHelper.writableDatabase
            val sql = "UPDATE ${TasksDB.Table_Name} SET ${TasksDB.Title} = '$newTitle', ${TasksDB.Description} = '$newDescription' WHERE ${TasksDB.Title} = '${task.title}' AND ${TasksDB.Description} = '${task.description}'"
            db.execSQL(sql)
            // Refresh the adapter
            readData()
            adapter.setData(DataManager.getCompleted())
            dialog.dismiss()
            Toast.makeText(requireContext(), "Task has been updated", Toast.LENGTH_SHORT).show()
        }

        dialogView.findViewById<Button>(R.id.cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun changeState(compoundButton: CompoundButton,isChecked: Int, task: Task) {
        val db = dbHelper.writableDatabase
        val sql = "UPDATE ${TasksDB.Table_Name} SET ${TasksDB.Completed} = $isChecked WHERE ${TasksDB.Title} = ? AND ${TasksDB.Description} = ?"
        val selectionArgs = arrayOf(task.title, task.description)
        db.execSQL(sql, selectionArgs)
        readData()
        // Refresh the adapter or update the UI as needed
        adapter.setData(DataManager.getCompleted())
    }
}