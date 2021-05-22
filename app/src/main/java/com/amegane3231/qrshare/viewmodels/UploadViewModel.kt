package com.amegane3231.qrshare.viewmodels

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amegane3231.qrshare.data.QRCode
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class UploadViewModel : ViewModel() {
    private val _channel = Channel<Result<Int>>(Channel.UNLIMITED)
    val channel: ReceiveChannel<Result<Int>> get() = _channel
    private val storage: FirebaseStorage = Firebase.storage
    private val storageRef = storage.reference
    private val database = Firebase.firestore

    fun upload(qrCode: QRCode, uid: String, tags: List<String>) {
        val fileData = hashMapOf(
            "uid" to uid,
            "name" to qrCode.name,
            "tags" to tags
        )

        database.collection(uid)
            .add(fileData)
            .addOnSuccessListener {
                Log.d("Add document success", it.id)
            }
            .addOnFailureListener {
                Log.w("Add document failure", it)
            }

        val qrCodeRef = storageRef.child("QRCode/${qrCode.name}")
        val byteArrayOutputStream = ByteArrayOutputStream()
        qrCode.image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val data = byteArrayOutputStream.toByteArray()
        val uploadTask = qrCodeRef.putBytes(data)
        uploadTask.addOnFailureListener {
            Log.e("Exception", it.toString())
            Log.v("Success", "Upload Finished")
            viewModelScope.launch {
                _channel.send(Result.failure(it))
            }
        }.addOnSuccessListener {
            Log.v("Success", "Upload Finished")
            viewModelScope.launch {
                _channel.send(Result.success(1))
            }
        }
    }
}