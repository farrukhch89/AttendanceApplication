package com.thecodefire.attendancemonitor

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thecodefire.attendancemonitor.databinding.ActivityManuallyMarkAttendanceBinding
import com.thecodefire.attendancemonitori.LecturerViewRecyclerVIewAdapter
import java.lang.reflect.Array

class ManuallyMarkAttendance : AppCompatActivity() {
    private lateinit var binding: ActivityManuallyMarkAttendanceBinding
    private var courseName = ""
    private var subjectName = ""
    private var date = ""
    var dateArray = ArrayList<String>()

    private lateinit var attendanceRecyclerView: RecyclerView
    private lateinit var studentList: ArrayList<DataSnapshot>
    private lateinit var tempList: ArrayList<DataSnapshot>
    private lateinit var adapter: ManualAttendanceRVadapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManuallyMarkAttendanceBinding.inflate(layoutInflater)
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

                // subject spinner
                val subjectSpinner: Spinner = binding.subject
                ArrayAdapter.createFromResource(
                    applicationContext,
                    subjectArray,
                    android.R.layout.simple_spinner_item
                ).also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    subjectSpinner.adapter = adapter
                }

                // on select subject from spinner
                subjectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        subjectName = subjectSpinner.getItemAtPosition(p2).toString()

                        // get student from firebase with particular subject
                        FirebaseDatabase.getInstance().getReference("Attendance").child(courseName).child(subjectName).get().addOnSuccessListener {
                            dateArray.clear()
                            for(snapshot in it.children){
                                dateArray.add(snapshot.key.toString())
                            }

                            val dateSpinner: Spinner = binding.date

                            // select date of attendance needs to be changed
                            val aa = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, dateArray)
                            aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            dateSpinner.setAdapter(aa)

                            dateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                                    date = dateSpinner.getItemAtPosition(p2).toString()
                                }

                                override fun onNothingSelected(p0: AdapterView<*>?) {
                                    TODO("Not yet implemented")
                                }

                            }
                        }
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

        studentList = ArrayList()
        tempList = ArrayList()
        adapter = ManualAttendanceRVadapter(date, this, studentList)
        attendanceRecyclerView = binding.rvStuView
        attendanceRecyclerView.layoutManager = LinearLayoutManager(this)
        attendanceRecyclerView.adapter = adapter

        // on clicking the submit button
        binding.btnSubmit.setOnClickListener {
            FirebaseDatabase.getInstance().getReference("tempData").removeValue()
            FirebaseDatabase.getInstance().getReference("tempData").child("courseName").setValue(courseName)
            FirebaseDatabase.getInstance().getReference("tempData").child("subjectName").setValue(subjectName)
            FirebaseDatabase.getInstance().getReference("tempData").child("date").setValue(date)

            // add the student to list of attendance
            FirebaseDatabase.getInstance().getReference("Attendance").child(courseName).child(subjectName).child(date).addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    studentList.clear()
                    for(postSnapshot in snapshot.children){
                            val currentStudent = postSnapshot
                            studentList.add(currentStudent!!)
                            tempList.add(currentStudent!!)
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

            // add and remove student from lists
            FirebaseDatabase.getInstance().getReference(courseName).addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(postSnapshot in snapshot.children){
                        val currentStudent = postSnapshot
                        var stuMatch = false
                        for(sn in studentList){
                            if(postSnapshot.key == sn.key){
                                stuMatch = true
                            }
                        }
                        if(!stuMatch){
                            studentList.add(currentStudent!!)
                        }
                    }

                    for(i in tempList){
                        studentList.remove(i)
                    }

                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }

    }
}