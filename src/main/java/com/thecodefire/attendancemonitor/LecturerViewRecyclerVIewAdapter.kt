package com.thecodefire.attendancemonitori

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thecodefire.attendancemonitor.Lecturer
import com.thecodefire.attendancemonitor.R

class LecturerViewRecyclerVIewAdapter(val context: Context, val lecturerList: ArrayList<Lecturer>): RecyclerView.Adapter<LecturerViewRecyclerVIewAdapter.lecturerViewHolder>() {
    // to view lecturer's  details
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): lecturerViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.faculty_view, parent, false)
        return lecturerViewHolder(view)
    }

    // put all detail of lecturer together
    override fun onBindViewHolder(holder: lecturerViewHolder, position: Int) {
        val currentUser = lecturerList[position]
        holder.lecName.text = currentUser.name
        holder.lecAddress.text = currentUser.address
        holder.lecCourse.text = currentUser.faculty
        holder.lecEmail.text = currentUser.email
        holder.delBtn.setOnClickListener {
        var password = ""
            FirebaseDatabase.getInstance().getReference("lecturer").child(currentUser.email.toString().substringBefore("@")).get().addOnSuccessListener{
            for(child in it.children){
                if(child.key.toString() == "password"){
                    password = child.value.toString()
                }
            }
                // delete lecturer
                FirebaseAuth.getInstance().signInWithEmailAndPassword(currentUser.email.toString(), password).addOnSuccessListener {
                    val credential: AuthCredential = EmailAuthProvider.getCredential(currentUser.email.toString(), password)
                    val user = FirebaseAuth.getInstance().currentUser
                    user?.reauthenticate(credential)?.addOnCompleteListener{
                        user.delete().addOnSuccessListener {
                            FirebaseDatabase.getInstance().getReference("lecturer").child(currentUser.email.toString().substringBefore("@")).removeValue()
                            lecturerList.removeAt(position)
                            notifyItemRemoved(position)
                            Toast.makeText(context, "DELETED", Toast.LENGTH_LONG).show()
                        }.addOnFailureListener {its->
                            Toast.makeText(context, its.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
                FirebaseAuth.getInstance().signOut()
                FirebaseAuth.getInstance().signInWithEmailAndPassword("admin_me@gmail.com", "0000")
        }
        }
    }

    override fun getItemCount(): Int {
        return lecturerList.size
    }

    class lecturerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val lecName = itemView.findViewById<TextView>(R.id.tvLecName)
        val lecEmail = itemView.findViewById<TextView>(R.id.ftvEmail)
        val lecAddress = itemView.findViewById<TextView>(R.id.ftvAddress)
        val lecCourse = itemView.findViewById<TextView>(R.id.ftvCourse)
        val delBtn = itemView.findViewById<Button>(R.id.del_btn)
    }
}