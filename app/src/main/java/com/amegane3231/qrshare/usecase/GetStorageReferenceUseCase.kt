package com.amegane3231.qrshare.usecase

import com.amegane3231.qrshare.data.PageTaskResult
import com.amegane3231.qrshare.repository.QRCodeRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.ListResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetStorageReferenceUseCase @Inject constructor(private val repository: QRCodeRepository) {
    suspend fun listAllPaginated(listPageTask: Task<ListResult>): Flow<Task<PageTaskResult>> {
        return flow {
            emit(
                repository.listAllPaginated(listPageTask)
            )
        }
    }
}