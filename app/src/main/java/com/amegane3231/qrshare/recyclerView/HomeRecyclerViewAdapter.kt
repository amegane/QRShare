package com.amegane3231.qrshare.recyclerView

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amegane3231.qrshare.databinding.ItemHomeBinding
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class HomeRecyclerViewAdapter(
    private val context: Context,
) : RecyclerView.Adapter<HomeRecyclerViewAdapter.HomeRecyclerViewHolder>() {
    private lateinit var listener: OnItemClickListener
    private val storageRefList: MutableList<StorageReference> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRecyclerViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return HomeRecyclerViewHolder(ItemHomeBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: HomeRecyclerViewHolder, position: Int) {
        val item = storageRefList[position]
        val storageRef = Firebase.storage.getReference(item.path)
        val imageView = holder.imageView
        Glide.with(context).load(storageRef).into(imageView)

        holder.imageView.setOnClickListener {
            listener.onClick(holder.itemView, position, item.path, item.name)
        }
    }

    override fun getItemCount(): Int = storageRefList.size

    inner class HomeRecyclerViewHolder(binding: ItemHomeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val imageView = binding.image
    }

    interface OnItemClickListener {
        fun onClick(view: View, position: Int, path: String, imageName: String)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    fun update(list: List<StorageReference>) {
        storageRefList.removeAll(storageRefList)
        storageRefList.addAll(list)
        notifyDataSetChanged()
    }
}