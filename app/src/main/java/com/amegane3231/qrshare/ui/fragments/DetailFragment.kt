package com.amegane3231.qrshare.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.amegane3231.qrshare.R
import com.amegane3231.qrshare.databinding.FragmentDetailBinding

class DetailFragment : Fragment() {
    private lateinit var binding: FragmentDetailBinding
    private val args: DetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View  {
        binding = FragmentDetailBinding.inflate(inflater, container, false)

        val selectedQRCode = args.imageArg
        binding.imageviewQRCode.setImageBitmap(selectedQRCode)
        val selectedQRCodeURL = args.urlArg
        binding.textviewURL.text = selectedQRCodeURL
        val selectedQRCodeTag = args.tagArg
        selectedQRCodeTag?.let {
            binding.textviewTag.text = it
        }

        return binding.root
    }

}