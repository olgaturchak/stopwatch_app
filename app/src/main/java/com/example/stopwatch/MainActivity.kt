package com.example.stopwatch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private var isRunning = false
    private var isResume = false

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (!isResume) {
                isResume = true
                isRunning = true
            }

            val message = intent?.getStringExtra(AppConstants.ACTIVITY_EXTRA_NAME)
            findViewById<TextView>(R.id.stopWatch).text = message
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val foregroundServiceIntent = Intent(this, MyService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                receiver,
                IntentFilter(AppConstants.ACTIVITY_RECEIVER_NAME),
                RECEIVER_NOT_EXPORTED
            )
        }

        findViewById<Button>(R.id.buttonStart).setOnClickListener {
            if (!isRunning) {
                startService(foregroundServiceIntent)
                isRunning = true
            }
        }

        findViewById<Button>(R.id.buttonStop).setOnClickListener {
            if (isRunning) {
                stopService(foregroundServiceIntent)
                isRunning = false
            }
        }

        val pauseBtn = findViewById<Button>(R.id.buttonPause)
        pauseBtn.setOnClickListener {
            if (isRunning) {
                MyService.isPause = !MyService.isPause

                when (MyService.isPause) {
                    true -> {
                        pauseBtn.text = "Resume"
                    }

                    false -> {
                        pauseBtn.text = "Pause"
                    }
                }

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        val foregroundServiceIntent = Intent(this, MyService::class.java)

        if(MyService.isPause) {
            stopService(foregroundServiceIntent)
        }
    }
}