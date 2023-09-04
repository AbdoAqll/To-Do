package com.example.to_do

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.to_do.base.BaseActivity
import com.example.to_do.data.DataManager
import com.example.to_do.data.TasksDBHelper
import com.example.to_do.data.domain.Task
import com.example.to_do.data.domain.TasksDB
import com.example.to_do.databinding.ActivityMainBinding
import com.example.to_do.ui.completed.CompletedFragment
import com.example.to_do.ui.get_started.GetStated
import com.example.to_do.ui.home.HomeFragment
import com.example.to_do.ui.important.ImportantFragment
import com.example.to_do.ui.late.LateFragment
import com.example.to_do.util.Case
import com.example.to_do.util.Constants
import com.example.to_do.util.Notification
import com.example.to_do.util.TaskAddedListener
import java.time.LocalDateTime
import java.time.ZoneId

class MainActivity : BaseActivity<ActivityMainBinding>() , TaskAddedListener{
    private lateinit var dbHelper: TasksDBHelper
    override val bindingInflationFunction: (LayoutInflater) -> ActivityMainBinding = ActivityMainBinding :: inflate

    private lateinit var permissionLauncher : ActivityResultLauncher<Array<String>>
    private var isNotificationPermissionGranted = false

    override fun setup() {
        dbHelper = TasksDBHelper(applicationContext)
        val window = this.window
        window.statusBarColor = resources.getColor(R.color.primary)

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permission ->
            isNotificationPermissionGranted = permission[Manifest.permission.POST_NOTIFICATIONS] ?: isNotificationPermissionGranted
        }
        requestPermission()

        val sharedPreferences = this.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME , Context.MODE_PRIVATE)
        val isFirstTime = sharedPreferences.getBoolean(Constants.IS_FIRST_TIME , true)
        if (isFirstTime) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(Constants.IS_FIRST_TIME, false) // Set to false after the first launch
            editor.apply()

            val intent = Intent(this, GetStated::class.java)
            startActivity(intent)
            finish() // Finish this activity so the user can't go back to it
        }
        else
        {
            initBaseFragment()
        }
        readData()
        createNotificationChannel()
    }

    private fun requestPermission()
    {
        isNotificationPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED

        val permissionRequest : MutableList<String> = ArrayList()

        if(!isNotificationPermissionGranted)
        {
            permissionRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (permissionRequest.isNotEmpty())
        {
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }
    }

    private fun scheduleNotification(task: Task) {
        val notificationId = task.hashCode()

        val intent = Intent(applicationContext, Notification::class.java)
        intent.putExtra("title", task.title)
        intent.putExtra("message", task.description)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Use System.currentTimeMillis() to calculate the time difference
        val currentTimeMillis = System.currentTimeMillis()
        val newTime = task.dueDate.minusMinutes(15)
        val dueDateTimeMillis = newTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        // Check if the due date is in the future
        if (dueDateTimeMillis > currentTimeMillis) {
            // Schedule the notification
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                dueDateTimeMillis,
                pendingIntent
            )
            Toast.makeText(this ,  "${task.title} has been scheduled" , Toast.LENGTH_SHORT).show()
        }
    }



    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "channel1"
            val channelName = "Channel 1"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    override fun addCallbacks() {
        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId)
            {
                R.id.main_page ->{
                    fragmentAddition(HomeFragment(this) ,Case.Replace )
                    true}
                R.id.Important_page ->{
                    fragmentAddition(ImportantFragment() ,Case.Replace )
                    true}
                R.id.missed_page ->{
                    fragmentAddition(LateFragment() ,Case.Replace )
                    true}
                R.id.completed_page ->{
                    fragmentAddition(CompletedFragment() ,Case.Replace )
                    true}

                else -> {false}
            }
        }
    }

    private fun initBaseFragment()
    {
        fragmentAddition(HomeFragment(this) , Case.Add)
    }

    private fun fragmentAddition(fragment : Fragment, case : Case)
    {
        val transaction = supportFragmentManager.beginTransaction()
        when(case)
        {
            Case.Add -> {transaction.add(binding.fragmentContainer.id , fragment)}
            Case.Replace -> {transaction.replace(binding.fragmentContainer.id , fragment)}
            Case.Remove -> {transaction.remove(fragment)}
        }
        transaction.commit()
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

    override fun onTaskAdded(task: Task) {
        scheduleNotification(task)
    }
}