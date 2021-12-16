package com.amegane3231.qrshare.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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

    private val _storageList: MutableLiveData<MutableList<StorageReference>> by lazy {
        MutableLiveData<MutableList<StorageReference>>()
    }

    val storageList: MutableLiveData<MutableList<StorageReference>> get() = _storageList

    private val _searchedQRCodePathList: MutableLiveData<List<StorageReference>> by lazy {
        MutableLiveData<List<StorageReference>>()
    }

    val searchedQRCodePathList: LiveData<List<StorageReference>> get() = _searchedQRCodePathList

    private var pageToken: String? = null

    private var isTokenNullable = true

    init {
        _storageList.value = mutableListOf()
    }

    fun initialize() {
        pageToken = null
        isTokenNullable = true
        _storageList.value = mutableListOf()
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
                    _storageList.value?.addAll(it.items)
                    _storageList.postValue(_storageList.value)
                }.addOnFailureListener {
                    Log.e("Exception", it.toString())
                }
            }
        }
    }

    @ExperimentalStdlibApi
    fun searchQRCode(query: String) {
        viewModelScope.launch {
            getStorageReferenceUseCase.searchQRCode(query).collect { task ->
                task.addOnSuccessListener {
                    _searchedQRCodePathList.postValue(it)
                }.addOnFailureListener {
                    Log.e("Exception", it.toString())
                }
            }
        }
    }

    fun getBitmap(context: Context, uri: Uri): Bitmap {
        val openFileDescriptor =
            context.contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = openFileDescriptor?.fileDescriptor
        val bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        openFileDescriptor?.close()
        return bitmap
    }

    companion object {
        private const val COUNT_RESULTS = 8
    }
}