package com.vinh.identify_identity_card.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert suspend fun insert(item: HistoryEntity): Long

    @Query("SELECT * FROM history ORDER BY createdAt DESC")
    //Don't need to use suspend here because Flow is data stream, automatically update
    fun getAll() : Flow<List<HistoryEntity>>

    @Query("DELETE FROM history")
    suspend fun clearAll()
}