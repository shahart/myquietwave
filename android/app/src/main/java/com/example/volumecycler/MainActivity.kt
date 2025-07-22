package com.example.volumecycler

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var isServiceRunning = false
    private lateinit var statusText: TextView
    private lateinit var toggleButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        toggleButton = findViewById(R.id.toggleButton)

        toggleButton.setOnClickListener {
            if (isServiceRunning) {
                stopService(Intent(this, VolumeCycleService::class.java))
                statusText.text = "Service stopped"
                toggleButton.text = "Start"
            } else {
                startForegroundService(Intent(this, VolumeCycleService::class.java))
                statusText.text = "Service running"
                toggleButton.text = "Stop"
            }
            isServiceRunning = !isServiceRunning
        }
    }
} 