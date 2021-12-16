package com.amegane3231.qrshare.viewmodels

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amegane3231.qrshare.data.QRCode
import com.amegane3231.qrshare.data.UploadedQRCodeData
import com.amegane3231.qrshare.usecase.UploadUseCase
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(private val uploadUseCase: UploadUseCase) : ViewModel() {
    private val _uploadState = MutableStateFlow<Result<Int>?>(null)

    val uploadState: StateFlow<Result<Int>?> get() = _uploadState

    fun upload(url: String, uid: String, image: Bitmap?, hashTags: List<String>) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                val responseCode = response.code()
                if (responseCode != 200) return
                val date = LocalDateTime.now()
                val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                val fileName = uid + dateTimeFormatter.format(date)
                image?.also {
                    val qrCode = QRCode(it, "$fileName.jpg", url)
                    viewModelScope.launch {
                        uploadUseCase.uploadQRCode(
                            UploadedQRCodeData(
                                uid,
                                qrCode,
                                hashTags,
                                dateTimeFormatter.format(date)
                            )
                        ).collect { task ->
                            task.addOnFailureListener { exception ->
                                Log.e("Exception", exception.toString())
                                Log.v("Success", "Upload Finished")
                                viewModelScope.launch {
                                    _uploadState.emit(Result.failure(exception))
                                }
                            }.addOnSuccessListener {
                                Log.v("Success", "Upload Finished")
                                viewModelScope.launch {
                                    _uploadState.emit(Result.success(1))
                                }
                            }
                        }
                    }
                }

            }
        })
    }

    fun createQRCode(URL: String): Bitmap? {
        return try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap =
                barcodeEncoder.encodeBitmap(
                    URL,
                    BarcodeFormat.QR_CODE,
                    SIZE_QRCODE,
                    SIZE_QRCODE
                )
            bitmap
        } catch (e: Exception) {
            Log.e("Exception", e.toString())
            null
        }
    }

    companion object {
        private const val SIZE_QRCODE = 512
    }
}