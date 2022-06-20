package com.thecodefire.attendancemonitor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import kotlin.math.roundToInt
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.thecodefire.attendancemonitor.databinding.ActivityViewattendanceBinding
import kotlinx.coroutines.flow.asFlow
import kotlin.math.log

class ViewAttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewattendanceBinding
    private lateinit var subjectName: String

    private lateinit var attendanceRecyclerView: RecyclerView
    private lateinit var attendanceList: ArrayList<Attendance>
    private lateinit var adapter: AttendanceViewRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewattendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // course spinner
        var courseName = intent.getStringExtra("CURRENTSTUDENTCOURSENAME").toString()
        var subjectArray : Int =  R.array.subject_art
        when(courseName){
            "Art" -> subjectArray = R.array.subject_art
            "Computer Science" -> subjectArray = R.array.subject_cs
            "Computing Business" -> subjectArray = R.array.subject_CB
            "Architecture" -> subjectArray = R.array.subject_architecture
        }

        // subject spinner
        val subjectSpinner: Spinner = binding.spinSub
        ArrayAdapter.createFromResource(
            applicationContext,
            subjectArray,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            subjectSpinner.adapter = adapter
        }

        attendanceList = ArrayList()
        adapter = AttendanceViewRecyclerViewAdapter(this, attendanceList)
        attendanceRecyclerView = binding.rvAttView
        attendanceRecyclerView.layoutManager = LinearLayoutManager(this)
        attendanceRecyclerView.adapter = adapter

        // on select subject
        subjectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                subjectName = subjectSpinner.getItemAtPosition(p2).toString()
                attendanceList.clear()
                adapter.notifyDataSetChanged()
                FirebaseDatabase.getInstance().getReference("Attendance").child(courseName).child(subjectName).addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var attendanceCount = 0.0F
                        for(child in snapshot.children){ // get all attendance
                            for(i in child.children){
                                if(i.key.toString() == FirebaseAuth.getInstance().currentUser?.email.toString().substringBefore("@")){
                                    attendanceCount++ // increase attendance count
                                    Log.d("VAA", child.key.toString())
                                    Log.d("VAA", i.value.toString())
                                    attendanceList.add(Attendance(child.key.toString(), i.value.toString()))
                                    adapter.notifyDataSetChanged()
                                }
                            }
                        }

                        // percentage attendance
                        FirebaseDatabase.getInstance().getReference("AttendanceCount").child(courseName).child(subjectName).child("total").get().addOnSuccessListener {
                            if(it.value.toString() != "null"){
                                var totalDays = it.value.toString().toFloat()
                                var perc = ((attendanceCount/totalDays)*100).toString() // prcentage attendance
                                val roundoff = perc.toFloat().roundToInt()
                                val roundoffS = roundoff.toString()
                                Log.d("Farrukh", roundoffS)
                                Log.d("Farrukh", totalDays.toString())
                                Log.d("Farrukh", perc.toString())
                                binding.attendenceTv.text = roundoffS + "%"
                            }else{
                                Toast.makeText(applicationContext, "No Attendance Data Found regarding to this student or subject.", Toast.LENGTH_LONG).show()
                            }

                        }

                        FirebaseDatabase.getInstance().getReference("Attendance").child(courseName).get().addOnSuccessListener {
                            var count = 0F
                            for(snap in it.children){
                                for(snap2 in snap.children){
                                    for(snap3 in snap2.children){
                                        if(snap3.key.toString() == FirebaseAuth.getInstance().currentUser?.email.toString().substringBefore("@")){
                                            count++
                                        }
                                    }
                                }
                            }

                            Log.d("Farrukh", count.toString())

                            FirebaseDatabase.getInstance().getReference("AttendanceCount").child(courseName).get().addOnSuccessListener {
                                var total = 0F
                                for(snap in it.children){
                                    for(snap2 in snap.children){
                                        total += snap2.value.toString().toInt()
                                    }
                                }

                                var perc = (count/total)*100
                                binding.overallAttendanceTv.text = perc.toString()+"%"
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

    }
}
