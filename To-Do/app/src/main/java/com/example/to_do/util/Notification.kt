package com.example.to_do.util

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.to_do.MainActivity
import com.example.to_do.R

class Notification : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            val title = intent.getStringExtra("title")
            val message = intent.getStringExtra("message")

            if (title != null && message != null) {
                showNotification(context, title, message)
            }
        }
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val channelId = "channel1"
        val notificationId = 1
        val intent = Intent(context , MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId, // Use a unique request code if you have multiple notifications
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.app_icon)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.app_icon))
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .setWhen(System.currentTimeMillis())
            .setContentTitle("Remainder the task with title '$title' due date is after 15 minutes")
            .setContentText("Title: $title \nDescription: $message")
            .setContentIntent(pendingIntent) // Set the PendingIntent here
            .setAutoCancel(true) // Automatically dismiss the notification when clicked
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(notificationId, notification)
    }
}