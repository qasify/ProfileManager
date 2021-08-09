package com.example.myapplication

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import java.io.Serializable
import java.util.*

class ProceedActivity : AppCompatActivity() {
    var fName: EditText? = null
    var lName: EditText? = null
    var number: EditText? = null
    var dob: TextView? = null
    var dp: ImageView? = null

    private var uri:Uri?=null

    var user =User()
    lateinit var datePickerDialog: Dialog

    val PICK_IMAGE: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.proceed_layout)

        val intent = intent
        user= intent.getSerializableExtra("user") as User

        fName = findViewById<View>(R.id.proceed_firstName) as EditText
        lName = findViewById<View>(R.id.proceed_lastName) as EditText
        number = findViewById<View>(R.id.proceed_number) as EditText
        dob = findViewById<View>(R.id.proceed_dob) as TextView
        dp = findViewById<View>(R.id.proceed_dp) as ImageView

        datePickerDialog = Dialog(this)
        datePickerDialog.setCanceledOnTouchOutside(false)

        dp!!.setOnClickListener{
            var gallery = Intent()
            gallery.type = "image/*"
            gallery.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(gallery,"Select Picture"),PICK_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==PICK_IMAGE && resultCode== AppCompatActivity.RESULT_OK) {

            var imageUri = data?.data
            if (imageUri != null) {
                uri= imageUri
                dp?.setImageURI(uri)
            }
        }
    }

    fun onClickDobTab(view: View?) {
        datePickerDialog.setContentView(R.layout.dob_layout)
        var dobPicker = datePickerDialog.findViewById<DatePicker>(R.id.dob_datepicker)
        var datePickerSave = datePickerDialog.findViewById<Button>(R.id.dob_set)

        datePickerSave.setOnClickListener{
            val msg = dobPicker.dayOfMonth.toString() + "-" +(dobPicker.month+1).toString() + "-" + dobPicker.year.toString()
            dob?.text  = msg
            dob?.setTextColor(resources.getColor(R.color.black))
            datePickerDialog.dismiss()
        }
        datePickerDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        datePickerDialog.show()
    }

    fun onClickSkipButton(view: View?) {
        val intent = Intent(this@ProceedActivity, MainActivity::class.java)
        intent.putExtra("user",user as Serializable)
        startActivity(intent)
        finish()
    }

    fun onClickFinishButton(view: View?) {
        var ufName = fName?.text.toString()
        var ulName = lName?.text.toString()
        var uNumber = number?.text.toString()
        var uDob = dob?.text.toString()

        var existNumber: Boolean = false

        if (ufName.isEmpty() && ulName.isEmpty() && uNumber.isEmpty() && uDob.isEmpty() && uri==null) {
            fName!!.error ="All Empty!"
            lName!!.error ="All Empty!"
            number!!.error = "All Empty!"
            dob!!.error = "All Empty!"
        }
        else {

            var pd = ProgressDialog(this@ProceedActivity);
            pd.setTitle("Saving")
            pd.show()

            var database = Firebase.database
            var myRef = database.reference.child("Users")


            val valueEventListener: ValueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (ds in dataSnapshot.children) {
                        val u = ds.getValue(User::class.java)
                        if (u != null) {
                             if (u.number.equals(uNumber)) {
                                existNumber = true
                                break
                            }
                        }
                    }

                    if (existNumber)
                        number!!.error = "Number already Used!"
                    else {
                        if(ufName.length != 0)
                            user.firstName=ufName
                        if(ulName.length != 0)
                            user.lastName=ulName
                        if(uNumber.length != 0)
                            user.number=uNumber
                        if(uDob.length != 0)
                            user.dob=uDob


                        if (uri != null)
                        {

                            var storageRef = Firebase.storage.reference
                            val myRef = storageRef.child("images").child(user.userId)


                            // Create file metadata including the content type
                            var metadata = storageMetadata {
                                contentType = "image/jpg"
                            }
                            myRef.putFile(uri!!,metadata).addOnSuccessListener { taskSnapshot ->
                                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                                // ...
                                taskSnapshot.storage.downloadUrl.addOnCompleteListener {
                                    user.dpUrl= it.result.toString()
                                    Toast.makeText(
                                        this@ProceedActivity,
                                        "Image uploaded!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    var database = Firebase.database
                                    var myRef = database.reference.child("Users")
                                    myRef.child(user.userId).setValue(user).addOnCompleteListener {
                                        Toast.makeText(
                                            this@ProceedActivity,
                                            "Changes saved",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        pd.dismiss()
                                        moveNext(user)
                                    }
                                }

                            }.addOnFailureListener {
                                // Handle unsuccessful uploads
                                Toast.makeText(
                                    this@ProceedActivity,
                                    "Image upload failed!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }else{
                            var database = Firebase.database
                            var myRef = database.reference.child("Users")
                            myRef.child(user.userId).setValue(user).addOnCompleteListener {
                                Toast.makeText(
                                    this@ProceedActivity,
                                    "Changes saved",
                                    Toast.LENGTH_SHORT
                                ).show()
                                pd.dismiss()
                                moveNext(user)
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("TAG", databaseError.message) //Don't ignore errors!
                }
            }

            myRef.addListenerForSingleValueEvent(valueEventListener)
        }
    }
    fun moveNext(u:User)
    {
        val intent = Intent(this@ProceedActivity, MainActivity::class.java)
        intent.putExtra("user",u as Serializable)
        startActivity(intent)
        finish()
    }
}