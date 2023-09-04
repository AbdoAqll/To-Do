package com.example.to_do.ui.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.os.Build
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.to_do.MainActivity
import com.example.to_do.R
import com.example.to_do.base.BaseFragment
import com.example.to_do.data.DataManager
import com.example.to_do.data.TasksDBHelper
import com.example.to_do.data.domain.Task
import com.example.to_do.data.domain.TasksDB
import com.example.to_do.databinding.HomeFragmentBinding
import com.example.to_do.util.TaskAddedListener
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class HomeFragment(private val taskAddedListener: TaskAddedListener) : BaseFragment<HomeFragmentBinding>() , TaskInteractionInterface {

    lateinit var adapter: HomeAdapter
    private var tasks = DataManager.tasks
    override var bindingInflationFunction: (LayoutInflater, ViewGroup?, Boolean) -> HomeFragmentBinding = HomeFragmentBinding :: inflate
    private lateinit var dbHelper: TasksDBHelper


    override fun setup() {
        dbHelper = TasksDBHelper(requireContext())
        readData()
        adapter = HomeAdapter(tasks, this)
        binding.mainRecyclerView.adapter = adapter
        adapter.setData(DataManager.tasks)
    }

    override fun addCallBacks() {
        binding.floatingActionButton.setOnClickListener {
            showAlertDialog()
        }
    }

    private fun showAlertDialog() {
        val dialogView = LayoutInflater.from(this.context).inflate(R.layout.dialog_add_task, null)
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        var important : Int = 0
        var date = mutableListOf<String>()
        var time = mutableListOf<String>()


        // Set the custom view
        builder.setView(dialogView)

        val dialog = builder.create() // Create the dialog
        dialogView.findViewById<CheckBox>(R.id.important_checkbox).setOnCheckedChangeListener { compoundButton, isChecked ->
            important = if (isChecked)  1 else 0
        }
        dialogView.findViewById<Button>(R.id.pick_due_date).setOnClickListener {
            date.toMutableList()
            date = showDatePicker()
        }
        dialogView.findViewById<Button>(R.id.pick_due_time).setOnClickListener {
            time.toMutableList()
            time = showTimePicker()
        }


        // Set the click listeners for the buttons
        dialogView.findViewById<Button>(R.id.save).setOnClickListener {
            val title = dialogView.findViewById<EditText>(R.id.task_title)
            val description = dialogView.findViewById<EditText>(R.id.task_description)

            if (date.isEmpty() || time.isEmpty()) {
                Toast.makeText(requireContext(), "Please select both date and time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Don't proceed with task insertion
            }
            if (title.text.toString() == "")
            {
                Toast.makeText(requireContext(), "Please enter a title for the task", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Don't proceed with task insertion
            }

            var dueDateTime = createLocalDateTimeFromDateAndTime(date , time)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
            var dueDateTimeString = dueDateTime.format(formatter)
            val newEntry = ContentValues().apply {
                put(TasksDB.Title , title.text.toString())
                put(TasksDB.Description , description.text.toString())
                put(TasksDB.Completed , 0)
                put(TasksDB.Important , important)
                put(TasksDB.DueDate , dueDateTimeString)
            }
            dbHelper.writableDatabase.insert(TasksDB.Table_Name , null , newEntry)
            readData()
            tasks = DataManager.tasks
            adapter.setData(DataManager.tasks)
            Toast.makeText(requireContext(), "Task has been added", Toast.LENGTH_SHORT).show()
            taskAddedListener.onTaskAdded(Task(title.text.toString() , description.text.toString() , 0 , important , dueDateTime))

            dialog.dismiss() // Close the dialog
        }

        dialogView.findViewById<Button>(R.id.cancel).setOnClickListener {
            dialog.dismiss() // Close the dialog
        }

        val rootView = dialog.window?.decorView?.findViewById<View>(android.R.id.content)
        rootView?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary))

        dialog.show() // Show the dialog
    }

    private fun showDatePicker() : MutableList<String>
    {
        val now = Calendar.getInstance()
        var date = mutableListOf<String>()
        val datePicker = DatePickerDialog(requireContext() , DatePickerDialog.OnDateSetListener { picker, year, month, day ->
            date.add(year.toString())
            date.add(month.toString())
            date.add(day.toString())
        },
            now.get(Calendar.YEAR) , now.get(Calendar.MONTH) , now.get(Calendar.DAY_OF_MONTH))
        datePicker.show()
        return date
    }

    private fun showTimePicker() : MutableList<String>
    {
        val now = Calendar.getInstance()
        var time = mutableListOf<String>()
        val timePicker = TimePickerDialog(requireContext() ,
            TimePickerDialog.OnTimeSetListener { picker, hour, minute ->
            time.add(hour.toString())
            time.add(minute.toString())
        },
            now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE) , false)
        timePicker.show()
        return time
    }
    private fun createLocalDateTimeFromDateAndTime(date: MutableList<String>, time: MutableList<String>): LocalDateTime {
        val year = date[0].toInt()
        val month = date[1].toInt() + 1
        val day = date[2].toInt()
        val hour = time[0].toInt()
        val minute = time[1].toInt()

        return LocalDateTime.of(year, month, day, hour, minute)
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
                adapter.setData(DataManager.tasks)
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
            adapter.setData(DataManager.tasks)
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

        // Refresh the adapter or update the UI as needed
        adapter.setData(DataManager.tasks)
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
            Log.d("DateParse" , dueDate.toString())

            tasks.add(Task(title , description , completed , important , dueDate))
        }
        DataManager.setTasks(tasks)
    }

}