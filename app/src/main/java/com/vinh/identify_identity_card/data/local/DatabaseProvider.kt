package com.vinh.identify_identity_card.data.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile private var db: AppDatabase? = null

    fun get(context: Context): AppDatabase =
        db ?: synchronized(this) {
            db ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "id_scanner.db"
            ).build().also { db = it }
        }
}