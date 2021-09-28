package com.amegane3231.qrshare.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.amegane3231.qrshare.di.ViewModelFactory
import javax.inject.Inject

class DetailViewModelFactory @Inject constructor() :
    ViewModelFactory<DetailViewModel> {
    override fun create(handle: SavedStateHandle): DetailViewModel {
        return DetailViewModel()
    }
}