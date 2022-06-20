package com.thecodefire.attendancemonitor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import java.util.ArrayList

class StudentViewRecyclerViewAdapter(val context: Context, val userList: ArrayList<student>):
    RecyclerView.Adapter<StudentViewRecyclerViewAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.student_view_delete, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]
        holder.stuName.text = currentUser.name
        holder.stuCourse.text = currentUser.course
        holder.stuDOB.text = currentUser.dob
        holder.stuEmail.text = currentUser.email
        holder.stuAddress.text = currentUser.address
        // delete button
        holder.delStuBtn.setOnClickListener {
            currentUser.email?.let { it1 ->
                FirebaseDatabase.getInstance().getReference("course").child(
                    it1.substringBefore("@")).removeValue()
            }

            FirebaseDatabase.getInstance().getReference("StudentAuthentication").child(currentUser.course.toString()).child(currentUser.email.toString().substringBefore("@")).removeValue()
            FirebaseDatabase.getInstance().getReference(currentUser.course.toString()).child(currentUser.email.toString().substringBefore("@")).removeValue()
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val stuName    = itemView.findViewById<TextView>(R.id.tvStuName)
        val stuEmail   = itemView.findViewById<TextView>(R.id.tvEmail)
        val stuCourse  = itemView.findViewById<TextView>(R.id.tvCourse)
        val stuDOB     = itemView.findViewById<TextView>(R.id.tvDOB)
        val stuAddress = itemView.findViewById<TextView>(R.id.tvAddress)
        val delStuBtn = itemView.findViewById<Button>(R.id.btn_del_stu)
    }
}