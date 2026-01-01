package com.vinh.identify_identity_card.data.repo

import com.vinh.identify_identity_card.data.local.HistoryDao
import com.vinh.identify_identity_card.data.local.HistoryEntity
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val dao: HistoryDao) {
    fun getAll(): Flow<List<HistoryEntity>> = dao.getAll()

    suspend fun insertManual(docType: String, dataJson: String) {
        dao.insert(
            HistoryEntity(
                docType = docType,
                mode = "MANUAL",
                dataJson = dataJson
            )
        )
    }
    suspend fun insertScan(docType: String, dataJson: String, imageUri: String?) {
        dao.insert(
            HistoryEntity(
                docType = docType,
                mode = "SCAN",
                dataJson = dataJson,
                imageUri = imageUri
            )
        )
    }

}