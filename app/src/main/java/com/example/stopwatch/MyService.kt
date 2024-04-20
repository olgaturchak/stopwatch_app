package com.example.stopwatch

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import java.util.concurrent.Executors

class MyService : Service() {

    companion object {
        var isPause = false
        var isDestroyed = false
        var time = 0
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()
        startForeground(AppConstants.NOTIFICATION_ID, showNotification("Start timer ..."))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this, "Starting service...", Toast.LENGTH_SHORT).show()

        isDestroyed = false
        startStopwatch()

        return super.onStartCommand(intent, flags, startId)
    }

    private fun startStopwatch() {
        val executorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executorService.execute {
            while (!isDestroyed) {
                Thread.sleep(100)

                if (!isPause) {
                    time += 100
                }

                if (time % 1000 == 0 && !isPause) {
                    val formatTime = formatMillis(time)

                    val intent = Intent(AppConstants.ACTIVITY_RECEIVER_NAME)
                    intent.putExtra(AppConstants.ACTIVITY_EXTRA_NAME, formatTime)
                    sendBroadcast(intent)

                    handler.post {
                        updateNotification(formatTime)
                    }
                }
            }
        }
    }

    private fun updateNotification(formatTime: String) {
        val notification: Notification = showNotification(formatTime)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(AppConstants.NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()

        val intent = Intent(AppConstants.ACTIVITY_RECEIVER_NAME)
        intent.putExtra(AppConstants.ACTIVITY_EXTRA_NAME, "00:00:00")
        sendBroadcast(intent)

        isDestroyed = true
        isPause = false
        time = 0

        Toast.makeText(this, "Stopping service...", Toast.LENGTH_SHORT).show()
    }

    private fun showNotification(content: String): Notification {
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            NotificationChannel(
                AppConstants.CHANNEL_ID,
                "Service",
                NotificationManager.IMPORTANCE_HIGH
            )
        )

        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, AppConstants.CHANNEL_ID)
            .setContentTitle("Stopwatch")
            .setContentText(content)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun formatMillis(mills: Int): String {
        val hours = mills / (1000 * 60 * 60)
        val minutes = (mills % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (mills % (1000 * 60)) / 1000

        return String.format(
            "%02d:%02d:%02d",
            hours,
            minutes,
            seconds
        )
    }
}