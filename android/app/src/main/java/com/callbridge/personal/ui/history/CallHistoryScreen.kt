package com.callbridge.personal.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.PhoneMissed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.callbridge.personal.ui.theme.CallGreen
import com.callbridge.personal.ui.theme.CallRed

data class CallHistoryPlaceholder(
    val id: String,
    val destination: String,
    val contactName: String?,
    val status: String, // COMPLETED, FAILED, CANCELLED
    val durationText: String,
    val timestamp: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallHistoryScreen(
    onNavigateToDetail: (String) -> Unit,
    onCallNumber: (String) -> Unit
) {
    // Sample placeholder data for M1 UI preview
    val sampleHistory = listOf(
        CallHistoryPlaceholder("1", "+1 (202) 555-0143", "John Miller", "COMPLETED", "2m 15s", "Today, 11:30 AM"),
        CallHistoryPlaceholder("2", "+1 (415) 555-0189", "Sarah Jenkins", "COMPLETED", "0m 45s", "Yesterday, 4:10 PM"),
        CallHistoryPlaceholder("3", "+1 (212) 555-0199", null, "FAILED", "0s", "Oct 19, 2:00 PM")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Call History") }
            )
        }
    ) { paddingValues ->
        if (sampleHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No calls yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sampleHistory) { item ->
                    CallHistoryItemCard(
                        item = item,
                        onClick = { onNavigateToDetail(item.id) },
                        onCallClick = { onCallNumber(item.destination) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CallHistoryItemCard(
    item: CallHistoryPlaceholder,
    onClick: () -> Unit,
    onCallClick: () -> Unit
) {
    val isCompleted = item.status == "COMPLETED"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.AutoMirrored.Filled.CallMade else Icons.Filled.PhoneMissed,
                contentDescription = item.status,
                tint = if (isCompleted) CallGreen else CallRed,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.contactName ?: item.destination,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (item.contactName != null) {
                    Text(
                        text = item.destination,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${item.timestamp} • ${item.durationText}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            IconButton(onClick = onCallClick) {
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = "Call back",
                    tint = CallGreen
                )
            }
        }
    }
}
