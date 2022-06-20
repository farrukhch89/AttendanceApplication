package com.thecodefire.attendancemonitor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.thecodefire.attendancemonitor.databinding.ActivityAdminBinding

class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // add faculty button listener
        binding.addFacultyBtn.setOnClickListener {
            val intent = Intent(this, AddFacultyActivity::class.java) // go to AddFacultyActivity page
            startActivity(intent)
        }

        // add student button listener
        binding.addStudentBtn.setOnClickListener {
            val intent = Intent(this, AddStudentActivity::class.java) // go to AddStudentActivity page
            startActivity(intent)
        }

        // view student button listener
        binding.viewStudentsBtn.setOnClickListener {
            val intent = Intent(this, ViewStudentActivity::class.java)// go to ViewStudentActivity page
            startActivity(intent)
        }

        // view faculty button listener
        binding.viewFacultyBtn.setOnClickListener {
            val intent = Intent(this, ViewFacultyActivity::class.java)// go to ViewFacultyActivity Page
            startActivity(intent)
        }

        // logout button listener
        binding.logoutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java) // logout and go to main page
            startActivity(intent)
            finish()
        }

        // student Authentication button listener
        binding.authStuBtn.setOnClickListener {
            val intent = Intent(this, StudentAuthentication::class.java)
            startActivity(intent)
        }
    }
}