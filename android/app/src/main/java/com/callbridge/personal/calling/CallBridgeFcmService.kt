package com.callbridge.personal.calling

import android.util.Log
import com.callbridge.personal.CallBridgeApplication
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Handles Firebase Cloud Messaging (FCM) push notifications for incoming Twilio Voice calls.
 */
class CallBridgeFcmService : FirebaseMessagingService() {

    private val tag = "CallBridgeFcmService"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(tag, "Refreshed FCM token received")
        val app = applicationContext as? CallBridgeApplication ?: return
        app.callManager.registerForIncomingCalls(
            coroutineScope = CoroutineScope(Dispatchers.IO),
            overrideFcmToken = token
        )
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(tag, "FCM data payload received for incoming call check")
            val app = applicationContext as? CallBridgeApplication ?: return
            app.callManager.handleIncomingCallMessage(remoteMessage.data)
        }
    }
}
