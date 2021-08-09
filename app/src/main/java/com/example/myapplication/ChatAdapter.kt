package com.example.myapplication

import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.*

class ChatAdapter(chat: ArrayList<Message>, activity: Activity?) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private var chat : ArrayList<Message> = chat
    private var mListener: ChatClickListener? = null
    private var context = activity
    fun GetItem(position: Int): Message {
        return chat[position]
    }

    interface ChatClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: ChatClickListener?) {
        mListener = listener
    }

    class ChatViewHolder(itemView: View, listener: ChatClickListener?) :
        RecyclerView.ViewHolder(itemView) {

        var received: LinearLayout = itemView.findViewById(R.id.msg_other_box)
        var sent: LinearLayout = itemView.findViewById(R.id.msg_us_box)

        var rMsg: TextView = itemView.findViewById(R.id.msg_other)
        var rTime: TextView = itemView.findViewById(R.id.time_other)
        var sMsg: TextView = itemView.findViewById(R.id.msg_us)
        var sTime: TextView = itemView.findViewById(R.id.time_us)

        var dateTabOther: LinearLayout = itemView.findViewById(R.id.date_tab_other)
        var chatDateOther: TextView = itemView.findViewById(R.id.chat_date_other)

        var dateTabUs: LinearLayout = itemView.findViewById(R.id.date_tab_us)
        var chatDateUs: TextView = itemView.findViewById(R.id.chat_date_us)

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.chat_adapter_layout, parent, false)
        val evh: ChatViewHolder = ChatViewHolder(v, mListener)
        return evh
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val currentItem: Message = chat[position]

        var time = currentItem.time
        var delimiter = " "
        var parts = time?.split(delimiter)

        var date = parts[0]
        time=parts[1]
        var ampm = parts[2]

        var prevdate="null"

        if(position!=0) {
            val prevItem: Message = chat[position - 1]
            var prevtime = prevItem.time
            var prevparts = prevtime?.split(delimiter)

            prevdate = prevparts[0]
        }
        Log.d("Date", "PrevPosition: ${position - 1} // Position: ${position}")
        Log.d("Date", "PrevDate: ${prevdate} // Date: ${date}")

        if(currentItem.sender == Firebase.auth.currentUser!!.uid)
        {
            holder.received.visibility=View.GONE
            holder.sent.visibility=View.VISIBLE
            holder.dateTabOther.visibility=View.GONE

            if(prevdate != date || position==0){
                holder.dateTabUs.visibility=View.VISIBLE

                delimiter = "/"
                var d = date?.split(delimiter)
                val month = when (d[1]) {
                    "1" -> "January"
                    "2" -> "February"
                    "3" -> "March"
                    "4" -> "April"
                    "5" -> "May"
                    "6" -> "June"
                    "7" -> "July"
                    "8" -> "August"
                    "9" -> "September"
                    "10" -> "October"
                    "11" -> "November"
                    "12" -> "December"
                    else -> "Invalid Month"
                }
                holder.chatDateUs.text="$month ${d[0]}, ${d[2]}"
            }
            else
                holder.dateTabUs.visibility=View.GONE

            holder.sMsg.text = currentItem.text

            time=parts[1]
            var ampm = parts.last()

            delimiter = ":"
            parts = time?.split(delimiter)
            time="${parts[0]}:${parts[1]} $ampm"

            holder.sTime.text = time
        }
        else
        {
            holder.received.visibility=View.VISIBLE
            holder.sent.visibility=View.GONE
            holder.dateTabUs.visibility=View.GONE

            if(prevdate != date || position==0){
                holder.dateTabOther.visibility=View.VISIBLE

                delimiter = "/"
                var d = date?.split(delimiter)
                val month = when (d[1]) {
                    "1" -> "January"
                    "2" -> "February"
                    "3" -> "March"
                    "4" -> "April"
                    "5" -> "May"
                    "6" -> "June"
                    "7" -> "July"
                    "8" -> "August"
                    "9" -> "September"
                    "10" -> "October"
                    "11" -> "November"
                    "12" -> "December"
                    else -> "Invalid Month"
                }
                holder.chatDateOther.text="$month ${d[0]}, ${d[2]}"
            }
            else
                holder.dateTabOther.visibility=View.GONE

            holder.rMsg.text = currentItem.text

            delimiter = ":"
            parts = time?.split(delimiter)
            time="${parts[0]}:${parts[1]} $ampm"

            holder.rTime.text = time
        }

    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return chat.size
    }

}