package com.callbridge.personal.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.callbridge.personal.data.local.entity.CallHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CallHistoryDao {

    @Query("SELECT * FROM call_history ORDER BY createdAt DESC")
    fun getAllCallHistory(): Flow<List<CallHistoryEntity>>

    @Query("SELECT * FROM call_history ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentCalls(limit: Int = 10): Flow<List<CallHistoryEntity>>

    @Query("SELECT * FROM call_history WHERE id = :id LIMIT 1")
    fun getCallHistoryById(id: String): Flow<CallHistoryEntity?>

    @Query("SELECT * FROM call_history WHERE providerCallSid = :sid LIMIT 1")
    fun getCallBySid(sid: String): Flow<CallHistoryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: CallHistoryEntity)

    @Update
    suspend fun updateCall(call: CallHistoryEntity): Int

    @Delete
    suspend fun deleteCall(call: CallHistoryEntity): Int

    @Query("DELETE FROM call_history WHERE id = :id")
    suspend fun deleteCallById(id: String): Int

    @Query("DELETE FROM call_history")
    suspend fun clearAllHistory(): Int
}
