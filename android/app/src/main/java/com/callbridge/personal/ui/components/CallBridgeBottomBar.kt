package com.callbridge.personal.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.callbridge.personal.navigation.Screen

data class NavigationItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val BottomNavigationItems = listOf(
    NavigationItem(
        route = Screen.Dashboard.route,
        label = "Dashboard",
        selectedIcon = Icons.Filled.Call,
        unselectedIcon = Icons.Outlined.Call
    ),
    NavigationItem(
        route = Screen.Contacts.route,
        label = "Contacts",
        selectedIcon = Icons.Filled.Contacts,
        unselectedIcon = Icons.Outlined.Contacts
    ),
    NavigationItem(
        route = Screen.History.route,
        label = "History",
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History
    ),
    NavigationItem(
        route = Screen.Settings.route,
        label = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
)

@Composable
fun CallBridgeBottomBar(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit
) {
    androidx.compose.material3.NavigationBar(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        BottomNavigationItems.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        onNavigateToRoute(item.route)
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(text = item.label) }
            )
        }
    }
}
