package com.amegane3231.qrshare.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amegane3231.qrshare.usecase.GetStorageReferenceUseCase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getStorageReferenceUseCase: GetStorageReferenceUseCase
) : ViewModel() {
    private val storage = Firebase.storage
    private val listRef = storage.reference.child("QRCode")
    private val _storageList: MutableLiveData<List<StorageReference>> by lazy {
        MutableLiveData<List<StorageReference>>()
    }
    val storageList: LiveData<List<StorageReference>> get() = _storageList
    private var pageToken: String? = null
    private var isTokenNullable = true

    init {
        _storageList.value = listOf()
    }

    fun initialize() {
        isTokenNullable = true
        _storageList.value = listOf()
    }

    fun listAllPaginated(pageToken: String?) {
        val listPageTask = if (pageToken != null) {
            listRef.list(COUNT_RESULTS, pageToken)
        } else if (this.pageToken != null) {
            listRef.list(COUNT_RESULTS, this.pageToken!!)
        } else if (isTokenNullable) {
            isTokenNullable = false
            listRef.list(COUNT_RESULTS)
        } else {
            return
        }

        viewModelScope.launch {
            getStorageReferenceUseCase.listAllPaginated(listPageTask).collect { task ->
                task.addOnSuccessListener {
                    this@HomeViewModel.pageToken = it.pageToken
                    Log.d("pageToken", pageToken.toString())
                    _storageList.postValue(it.items)
                }.addOnFailureListener {
                    Log.e("Exception", it.toString())
                }
            }
        }
    }

    companion object {
        private const val COUNT_RESULTS = 8
    }
}