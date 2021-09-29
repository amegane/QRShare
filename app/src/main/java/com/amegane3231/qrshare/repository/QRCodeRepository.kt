package com.amegane3231.qrshare.repository

import android.graphics.Bitmap
import android.util.Log
import com.amegane3231.qrshare.data.EnteredQRCodeData
import com.amegane3231.qrshare.data.PageTaskResult
import com.amegane3231.qrshare.data.UploadedQRCodeData
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QRCodeRepository @Inject constructor() {
    private val database = Firebase.firestore
    private val storage: FirebaseStorage = Firebase.storage
    private val storageRef = storage.reference
    private val listRef = storage.reference.child("QRCode")

    suspend fun upload(fileData: UploadedQRCodeData): UploadTask {
        createFileId(fileData)

        val qrCodeRef = storageRef.child("QRCode/${fileData.qrCode.name}")
        val byteArrayOutputStream = ByteArrayOutputStream()
        fileData.qrCode.image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val data = byteArrayOutputStream.toByteArray()
        return qrCodeRef.putBytes(data)
    }

    private suspend fun createFileId(fileData: UploadedQRCodeData) {
        database.collection(fileData.uid)
            .add(fileData)
            .addOnSuccessListener {
                Log.d("Add document success", it.id)
            }
            .addOnFailureListener {
                Log.w("Add document failure", it)
            }
    }

    suspend fun getFileData(uid: String, fileName: String): Task<EnteredQRCodeData> {
        val taskCompletionSource = TaskCompletionSource<EnteredQRCodeData>()

        database.collection(uid).whereIn("name", mutableListOf(fileName)).get()
            .addOnSuccessListener { result ->
                val data = result.documents[0].data
                val tags = data?.getValue("tags") as ArrayList<String>
                val url = data.getValue("url") as String
                taskCompletionSource.setResult(EnteredQRCodeData(tags, url))
            }
            .addOnFailureListener {
                Log.e("Exception", it.toString())
                taskCompletionSource.setResult(null)
            }

        return taskCompletionSource.task
    }

    suspend fun listAllPaginated(listPageTask: Task<ListResult>): Task<PageTaskResult> {
        val taskCompleteSource = TaskCompletionSource<PageTaskResult>()
        listPageTask
            .addOnSuccessListener { result ->
                Log.d("pageToken", result.toString())
                taskCompleteSource.setResult(PageTaskResult(result.items, result.pageToken))
            }.addOnFailureListener {
                Log.e("Exception", it.toString())
                taskCompleteSource.setResult(null)
            }

        return taskCompleteSource.task
    }
}