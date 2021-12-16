package com.amegane3231.qrshare.usecase

import com.amegane3231.qrshare.data.QRCode
import com.amegane3231.qrshare.repository.QRCodeRepository
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UploadUseCase @Inject constructor(private val repository: QRCodeRepository) {
    suspend fun uploadQRCode(
        uid: String,
        qrCode: QRCode,
        tags: List<String>,
        date: String
    ): Flow<UploadTask> {
        return flow {
            emit(
                repository.upload(uid, qrCode, tags, date)
            )
        }
    }
}