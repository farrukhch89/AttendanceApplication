
package com.thecodefire.attendancemonitor

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import com.thecodefire.attendancemonitor.databinding.ActivityDownloadAttendanceBinding
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class DownloadAttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDownloadAttendanceBinding
    var courseName = ""
    var subjectName = ""
    private val READ_STORAGE_PERMISSION_REQUEST_CODE = 41
    private val EXTERNAL_STORAGE_PERMISSION_CODE = 23

    // permission for download
    private val ResultLauncher : ActivityResultLauncher<String> =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()){
                isGranted ->
            if(isGranted){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.subject.isEnabled = false
        binding.btnDownload.isEnabled = false

        // spinner for selecting course for download
        val spinner: Spinner = binding.course
        ArrayAdapter.createFromResource(
            this,
            R.array.course,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        // spinner for subject according to course
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                courseName = spinner.getItemAtPosition(p2).toString()
                if(courseName != "Choose Course"){
                    binding.subject.isEnabled = true
                    var subjectArray : Int =  R.array.subject_art

                    when(courseName){
                        "Art" -> subjectArray = R.array.subject_art
                        "Computer Science" -> subjectArray = R.array.subject_cs
                        "Computing Business" -> subjectArray = R.array.subject_CB
                        "Architecture" -> subjectArray = R.array.subject_architecture
                    }

                    val subjectSpinner: Spinner = binding.subject
                    ArrayAdapter.createFromResource(
                        applicationContext,
                        subjectArray,
                        android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        subjectSpinner.adapter = adapter
                    }

                    // on clicking on download button
                    subjectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                            subjectName = subjectSpinner.getItemAtPosition(p2).toString()
                            binding.btnDownload.isEnabled = true
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {
                            TODO("Not yet implemented")
                        }

                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        // download button listener
        binding.btnDownload.setOnClickListener {

            // check for permissions
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)){ // request for permission
                showRationalDialog("Permisssion REQUIRED", "Storage permission is denied")
            }
            else{ // write to external memory
                ResultLauncher.launch(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                download()
            }
        }

    }

    // permission dialog box
    private fun showRationalDialog(title: String, message: String){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Cancel"){dialog, _->
                dialog.dismiss()}
        builder.create().show()
    }

    // download upon request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            100 -> { // download
                if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    download() // go to download method
                }else{ // denied
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // download file
    private fun download(){
        binding.DownlaodText.visibility = View.VISIBLE
        val c = Calendar.getInstance()

  /*      val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)*/

        val mDoc = com.itextpdf.text.Document()
        // file name for download
        val mFIleName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
        val mFilePath = getExternalFilesDir(null).toString() +  "/" + mFIleName + ".pdf"

        try{ //
            PdfWriter.getInstance(mDoc, FileOutputStream(mFilePath))
            //open file to write
            mDoc.open()
            mDoc.addAuthor("Farrukh")
//
//            Toast.makeText(this, "$mFIleName.pdf\n saved to\n$mFilePath", Toast.LENGTH_LONG).show()
        }catch (e: Exception){
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }

        // get attendance name after selecting course and subject
        FirebaseDatabase.getInstance().getReference("Attendance").child(courseName).child(subjectName).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(child in snapshot.children){
                    mDoc.add(Paragraph(child.key.toString()))
                    for(ch in child.children){
                        mDoc.add(Paragraph(ch.key.toString() + " " + ch.value.toString()))
                    }
                }

                mDoc.close()
                // close file after writing
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}