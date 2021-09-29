package com.amegane3231.qrshare.ui.fragments

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.amegane3231.qrshare.databinding.FragmentDetailBinding
import com.amegane3231.qrshare.di.withFactory
import com.amegane3231.qrshare.recyclerView.FlexboxListAdapter
import com.amegane3231.qrshare.viewmodels.DetailViewModel
import com.amegane3231.qrshare.viewmodels.DetailViewModelFactory
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DetailFragment : Fragment() {
    @Inject
    lateinit var detailViewModelFactory: DetailViewModelFactory

    private lateinit var binding: FragmentDetailBinding

    private val args: DetailFragmentArgs by navArgs()

    private val detailViewModel: DetailViewModel by viewModels { withFactory(detailViewModelFactory) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val path = args.pathArg
        val storageRef = Firebase.storage.getReference(path)
        Glide.with(requireContext()).load(storageRef).into(binding.imageviewQRCode)

        val selectedQRCodeUid = args.uidArg
        val selectedQRCodeName = args.imageNameArg
        detailViewModel.getFileData(selectedQRCodeUid, selectedQRCodeName)

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
            detailViewModel.saveQRCode(
                requireContext(),
                requireActivity(),
                (binding.imageviewQRCode.drawable as BitmapDrawable).bitmap
            )
        }
    }
}