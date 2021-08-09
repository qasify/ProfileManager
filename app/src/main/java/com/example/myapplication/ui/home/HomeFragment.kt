package com.example.myapplication.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.Serializable
import java.util.ArrayList


class HomeFragment : Fragment() {



    var mRecyclerView: RecyclerView? = null
    var mAdapter: HomeListAdapter? = null
    var mLayoutManager: RecyclerView.LayoutManager? = null
    var users =  ArrayList<User>()
    var cUserId = String()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        cUserId = arguments?.getString("UserId").toString()
        readUsers()

        mRecyclerView = root.findViewById(R.id.home_recyclerView)
        mRecyclerView!!.setHasFixedSize(true)
        mLayoutManager = LinearLayoutManager(activity)
        mAdapter = HomeListAdapter(users,activity)
        mRecyclerView!!.layoutManager = mLayoutManager
        mRecyclerView!!.adapter = mAdapter


        mAdapter!!.setOnItemClickListener(object : HomeListAdapter.HomeListClickListener {
            override fun onItemClick(position: Int) {
                MainActivity.notClick=false
                val selectedUser: User = mAdapter!!.GetItem(position)

                val intent = Intent(activity, VisitActivity::class.java)
                intent.putExtra("selectedUser", selectedUser as Serializable)
                startActivity(intent)
            }
        })


        return root
    }


    private fun readUsers(){


        var database = Firebase.database
        var myRef = database.reference.child("Users")


        val valueEventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                users.clear()
                for (ds in dataSnapshot.children) {
                    val u = ds.getValue(User::class.java)
                    if (u != null && u.userId!= cUserId ) {
                        users.add(u)
                        mAdapter?.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("TAG", databaseError.message) //Don't ignore errors!
            }
        }

        myRef.addValueEventListener(valueEventListener)
    }
}