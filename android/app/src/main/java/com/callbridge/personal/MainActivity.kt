package com.callbridge.personal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.Call
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
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
import com.callbridge.personal.data.repository.MessagesRepository
import com.callbridge.personal.navigation.Screen
import com.callbridge.personal.ui.activecall.ActiveCallScreen
import com.callbridge.personal.ui.activecall.IncomingCallScreen
import com.callbridge.personal.ui.components.CallBridgeBottomBar
import com.callbridge.personal.ui.contacts.AddEditContactScreen
import com.callbridge.personal.ui.contacts.ContactDetailScreen
import com.callbridge.personal.ui.contacts.ContactsScreen
import com.callbridge.personal.ui.dashboard.DashboardScreen
import com.callbridge.personal.ui.dialpad.DialPadScreen
import com.callbridge.personal.ui.history.CallDetailScreen
import com.callbridge.personal.ui.history.CallHistoryScreen
import com.callbridge.personal.ui.messages.ChatThreadScreen
import com.callbridge.personal.ui.messages.MessagesScreen
import com.callbridge.personal.ui.onboarding.OnboardingScreen
import com.callbridge.personal.ui.onboarding.SplashScreen
import com.callbridge.personal.ui.settings.SettingsScreen
import com.callbridge.personal.ui.theme.CallBridgeTheme
import com.callbridge.personal.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {

    private val app by lazy { application as CallBridgeApplication }
    val callManager: CallManager by lazy { app.callManager }
    val contactsRepository: ContactsRepository by lazy { app.contactsRepository }
    val callHistoryRepository: CallHistoryRepository by lazy { app.callHistoryRepository }
    val messagesRepository: MessagesRepository by lazy { app.messagesRepository }
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
                                imageVector = androidx.compose.material.icons.Icons.Filled.Call,
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
                    messagesRepository = messagesRepository,
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
    messagesRepository: MessagesRepository,
    themeManager: ThemeManager,
    onCheckAudioPermission: (() -> Unit) -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val callState by callManager.callState.collectAsState()
    val isMuted by callManager.isMuted.collectAsState()
    val isSpeakerOn by callManager.isSpeakerOn.collectAsState()
    val isHeld by callManager.isHeld.collectAsState()
    val activeCallNote by callManager.activeCallNote.collectAsState()
    val isDialSoundEnabled by themeManager.dialSoundEnabled.collectAsState()
    val hasCompletedOnboarding by themeManager.hasCompletedOnboarding.collectAsState()
    val appLanguage by themeManager.appLanguage.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        callManager.registerForIncomingCalls(coroutineScope)
    }

    // Show BottomBar consistently on primary top-level tabs
    val isBottomBarVisible = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Contacts.route,
        Screen.History.route,
        Screen.Settings.route
    )

    // Unified, glitch-free top-level tab switcher
    val navigateToTab: (String) -> Unit = { targetRoute ->
        if (currentRoute != targetRoute) {
            navController.navigate(targetRoute) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = false
                    saveState = false
                }
                launchSingleTop = true
                restoreState = false
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (isBottomBarVisible) {
                CallBridgeBottomBar(
                    currentRoute = currentRoute,
                    language = appLanguage,
                    onNavigateToRoute = navigateToTab
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            // Splash Screen
            composable(Screen.Splash.route) {
                SplashScreen(
                    onSplashFinished = {
                        val target = if (hasCompletedOnboarding) Screen.Dashboard.route else Screen.Onboarding.route
                        navController.navigate(target) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // Onboarding Walkthrough (Pure local, No sign-in)
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onGetStarted = {
                        themeManager.setHasCompletedOnboarding(true)
                        onCheckAudioPermission {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    }
                )
            }
            // Dashboard
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    callHistoryRepository = callHistoryRepository,
                    themeManager = themeManager,
                    onNavigateToDialPad = {
                        navController.navigate(Screen.DialPad.createRoute())
                    },
                    onNavigateToContacts = {
                        navigateToTab(Screen.Contacts.route)
                    },
                    onNavigateToHistory = {
                        navigateToTab(Screen.History.route)
                    },
                    onNavigateToMessages = {
                        navController.navigate(Screen.Messages.route)
                    },
                    onNavigateToSettings = {
                        navigateToTab(Screen.Settings.route)
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
                    isHeld = isHeld,
                    activeCallNote = activeCallNote,
                    onToggleMute = { callManager.toggleMute() },
                    onToggleSpeaker = { callManager.toggleSpeaker() },
                    onToggleHold = { callManager.toggleHold() },
                    onSendDtmfDigit = { digit -> callManager.sendDigits(digit) },
                    onUpdateNote = { note -> callManager.setCallNote(note) },
                    onEndCall = {
                        callManager.endCall()
                        callManager.resetStateToIdle()
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
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
                    },
                    onSendMessage = { phone ->
                        navController.navigate(Screen.ChatThread.createRoute(phone))
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
                    },
                    onSendMessage = { phone ->
                        navController.navigate(Screen.ChatThread.createRoute(phone))
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
                    },
                    onSendMessage = { phone ->
                        navController.navigate(Screen.ChatThread.createRoute(phone))
                    }
                )
            }

            // SMS Messages Hub
            composable(Screen.Messages.route) {
                MessagesScreen(
                    messagesRepository = messagesRepository,
                    themeManager = themeManager,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToThread = { address ->
                        navController.navigate(Screen.ChatThread.createRoute(address))
                    }
                )
            }

            // Chat Thread Detail
            composable(
                route = Screen.ChatThread.route,
                arguments = listOf(
                    navArgument("address") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val encodedAddress = backStackEntry.arguments?.getString("address") ?: ""
                val address = try {
                    java.net.URLDecoder.decode(encodedAddress, "UTF-8")
                } catch (e: Exception) {
                    encodedAddress
                }
                ChatThreadScreen(
                    recipientAddress = address,
                    messagesRepository = messagesRepository,
                    themeManager = themeManager,
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

    // Full-Screen Incoming Call Ringing Overlay
    val incomingState = callState as? CallUiState.IncomingRinging
    if (incomingState != null) {
        IncomingCallScreen(
            callerNumber = incomingState.callerNumber,
            contactName = incomingState.contactName,
            onAccept = {
                onCheckAudioPermission {
                    callManager.acceptIncomingCall()
                    navController.navigate(Screen.ActiveCall.createRoute(incomingState.callerNumber))
                }
            },
            onReject = {
                callManager.rejectIncomingCall()
            }
        )
    }
}
