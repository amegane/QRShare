package com.amegane3231.qrshare.recyclerView

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amegane3231.qrshare.databinding.ItemTagBinding

class FlexboxListAdapter constructor() :
    RecyclerView.Adapter<FlexboxListAdapter.FlexboxListViewHolder>() {
    private val tags = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlexboxListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return FlexboxListViewHolder(ItemTagBinding.inflate(inflater, parent, false))
    }

    override fun getItemCount(): Int {
        return tags.size
    }

    override fun onBindViewHolder(holder: FlexboxListViewHolder, position: Int) {
        val tag = tags[position]
        holder.tagName.text = tag
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(tags: List<String>) {
        this.tags.removeAll(this.tags)
        this.tags.addAll(tags)
        notifyDataSetChanged()
    }

    inner class FlexboxListViewHolder(binding: ItemTagBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val tagName = binding.textviewTag
    }
}