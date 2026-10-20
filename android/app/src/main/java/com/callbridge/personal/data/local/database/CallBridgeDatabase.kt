package com.callbridge.personal.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.callbridge.personal.data.local.dao.CallHistoryDao
import com.callbridge.personal.data.local.dao.ContactDao
import com.callbridge.personal.data.local.entity.CallHistoryEntity
import com.callbridge.personal.data.local.entity.ContactEntity

@Database(
    entities = [ContactEntity::class, CallHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CallBridgeDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao
    abstract fun callHistoryDao(): CallHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: CallBridgeDatabase? = null

        fun getDatabase(context: Context): CallBridgeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CallBridgeDatabase::class.java,
                    "callbridge_database"
                )
                .fallbackToDestructiveMigration() // Default fallback for development
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
