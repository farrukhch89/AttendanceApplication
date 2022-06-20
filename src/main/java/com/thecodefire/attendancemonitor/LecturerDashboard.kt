package com.thecodefire.attendancemonitor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.thecodefire.attendancemonitor.databinding.ActivityLecturerDashboardBinding

class LecturerDashboard : AppCompatActivity() {

    private lateinit var binding: ActivityLecturerDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLecturerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // take attendance button listener
        binding.btnTakeAttendance.setOnClickListener {
            val intent = Intent(this, TakeAttendanceActivity::class.java) // go to TakeAttendanceActivity
            startActivity(intent)
        }

        // download attendance button listener
        binding.btnDownloadAttendance.setOnClickListener {
            val intent = Intent(this, DownloadAttendanceActivity::class.java) // go to DownloadAttendanceActivity
            startActivity(intent)
        }

        // manual attendance button listener
        binding.btnManualAttendance.setOnClickListener {
            val intent = Intent(this, ManuallyMarkAttendance::class.java) //go to manuallyMarkAttendance
            startActivity(intent)
        }
    }
}