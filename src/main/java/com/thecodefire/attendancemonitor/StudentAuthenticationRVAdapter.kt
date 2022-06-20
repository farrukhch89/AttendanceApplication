package com.thecodefire.attendancemonitor

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import java.util.ArrayList

class StudentAuthenticationRVAdapter(val context: Context, val stuList: ArrayList<studentAuth>):
    RecyclerView.Adapter<StudentAuthenticationRVAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.student_authentication_cl, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentStudent = stuList[position]

        holder.stuName.text = currentStudent.stuName
        holder.stuEmail.text = currentStudent.stuEmail

        if(currentStudent.stuStatus == "Active"){
            holder.statusBtn.text = "DeActivate"
            holder.statusBtn.setOnClickListener {
                FirebaseDatabase.getInstance().getReference("course").child(currentStudent.stuEmail!!.substringBefore("@")).get().addOnSuccessListener {
                    FirebaseDatabase.getInstance().getReference("StudentAuthentication").child(it.value.toString()).child(currentStudent.stuEmail.toString().substringBefore("@")).child("stuStatus").setValue("inActive")
                }
            }
        }else{
            holder.statusBtn.text = "Activate"
            holder.statusBtn.setOnClickListener {
                FirebaseDatabase.getInstance().getReference("course").child(currentStudent.stuEmail!!.substringBefore("@")).get().addOnSuccessListener {
                    FirebaseDatabase.getInstance().getReference("StudentAuthentication").child(it.value.toString()).child(currentStudent.stuEmail.toString().substringBefore("@")).child("stuStatus").setValue("Active")
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return stuList.size
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val stuName = itemView.findViewById<TextView>(R.id.tvStuAuthName)
        val stuEmail = itemView.findViewById<TextView>(R.id.tvStuAuthEMail)
        val statusBtn = itemView.findViewById<Button>(R.id.btn_activate)
    }
}