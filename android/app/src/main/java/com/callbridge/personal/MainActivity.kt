package com.callbridge.personal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CallBridgeTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(navController: NavHostController = rememberNavController()) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show BottomBar only on primary top-level tabs
    val isBottomBarVisible = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Contacts.route,
        Screen.History.route,
        Screen.Settings.route
    )

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
                    onNavigateBack = { navController.popBackStack() },
                    onStartCall = { destination ->
                        navController.navigate(Screen.ActiveCall.createRoute(destination))
                    }
                )
            }

            // Active Call
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
                    onEndCall = {
                        navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                    }
                )
            }

            // Contacts
            composable(Screen.Contacts.route) {
                ContactsScreen(
                    onNavigateToAddContact = {
                        navController.navigate(Screen.AddEditContact.createRoute())
                    },
                    onNavigateToContactDetail = { contactId ->
                        navController.navigate(Screen.ContactDetail.createRoute(contactId))
                    },
                    onCallNumber = { number ->
                        navController.navigate(Screen.DialPad.createRoute(number))
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
                    onNavigateBack = { navController.popBackStack() },
                    onEditContact = { id ->
                        navController.navigate(Screen.AddEditContact.createRoute(id))
                    },
                    onCallNumber = { number ->
                        navController.navigate(Screen.DialPad.createRoute(number))
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
                    onNavigateBack = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }

            // Call History
            composable(Screen.History.route) {
                CallHistoryScreen(
                    onNavigateToDetail = { callId ->
                        navController.navigate(Screen.CallDetail.createRoute(callId))
                    },
                    onCallNumber = { number ->
                        navController.navigate(Screen.DialPad.createRoute(number))
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
                    onNavigateBack = { navController.popBackStack() },
                    onCallNumber = { number ->
                        navController.navigate(Screen.DialPad.createRoute(number))
                    }
                )
            }

            // Settings
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
