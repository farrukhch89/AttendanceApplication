package com.thecodefire.attendancemonitor

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.util.ArrayList

class ManualAttendanceRVadapter(val date: String, val context: Context, val userList: ArrayList<DataSnapshot>):
    RecyclerView.Adapter<ManualAttendanceRVadapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.mark_attendance_custom_layout, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]
        holder.stuEmail.text = currentUser.key.toString()
//        if(currentUser.value.toString() == "Present" || currentUser.value.toString() == "Absent"){
//            holder.btnMark.text = "Mark As Absent"
//            holder.btnMark.setBackgroundColor(Color.RED)
//
//            holder.btnMark.setOnClickListener {
//                holder.btnMark.text = "Marked"
//                holder.btnMark.setBackgroundColor(Color.GREEN)
//
//                FirebaseDatabase.getInstance().getReference("tempData").get().addOnSuccessListener {
//                    FirebaseDatabase.getInstance().getReference("ManualAttendance").child(it.child("courseName").value.toString()).child(it.child("subjectName").value.toString()).child(it.child("date").value.toString()).child(currentUser.key.toString()).setValue("Absent")
//                }
//                Toast.makeText(context, "Marked :: Do not press again", Toast.LENGTH_LONG).show()
//                notifyDataSetChanged()
//            }
//        }else{

//        fun changeColor(){
//            holder.btnMark.text = "Marked"
//            holder.btnMark.setBackgroundColor(Color.RED)
//            if(holder.btnMark.text == "Marked"){
//                Log.d("ASD", "ASD")
//            }
//
//        }

        // button listener
            holder.btnMark.setOnClickListener {
//                changeColor()

                // mark present
                FirebaseDatabase.getInstance().getReference("tempData").get().addOnSuccessListener {
                    FirebaseDatabase.getInstance().getReference("ManualAttendance").child(it.child("courseName").value.toString()).child(it.child("subjectName").value.toString()).child(it.child("date").value.toString()).child(currentUser.key.toString()).setValue("Present")
                }
                Toast.makeText(context, "Marked :: Do not press again", Toast.LENGTH_LONG).show()
                notifyDataSetChanged()
            }
        holder.btnMark.text = "Mark As Present"
        holder.btnMark.setBackgroundColor(Color.GREEN)
//        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    // student email and attendance
    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val stuEmail = itemView.findViewById<TextView>(R.id.tvEmailCL)
        val btnMark = itemView.findViewById<Button>(R.id.btn_mark)
    }
}