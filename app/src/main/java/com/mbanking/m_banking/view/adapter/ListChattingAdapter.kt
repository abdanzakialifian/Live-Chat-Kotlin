package com.mbanking.m_banking.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mbanking.m_banking.databinding.ItemListChatBinding
import com.mbanking.m_banking.model.User
import com.mbanking.m_banking.utils.IOnItemClickCallback
import com.mbanking.m_banking.utils.IOnItemClickLongCallback

class ListChattingAdapter(
    private var listUserChatting: List<User>,
    private val onItemClickCallback: IOnItemClickCallback,
    private val onItemClickLongCallback: IOnItemClickLongCallback
) :
    RecyclerView.Adapter<ListChattingAdapter.ViewHolder>(), Filterable {

    private var listUserChattingFiltered = ArrayList<User>()

    init {
        this.listUserChattingFiltered.clear()
        this.listUserChattingFiltered.addAll(listUserChatting)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private var binding: ItemListChatBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            Glide.with(itemView.context.applicationContext)
                .load(user.image)
                .circleCrop()
                .into(binding.imgUser)
            binding.tvNameUser.text = user.name
            itemView.setOnClickListener { onItemClickCallback.onItemClicked(listUserChattingFiltered[adapterPosition]) }
            itemView.setOnLongClickListener {
                onItemClickLongCallback.onItemLongClicked(listUserChattingFiltered[adapterPosition])
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemListChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listUserChattingFiltered[position])
    }

    override fun getItemCount(): Int = listUserChattingFiltered.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                listUserChattingFiltered = if (charSearch.isEmpty()) {
                    listUserChatting as ArrayList<User>
                } else {
                    val resultList = ArrayList<User>()
                    for (row in listUserChatting) {
                        if (row.name?.lowercase()?.contains(constraint.toString().lowercase())!!) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResult = FilterResults()
                filterResult.values = listUserChattingFiltered
                return filterResult
            }

            override fun publishResults(constraints: CharSequence?, results: FilterResults?) {
                listUserChattingFiltered = results?.values as ArrayList<User>
                notifyDataSetChanged()
            }

        }
    }
}