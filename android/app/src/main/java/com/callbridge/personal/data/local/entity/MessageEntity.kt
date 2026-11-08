package com.callbridge.personal.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["address"]),
        Index(value = ["timestamp"])
    ]
)
data class MessageEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val address: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isIncoming: Boolean = false,
    val deliveryStatus: String = "SENT"
)
