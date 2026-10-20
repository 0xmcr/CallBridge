package com.callbridge.personal.data.remote.api

import com.callbridge.personal.data.remote.dto.TokenResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface VoiceApiService {

    @POST("api/v1/voice/token")
    suspend fun getVoiceToken(
        @Body body: Map<String, String> = emptyMap()
    ): TokenResponse
}
