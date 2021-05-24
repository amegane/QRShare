package com.amegane3231.qrshare.ui.fragments

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.amegane3231.qrshare.R
import com.amegane3231.qrshare.databinding.FragmentDetailBinding
import com.amegane3231.qrshare.recyclerView.FlexboxListAdapter
import com.amegane3231.qrshare.viewmodels.DetailViewModel
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class DetailFragment : Fragment() {
    private lateinit var binding: FragmentDetailBinding
    private val args: DetailFragmentArgs by navArgs()
    private val detailViewModel: DetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailBinding.inflate(inflater, container, false)

        val path = args.pathArg
        val storageRef = Firebase.storage.getReference(path)
        Glide.with(requireContext()).load(storageRef).into(binding.imageviewQRCode)

        val selectedQRCodeUid = args.uidArg
        val selectedQRCodeName = args.imageNameArg
        detailViewModel.getTags(selectedQRCodeUid, selectedQRCodeName)

        val adapter = FlexboxListAdapter()

        detailViewModel.url.observe(viewLifecycleOwner, Observer {
            binding.textviewURL.text = it
        })

        detailViewModel.tagList.observe(viewLifecycleOwner, Observer {
            adapter.update(it)
        })

        binding.viewTag.adapter = adapter
        val layoutManager = FlexboxLayoutManager(requireContext())
        layoutManager.flexWrap = FlexWrap.WRAP
        binding.viewTag.layoutManager = layoutManager

        binding.buttonSaveQrCode.setOnClickListener {
            saveQRCode((binding.imageviewQRCode.drawable as BitmapDrawable).bitmap)
        }
        return binding.root
    }

    private fun saveQRCode(bitmap: Bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val date = LocalDateTime.now()
            val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

            val collection =
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val uri = requireContext().contentResolver.insert(
                collection,
                ContentValues().apply {
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(
                        MediaStore.Images.Media.TITLE,
                        "QRShare_${dateTimeFormatter.format(date)}"
                    )
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            )!!
            requireContext().contentResolver.openOutputStream(uri).use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }

            requireContext().contentResolver.update(
                uri,
                ContentValues().also { values ->
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                }, null, null
            )
        } else {
            val date = Date()
            val simpleDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val collection =
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val permission =
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
                return
            }
            val uri = requireContext().contentResolver.insert(
                collection,
                ContentValues().apply {
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(
                        MediaStore.Images.Media.TITLE,
                        "QRShare_${simpleDateFormat.format(date)}"
                    )
                }
            )!!
            requireContext().contentResolver.openOutputStream(uri).use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
        }
        Toast.makeText(requireContext(), getString(R.string.toast_save), Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val PATTERN = "(?:^|\\s)(#([^\\s]+))[^\\s]?"
        private const val COUNT_TO_GET_DATE_AND_FILE_EXTENSION = 19
    }
}