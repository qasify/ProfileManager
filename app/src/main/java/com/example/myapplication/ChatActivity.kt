package com. example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.*
import com.example.myapplication.ui.home.HomeFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*


class ChatActivity : AppCompatActivity() {

    var selectedUser = User()
    var currentUser = User()

    var name: TextView? = null
    var dp: ImageView? = null
    var msg: TextView? = null
    var chatKey=String()

    var mRecyclerView: RecyclerView? = null
    var mAdapter: ChatAdapter? = null
    var mLayoutManager: RecyclerView.LayoutManager? = null
    var conversation =  ArrayList<Message>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_layout)

        val intent = intent
        currentUser = intent.getSerializableExtra("currentUser") as User
        selectedUser = intent.getSerializableExtra("selectedUser") as User

        name = findViewById<View>(R.id.chat_name) as TextView?
        dp= findViewById<View>(R.id.chat_dp) as ImageView?
        msg = findViewById<View>(R.id.chat_message) as TextView?

        name?.text = "${selectedUser.firstName} ${selectedUser.lastName}"
        if(selectedUser.dpUrl!="Not-Specified"){
            Picasso.get().load(selectedUser.dpUrl).into(dp)
        }

        var myRef = Firebase.database.reference.child("Chats").child("Chats-Info")
        myRef.child(currentUser.userId).child(selectedUser.userId).ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if(snapshot.exists()) {
                    chatKey= snapshot.getValue<String>().toString()
                }
                else{
                    chatKey = Firebase.database.reference.child("Chats").child("Conversations").push().key.toString()
                    myRef.child(currentUser.userId).child(selectedUser.userId).setValue(chatKey)
                    myRef.child(selectedUser.userId).child(currentUser.userId).setValue(chatKey)
                }
                readConversation()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(baseContext, "Error Fetching User from Db",
                    Toast.LENGTH_LONG).show()
            }

        })



        mRecyclerView = findViewById(R.id.chat_recyclerview)
        mRecyclerView!!.setHasFixedSize(true)
        mLayoutManager = LinearLayoutManager(this)
        (mLayoutManager as LinearLayoutManager).reverseLayout = false
        (mLayoutManager as LinearLayoutManager).stackFromEnd = true
        mAdapter = ChatAdapter(conversation,this)
        mRecyclerView!!.layoutManager = mLayoutManager
        mRecyclerView!!.adapter = mAdapter


    }

    fun onClickChatBack(view: View?) {
        MainActivity.notClick=false
        finish()
    }

    fun onClickChatSend(view: View) {
        if(!msg?.text.toString().isEmpty()){
            var message = Message()
            message.text = msg?.text.toString()
            message.sender = currentUser.userId

            Firebase.database.reference.child("Chats").child("Conversations").child(chatKey).push().setValue(message)
//            Toast.makeText(baseContext, "Message Sent", Toast.LENGTH_LONG).show()
            msg?.text = ""
        }
        else
            msg?.error="Blank message!"

    }

    private fun readConversation(){
        var myRef = Firebase.database.reference.child("Chats").child("Conversations").child(chatKey)

        val valueEventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                conversation.clear()
                for (ds in dataSnapshot.children) {
                    val msg = ds.getValue(Message::class.java)
                    if (msg != null ) {
                        conversation.add(msg)
                        mAdapter?.notifyDataSetChanged()
                    }
                }
                if(dataSnapshot.exists())
                    mRecyclerView!!.smoothScrollToPosition(mAdapter!!.itemCount-1)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("TAG", databaseError.message) //Don't ignore errors!
            }
        }
        myRef.addValueEventListener(valueEventListener)
    }

    override fun onStart() {
        super.onStart()
        currentUser.status="online"
        Firebase.database.reference.child("Users").child(currentUser.userId).child("status").setValue(currentUser.status)
    }

    override fun onStop() {
        super.onStop()
        if(currentUser.status=="online" && MainActivity.notClick) {
            currentUser.status = "away"
            Firebase.database.reference.child("Users").child(currentUser.userId).child("status")
                .setValue(currentUser.status)
        }
        MainActivity.notClick=true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        MainActivity.notClick=false
    }
}