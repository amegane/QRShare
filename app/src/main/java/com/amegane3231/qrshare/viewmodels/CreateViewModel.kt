package com.amegane3231.qrshare.viewmodels

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class CreateViewModel : ViewModel() {
    private val _channel = Channel<Result<Int>>(Channel.UNLIMITED)
    val channel: ReceiveChannel<Result<Int>> get() = _channel
    private val storage: FirebaseStorage = Firebase.storage
    private val storageRef = storage.reference

    fun upload(bitmap: Bitmap, fileName: String) {
        val qrCodeRef = storageRef.child("QRCode/$fileName")

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
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