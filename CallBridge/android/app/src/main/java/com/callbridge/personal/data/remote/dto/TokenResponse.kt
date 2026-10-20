package com.callbridge.personal.data.remote.dto

data class TokenResponse(
    val token: String,
    val identity: String,
    val expiresAt: String
)
