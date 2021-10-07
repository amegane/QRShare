package com.amegane3231.qrshare.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.amegane3231.qrshare.di.ViewModelFactory
import com.amegane3231.qrshare.usecase.GetStorageReferenceUseCase
import javax.inject.Inject

class HomeViewModelFactory @Inject constructor(private val getStorageReferenceUseCase: GetStorageReferenceUseCase) :
    ViewModelFactory<HomeViewModel> {
    override fun create(handle: SavedStateHandle): HomeViewModel {
        return HomeViewModel(getStorageReferenceUseCase)
    }
}