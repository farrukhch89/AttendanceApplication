package com.thecodefire.attendancemonitor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thecodefire.attendancemonitor.databinding.ActivityStudentAuthenticationBinding

class StudentAuthentication : AppCompatActivity() {

    private lateinit var binding: ActivityStudentAuthenticationBinding
    var courseName = ""
    private lateinit var studentRecyclerView: RecyclerView
    private lateinit var studentList: ArrayList<studentAuth>
    private lateinit var adapter: StudentAuthenticationRVAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentAuthenticationBinding.inflate(layoutInflater)
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

        studentList = ArrayList()
        adapter = StudentAuthenticationRVAdapter(this, studentList)
        studentRecyclerView = binding.rvStuView
        studentRecyclerView.layoutManager = LinearLayoutManager(this)
        studentRecyclerView.adapter = adapter

        // on submit button
        binding.btnSubmit.setOnClickListener {
            Toast.makeText(this, courseName, Toast.LENGTH_LONG).show()
            FirebaseDatabase.getInstance().getReference("StudentAuthentication").child(courseName).addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) { // add student to student list
                    studentList.clear()
                    for(postSnapshot in snapshot.children){
                        val currentStudent = postSnapshot.getValue(studentAuth::class.java)
                        studentList.add(currentStudent!!)
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