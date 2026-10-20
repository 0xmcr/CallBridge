package com.callbridge.personal.data.repository

import com.callbridge.personal.data.local.dao.CallHistoryDao
import com.callbridge.personal.data.local.entity.CallHistoryEntity
import kotlinx.coroutines.flow.Flow

class CallHistoryRepository(private val callHistoryDao: CallHistoryDao) {

    val allHistory: Flow<List<CallHistoryEntity>> = callHistoryDao.getAllCallHistory()

    fun getRecentCalls(limit: Int = 5): Flow<List<CallHistoryEntity>> {
        return callHistoryDao.getRecentCalls(limit)
    }

    fun getCallById(id: String): Flow<CallHistoryEntity?> {
        return callHistoryDao.getCallHistoryById(id)
    }

    suspend fun saveCallRecord(call: CallHistoryEntity) {
        callHistoryDao.insertCall(call)
    }

    suspend fun updateCallRecord(call: CallHistoryEntity): Int {
        return callHistoryDao.updateCall(call)
    }

    suspend fun deleteCallById(id: String): Int {
        return callHistoryDao.deleteCallById(id)
    }

    suspend fun clearHistory(): Int {
        return callHistoryDao.clearAllHistory()
    }
}
