package com.callbridge.personal.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "call_history",
    foreignKeys = [
        ForeignKey(
            entity = ContactEntity::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["startedAt"]),
        Index(value = ["contactId"]),
        Index(value = ["destinationE164"]),
        Index(value = ["status"])
    ]
)
data class CallHistoryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val contactId: String? = null,
    val destinationE164: String,
    val direction: String = "OUTGOING",
    val status: String, // COMPLETED, FAILED, CANCELLED, UNKNOWN
    val startedAt: Long? = null,
    val connectedAt: Long? = null,
    val endedAt: Long? = null,
    val durationSeconds: Long? = null,
    val providerCallSid: String? = null,
    val errorCode: String? = null,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
