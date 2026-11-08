package com.callbridge.personal.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.callbridge.personal.data.local.dao.CallHistoryDao
import com.callbridge.personal.data.local.dao.ContactDao
import com.callbridge.personal.data.local.dao.MessageDao
import com.callbridge.personal.data.local.entity.CallHistoryEntity
import com.callbridge.personal.data.local.entity.ContactEntity
import com.callbridge.personal.data.local.entity.MessageEntity

@Database(
    entities = [ContactEntity::class, CallHistoryEntity::class, MessageEntity::class],
    version = 2,
    exportSchema = false
)
abstract class CallBridgeDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao
    abstract fun callHistoryDao(): CallHistoryDao
    abstract fun messageDao(): MessageDao

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
