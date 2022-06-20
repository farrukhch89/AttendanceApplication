package com.thecodefire.attendancemonitor

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.set
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.thecodefire.attendancemonitor.databinding.ActivityAddstudentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.*

class AddStudentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddstudentBinding
    var courseName : String = ""
    private lateinit var db: DatabaseReference
    private val auth = Firebase.auth
    private var button_date: Button? = null
    private var textview_date: TextView? = null
    private var cal = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddstudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get the references from layout file
        textview_date = binding.textViewDate1
        button_date = binding.buttonDate1

        textview_date!!.text = "--/--/----"

        // create an OnDateSetListener
        val dateSetListener = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int,
                                   dayOfMonth: Int) {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            }
        }

        // when you click on the button, show DatePickerDialog that is set with OnDateSetListener
        button_date!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                DatePickerDialog(this@AddStudentActivity,
                    dateSetListener,
                    // set DatePickerDialog to point to today's date when it loads up
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            }
        })

        // course spinner
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

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                courseName = spinner.getItemAtPosition(p2).toString()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
        // on submit listener
        binding.btnSubmit.setOnClickListener {
            if(!checkFieldsNotEmpty()){
                Toast.makeText(this, "Please Fill All Required Fields", Toast.LENGTH_LONG).show()
            }else if(binding.email.text.toString().substring(0,4) != "stu_"){
                showDialog("stu_")
            }else{
                signUp()
            }
        }
    }

    // check if all fields are completed
    private fun checkFieldsNotEmpty(): Boolean{
        if(!(binding.firstname.text.toString().isNotEmpty() && binding.lastname.text.toString().isNotEmpty() && binding.etPass.text.toString()
                .isNotEmpty() && binding.textViewDate1.text.toString().isNotEmpty() && binding.address.text.toString().isNotEmpty()
                    && binding.email.text.toString().isNotEmpty() && courseName!="" && courseName!="Choose Course")){
            return false
        }
        return true
    }

    private fun signUp(){
        // date of birth should be greater than 16
        val y = binding.textViewDate1.text.toString().split("/")[2].toInt()
        val calc = Calendar.getInstance()
        val year = calc.get(Calendar.YEAR)
        Log.d("DOB", (year-y).toString())
        Log.d("DOB", (year).toString())
        Log.d("DOB", (y).toString())
        if((year-y) < 16){
            Toast.makeText(applicationContext, "DOB must be 16+ to register", Toast.LENGTH_LONG).show()
        }else{
            // taking username and password as string
            val username = binding.email.text.toString()
            val password = binding.etPass.text.toString()

            if(username.isNotEmpty() && password.isNotEmpty()){
                CoroutineScope(Dispatchers.IO).launch {
                    try {//check if its registered before
                        auth.createUserWithEmailAndPassword(username, password).addOnCompleteListener {
                            binding.email.text.clear()
                            binding.firstname.text.clear()
                            binding.lastname.text.clear()
                            binding.textViewDate1.text = ""
                            binding.address.text.clear()
                            binding.etPass.text.clear()

                        }.await()
                        withContext(Dispatchers.Main){
                            checkLoggedInState()
                        }
                    }catch (e: Exception){ // error message
                        withContext(Dispatchers.Main){
                            Toast.makeText(this@AddStudentActivity, e.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }else{ // check for empty field
                Toast.makeText(this@AddStudentActivity, "username or password must not empty", Toast.LENGTH_SHORT).show()
            }

            if(username.substring(0,4)=="stu_"){
                db = FirebaseDatabase.getInstance().getReference(courseName)
                db.child(binding.email.text.toString().split("@")[0]).setValue(student(binding.firstname.text.toString() + " " + binding.lastname.text.toString(), binding.email.text.toString(),
                    binding.address.text.toString(), courseName, binding.textViewDate1.text.toString()))

                FirebaseDatabase.getInstance().getReference("StudentAuthentication").child(courseName).child(binding.email.text.toString().substringBefore("@")).child("stuEmail").setValue(binding.email.text.toString())
                FirebaseDatabase.getInstance().getReference("StudentAuthentication").child(courseName).child(binding.email.text.toString().substringBefore("@")).child("stuName").setValue(binding.firstname.text.toString() + " " + binding.lastname.text.toString())
                FirebaseDatabase.getInstance().getReference("StudentAuthentication").child(courseName).child(binding.email.text.toString().substringBefore("@")).child("stuStatus").setValue("Active")

                FirebaseDatabase.getInstance().getReference("course").child(binding.email.text.toString().substringBefore("@")).setValue(courseName)
            }
        }
    }
    // check for correct format for email
    private fun showDialog(prefix: String){
        var temp = "";
        if(prefix=="stu_"){
            temp = "Student"
        }else{
            temp = "Lecturer"
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alert")
        builder.setMessage("Please add \"$prefix\" as prefix of your email address, to continue registration as $temp.")
        builder.setPositiveButton("I Understand"){
                dialogInterface, which ->
            Toast.makeText(applicationContext, "Enter correct details!", Toast.LENGTH_SHORT).show()
            dialogInterface.dismiss()
        }
        // alert dialog box created
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
    // register date when there s a change
    private fun updateDateInView() {
        val myFormat = "MM/dd/yyyy" // mention the format you need
        val sdf = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat(myFormat, Locale.US)
        } else {
            TODO("VERSION.SDK_INT < N")
        }
        textview_date!!.text = sdf.format(cal.getTime())
    }

    // check login state
    private fun checkLoggedInState(){
        if(auth.currentUser == null){
            binding.tvLogInState.text = "REGISTRATION UNSUCCESSFUL"
        }else{
            binding.tvLogInState.text = "REGISTRATION SUCCESS"
            auth.signOut()
        }
    }
}
