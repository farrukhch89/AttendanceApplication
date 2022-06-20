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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.thecodefire.attendancemonitor.databinding.ActivityRegistrationBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

// student registration
class RegistrationActivity : AppCompatActivity() {

    var button_date: Button? = null
    var textview_date: TextView? = null
    var cal = Calendar.getInstance()

    lateinit var auth: FirebaseAuth
    var courseName : String = ""
    private var binding: ActivityRegistrationBinding? = null
    private lateinit var db: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        textview_date = binding?.textViewDate1
        button_date = binding?.buttonDate1

        textview_date!!.text = "--/--/----"

        // date of birth listener
        val dateSetListener = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int,
                                   dayOfMonth: Int) {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            }
        }
        // date of birth format
        button_date!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                DatePickerDialog(this@RegistrationActivity,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            }

        })

        // spinner for course
        val spinner: Spinner = findViewById(R.id.course_spinner)
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

        auth = FirebaseAuth.getInstance()

        /*binding?.lecturerRegBtn?.setOnClickListener {
            lecRegistration()
        }*/
        binding?.stuRegBtn?.setOnClickListener {
            stuRegistration()
        }
    }

    // date of birth
    private fun updateDateInView() {
        val myFormat = "MM/dd/yyyy"
        val sdf = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat(myFormat, Locale.US)
        } else {
            TODO("VERSION.SDK_INT < N")
        }
        textview_date!!.text = sdf.format(cal.getTime())
    }

    // lecturer registartion manually
    private fun lecRegistration(){
        binding?.stuRegBtn?.visibility = View.GONE

        binding?.etEmail?.visibility = View.VISIBLE
        binding?.etPassword?.visibility = View.VISIBLE
        binding?.btnSignUp?.visibility = View.VISIBLE
        binding?.btnSignUp?.text = "Request Access"


        binding?.btnSignUp?.setOnClickListener {
            val username = binding?.etEmail?.text
            if(!(username?.substring(0, 4) == "lec_")){
                showDialog("lec_")
            }else{
                requestNewLecturerLogin()
            }
        }
    }

    //  request send for lecturer login
    private fun requestNewLecturerLogin(){
        val username = binding?.etEmail?.text.toString()
        val password = binding?.etPassword?.text.toString()
        if(username.isNotEmpty() && password.isNotEmpty()){
            FirebaseDatabase.getInstance().getReference("NewLecturerAccessRequest").child(username.substringBefore("@")).child("email").setValue(username)
            FirebaseDatabase.getInstance().getReference("NewLecturerAccessRequest").child(username.substringBefore("@")).child("password").setValue(password)
            binding?.tvLogInState?.text = "New Request Sent Successfully, Please Wait for Response!"
        }else{
            Toast.makeText(this@RegistrationActivity, "username or password must not empty", Toast.LENGTH_SHORT).show()
        }
    }

    // registering student
    private fun stuRegistration(){
        /*binding?.lecturerRegBtn?.visibility = View.GONE*/

        binding?.etEmail?.visibility = View.VISIBLE
        binding?.etPassword?.visibility = View.VISIBLE
        binding?.btnSignUp?.visibility = View.VISIBLE
        binding?.courseSpinner?.visibility = View.VISIBLE
        binding?.etFName?.visibility = View.VISIBLE
        binding?.etLName?.visibility = View.VISIBLE
        binding?.DOB?.visibility = View.VISIBLE
        binding?.etAddress?.visibility = View.VISIBLE

        binding?.btnSignUp?.text = "Request New Registration"

        binding?.btnSignUp?.setOnClickListener {
            val username = binding?.etEmail?.text
            if(!checkFeildsNotEmpty()){
                Toast.makeText(this, "Please Fill All Required Fields", Toast.LENGTH_SHORT).show()
            }else if(username?.substring(0, 4) != "stu_"){
            showDialog("stu_")
        } else{
                signUp()
            }
        }
    }

    // all field are completed
    private fun checkFeildsNotEmpty(): Boolean{
        if(!(binding?.etFName?.text?.toString()?.isEmpty() != true && binding?.etLName?.text?.toString()?.isEmpty() != true && binding?.textViewDate1?.text?.toString()?.isEmpty() != true && courseName != "" && courseName != "Choose Course" && binding?.etEmail?.text?.toString()?.isEmpty() != true && binding?.etPassword?.text?.toString()?.isEmpty() != true)){
            return false
        }
        return true
    }

    // prefix for each user are  present?
    private fun showDialog(prefix: String){
        var temp = "";
        if(prefix=="stu_"){
            temp = "Student"
        }else{
            temp = "Lecturer"
        }
        var builder = AlertDialog.Builder(this)
        builder.setTitle("Alert")
        builder.setMessage("Please add \"$prefix\" as prefix of your email address, to continue registration as $temp.")
        builder.setPositiveButton("I Understand"){
                dialogInterface, which ->
            Toast.makeText(applicationContext, "Enter correct details!", Toast.LENGTH_SHORT).show()
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    // sign up
    private fun signUp(){
        val y = binding?.textViewDate1?.text.toString().split("/")[2].toInt()
        val calc = Calendar.getInstance()
        val year = calc.get(Calendar.YEAR)
        Log.d("DOB", (year-y).toString())
        Log.d("DOB", (year).toString())
        Log.d("DOB", (y).toString())
        // age should be more than 16
        if((year-y) < 16){
            Toast.makeText(applicationContext, "DOB must be 16+ to register", Toast.LENGTH_LONG).show()
        }else{
            val username = binding?.etEmail?.text.toString()
            val password = binding?.etPassword?.text.toString()
            // check for username and password
            if(username.isNotEmpty() && password.isNotEmpty()){
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        auth.createUserWithEmailAndPassword(username, password).addOnCompleteListener {
                            binding?.etEmail?.text?.clear()
                            binding?.etFName?.text?.clear()
                            binding?.etLName?.text?.clear()
                            binding?.textViewDate1?.text = ""
                            binding?.etAddress?.text?.clear()
                            binding?.etPassword?.text?.clear()
                        }.await()
                        withContext(Dispatchers.Main){
                            checkLoggedInState()
                        }
                    }catch (e: Exception){
                        withContext(Dispatchers.Main){
                            Toast.makeText(this@RegistrationActivity, e.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }else{ // empty field error
                Toast.makeText(this@RegistrationActivity, "username or password must not empty", Toast.LENGTH_SHORT).show()
            }
            // save information to go for approval
            if(username.substring(0,4)=="stu_"){
                db = FirebaseDatabase.getInstance().getReference(courseName)

                db.child(binding?.etEmail?.text.toString().split("@")[0]).setValue(student(binding?.etFName?.text.toString() + " " + binding?.etLName?.text.toString(), binding?.etEmail?.text.toString(),
                    binding?.etAddress?.text.toString(), courseName, binding?.textViewDate1?.text.toString()))

                FirebaseDatabase.getInstance().getReference("course").child(binding?.etEmail?.text?.toString()!!.split("@")[0]).setValue(courseName)

                FirebaseDatabase.getInstance().getReference("StudentAuthentication").child(courseName).child(binding?.etEmail?.text.toString().substringBefore("@")).setValue(studentAuth(binding?.etFName?.text.toString() + " " + binding?.etLName?.text.toString(), binding?.etEmail?.text.toString(), "inActive"))
            }
        }
    }

    // check login state
    private fun checkLoggedInState(){
        if(auth.currentUser == null){
            binding?.tvLogInState?.text = "REGISTRATION UNSUCCESSFUL"
        }else{
            binding?.tvLogInState?.text = "REGISTRATION SUCCESS - PLEASE WAIT FOR ADMIN APPROVAL"
            auth.signOut()
        }
    }
}