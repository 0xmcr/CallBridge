package com.callbridge.personal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.callbridge.personal.calling.CallManager
import com.callbridge.personal.calling.CallUiState
import com.callbridge.personal.data.repository.CallHistoryRepository
import com.callbridge.personal.data.repository.ContactsRepository
import com.callbridge.personal.navigation.Screen
import com.callbridge.personal.ui.activecall.ActiveCallScreen
import com.callbridge.personal.ui.components.CallBridgeBottomBar
import com.callbridge.personal.ui.contacts.AddEditContactScreen
import com.callbridge.personal.ui.contacts.ContactDetailScreen
import com.callbridge.personal.ui.contacts.ContactsScreen
import com.callbridge.personal.ui.dashboard.DashboardScreen
import com.callbridge.personal.ui.dialpad.DialPadScreen
import com.callbridge.personal.ui.history.CallDetailScreen
import com.callbridge.personal.ui.history.CallHistoryScreen
import com.callbridge.personal.ui.settings.SettingsScreen
import com.callbridge.personal.ui.theme.CallBridgeTheme
import com.callbridge.personal.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {

    private val app by lazy { application as CallBridgeApplication }
    val callManager: CallManager by lazy { app.callManager }
    val contactsRepository: ContactsRepository by lazy { app.contactsRepository }
    val callHistoryRepository: CallHistoryRepository by lazy { app.callHistoryRepository }
    val themeManager: ThemeManager by lazy { app.themeManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by themeManager.themeMode.collectAsState()
            var showFirstTimePermissionDialog by androidx.compose.runtime.remember {
                androidx.compose.runtime.mutableStateOf(
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                )
            }

            CallBridgeTheme(themeMode = themeMode) {
                if (showFirstTimePermissionDialog) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showFirstTimePermissionDialog = false },
                        icon = {
                            androidx.compose.material3.Icon(
                                imageVector = androidx.compose.material.icons.Icons.Filled.Phone,
                                contentDescription = null,
                                tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        },
                        title = {
                            androidx.compose.material3.Text(
                                "Microphone Access Required",
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        },
                        text = {
                            androidx.compose.material3.Text(
                                "CallBridge transmits high-definition VoIP audio to regular US mobile and landline phones. Microphone access is required so your recipients can hear you.\n\nYour voice audio is never recorded, stored, or synced to any cloud servers.",
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        confirmButton = {
                            androidx.compose.material3.Button(
                                onClick = {
                                    showFirstTimePermissionDialog = false
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            ) {
                                androidx.compose.material3.Text("Grant Microphone Access")
                            }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(
                                onClick = { showFirstTimePermissionDialog = false }
                            ) {
                                androidx.compose.material3.Text("Not Now")
                            }
                        }
                    )
                }

                MainApp(
                    callManager = callManager,
                    contactsRepository = contactsRepository,
                    callHistoryRepository = callHistoryRepository,
                    themeManager = themeManager,
                    onCheckAudioPermission = { onGranted ->
                        checkAndRequestAudioPermission(onGranted)
                    }
                )
            }
        }
    }

    private var pendingCallAction: (() -> Unit)? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingCallAction?.invoke()
        } else {
            Toast.makeText(this, "Microphone permission is required to place calls", Toast.LENGTH_LONG).show()
        }
        pendingCallAction = null
    }

    private fun checkAndRequestAudioPermission(onGranted: () -> Unit) {
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            onGranted()
        } else {
            pendingCallAction = onGranted
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}

@Composable
fun MainApp(
    callManager: CallManager,
    contactsRepository: ContactsRepository,
    callHistoryRepository: CallHistoryRepository,
    themeManager: ThemeManager,
    onCheckAudioPermission: (() -> Unit) -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val callState by callManager.callState.collectAsState()
    val isMuted by callManager.isMuted.collectAsState()
    val isSpeakerOn by callManager.isSpeakerOn.collectAsState()
    val isDialSoundEnabled by themeManager.dialSoundEnabled.collectAsState()

    // Show BottomBar only on primary top-level tabs when not in an active call
    val isBottomBarVisible = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Contacts.route,
        Screen.History.route,
        Screen.Settings.route
    ) && (callState is CallUiState.Idle || callState is CallUiState.Ended || callState is CallUiState.Failed)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isBottomBarVisible) {
                CallBridgeBottomBar(
                    currentRoute = currentRoute,
                    onNavigateToRoute = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Dashboard.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Dashboard
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    callHistoryRepository = callHistoryRepository,
                    onNavigateToDialPad = {
                        navController.navigate(Screen.DialPad.createRoute())
                    },
                    onNavigateToContacts = {
                        navController.navigate(Screen.Contacts.route)
                    },
                    onNavigateToHistory = {
                        navController.navigate(Screen.History.route)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onCallNumber = { number ->
                        onCheckAudioPermission {
                            navController.navigate(Screen.ActiveCall.createRoute(number))
                            val activity = navController.context as? ComponentActivity
                            activity?.lifecycleScope?.let { scope ->
                                callManager.initiateCall(number, "", scope)
                            }
                        }
                    },
                    onNavigateToCallDetail = { callId ->
                        navController.navigate(Screen.CallDetail.createRoute(callId))
                    }
                )
            }

            // Dial Pad
            composable(
                route = Screen.DialPad.route,
                arguments = listOf(
                    navArgument("initialNumber") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val initialNumber = backStackEntry.arguments?.getString("initialNumber") ?: ""
                DialPadScreen(
                    initialNumber = initialNumber,
                    dialSoundEnabled = isDialSoundEnabled,
                    onNavigateBack = { navController.popBackStack() },
                    onStartCall = { destination ->
                        onCheckAudioPermission {
                            navController.navigate(Screen.ActiveCall.createRoute(destination))
                            val activity = navController.context as? ComponentActivity
                            activity?.lifecycleScope?.let { scope ->
                                callManager.initiateCall(destination, "", scope)
                            }
                        }
                    }
                )
            }

            // Active Call Screen
            composable(
                route = Screen.ActiveCall.route,
                arguments = listOf(
                    navArgument("destination") { type = NavType.StringType },
                    navArgument("contactName") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val destination = backStackEntry.arguments?.getString("destination") ?: ""
                val contactName = backStackEntry.arguments?.getString("contactName") ?: ""
                ActiveCallScreen(
                    destination = destination,
                    contactName = contactName,
                    callState = callState,
                    isMuted = isMuted,
                    isSpeakerOn = isSpeakerOn,
                    onToggleMute = { callManager.toggleMute() },
                    onToggleSpeaker = { callManager.toggleSpeaker() },
                    onEndCall = {
                        callManager.endCall()
                        callManager.resetStateToIdle()
                        navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                    }
                )
            }

            // Contacts
            composable(Screen.Contacts.route) {
                ContactsScreen(
                    contactsRepository = contactsRepository,
                    onNavigateToAddContact = {
                        navController.navigate(Screen.AddEditContact.createRoute())
                    },
                    onNavigateToContactDetail = { contactId ->
                        navController.navigate(Screen.ContactDetail.createRoute(contactId))
                    },
                    onCallNumber = { number ->
                        onCheckAudioPermission {
                            navController.navigate(Screen.ActiveCall.createRoute(number))
                            val activity = navController.context as? ComponentActivity
                            activity?.lifecycleScope?.let { scope ->
                                callManager.initiateCall(number, "", scope)
                            }
                        }
                    }
                )
            }

            // Contact Detail
            composable(
                route = Screen.ContactDetail.route,
                arguments = listOf(navArgument("contactId") { type = NavType.StringType })
            ) { backStackEntry ->
                val contactId = backStackEntry.arguments?.getString("contactId") ?: ""
                ContactDetailScreen(
                    contactId = contactId,
                    contactsRepository = contactsRepository,
                    onNavigateBack = { navController.popBackStack() },
                    onEditContact = { id ->
                        navController.navigate(Screen.AddEditContact.createRoute(id))
                    },
                    onCallNumber = { number ->
                        onCheckAudioPermission {
                            navController.navigate(Screen.ActiveCall.createRoute(number))
                            val activity = navController.context as? ComponentActivity
                            activity?.lifecycleScope?.let { scope ->
                                callManager.initiateCall(number, "", scope)
                            }
                        }
                    }
                )
            }

            // Add / Edit Contact
            composable(
                route = Screen.AddEditContact.route,
                arguments = listOf(
                    navArgument("contactId") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val contactId = backStackEntry.arguments?.getString("contactId") ?: ""
                AddEditContactScreen(
                    contactId = contactId,
                    contactsRepository = contactsRepository,
                    onNavigateBack = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }

            // Call History
            composable(Screen.History.route) {
                CallHistoryScreen(
                    callHistoryRepository = callHistoryRepository,
                    onNavigateToDetail = { callId ->
                        navController.navigate(Screen.CallDetail.createRoute(callId))
                    },
                    onCallNumber = { number ->
                        onCheckAudioPermission {
                            navController.navigate(Screen.ActiveCall.createRoute(number))
                            val activity = navController.context as? ComponentActivity
                            activity?.lifecycleScope?.let { scope ->
                                callManager.initiateCall(number, "", scope)
                            }
                        }
                    }
                )
            }

            // Call Detail
            composable(
                route = Screen.CallDetail.route,
                arguments = listOf(navArgument("callId") { type = NavType.StringType })
            ) { backStackEntry ->
                val callId = backStackEntry.arguments?.getString("callId") ?: ""
                CallDetailScreen(
                    callId = callId,
                    callHistoryRepository = callHistoryRepository,
                    onNavigateBack = { navController.popBackStack() },
                    onCallNumber = { number ->
                        onCheckAudioPermission {
                            navController.navigate(Screen.ActiveCall.createRoute(number))
                            val activity = navController.context as? ComponentActivity
                            activity?.lifecycleScope?.let { scope ->
                                callManager.initiateCall(number, "", scope)
                            }
                        }
                    }
                )
            }

            // Settings
            composable(Screen.Settings.route) {
                SettingsScreen(themeManager = themeManager)
            }
        }
    }
}
