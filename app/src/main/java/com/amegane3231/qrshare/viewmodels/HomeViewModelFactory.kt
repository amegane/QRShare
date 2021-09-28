package com.amegane3231.qrshare.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.amegane3231.qrshare.di.ViewModelFactory
import javax.inject.Inject

class HomeViewModelFactory @Inject constructor() :
    ViewModelFactory<HomeViewModel> {
    override fun create(handle: SavedStateHandle): HomeViewModel {
        return HomeViewModel()
    }
}