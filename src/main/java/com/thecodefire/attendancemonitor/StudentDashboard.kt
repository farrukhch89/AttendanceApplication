package com.thecodefire.attendancemonitor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.thecodefire.attendancemonitor.databinding.ActivityStudentDashboardBinding

class StudentDashboard : AppCompatActivity() {

    private var currentUserCourse: String = ""
    private lateinit var binding: ActivityStudentDashboardBinding
    lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        binding.btnLogOut.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        // listener for mark attendance button
        binding.btnMarkAttendance.setOnClickListener {
            val intent = Intent(this, MarkMyAttendance::class.java)// page MarkMyAttendance
            startActivity(intent)
        }

        // listener for view attendance button
        binding.btnViewAttendance.setOnClickListener {
            val intent = Intent(this, ViewAttendanceActivity::class.java) // page viewAttendanceActivity
            if(currentUserCourse == ""){
                Toast.makeText(this, "Please wait until your information appears!", Toast.LENGTH_LONG).show()
            }else{
                intent.putExtra("CURRENTSTUDENTCOURSENAME", currentUserCourse)
                startActivity(intent)
            }
        }

        val userName = FirebaseAuth.getInstance().currentUser?.email.toString().substringBefore("@")

        // get student details
        FirebaseDatabase.getInstance().getReference("course").child(userName).get().addOnSuccessListener { it ->
            currentUserCourse = it.value.toString()
            FirebaseDatabase.getInstance().getReference(currentUserCourse).child(userName).get().addOnSuccessListener {
                var stuInfoArray : ArrayList<String> = arrayListOf()
                for(i in it.children){
                    stuInfoArray.add(i.value.toString())
                }
                binding.layoutStv.tvAddress.text = stuInfoArray[0]
                binding.layoutStv.tvCourse.text = stuInfoArray[1]
                binding.layoutStv.tvDOB.text = stuInfoArray[2]
                binding.layoutStv.tvEmail.text = stuInfoArray[3]
                binding.layoutStv.tvStuName.text = stuInfoArray[4]
            }
        }
    }
}