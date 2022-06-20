package com.thecodefire.attendancemonitor

import android.content.Context
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

class AttendanceViewRecyclerViewAdapter(val context: Context, val attendanceList: ArrayList<Attendance>): RecyclerView.Adapter<AttendanceViewRecyclerViewAdapter.AttendanceViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.attendance_view, parent, false)
        return AttendanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val currentAttendance = attendanceList[position]
        holder.date.text = currentAttendance.date
        holder.status.text = currentAttendance.status
    }

    override fun getItemCount(): Int {
        return attendanceList.size
    }

    class AttendanceViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val date = itemView.findViewById<TextView>(R.id.tvDate)
        val status = itemView.findViewById<TextView>(R.id.tvAttendance)
    }
}