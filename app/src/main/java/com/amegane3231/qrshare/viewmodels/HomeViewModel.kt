package com.amegane3231.qrshare.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.component3
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val storage = Firebase.storage
    private val listRef = storage.reference.child("QRCode")
    private val dataBase = Firebase.firestore
    private val _storageList: MutableLiveData<List<StorageReference>> by lazy {
        MutableLiveData<List<StorageReference>>()
    }
    val storageList: LiveData<List<StorageReference>> get() = _storageList
    private val _flow = MutableSharedFlow<List<String>>(Channel.UNLIMITED)
    val flow: SharedFlow<List<String>> get() = _flow

    init {
        _storageList.value = listOf()
    }

    fun listAllPaginated(pageToken: String?) {
        val listPageTask = if (pageToken != null) {
            listRef.list(100, pageToken)
        } else {
            listRef.list(100)
        }

        viewModelScope.launch {
            listPageTask
                .addOnSuccessListener { (items, prefixes, pageToken) ->
                    _storageList.postValue(items)
                }.addOnFailureListener {
                    Log.e("Exception", it.toString())
                }
        }
    }

    fun getTags(uid: String, fileName: String) {
        Log.d("uid", uid)
        Log.d("fileName", fileName)
        dataBase.collection(uid).whereIn("name", mutableListOf(fileName)).get()
            .addOnSuccessListener { result ->
                val data = result.documents[0].data
                val tag = data?.getValue("tags") as ArrayList<String>
                Log.d("Tags", tag.toString())
                viewModelScope.launch {
                    _flow.emit(tag)
                }
            }
            .addOnFailureListener {
                Log.e("Exception", it.toString())
            }
    }

//    fun getTags (uid: String, fileName: String) {
//        viewModelScope.launch {
//            dataBase.collection(uid).whereIn("name", mutableListOf(fileName)).get()
//                .addOnSuccessListener { result ->
//                    val list = mutableListOf<String>()
//                    for(document in result) {
//                        val data = document.data
//                        val tag = data.getValue("tags")
//                        list.add(tag.toString())
//                    }
//                    Log.d("Tags", list.toString())
//                    _tagsList.postValue(list)
//                }
//                .addOnFailureListener {
//                    Log.e("Exception", it.toString())
//                }
//        }
//    }
}