package com.amegane3231.qrshare.ui.fragments

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.amegane3231.qrshare.R
import com.amegane3231.qrshare.data.QRCode
import com.amegane3231.qrshare.databinding.FragmentUploadBinding
import com.amegane3231.qrshare.extentionFunction.isEditing
import com.amegane3231.qrshare.extentionFunction.isURL
import com.amegane3231.qrshare.extentionFunction.removeHashTagSpans
import com.amegane3231.qrshare.hashTagSpan.HashTagForegroundColorSpan
import com.amegane3231.qrshare.hashTagSpan.HashTagUnderlineSpan
import com.amegane3231.qrshare.interfaces.CustomTextWatcher
import com.amegane3231.qrshare.viewmodels.UploadViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import okhttp3.*
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class UploadFragment : Fragment() {
    private lateinit var binding: FragmentUploadBinding
    private lateinit var auth: FirebaseAuth
    private val args: UploadFragmentArgs by navArgs()
    private val uploadViewModel: UploadViewModel by viewModels()
    private var qrCodeImage: Bitmap? = null
    private var url: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUploadBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)

        val selectedQRCode = args.imageArg
        selectedQRCode?.let {
            binding.textviewQRCode.isVisible = false
            binding.imageviewQRCode.setImageBitmap(it)
        }

        val selectedQRCodeURL = args.urlArg
        selectedQRCodeURL?.let {
            binding.edittextInputURL.setText(it)
        }

        lifecycleScope.launchWhenCreated {
            uploadViewModel.channel.receiveAsFlow().collect {
                if (it.isSuccess) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.toast_finish_upload),
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.toast_fail_upload),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.edittextInputURL.addTextChangedListener(object : CustomTextWatcher {
            override fun afterTextChanged(charSequence: Editable?) {
                if (charSequence == null || charSequence.isEmpty()) return
                val data = charSequence.toString()
                if (!data.isURL()) {
                    binding.edittextInputURL.error = getString(R.string.error_URL)
                    binding.textviewQRCode.isVisible = true
                    binding.imageviewQRCode.setImageBitmap(null)
                    return
                }
                url = data
                val qrCode = createQRCode(data)
                qrCode?.let {
                    this@UploadFragment.qrCodeImage = it
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
                while (matcher.find()) {
                    if (matcher.groupCount() != 2) continue
                    val content = charSequence.subSequence(matcher.start(2), matcher.end(2))

                    val start = matcher.start(1)
                    val end = matcher.end(1)
                    if (content.length != content.toString()
                            .codePointCount(0, content.length)
                    ) continue

                    spannable.setSpan(
                        HashTagUnderlineSpan(),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    HashTagForegroundColorSpan(Color.BLUE).run {
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_add_qr_code, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_submit -> {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val call = client.newCall(request)
                try {
                    call.enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            throw e
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val responseCode = response.code()
                            if (responseCode != 200) {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.toast_invalid_or_fail_access_URL),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return
                            }
                            val date = LocalDateTime.now()
                            val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                            val fileName = auth.uid + dateTimeFormatter.format(date)
                            qrCodeImage?.let {
                                val qrCode = QRCode(it, "$fileName.jpg")
                                uploadViewModel.upload(
                                    qrCode,
                                    auth.uid!!,
                                    binding.edittextInputTag.text.split(" ")
                                )
                            } ?: run {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.toast_prompt_qr_code),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    })
                } catch (e: Exception) {
                    Log.e("Exception", e.toString())
                    binding.edittextInputURL.error = getString(R.string.text_invalid_URL)
                    return false
                }
            }
        }
        return false
    }

    private fun createQRCode(URL: String): Bitmap? {
        return try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap =
                barcodeEncoder.encodeBitmap(URL, BarcodeFormat.QR_CODE, SIZE_QRCODE, SIZE_QRCODE)
            bitmap
        } catch (e: Exception) {
            Log.e("Exception", e.toString())
            binding.edittextInputURL.error = getString(R.string.error_URL)
            null
        }
    }

    companion object {
        private const val SIZE_QRCODE = 512
        private const val PATTERN = "(?:^|\\s)(#([^\\s]+))[^\\s]?"
    }
}