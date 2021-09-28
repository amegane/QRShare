package com.amegane3231.qrshare.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.amegane3231.qrshare.di.ViewModelFactory
import com.amegane3231.qrshare.usecase.UploadUseCase
import javax.inject.Inject

class UploadViewModelFactory @Inject constructor(private val uploadUseCase: UploadUseCase) :
    ViewModelFactory<UploadViewModel> {
    override fun create(handle: SavedStateHandle): UploadViewModel {
        return UploadViewModel(uploadUseCase)
    }
}