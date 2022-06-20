package com.thecodefire.attendancemonitor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.thecodefire.attendancemonitor.databinding.ActivityTakeAttendanceBinding
import java.util.*

class TakeAttendanceActivity : AppCompatActivity() {

    var START_MILLI_SECONDS = 5L * 60000L

    lateinit var countdown_timer: CountDownTimer
    var isRunning: Boolean = false;
    var time_in_milli_seconds = 5L * 60000L

    var courseName = ""
    var subjectName = ""
    private lateinit var binding: ActivityTakeAttendanceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTakeAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // spinner for course
        val spinner: Spinner = binding.course
        ArrayAdapter.createFromResource(
            this,
            R.array.course,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                courseName = spinner.getItemAtPosition(p2).toString()
                var subjectArray : Int =  R.array.subject_art

                when(courseName){
                    "Art" -> subjectArray = R.array.subject_art
                    "Computer Science" -> subjectArray = R.array.subject_cs
                    "Computing Business" -> subjectArray = R.array.subject_CB
                    "Architecture" -> subjectArray = R.array.subject_architecture
                }

                // spinner for subject
                val subjectSpinner: Spinner = binding.subject
                ArrayAdapter.createFromResource(
                    applicationContext,
                    subjectArray,
                    android.R.layout.simple_spinner_item
                ).also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    subjectSpinner.adapter = adapter
                }

                subjectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        subjectName = subjectSpinner.getItemAtPosition(p2).toString()
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        TODO("Not yet implemented")
                    }

                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        // head to method countThis Attendance
        binding.btnCta.setOnClickListener {
            countThisAttendance()
        }

        binding.btnStart.isEnabled = false
        binding.btnSubmit.setOnClickListener {
            binding.btnStart.isEnabled = true
            binding.btnUpa.isEnabled = true
        }

        // run timer for 5 min
        binding.btnStart.setOnClickListener {
            if (isRunning) {
                pauseTimer()
            } else {
                binding.btnCta.isEnabled = true
                binding.btnReset.visibility = View.VISIBLE
                val time  = 5
                time_in_milli_seconds = time.toLong() *60000L
                startTimer(time_in_milli_seconds)
            }
        }

        // reset button
        binding.btnReset.setOnClickListener {
            resetTimer()
        }
        //update attendance
        // course, subject and date as file name to save attendance
        binding.btnUpa.setOnClickListener {
            FirebaseDatabase.getInstance().getReference("ManualAttendance").child(courseName).child(subjectName).get().addOnSuccessListener {   date->
                Log.d("ComputerScience", courseName)
                Log.d("Database", subjectName)
                Log.d("Date", "DATE: "+date.value.toString().substringBefore("=").replace("{", ""))
                FirebaseDatabase.getInstance().getReference("ManualAttendance").child(courseName).child(subjectName).child(date.value.toString().substringBefore("=").replace("{", "")).get().addOnSuccessListener {
                    for(snap in it.children){
                        if(snap.value.toString() == "Present"){ // put everyone presents' name in this file
                            FirebaseDatabase.getInstance().getReference("Attendance").child(courseName).child(subjectName).child(date.value.toString().substringBefore("=").replace("{", "")).child(snap.key.toString()).setValue(snap.value.toString())
                        }
                    }
                }
            }
        }
    }

    override fun onPause() { // pause timer for attendance, attendance become inactive
        FirebaseDatabase.getInstance().getReference("AttendanceAuthentication").child(FirebaseAuth.getInstance().currentUser?.email.toString().substringBefore("@")).setValue(courseName+"_"+subjectName+"_"+"InActive")
        super.onPause()
    }

    override fun onStop() { // put status as inactive for attendance
        FirebaseDatabase.getInstance().getReference("AttendanceAuthentication").child(FirebaseAuth.getInstance().currentUser?.email.toString().substringBefore("@")).setValue(courseName+"_"+subjectName+"_"+"InActive")
        FirebaseDatabase.getInstance().getReference("AttendanceCount").child(courseName).child(subjectName).child("total").get().addOnSuccessListener {
            FirebaseDatabase.getInstance().getReference("AttendanceCount").child(courseName).child(subjectName).child("total").setValue((it.value.toString().toInt() + 1).toString())
        }
        super.onStop()
    }

    private fun countThisAttendance(){ // increase attendance count
        FirebaseDatabase.getInstance().getReference("AttendanceCount").child(courseName).child(subjectName).child("total").get().addOnSuccessListener {
            FirebaseDatabase.getInstance().getReference("AttendanceCount").child(courseName).child(subjectName).child("total").setValue((it.value.toString().toInt() + 1).toString())
        }
    }

    private fun pauseTimer() { // pause timer
        FirebaseDatabase.getInstance().getReference("AttendanceAuthentication").child(FirebaseAuth.getInstance().currentUser?.email.toString().substringBefore("@")).setValue(courseName+"_"+subjectName+"_"+"InActive")
        binding.btnStart.text = "Start" // start attendande timer
        countdown_timer.cancel()
        isRunning = false
        binding.btnReset.visibility = View.VISIBLE
    }

    private fun startTimer(time_in_seconds: Long) { // timer
        FirebaseDatabase.getInstance().getReference("AttendanceAuthentication").child(FirebaseAuth.getInstance().currentUser?.email.toString().substringBefore("@")).setValue(courseName+"_"+subjectName+"_"+"Active")

        countdown_timer = object : CountDownTimer(time_in_seconds, 1000) {
            override fun onFinish() {
//                loadConfeti()
            }

            override fun onTick(p0: Long) {
                time_in_milli_seconds = p0
                updateTextUI()
            }
        }
        countdown_timer.start()

        isRunning = true
        binding.btnStart.text = "Pause"
        binding.btnReset.visibility = View.INVISIBLE

    }

    private fun resetTimer() { // reset timer
        FirebaseDatabase.getInstance().getReference("AttendanceAuthentication").child(FirebaseAuth.getInstance().currentUser?.email.toString().substringBefore("@")).setValue(courseName+"_"+subjectName+"_"+"InActive")
        time_in_milli_seconds = START_MILLI_SECONDS
        updateTextUI()
        binding.btnReset.visibility = View.INVISIBLE
    }

    private fun updateTextUI() { // time count down update
        val minute = (time_in_milli_seconds / 1000) / 60
        val seconds = (time_in_milli_seconds / 1000) % 60
        binding.tvTimer.text = "$minute:$seconds"
    }
}