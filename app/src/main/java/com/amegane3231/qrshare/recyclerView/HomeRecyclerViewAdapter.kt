package com.amegane3231.qrshare.recyclerView

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amegane3231.qrshare.databinding.ItemHomeBinding
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader

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
            val bitmap = (imageView.drawable as BitmapDrawable).bitmap
            listener.onClick(it, position, bitmap, item)
        }
    }

    override fun getItemCount(): Int = storageRefList.size

    inner class HomeRecyclerViewHolder(binding: ItemHomeBinding) : RecyclerView.ViewHolder(binding.root) {
        val imageView = binding.image
    }

    interface OnItemClickListener {
        fun onClick(view: View, position: Int, bitmap: Bitmap, storageRef: StorageReference)
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