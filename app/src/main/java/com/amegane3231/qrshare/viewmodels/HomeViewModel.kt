package com.amegane3231.qrshare.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.component3
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
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
            listPageTask
                .addOnSuccessListener { (items, prefixes, pageToken) ->
                    this@HomeViewModel.pageToken = pageToken
                    Log.d("pageToken", pageToken.toString())
                    _storageList.postValue(items)
                }.addOnFailureListener {
                    Log.e("Exception", it.toString())
                }
        }
    }

    companion object {
        private const val COUNT_RESULTS = 8
    }
}