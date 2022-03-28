package com.mbanking.m_banking.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mbanking.m_banking.R
import com.mbanking.m_banking.model.Chatting

class ChattingAdapter : RecyclerView.Adapter<ChattingAdapter.ViewHolder>() {

    private val listChat = ArrayList<Chatting>()

    fun setListChat(listChat: List<Chatting>) {
        this.listChat.clear()
        this.listChat.addAll(listChat)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.findViewById(R.id.tv_chat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == MESSAGE_TYPE_RIGHT) {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_right, parent, false)
            ViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_left, parent, false)
            ViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatting = listChat[position]
        holder.tvMessage.text = chatting.message
    }

    override fun getItemCount(): Int = listChat.size

    override fun getItemViewType(position: Int): Int {
        val firebaseUser = Firebase.auth.currentUser
        return if (listChat[position].senderId == firebaseUser?.uid) {
            MESSAGE_TYPE_RIGHT
        } else {
            MESSAGE_TYPE_LEFT
        }
    }

    companion object {
        private const val MESSAGE_TYPE_LEFT = 0
        private const val MESSAGE_TYPE_RIGHT = 1
    }
}