package com.thecodefire.attendancemonitor

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.thecodefire.attendancemonitor.databinding.ActivityViewStudentBinding

class ViewStudentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewStudentBinding
    var courseName = ""
    private lateinit var studentRecyclerView: RecyclerView
    private lateinit var studentList: ArrayList<student>
    private lateinit var adapter: StudentViewForLecturerAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewStudentBinding.inflate(layoutInflater)
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

        studentList = ArrayList()
        adapter = StudentViewForLecturerAdapter(this, studentList)
        studentRecyclerView = binding.rvStuView
        studentRecyclerView.layoutManager = LinearLayoutManager(this)
        studentRecyclerView.adapter = adapter

        // bring list of student
        binding.btnSubmit.setOnClickListener {
            FirebaseDatabase.getInstance().getReference(courseName).addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    studentList.clear()
                    for(postSnapshot in snapshot.children){
                        val currentStudent = postSnapshot.getValue(student::class.java)
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
