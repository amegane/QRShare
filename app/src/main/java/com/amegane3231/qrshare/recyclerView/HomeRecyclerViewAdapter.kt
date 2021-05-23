package com.amegane3231.qrshare.recyclerView

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amegane3231.qrshare.databinding.ItemHomeBinding
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class HomeRecyclerViewAdapter(
    private val context: Context,
    private val list: List<StorageReference>
) : RecyclerView.Adapter<HomeRecyclerViewAdapter.HomeRecyclerViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRecyclerViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return HomeRecyclerViewHolder(ItemHomeBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: HomeRecyclerViewHolder, position: Int) {
        val item = list[position]
        val storageRef = Firebase.storage.getReference(item.path)
        val imageView = holder.imageView
        Glide.with(context).load(storageRef).into(imageView)
    }

    override fun getItemCount(): Int = list.size

    inner class HomeRecyclerViewHolder(binding: ItemHomeBinding) : RecyclerView.ViewHolder(binding.root) {
        val imageView = binding.image
    }
}