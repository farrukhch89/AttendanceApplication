package com.thecodefire.attendancemonitor

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.thecodefire.attendancemonitor.databinding.ActivityMarkMyAttendanceBinding
import kotlinx.coroutines.*
import java.util.*

class MarkMyAttendance : AppCompatActivity() {

    private lateinit var db : DatabaseReference
    private lateinit var binding: ActivityMarkMyAttendanceBinding
    lateinit var auth: FirebaseAuth
    lateinit var currentSubjectName: String
    private var currentUserCourse = ""
    private var subjectName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarkMyAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnMarkMe.isEnabled = false
        binding.courseSpinner.isEnabled = false
        auth = FirebaseAuth.getInstance()
        val db = FirebaseDatabase.getInstance()

        // select course
        db.getReference("course").child(auth.currentUser?.email!!.split("@")[0]).get().addOnSuccessListener {
            currentUserCourse = (it.value).toString()

            db.getReference("AttendanceAuthentication").addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot1: DataSnapshot) {
                    var bool = false
                    var clas = ""
                    for(i in snapshot1.children){
                        val course = (i.value).toString().split("_")[0]
                        val status = (i.value).toString().split("_")[2]
                        if(currentUserCourse==course){
                            bool = true
                            clas = i.value.toString()
                        }
                        if(status == "Active"){
                            break
                        }
                    }

                    if(bool){ // check for active attendance / open attendance
                        if(clas != "null"){
                            val course = clas.split("_")[0]
                            subjectName = clas.split("_")[1]
                            val status = clas.split("_")[2]
                            Log.d("MarkMyAttendance", course)
                            Log.d("MarkMyAttendance", subjectName)
                            Log.d("MarkMyAttendance", status)
                            Log.d("MarkMyAttendance", "current Course "+ currentUserCourse)
                            // check status  and subject
                            if(status == "Active" && course == currentUserCourse){
                                binding.tvStatus.text = "Active"
                                binding.tvStatus.setTextColor(Color.GREEN)
                                takeAttendance()
                                // if status in inactive
                            }else if(status == "InActive" || course!=currentUserCourse){
                                binding.tvStatus.text = "InActive"
                                binding.tvStatus.setTextColor(Color.RED)
                                binding.btnMarkMe.isEnabled = false
                                binding.courseSpinner.isEnabled = false
                                bool = true
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
    }


    // marking attendance
    private fun takeAttendance(){
        var courseSubjects = R.array.subject_cs
        val db = FirebaseDatabase.getInstance()
        db.getReference("course").child(auth.currentUser?.email!!.split("@")[0]).get().addOnSuccessListener {
            when((it.value).toString()){
                // subject selection after selecting course
                "Computer Science" -> courseSubjects = R.array.subject_cs
                "Computing Business" -> courseSubjects = R.array.subject_CB
                "Art" -> {
                    courseSubjects = R.array.subject_art
                }
                "Architecture" -> courseSubjects = R.array.subject_architecture
            }

            binding.courseSpinner.isEnabled = true
            val spinner: Spinner = binding.courseSpinner
            var ad = ArrayAdapter.createFromResource(
                this,
                courseSubjects,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }

            // on select subject
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    currentSubjectName = spinner.getItemAtPosition(p2).toString()
                    if(subjectName == currentSubjectName){
                        binding.tvSubjectStatus.text = currentSubjectName + " class is Active"
                        binding.tvSubjectStatus.setTextColor(Color.GREEN)
                        binding.btnMarkMe.isEnabled = true
                        var email = auth.currentUser?.email
                        email = email?.substringBefore("@")
                        binding.btnMarkMe.setOnClickListener {
                            val c = Calendar.getInstance()

                            // take date to save attendance
                            val year = c.get(Calendar.YEAR)
                            val month = c.get(Calendar.MONTH)
                            val day = c.get(Calendar.DAY_OF_MONTH)

                            val hour = c.get(Calendar.HOUR_OF_DAY)
                            val minute = c.get(Calendar.MINUTE)

                            val database = db.getReference("Attendance").child(currentUserCourse).child(currentSubjectName).child("$year-$month-$day-$hour")
                            database.child("testMail").setValue("test")

                            var studentAlreadyMarked = false
                            database.addValueEventListener(object : ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for(sp in snapshot.children){
                                        if(auth.currentUser?.email.toString().split("@")[0] == sp.key.toString()){
                                            studentAlreadyMarked = true
                                            break
                                        }
                                    }

                                    if(studentAlreadyMarked){ // present mark
                                        binding.tvSubjectStatus.append(" You're Marked As Present")
                                    }else{
                                        if (email != null) { // toast message output on completion
                                            database.child(email).setValue("Present").addOnSuccessListener {
                                                Toast.makeText(this@MarkMyAttendance, "SUCCESS", Toast.LENGTH_SHORT).show()
                                            }.addOnFailureListener {
                                                Toast.makeText(this@MarkMyAttendance, "FAILURE", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                            })
                            // mark me button inactive
                            binding.btnMarkMe.isClickable = false
                            binding.btnMarkMe.setTextColor(Color.GRAY)
                        }
                    }else{ // mark me button inactive
                        binding.btnMarkMe.isEnabled = false
                        binding.tvSubjectStatus.text = currentSubjectName + " class is InActive"
                        binding.tvSubjectStatus.setTextColor(Color.RED)
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }

            }
        }
    }
}
