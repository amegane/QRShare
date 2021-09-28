package com.amegane3231.qrshare.usecase

import com.amegane3231.qrshare.data.UploadedQRCodeData
import com.amegane3231.qrshare.repository.QRCodeRepository
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UploadUseCase @Inject constructor(private val repository: QRCodeRepository) {
    fun uploadQRCode(fileData: UploadedQRCodeData): Flow<UploadTask> {
        return flow {
            emit(
                repository.upload(fileData)
            )
        }
    }
}