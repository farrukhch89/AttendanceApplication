package com.thecodefire.attendancemonitor

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.thecodefire.attendancemonitor.databinding.ActivityDashboardBinding
import java.util.*

        // lecturer dashboard activity
class DashboardActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDashboardBinding
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // welcome message with lec_name
        binding.tvLecName.text = "WELCOME " + FirebaseAuth.getInstance().currentUser?.email.toString().split("@")[0].uppercase(
            Locale.getDefault()
        )

        // view Student button listener
        binding.viewStudentsBtn.setOnClickListener {
            val intent = Intent(this, ViewStudentActivity::class.java)
            startActivity(intent)
        }

        // manage attendance listenr button
        binding.manageAttendanceBtn.setOnClickListener {
            val intent = Intent(this, LecturerDashboard::class.java)
            startActivity(intent)
        }

        // logout
        binding.logoutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}