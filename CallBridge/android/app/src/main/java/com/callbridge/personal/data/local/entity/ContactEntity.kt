package com.callbridge.personal.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "contacts",
    indices = [
        Index(value = ["phoneE164"]),
        Index(value = ["displayName"]),
        Index(value = ["updatedAt"])
    ]
)
data class ContactEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val displayName: String,
    val phoneE164: String,
    val company: String? = null,
    val notes: String? = null,
    val followUpAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
