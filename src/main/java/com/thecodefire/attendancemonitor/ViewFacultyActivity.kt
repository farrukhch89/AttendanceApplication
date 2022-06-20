package com.thecodefire.attendancemonitor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.thecodefire.attendancemonitor.databinding.ActivityViewFacultyBinding
import com.thecodefire.attendancemonitori.LecturerViewRecyclerVIewAdapter

class ViewFacultyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewFacultyBinding
    var courseName = ""
    private lateinit var lecturerRecyclerView: RecyclerView
    private lateinit var studentList: ArrayList<Lecturer>
    private lateinit var adapter: LecturerViewRecyclerVIewAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewFacultyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // course spinner
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

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()

        studentList = ArrayList() // variable for lecturer
        adapter = LecturerViewRecyclerVIewAdapter(this, studentList)
        lecturerRecyclerView = binding.rvLecView
        lecturerRecyclerView.layoutManager = LinearLayoutManager(this)
        lecturerRecyclerView.adapter = adapter

        // get all faculty / lecturer name and details
        binding.btnSubmit.setOnClickListener {
            FirebaseDatabase.getInstance().getReference("lecturer").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    studentList.clear()
                    for(postSnapshot in snapshot.children){
                        if(postSnapshot.child("faculty").value.toString() == courseName){
                            val currentStudent = postSnapshot.getValue(Lecturer::class.java)
                            studentList.add(currentStudent!!)
                        }
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