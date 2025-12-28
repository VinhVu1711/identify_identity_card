package com.vinh.identify_identity_card.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val  id: Long=0,
    val docType: String,
    val mode: String,
    val createdAt: Long = System.currentTimeMillis(),
    val dataJson: String,
    val imageUri: String? = null,
)
