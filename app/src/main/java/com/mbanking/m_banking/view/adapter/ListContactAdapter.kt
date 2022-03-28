package com.mbanking.m_banking.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mbanking.m_banking.databinding.ItemListContactBinding
import com.mbanking.m_banking.model.User
import com.mbanking.m_banking.utils.IOnItemClickCallback

class ListContactAdapter(private val onItemClickCallback: IOnItemClickCallback) :
    RecyclerView.Adapter<ListContactAdapter.ViewHolder>() {

    private val listContact = ArrayList<User>()

    fun setListContact(listContact: List<User>) {
        this.listContact.clear()
        this.listContact.addAll(listContact)
    }

    inner class ViewHolder(private var binding: ItemListContactBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            Glide.with(itemView.context.applicationContext)
                .load(user.image)
                .circleCrop()
                .into(binding.imgUser)
            binding.tvNameUser.text = user.name
            itemView.setOnClickListener { onItemClickCallback.onItemClicked(listContact[adapterPosition]) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemListContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listContact[position])
    }

    override fun getItemCount(): Int = listContact.size
}