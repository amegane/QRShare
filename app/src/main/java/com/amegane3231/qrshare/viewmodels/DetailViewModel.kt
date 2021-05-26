package com.amegane3231.qrshare.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class DetailViewModel : ViewModel() {
    private val dataBase = Firebase.firestore
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
        dataBase.collection(uid).whereIn("name", mutableListOf(fileName)).get()
            .addOnSuccessListener { result ->
                val data = result.documents[0].data
                val tags = data?.getValue("tags") as ArrayList<String>
                val url = data.getValue("url") as String
                viewModelScope.launch {
                    _tagList.postValue(tags)
                    _url.postValue(url)
                }
            }
            .addOnFailureListener {
                Log.e("Exception", it.toString())
            }
    }
}