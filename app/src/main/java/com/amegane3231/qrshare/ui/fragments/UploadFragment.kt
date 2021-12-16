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
import com.amegane3231.qrshare.databinding.FragmentUploadBinding
import com.amegane3231.qrshare.di.withFactory
import com.amegane3231.qrshare.extentionFunction.isEditing
import com.amegane3231.qrshare.extentionFunction.isURL
import com.amegane3231.qrshare.extentionFunction.removeHashTagSpans
import com.amegane3231.qrshare.hashTagSpan.HashTagForegroundColorSpan
import com.amegane3231.qrshare.hashTagSpan.HashTagUnderlineSpan
import com.amegane3231.qrshare.interfaces.CustomTextWatcher
import com.amegane3231.qrshare.viewmodels.UploadViewModel
import com.amegane3231.qrshare.viewmodels.UploadViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.*
import javax.inject.Inject

@AndroidEntryPoint
class UploadFragment : Fragment() {
    @Inject
    lateinit var uploadViewModelFactory: UploadViewModelFactory

    private lateinit var binding: FragmentUploadBinding

    private lateinit var auth: FirebaseAuth

    private val args: UploadFragmentArgs by navArgs()

    private val uploadViewModel: UploadViewModel by viewModels { withFactory(uploadViewModelFactory) }

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
        return binding.root
    }

    @InternalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedQRCode = args.imageArg
        selectedQRCode?.let {
            binding.textviewQRCode.isVisible = false
            qrCodeImage = it
            binding.imageviewQRCode.setImageBitmap(it)
        }

        val selectedQRCodeURL = args.urlArg
        selectedQRCodeURL?.let {
            binding.edittextInputURL.setText(it)
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
                val qrCode = uploadViewModel.createQRCode(data)
                if (qrCode != null) {
                    this@UploadFragment.qrCodeImage = qrCode
                    binding.textviewQRCode.isVisible = false
                    binding.imageviewQRCode.setImageBitmap(qrCode)
                } else {
                    binding.edittextInputURL.error = getString(R.string.error_URL)
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

        lifecycleScope.launch {
            uploadViewModel.uploadState.collect {
                if (it == null) {
                    return@collect
                }
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
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_add_qr_code, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_submit -> {
                url = binding.edittextInputURL.text.toString()
                if (!url.isURL()) {
                    binding.edittextInputURL.error = getString(R.string.error_URL)
                    binding.textviewQRCode.isVisible = true
                    binding.imageviewQRCode.setImageBitmap(null)
                    return false
                }
                val list = binding.edittextInputTag.text.split(" ")
                val hashTags = list.filter { it.startsWith("#") }
                if (list.size != hashTags.size) {
                    binding.edittextInputTag.error = getString(R.string.error_hash_tag)
                    return false
                }

                try {
                    uploadViewModel.upload(url, auth.uid!!, qrCodeImage, hashTags)
                } catch (e: Exception) {
                    Log.e("Exception", e.toString())
                    binding.edittextInputURL.error = getString(R.string.text_invalid_URL)
                    return false
                }
            }
        }
        return false
    }

    companion object {
        private const val PATTERN = "(?:^|\\s)(#([^\\s]+))[^\\s]?"
    }
}