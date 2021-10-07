package com.amegane3231.qrshare.viewmodels

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amegane3231.qrshare.R
import com.amegane3231.qrshare.usecase.GetQRCodeDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
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

    fun saveQRCode(context: Context, activity: FragmentActivity, bitmap: Bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val date = LocalDateTime.now()
            val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

            val collection =
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val uri = context.contentResolver.insert(
                collection,
                ContentValues().apply {
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(
                        MediaStore.Images.Media.TITLE,
                        "QRShare_${dateTimeFormatter.format(date)}"
                    )
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            )!!
            context.contentResolver.openOutputStream(uri).use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }

            context.contentResolver.update(
                uri,
                ContentValues().also { values ->
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                }, null, null
            )
        } else {
            val date = Date()
            val simpleDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val collection =
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val permission =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
                return
            }
            val uri = context.contentResolver.insert(
                collection,
                ContentValues().apply {
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(
                        MediaStore.Images.Media.TITLE,
                        "QRShare_${simpleDateFormat.format(date)}"
                    )
                }
            )!!
            context.contentResolver.openOutputStream(uri).use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
        }
        Toast.makeText(context, context.getString(R.string.toast_save), Toast.LENGTH_SHORT).show()
    }
}