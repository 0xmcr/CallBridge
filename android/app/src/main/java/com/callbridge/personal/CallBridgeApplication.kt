package com.callbridge.personal

import android.app.Application
import com.callbridge.personal.calling.CallManager
import com.callbridge.personal.data.local.database.CallBridgeDatabase
import com.callbridge.personal.data.remote.api.ApiClient
import com.callbridge.personal.data.remote.api.VoiceApiService
import com.callbridge.personal.data.repository.CallHistoryRepository
import com.callbridge.personal.data.repository.ContactsRepository

class CallBridgeApplication : Application() {

    val database: CallBridgeDatabase by lazy {
        CallBridgeDatabase.getDatabase(this)
    }

    val contactsRepository: ContactsRepository by lazy {
        ContactsRepository(database.contactDao())
    }

    val callHistoryRepository: CallHistoryRepository by lazy {
        CallHistoryRepository(database.callHistoryDao())
    }

    val voiceApiService: VoiceApiService by lazy {
        ApiClient.createVoiceApiService()
    }

    val callManager: CallManager by lazy {
        CallManager(this, voiceApiService, callHistoryRepository)
    }

    val themeManager: com.callbridge.personal.ui.theme.ThemeManager by lazy {
        com.callbridge.personal.ui.theme.ThemeManager(this)
    }

    override fun onCreate() {
        super.onCreate()
    }
}
