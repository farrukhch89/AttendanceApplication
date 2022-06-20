package com.thecodefire.attendancemonitor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.thecodefire.attendancemonitor.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // listener for registration
        binding?.userRegisterBtn?.setOnClickListener {
            var intent: Intent = Intent(this, RegistrationActivity::class.java) // head to registration page
            startActivity(intent)
        }

        // listener for user
        binding?.userLoginBtn?.setOnClickListener {
            var intent: Intent = Intent(this, LoginActivity::class.java) // login page
            startActivity(intent)
        }
    }
}