package com.example.myapplication.ui.profile

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {
    private var auth : FirebaseAuth? = null

    lateinit var fname: TextView
    lateinit var lname: TextView
    lateinit var number: TextView
    lateinit var email: TextView
    lateinit var password: TextView
    lateinit var dob: TextView
    lateinit var dp: ImageView

    var user =User()

    lateinit var editFname: EditText
    lateinit var editLname: EditText
    lateinit var editNumber: EditText
    lateinit var editEmail: EditText
    lateinit var editPassword: EditText
    lateinit var editDob: TextView
    lateinit var editDp: ImageView
    lateinit var dpText: TextView

    private var uri:Uri?=null

    lateinit var datePickerDialog: Dialog

    val PICK_IMAGE: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = Firebase.auth

        user= arguments?.getSerializable("User") as User
        fname = root.findViewById(R.id.profile_firstName)
        lname = root.findViewById(R.id.profile_lastName)
        number = root.findViewById(R.id.profile_number)
        email = root.findViewById(R.id.profile_email)
        password = root.findViewById(R.id.profile_password)
        dob = root.findViewById(R.id.profile_dob)
        dp= root.findViewById(R.id.profile_dp)
        setProfileValues()

        editFname = root.findViewById(R.id.profile_edit_firstName)
        editLname = root.findViewById(R.id.profile_edit_lastName)
        editNumber = root.findViewById(R.id.profile_edit_number)
        editEmail = root.findViewById(R.id.profile_edit_email)
        editPassword = root.findViewById(R.id.profile_edit_password)
        editDob = root.findViewById(R.id.profile_edit_dob)
        editDp = root.findViewById(R.id.profile_edit_dp)
        dpText = root.findViewById(R.id.profile_dp_text)

        setProfileEdits()

        datePickerDialog = activity?.let { Dialog(it) }!!
        datePickerDialog.setCanceledOnTouchOutside(false)

        editDp.setOnClickListener{
            var gallery = Intent()
            gallery.type = "image/*"
            gallery.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(gallery,"Select Picture"),PICK_IMAGE)
        }

        var editProfile = root.findViewById(R.id.edit_profile) as Button
        var changePass = root.findViewById(R.id.change_password) as Button
        var saveProf = root.findViewById(R.id.save_profile) as Button

        editProfile.setOnClickListener(){
            if(dpText.visibility==View.GONE)
            {
                setProfileEdits()
                fname.visibility=View.GONE
                lname.visibility=View.GONE
                number.visibility=View.GONE
//                email.visibility=View.GONE
                dob.visibility=View.GONE
                dp.visibility=View.GONE


                editFname.visibility=View.VISIBLE
                editLname.visibility=View.VISIBLE
                editNumber.visibility=View.VISIBLE
//                editEmail.visibility=View.VISIBLE
                editDob.visibility=View.VISIBLE
                editDp.visibility=View.VISIBLE
                dpText.visibility=View.VISIBLE

                saveProf.isEnabled=true
                changePass.isEnabled=true

                saveProf.setOnClickListener() {
                    var pd = ProgressDialog(activity);
                    pd.setTitle("Saving Changes")
                    pd.show()
                    user.firstName = editFname.text.toString()
                    user.lastName = editLname.text.toString()
                    user.number = editNumber.text.toString()
//                    user.email = editEmail.text.toString()
                    user.password = editPassword.text.toString()
                    user.dob = editDob.text.toString()
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

                                var database = Firebase.database
                                var myRef = database.reference.child("Users")
                                myRef.child(user.userId).setValue(user).addOnCompleteListener {
                                    Toast.makeText(
                                        activity,
                                        "Changes saved",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    if(user.password!="Not-Specified" && user.password!=null &&  user.password!="") {
                                        auth = Firebase.auth
                                        val currentUser = auth!!.currentUser
                                        currentUser?.updatePassword(user.password)
                                            ?.addOnFailureListener {
                                                // Handle unsuccessful uploads
                                                Toast.makeText(
                                                    activity,
                                                    "Password change failed!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }

                                    setProfileValues()

                                    editFname.visibility=View.GONE
                                    editLname.visibility=View.GONE
                                    editNumber.visibility=View.GONE
//                editEmail.visibility=View.VISIBLE
                                    editDob.visibility=View.GONE
                                    editDp.visibility=View.GONE
                                    dpText.visibility=View.GONE

                                    fname.visibility=View.VISIBLE
                                    lname.visibility=View.VISIBLE
                                    number.visibility=View.VISIBLE
//                email.visibility=View.GONE
                                    dob.visibility=View.VISIBLE
                                    dp.visibility=View.VISIBLE

                                    editPassword.visibility = View.GONE
                                    password.visibility=View.VISIBLE

                                    saveProf.isEnabled=false
                                    changePass.isEnabled=false
                                    pd.dismiss()
                                }
                            }

                        }.addOnFailureListener {
                            // Handle unsuccessful uploads
                            Toast.makeText(
                                activity,
                                "Image upload failed!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }else{
                        var database = Firebase.database
                        var myRef = database.reference.child("Users")
                        myRef.child(user.userId).setValue(user).addOnCompleteListener {
                            Toast.makeText(
                                activity,
                                "Changes saved",
                                Toast.LENGTH_SHORT
                            ).show()

                            if(user.password!="Not-Specified" && user.password!=null &&  user.password!="") {
                                auth = Firebase.auth
                                val currentUser = auth!!.currentUser

                                currentUser?.updatePassword(user.password)?.addOnFailureListener {
                                    // Handle unsuccessful uploads
                                    Toast.makeText(
                                        activity,
                                        "Password change failed!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }


                            setProfileValues()

                            editFname.visibility=View.GONE
                            editLname.visibility=View.GONE
                            editNumber.visibility=View.GONE
//                editEmail.visibility=View.VISIBLE
                            editDob.visibility=View.GONE
                            editDp.visibility=View.GONE
                            dpText.visibility=View.GONE

                            fname.visibility=View.VISIBLE
                            lname.visibility=View.VISIBLE
                            number.visibility=View.VISIBLE
//                email.visibility=View.GONE
                            dob.visibility=View.VISIBLE
                            dp.visibility=View.VISIBLE

                            editPassword.visibility = View.GONE
                            password.visibility=View.VISIBLE

                            saveProf.isEnabled=false
                            changePass.isEnabled=false
                            pd.dismiss()
                        }
                    }
                    uri=null

                }

                changePass.setOnClickListener(){
                    if (editPassword.visibility == View.VISIBLE) {
                        editPassword.visibility = View.GONE
                        password.visibility = View.VISIBLE
                        password?.text = editPassword.text
                    } else {
                        editPassword.visibility = View.VISIBLE
                        password.visibility = View.GONE
                    }

                }

                editDob.setOnClickListener(){
                    datePickerDialog.setContentView(R.layout.dob_layout)
                    var dobPicker =
                        datePickerDialog.findViewById<DatePicker>(R.id.dob_datepicker)
                    var datePickerSave = datePickerDialog.findViewById<Button>(R.id.dob_set)

                    datePickerSave.setOnClickListener {
                        val msg =
                            dobPicker.dayOfMonth.toString() + "-" + (dobPicker.month + 1).toString() + "-" + dobPicker.year.toString()
                        editDob?.text = msg
                        editDob?.setTextColor(resources.getColor(R.color.black))
                        datePickerDialog.dismiss()
                    }
                    datePickerDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    datePickerDialog.show()
                }

                var dobButton = root.findViewById(R.id.profile_dob_button) as ImageButton
                dobButton.setOnClickListener(){
                    if(dpText.visibility==View.GONE) {
                        datePickerDialog.setContentView(R.layout.dob_layout)
                        var dobPicker =
                            datePickerDialog.findViewById<DatePicker>(R.id.dob_datepicker)
                        var datePickerSave = datePickerDialog.findViewById<Button>(R.id.dob_set)

                        datePickerSave.setOnClickListener {
                            val msg =
                                dobPicker.dayOfMonth.toString() + "-" + (dobPicker.month + 1).toString() + "-" + dobPicker.year.toString()
                            editDob?.text = msg
                            editDob?.setTextColor(resources.getColor(R.color.black))
                            datePickerDialog.dismiss()
                        }
                        datePickerDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        datePickerDialog.show()
                    }
                }

                var dobLayout = root.findViewById(R.id.profile_dob_button) as ImageButton
                dobLayout.setOnClickListener(){
                    if(dpText.visibility==View.GONE) {
                        datePickerDialog.setContentView(R.layout.dob_layout)
                        var dobPicker =
                            datePickerDialog.findViewById<DatePicker>(R.id.dob_datepicker)
                        var datePickerSave = datePickerDialog.findViewById<Button>(R.id.dob_set)

                        datePickerSave.setOnClickListener {
                            val msg =
                                dobPicker.dayOfMonth.toString() + "-" + (dobPicker.month + 1).toString() + "-" + dobPicker.year.toString()
                            editDob?.text = msg
                            editDob?.setTextColor(resources.getColor(R.color.black))
                            datePickerDialog.dismiss()
                        }
                        datePickerDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        datePickerDialog.show()
                    }
                }



            }else{
                setProfileValues()

                editFname.visibility=View.GONE
                editLname.visibility=View.GONE
                editNumber.visibility=View.GONE
//                editEmail.visibility=View.VISIBLE
                editDob.visibility=View.GONE
                editDp.visibility=View.GONE
                dpText.visibility=View.GONE

                fname.visibility=View.VISIBLE
                lname.visibility=View.VISIBLE
                number.visibility=View.VISIBLE
//                email.visibility=View.GONE
                dob.visibility=View.VISIBLE
                dp.visibility=View.VISIBLE

                saveProf.isEnabled=false
                changePass.isEnabled=false
            }

            uri=null
        }

        return root
    }

    private fun setProfileValues() {
        fname?.text = user.firstName
        lname?.text = user.lastName
        number?.text = user.number
        email?.text = user.email
        password?.text = user.password
        dob?.text  = user.dob

        if(user.dpUrl!="Not-Specified"){
            Picasso.get().load(user.dpUrl).into(dp)
        }
    }

    private fun setProfileEdits(){

        editFname?.setText(user.firstName)
        editLname?.setText(user.lastName)
        editNumber?.setText(user.number)
        editEmail?.setText(user.email)
        editPassword?.setText(user.password)
        editDob?.text = user.dob

        if(user.dpUrl!="Not-Specified"){
            Picasso.get().load(user.dpUrl).into(editDp)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==PICK_IMAGE && resultCode== AppCompatActivity.RESULT_OK) {

            var imageUri = data?.data
            if (imageUri != null) {
                uri= imageUri
                editDp.setImageURI(uri)
            }
        }
    }

}