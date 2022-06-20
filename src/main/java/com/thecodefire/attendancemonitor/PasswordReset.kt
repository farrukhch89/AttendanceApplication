package com.thecodefire.attendancemonitor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.thecodefire.attendancemonitor.databinding.ActivityPasswordResetBinding

class PasswordReset : AppCompatActivity() {
    private lateinit var binding: ActivityPasswordResetBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordResetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.passwordResetBtn.setOnClickListener {
            Toast.makeText(this, "This Function is implemented but not working because of not use of proper gmail ID's", Toast.LENGTH_LONG).show()
//            FirebaseAuth.getInstance().sendPasswordResetEmail(binding.username.text.toString()).addOnSuccessListener {
//                Toast.makeText(this, "Password Reset Mail Send Successfully...", Toast.LENGTH_LONG).show()
//            }
        }
    }
}