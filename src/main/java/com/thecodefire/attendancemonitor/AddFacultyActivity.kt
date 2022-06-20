package com.thecodefire.attendancemonitor

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.thecodefire.attendancemonitor.databinding.ActivityAddfacultyBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception

class AddFacultyActivity: AppCompatActivity() {

    private lateinit var binding: ActivityAddfacultyBinding
    var courseName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddfacultyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // spinner for selecting course for lecturer
        val spinner: Spinner = binding.courseSpinner
        ArrayAdapter.createFromResource(
            this,
            R.array.course,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            adapter.setDropDownViewResource(R.layout.spinner_style)
            spinner.adapter = adapter
        }

        // Listening to data input from spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                courseName = spinner.getItemAtPosition(p2).toString()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        // submit button
        binding.submitBtn.setOnClickListener {
            if(checkFieldsNotEmpty()){
                if(binding.email.text.substring(0,4) != "lec_"){
                    showDialog()
                }else{
                    signUp() // sign up if correct information is entered
                }
            }else{
                Toast.makeText(this, "Please Fill All Required Fields", Toast.LENGTH_SHORT).show()
            }
        }

    }

    // form information
    private fun signUp(){
        val username = binding.email.text.toString()
        val password = binding.password.text.toString()

        // add user in database
        if(username.isNotEmpty() && password.isNotEmpty()){
            // coroutinescope is used as routine
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(username, password).addOnCompleteListener {
                        // clear textfield
                        binding.email.text.clear()
                        binding.firstname.text.clear()
                        binding.lastname.text.clear()
                        binding.address.text.clear()
                        binding.password.text.clear()
                    }.await()
                    withContext(Dispatchers.Main){
                        checkLoggedInState()
                    }
                }catch (e: Exception){
                    withContext(Dispatchers.Main){
                        Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // check if fields are empty
        }else{
            Toast.makeText(applicationContext, "username or password must not empty", Toast.LENGTH_SHORT).show()
        }

        // save data in database
        val db = FirebaseDatabase.getInstance().getReference("lecturer")
        db.child(binding.email.text.toString().substringBefore("@")).child("name").setValue(binding.firstname.text.toString() + " "+ binding.lastname.text.toString())
        db.child(binding.email.text.toString().substringBefore("@")).child("email").setValue(binding.email.text.toString())
        db.child(binding.email.text.toString().substringBefore("@")).child("address").setValue(binding.address.text.toString())
        db.child(binding.email.text.toString().substringBefore("@")).child("faculty").setValue(courseName)
        db.child(binding.email.text.toString().substringBefore("@")).child("password").setValue(binding.password.text.toString())
        FirebaseDatabase.getInstance().getReference("Lecturer").child(binding.email.text.toString().substringBefore("@")).setValue(courseName)
    }

    // check if registration is successful
    private fun checkLoggedInState(){
        if(FirebaseAuth.getInstance().currentUser == null){
            binding.tvLogInState.text = "FALIURE"
        }else{
            binding.tvLogInState.text = "REGISTRATION SUCCESS - YOU CAN LOGIN NOW"
            FirebaseAuth.getInstance().signOut()
        }
    }

    // toast Output if email is not according to requirement
    private fun showDialog(){
        var builder = AlertDialog.Builder(this)
        builder.setTitle("Alert")
        builder.setMessage("Please add \"lec_\" as prefix of your email address, to continue registration as Lecturer.")
        builder.setPositiveButton("I Understand"){
                dialogInterface, which ->
            Toast.makeText(applicationContext, "Enter correct details!", Toast.LENGTH_SHORT).show()
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    // check if all fills are filled in
    private fun checkFieldsNotEmpty(): Boolean{
        if(!(binding.firstname.text.toString().isNotEmpty() && binding.lastname.text.toString().isNotEmpty() && binding.password.text.toString()
                .isNotEmpty() && binding.address.text.toString().isNotEmpty()
                    && binding.email.text.toString().isNotEmpty() && courseName!="" && courseName!="Choose Course")){
            return false
        }
        return true
    }
}