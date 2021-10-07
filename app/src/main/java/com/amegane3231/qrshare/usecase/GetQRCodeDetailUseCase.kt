package com.amegane3231.qrshare.usecase

import com.amegane3231.qrshare.data.EnteredQRCodeData
import com.amegane3231.qrshare.repository.QRCodeRepository
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetQRCodeDetailUseCase @Inject constructor(private val repository: QRCodeRepository) {
    suspend fun getFileData(uid: String, fileName: String): Flow<Task<EnteredQRCodeData>> {
        return flow {
            emit(
                repository.getFileData(uid, fileName)
            )
        }
    }
}