package com.amegane3231.qrshare.ui.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.amegane3231.qrshare.R
import com.amegane3231.qrshare.databinding.FragmentHomeBinding
import com.amegane3231.qrshare.recyclerView.HomeRecyclerViewAdapter
import com.amegane3231.qrshare.viewmodels.HomeViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.component3
import com.google.firebase.storage.ktx.storage
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by viewModels()
    private val imageContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                val uri = it.data!!.data

                uri?.let {
                    val bitmap = getBitmap(uri)
                    val width = bitmap.width
                    val height = bitmap.height
                    val pixels = IntArray(width * height)
                    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
                    try {
                        val source = RGBLuminanceSource(width, height, pixels)
                        val binarizer = HybridBinarizer(source)
                        val binaryBitmap = BinaryBitmap(binarizer)
                        val reader = QRCodeReader()
                        val decodeResult = reader.decode(binaryBitmap)
                        val resultText = decodeResult.text
                        val action = HomeFragmentDirections.actionHomeToUpload(bitmap, resultText)
                        findNavController().navigate(action)
                    } catch (e: Exception) {
                        Log.e("Exception", e.toString())
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.toast_fail_decode_qr_code),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        homeViewModel.storageList.observe(viewLifecycleOwner, Observer {
            binding.viewHome.adapter = HomeRecyclerViewAdapter(
                requireContext(),
                it
            )
        })
        binding.viewHome.layoutManager = GridLayoutManager(requireContext(), 2)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewModel.listAllPaginated(null)

        binding.fabCreateQRCode.setOnClickListener {
            findNavController().navigate(R.id.action_Home_to_Upload)
        }
        binding.fabUploadQRCode.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                imageContent.launch(intent)
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.toast_not_found_app),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getBitmap(uri: Uri): Bitmap {
        val openFileDescriptor =
            requireContext().contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = openFileDescriptor?.fileDescriptor
        val bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        openFileDescriptor?.close()
        return bitmap
    }
}