package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.ui.home.HomeFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.io.Serializable


class VisitActivity : AppCompatActivity() {
    var fname: TextView? = null
    var lname: TextView? = null
    var number: TextView? = null
    var email: TextView? = null
    var dob: TextView? = null
    var dp: ImageView? = null

    var selectedUser =User()
    var currentUser =User()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.v_profile_layout)

        val intent = intent
        selectedUser= intent.getSerializableExtra("selectedUser") as User


        var myRef = Firebase.database.reference.child("Users").child(Firebase.auth.currentUser!!.uid).ref
        myRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(com.example.myapplication.User::class.java)!!
                currentUser.status="online"
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(baseContext, "Error Fetching User from Db",
                    Toast.LENGTH_LONG).show()
            }
        })


        fname = findViewById<View>(R.id.v_profile_firstName) as TextView?
        lname = findViewById<View>(R.id.v_profile_lastName) as TextView?
        number = findViewById<View>(R.id.v_profile_number) as TextView?
        email= findViewById<View>(R.id.v_profile_email) as TextView?
        dob= findViewById<View>(R.id.v_profile_dob) as TextView?
        dp= findViewById<View>(R.id.v_profile_dp) as ImageView?

        fname?.text = selectedUser.firstName
        lname?.text = selectedUser.lastName
        number?.text = selectedUser.number
        email?.text = selectedUser.email
        dob?.text  = selectedUser.dob

        if(selectedUser.dpUrl!="Not-Specified"){
            Picasso.get().load(selectedUser.dpUrl).into(dp)
        }

        var back = findViewById<View>(R.id.v_profile_back) as TextView
        back.setOnClickListener(){
            MainActivity.notClick=false
            finish()
        }

        var chat = findViewById<View>(R.id.v_profile_chat) as Button
        chat.setOnClickListener(){

            MainActivity.notClick=false
            val intent = Intent(this@VisitActivity, ChatActivity::class.java)
            intent.putExtra("currentUser", currentUser as Serializable)
            intent.putExtra("selectedUser", selectedUser as Serializable)
            startActivity(intent)

        }
    }
    override fun onStart() {
        super.onStart()
        currentUser.status="online"
        Firebase.database.reference.child("Users").child(Firebase.auth.currentUser!!.uid).child("status").setValue("online")
    }

    override fun onStop() {
        super.onStop()
        if(currentUser.status=="online" && MainActivity.notClick) {
            currentUser.status = "away"
            Firebase.database.reference.child("Users").child(Firebase.auth.currentUser!!.uid).child("status")
                .setValue(currentUser.status)
        }
        MainActivity.notClick=true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        MainActivity.notClick=false
    }
}