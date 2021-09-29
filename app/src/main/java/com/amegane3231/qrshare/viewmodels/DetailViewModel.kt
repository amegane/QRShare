package com.amegane3231.qrshare.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amegane3231.qrshare.usecase.GetQRCodeDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getQRCodeDetailUseCase: GetQRCodeDetailUseCase
) : ViewModel() {
    private val _tagList: MutableLiveData<List<String>> by lazy {
        MutableLiveData<List<String>>()
    }
    val tagList: LiveData<List<String>> get() = _tagList
    private val _url: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val url: LiveData<String> get() = _url

    init {
        _tagList.value = listOf()
        _url.value = ""
    }

    fun getFileData(uid: String, fileName: String) {
        Log.d("uid", uid)
        Log.d("fileName", fileName)

        viewModelScope.launch {
            getQRCodeDetailUseCase.getFileData(uid, fileName).collect { task ->
                task.addOnSuccessListener {
                    _tagList.postValue(it.tags)
                    _url.postValue(it.url)
                }.addOnFailureListener {
                    Log.e("Exception", it.toString())
                }
            }
        }
    }
}