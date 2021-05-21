package com.amegane3231.qrshare.ui.fragments

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
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
import com.amegane3231.qrshare.extentionFunction.isEditing
import com.amegane3231.qrshare.extentionFunction.isURL
import com.amegane3231.qrshare.extentionFunction.removeHashTagSpans
import com.amegane3231.qrshare.hashTagSpan.HashTagForegroundColorSpan
import com.amegane3231.qrshare.hashTagSpan.HashTagUnderlineSpan
import com.amegane3231.qrshare.interfaces.CustomTextWatcher
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

class CreateFragment : Fragment() {
    private lateinit var binding: FragmentCreateBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateBinding.inflate(inflater, container, false)

        binding.edittextInputURL.addTextChangedListener(object : CustomTextWatcher {
            override fun afterTextChanged(charSequence: Editable?) {
                if (charSequence == null || charSequence.isEmpty()) return
                val data = charSequence.toString()
                if (!data.isURL()) {
                    setError(binding.edittextInputURL)
                    binding.textviewQRCode.isVisible = true
                    binding.imageviewQRCode.setImageBitmap(null)
                    return
                }
                val qrCode = createQRCode(data)
                qrCode?.let {
                    binding.textviewQRCode.isVisible = false
                    binding.imageviewQRCode.setImageBitmap(it)
                }
            }
        })

        binding.edittextInputTag.addTextChangedListener(object : CustomTextWatcher {
            override fun afterTextChanged(charSequence: Editable?) {
                if (charSequence == null || charSequence.isEmpty()) return
                val textView = binding.edittextInputTag
                val spannable: Spannable = textView.editableText ?: SpannableString(textView.text)
                if (spannable.isEditing() || spannable.first() != '#') return
                spannable.removeHashTagSpans()

                val matcher = PATTERN.toRegex().toPattern().matcher(charSequence)
                while(matcher.find()) {
                    if (matcher.groupCount() != 2) continue
                    val content = charSequence.subSequence(matcher.start(2), matcher.end(2))

                    val start = matcher.start(1)
                    val end = matcher.end(1)
                    if (content.length != content.toString().codePointCount(0, content.length)) continue

                    spannable.setSpan(HashTagUnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    HashTagForegroundColorSpan(Color.BLACK).run {
                        spannable.setSpan(this, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    if (spannable !is Editable) {
                        textView.removeTextChangedListener(this)
                        textView.setTextKeepState(spannable)
                        textView.addTextChangedListener(this)
                    }
                }
            }
        })

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
        private const val PATTERN = "(?:^|\\s)(#([^\\s]+))[^\\s]?"
    }
}