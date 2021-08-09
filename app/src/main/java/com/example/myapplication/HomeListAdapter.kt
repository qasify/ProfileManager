package com.example.myapplication

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*


class HomeListAdapter(userList: ArrayList<User>, activity: Activity?) :
    RecyclerView.Adapter<com.example.myapplication.HomeListAdapter.HomeListViewHolder>() {

    private var homeList : ArrayList<User>
    private var context=activity
    private var mListener: HomeListClickListener? = null

    fun GetItem(position: Int): User {
        return homeList[position]
    }

    interface HomeListClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: HomeListClickListener?) {
        mListener = listener
    }

    class HomeListViewHolder(itemView: View, listener: HomeListClickListener?) :
        RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.homelist_name)
        var email: TextView = itemView.findViewById(R.id.homelist_mail)
        var age: TextView = itemView.findViewById(R.id.homelist_age)
        var dp: ImageView = itemView.findViewById((R.id.homelist_dp))
        var status: TextView = itemView.findViewById(R.id.homelist_status)

        init {
            itemView.setOnClickListener {
                if (listener != null) {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeListViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(
            R.layout.homelist_adapter_layout,
            parent,
            false
        )
        return HomeListViewHolder(v, mListener)
    }


    override fun onBindViewHolder(holder: HomeListViewHolder, position: Int) {
        val currentItem: User = homeList[position]
        if(currentItem.firstName=="Not-Specified" && currentItem.lastName=="Not-Specified")
            holder.name.text = currentItem.firstName
        else
            holder.name.text = currentItem.firstName + " " + currentItem.lastName
        holder.email.text = currentItem.email

        if(currentItem.dob!="Not-Specified"){
            val sdf = SimpleDateFormat("dd/M/yyyy")
            val currentDate = sdf.format(Date())

            var delimiter = "/"
            var parts = currentDate?.split(delimiter)

            val currentYear = parts?.last().toString()
            val currentMonth = parts?.get(1).toString()

            var dob = currentItem.dob
            delimiter = "-"
            parts = dob?.split(delimiter)

            var dobYear = parts?.last().toString()
            var dobMonth = parts?.get(1).toString()

            var age:Int=currentYear.toInt()-dobYear.toInt()
            if(currentMonth.toInt()>dobMonth.toInt())
                age += 1

            holder.age.text = age.toString()
        }
        else
            holder.age.text = currentItem.dob

        if(currentItem.dpUrl!="Not-Specified"){
            Picasso.get().load(currentItem.dpUrl).into(holder.dp)
        }

        if(currentItem.status=="away"){
            holder.status.background= context?.let { ContextCompat.getDrawable(it, R.drawable.draw_status_away) }
        }else if(currentItem.status=="online"){
            holder.status.background= context?.let { ContextCompat.getDrawable(it, R.drawable.draw_status_online) }
        }else
            holder.status.background= context?.let { ContextCompat.getDrawable(it, R.drawable.draw_status_offline) }
    }

    override fun getItemCount(): Int {
        return homeList.size
    }

    init {
        this.homeList = userList
    }
}