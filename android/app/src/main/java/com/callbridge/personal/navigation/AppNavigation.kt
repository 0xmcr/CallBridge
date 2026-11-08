package com.callbridge.personal.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Dashboard : Screen("dashboard")
    data object DialPad : Screen("dialpad?initialNumber={initialNumber}") {
        fun createRoute(initialNumber: String = ""): String {
            return if (initialNumber.isNotEmpty()) "dialpad?initialNumber=$initialNumber" else "dialpad"
        }
    }
    data object ActiveCall : Screen("active_call?destination={destination}&contactName={contactName}") {
        fun createRoute(destination: String, contactName: String = ""): String {
            return "active_call?destination=$destination&contactName=$contactName"
        }
    }
    data object Contacts : Screen("contacts")
    data object ContactDetail : Screen("contact_detail/{contactId}") {
        fun createRoute(contactId: String): String = "contact_detail/$contactId"
    }
    data object AddEditContact : Screen("add_edit_contact?contactId={contactId}") {
        fun createRoute(contactId: String = ""): String {
            return if (contactId.isNotEmpty()) "add_edit_contact?contactId=$contactId" else "add_edit_contact"
        }
    }
    data object History : Screen("history")
    data object CallDetail : Screen("call_detail/{callId}") {
        fun createRoute(callId: String): String = "call_detail/$callId"
    }
    data object Messages : Screen("messages")
    data object ChatThread : Screen("chat_thread/{address}") {
        fun createRoute(address: String): String = "chat_thread/${java.net.URLEncoder.encode(address, "UTF-8")}"
    }
    data object Settings : Screen("settings")
}
