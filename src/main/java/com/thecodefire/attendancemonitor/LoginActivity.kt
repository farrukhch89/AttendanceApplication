package com.thecodefire.attendancemonitor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.thecodefire.attendancemonitor.databinding.ActivityLoginBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception

class LoginActivity : AppCompatActivity() {

    private var binding: ActivityLoginBinding? = null
    lateinit var auth: FirebaseAuth

    var bool = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // check for internet
        if(!checkForInternet(this)){
            Toast.makeText(this, "NO INTERNET", Toast.LENGTH_LONG).show()
        }

        auth = FirebaseAuth.getInstance()
        
        binding?.passwordResetBtn?.setOnClickListener { 
            val intent = Intent(this, PasswordReset::class.java)
            startActivity(intent)
        }

        binding?.lecLoginBtn?.setOnClickListener { showSignInDetails(1) }

        binding?.stuLoginBtn?.setOnClickListener {
            showSignInDetails(0)
            bool = true
        }

        binding?.adminLoginBtn?.setOnClickListener {
            binding?.lecLoginBtn?.visibility = View.GONE // hide button lec login
            binding?.stuLoginBtn?.visibility = View.GONE
            binding?.passwordResetBtn?.visibility = View.GONE

            // show login edit text to enter email and password
            binding?.username?.visibility = View.VISIBLE
            binding?.password?.visibility = View.VISIBLE
            binding?.adminBtn?.visibility = View.VISIBLE
        }

        // admin sign in
        binding?.adminBtn?.setOnClickListener {
            if(binding?.username?.text?.isNotEmpty() == true){
                if(binding?.username?.text.toString().substring(0,6) == "admin_"){
                    signIn()
                }
            }
        }


        // student login
        binding?.loginBtn?.setOnClickListener {
            val username = binding?.username?.text
            if(!checkForInternet(this)){
                Toast.makeText(this, "NO INTERNET", Toast.LENGTH_LONG).show()
            }else if((username?.substring(0,4) == "stu_") && binding?.lecLoginBtn?.visibility == View.VISIBLE){ // check for incorrect credential entered
                Toast.makeText(this, "This is not a Lecturer ID", Toast.LENGTH_LONG).show()
            }else if((username?.substring(0,4) == "lec_") && binding?.stuLoginBtn?.visibility == View.VISIBLE){
                Toast.makeText(this, "This is not a Student ID", Toast.LENGTH_LONG).show()
            }else if((username?.substring(0,4) == "stu_") || (username?.substring(0,4) == "lec_")){
                signIn()
            }else{
                Toast.makeText(this, "Invalid Username or Password", Toast.LENGTH_LONG).show()
            }
        }
    }

    // login
    override fun onStart() {
        super.onStart()
        checkLoggedInState()
    }

    // lecturer login id = 1
    private fun showSignInDetails(id: Int){
        if(id == 1){
            binding?.stuLoginBtn?.visibility = View.GONE // make other 2 user disappear from screen
            binding?.adminLoginBtn?.visibility = View.GONE
            binding?.passwordResetBtn?.visibility = View.GONE
        }else{
            binding?.lecLoginBtn?.visibility = View.GONE
            binding?.adminLoginBtn?.visibility = View.GONE
            binding?.passwordResetBtn?.visibility = View.GONE
        }

        binding?.password?.visibility = View.VISIBLE
        binding?.username?.visibility = View.VISIBLE
        binding?.loginBtn?.visibility = View.VISIBLE
    }

    // sign in
    private fun signIn(){
        val username = binding?.username?.text.toString()
        val password = binding?.password?.text.toString()

        if(bool){
            FirebaseDatabase.getInstance().getReference("course").child(username.substringBefore("@")).get().addOnSuccessListener {
                FirebaseDatabase.getInstance().getReference("StudentAuthentication").child(it.value.toString()).child(username.substringBefore("@")).get().addOnSuccessListener { sn->
                    for(ch in sn.children){
                        if(ch.key.toString() == "stuStatus"){ // check status if active
                            if(ch.value.toString() == "Active"){
                                if(username.isNotEmpty() && password.isNotEmpty()){ // making sure credentials are not empty
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            auth.signInWithEmailAndPassword(username, password).await()
                                            withContext(Dispatchers.Main){
                                                checkLoggedInState() // log in successful
                                            }
                                        }catch (e: Exception){
                                            withContext(Dispatchers.Main){
                                                Toast.makeText(this@LoginActivity, e.message, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }else{ // unsuccessful sign in
                                    Toast.makeText(this@LoginActivity, "username or password must not empty", Toast.LENGTH_SHORT).show()
                                }
                            }else{ // not active student
                                Toast.makeText(applicationContext, "You are not an Active Student :: Kindly Contact your Admin", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }else{ // if credential correct login successful
            if(username.isNotEmpty() && password.isNotEmpty()){
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        auth.signInWithEmailAndPassword(username, password).await()
                        withContext(Dispatchers.Main){
                            checkLoggedInState()
                        }
                    }catch (e: Exception){
                        withContext(Dispatchers.Main){
                            Toast.makeText(this@LoginActivity, e.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }else{
                Toast.makeText(this@LoginActivity, "username or password must not empty", Toast.LENGTH_SHORT).show()
            }
        }


    }

    // login status
    private fun checkLoggedInState(){
        if(auth.currentUser == null){
            binding?.tvLogInState?.text = "NO LOGIN FOUND" // unsuccessful login
            //Toast.makeText(this@LoginActivity, "Login Failed", Toast.LENGTH_SHORT).show()
        }else{
            binding?.tvLogInState?.text = "LOGIN SUCCESS" // successful message
            if(auth.currentUser!!.email?.substring(0,4) == "stu_"){ // if student go to student dashboard
                val intent : Intent = Intent(this, StudentDashboard::class.java)
                Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                startActivity(intent)
                finish()
            }

            if(auth.currentUser!!.email?.substring(0,4) == "lec_"){// if lecturer go to lecturer dashboard
                val intent = Intent(this, DashboardActivity::class.java)
                Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                startActivity(intent)
                finish()
            }

            if(auth.currentUser!!.email?.substring(0,6) == "admin_"){ // admin dashboard
                val intent = Intent(this, AdminActivity::class.java)
                Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                startActivity(intent)
                finish()
            }
        }
    }
}