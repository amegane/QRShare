package com.amegane3231.qrshare.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.amegane3231.qrshare.databinding.FragmentDetailBinding
import com.amegane3231.qrshare.viewmodels.DetailViewModel
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

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

        detailViewModel.url.observe(viewLifecycleOwner, Observer {
            binding.textviewURL.text = it
        })

        detailViewModel.tagList.observe(viewLifecycleOwner, Observer {
            binding.textviewTag.text = it.joinToString(", ")
        })

        return binding.root
    }
}