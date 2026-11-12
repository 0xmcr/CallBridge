package com.callbridge.personal.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PhoneMissed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callbridge.personal.data.local.entity.CallHistoryEntity
import com.callbridge.personal.data.repository.CallHistoryRepository
import com.callbridge.personal.ui.i18n.LocalAppStrings
import com.callbridge.personal.ui.theme.CallGreen
import com.callbridge.personal.ui.theme.CallRed
import com.callbridge.personal.ui.util.FormatUtils
import kotlinx.coroutines.launch
import java.util.Calendar

private enum class HistoryFilter {
    ALL, OUTGOING, MISSED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallHistoryScreen(
    callHistoryRepository: CallHistoryRepository,
    onNavigateToDetail: (String) -> Unit,
    onCallNumber: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(HistoryFilter.ALL) }
    val strings = LocalAppStrings.current

    val rawHistoryList by callHistoryRepository.allHistory.collectAsState(initial = emptyList())

    // Filter by tab
    val filteredList = remember(rawHistoryList, selectedFilter) {
        when (selectedFilter) {
            HistoryFilter.ALL -> rawHistoryList
            HistoryFilter.OUTGOING -> rawHistoryList.filter { it.status.equals("COMPLETED", ignoreCase = true) }
            HistoryFilter.MISSED -> rawHistoryList.filter { !it.status.equals("COMPLETED", ignoreCase = true) }
        }
    }

    // Group by Date: Today, Yesterday, Older
    val groupedHistory = remember(filteredList) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis

        cal.add(Calendar.DAY_OF_YEAR, -1)
        val startOfYesterday = cal.timeInMillis

        val todayList = mutableListOf<CallHistoryEntity>()
        val yesterdayList = mutableListOf<CallHistoryEntity>()
        val olderList = mutableListOf<CallHistoryEntity>()

        filteredList.forEach { item ->
            val timestamp = item.startedAt ?: item.createdAt
            when {
                timestamp >= startOfToday -> todayList.add(item)
                timestamp >= startOfYesterday -> yesterdayList.add(item)
                else -> olderList.add(item)
            }
        }

        listOf(
            "Today" to todayList,
            "Yesterday" to yesterdayList,
            "Older" to olderList
        ).filter { it.second.isNotEmpty() }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Call History", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete all call logs? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        scope.launch {
                            callHistoryRepository.clearHistory()
                        }
                    }
                ) {
                    Text("Clear All", color = CallRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(strings.actionCancel)
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(strings.tabHistory, fontWeight = FontWeight.Bold) },
                actions = {
                    if (rawHistoryList.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.DeleteSweep,
                                contentDescription = "Clear all call history"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Filter Pills Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == HistoryFilter.ALL,
                    onClick = { selectedFilter = HistoryFilter.ALL },
                    label = { Text("All", fontWeight = if (selectedFilter == HistoryFilter.ALL) FontWeight.Bold else FontWeight.Normal) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )

                FilterChip(
                    selected = selectedFilter == HistoryFilter.OUTGOING,
                    onClick = { selectedFilter = HistoryFilter.OUTGOING },
                    label = { Text(strings.dashboardOutgoing, fontWeight = if (selectedFilter == HistoryFilter.OUTGOING) FontWeight.Bold else FontWeight.Normal) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )

                FilterChip(
                    selected = selectedFilter == HistoryFilter.MISSED,
                    onClick = { selectedFilter = HistoryFilter.MISSED },
                    label = { Text(strings.dashboardMissed, fontWeight = if (selectedFilter == HistoryFilter.MISSED) FontWeight.Bold else FontWeight.Normal) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Call,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (rawHistoryList.isEmpty()) "No calls yet" else "No matching calls",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Calls you make to the US will appear here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupedHistory.forEach { (sectionTitle, itemsInSection) ->
                        item {
                            Text(
                                text = sectionTitle,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }

                        items(
                            items = itemsInSection,
                            key = { it.id }
                        ) { item ->
                            CallHistoryItemCard(
                                item = item,
                                onClick = { onNavigateToDetail(item.id) },
                                onCallClick = { onCallNumber(item.destinationE164) }
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CallHistoryItemCard(
    item: CallHistoryEntity,
    onClick: () -> Unit,
    onCallClick: () -> Unit
) {
    val isSuccess = item.status.equals("COMPLETED", ignoreCase = true)
    val statusColor = if (isSuccess) CallGreen else CallRed
    val statusIcon = if (isSuccess) Icons.AutoMirrored.Filled.CallMade else Icons.Filled.PhoneMissed
    val timestamp = item.startedAt ?: item.createdAt

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Direction / Status Avatar Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = item.status,
                    tint = statusColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = FormatUtils.formatNanpDisplay(item.destinationE164),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                val loc = FormatUtils.getAreaCodeLocation(item.destinationE164)
                if (!loc.isNullOrBlank()) {
                    Text(
                        text = "📍 $loc",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (!item.note.isNullOrBlank()) {
                    Text(
                        text = "📝 ${item.note}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${if (isSuccess) "Outgoing" else "Missed"} • ${FormatUtils.formatTimestamp(timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }

            // Duration Badge on the Right (e.g. ↗ 2m 36s)
            if (isSuccess && (item.durationSeconds ?: 0) > 0) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CallGreen.copy(alpha = 0.12f),
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Text(
                        text = "↗ ${FormatUtils.formatDuration(item.durationSeconds)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.Bold,
                        color = CallGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            } else if (!isSuccess) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CallRed.copy(alpha = 0.12f),
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Text(
                        text = "Missed",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.Bold,
                        color = CallRed,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // Quick Call-back Button
            IconButton(
                onClick = onCallClick,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(CallGreen.copy(alpha = 0.12f))
            ) {
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = "Call ${item.destinationE164}",
                    tint = CallGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
