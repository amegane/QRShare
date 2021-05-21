package com.amegane3231.qrshare.ui.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.amegane3231.qrshare.R
import com.amegane3231.qrshare.databinding.FragmentCreateBinding
import com.amegane3231.qrshare.extentionFunction.isURL
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

class CreateFragment : Fragment() {
    private lateinit var binding: FragmentCreateBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateBinding.inflate(inflater, container, false)

        binding.edittextInputURL.setOnEditorActionListener { v, actionId, event ->
            if (actionId != EditorInfo.IME_ACTION_DONE) return@setOnEditorActionListener true
            val data = binding.edittextInputURL.text.toString()
            if (!data.isURL()) {
                setError(binding.edittextInputURL)
                return@setOnEditorActionListener true
            }
            val qrCode = createQRCode(data)
            qrCode?.let {
                binding.textviewQRCode.isVisible = false
                binding.imageviewQRCode.setImageBitmap(it)
            }
            true
        }

        return binding.root
    }

    private fun createQRCode(URL: String): Bitmap? {
        return try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap =
                barcodeEncoder.encodeBitmap(URL, BarcodeFormat.QR_CODE, SIZE_QRCODE, SIZE_QRCODE)
            bitmap
        } catch (e: Exception) {
            Log.e("Exception", e.toString())
            setError(binding.edittextInputURL)
            null
        }
    }

    private fun setError(editText: EditText) {
        when(editText) {
            binding.edittextInputURL -> binding.edittextInputURL.error = getString(R.string.error_URL)
        }
    }

    companion object {
        private const val SIZE_QRCODE = 512
    }
}