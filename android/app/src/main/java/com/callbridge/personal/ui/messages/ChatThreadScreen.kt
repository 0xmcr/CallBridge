package com.callbridge.personal.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callbridge.personal.data.local.entity.MessageEntity
import com.callbridge.personal.data.repository.MessagesRepository
import com.callbridge.personal.ui.theme.CallGreen
import com.callbridge.personal.ui.theme.ThemeManager
import com.callbridge.personal.ui.util.FormatUtils
import kotlinx.coroutines.launch

// CallBridge Telecom Color Tokens
private val CallBridgeBlue = Color(0xFF007AFF)
private val CallBridgeBlueGradient = listOf(Color(0xFF007AFF), Color(0xFF0066D6))
private val CallBridgeIncomingDark = Color(0xFF26252A)
private val CallBridgeIncomingLight = Color(0xFFE9E9EB)
private val SegmentWarningColor = Color(0xFFFF9500)

val QuickSmsTemplates = listOf(
    "Please call me back when you're free.",
    "Hi, I'm reaching out to follow up on our discussion.",
    "I'm currently in a meeting, will message you shortly.",
    "Received your message, thank you!",
    "On my way right now, will arrive soon.",
    "Could you please share the details?"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatThreadScreen(
    recipientAddress: String,
    messagesRepository: MessagesRepository,
    themeManager: ThemeManager,
    onNavigateBack: () -> Unit,
    onCallNumber: (String) -> Unit
) {
    val messages by messagesRepository.getMessagesForAddress(recipientAddress).collectAsState(initial = emptyList())
    val backendUrl by themeManager.backendUrl.collectAsState()
    val callerId by themeManager.callerId.collectAsState()
    val country = remember(recipientAddress) { FormatUtils.resolveCountryInfo(recipientAddress) }

    var inputText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var showTemplateDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val smsSegments = remember(inputText) {
        FormatUtils.calculateSmsSegments(inputText)
    }

    // Scroll to bottom whenever messages update
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Quick Templates Dialog
    if (showTemplateDialog) {
        AlertDialog(
            onDismissRequest = { showTemplateDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Notes,
                        contentDescription = null,
                        tint = CallBridgeBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Quick SMS Templates", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Standard GSM SMS is limited to 160 characters per segment. Pick a quick template to send instantly:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(QuickSmsTemplates) { template ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        inputText = template
                                        showTemplateDialog = false
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = template,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTemplateDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Dynamic Country Avatar
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = country.flag, fontSize = 20.sp)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = recipientAddress,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "CallBridge SMS • ${country.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Quick Call Button
                    IconButton(onClick = { onCallNumber(recipientAddress) }) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(CallGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Call,
                                contentDescription = "Call Contact",
                                tint = CallGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // CallBridge Gateway Header Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ) {
                Text(
                    text = "Encrypted SMS via CallBridge Gateway • Twilio ($callerId)",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            // Message Bubble History
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    CallBridgeMessageBubble(message = msg)
                }
            }

            // Bottom Input Bar with Real-Time Character & Segment Counter
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Real-Time Segment & Character Counter
                    if (inputText.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (smsSegments.isUnicode) "Unicode UCS-2 (70 char limit)" else "Standard GSM (160 char limit)",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (smsSegments.segmentCount > 1) SegmentWarningColor.copy(alpha = 0.15f) else CallBridgeBlue.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (smsSegments.segmentCount == 1)
                                            "${smsSegments.charCount} / ${smsSegments.maxCharsForSegments} • 1 SMS"
                                        else
                                            "${smsSegments.charCount} chars • ${smsSegments.segmentCount} SMS Segments",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = if (smsSegments.segmentCount > 1) SegmentWarningColor else CallBridgeBlue
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick Templates Button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { showTemplateDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Quick Templates",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // CallBridge Pill Input Field
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    text = "CallBridge SMS...",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            maxLines = 4,
                            shape = RoundedCornerShape(22.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CallBridgeBlue,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Send Button
                        val canSend = inputText.isNotBlank() && !isSending
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    if (canSend) CallBridgeBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                )
                                .clickable(enabled = canSend) {
                                    isSending = true
                                    val text = inputText.trim()
                                    inputText = ""
                                    coroutineScope.launch {
                                        messagesRepository.sendSms(backendUrl, recipientAddress, text)
                                        isSending = false
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Message",
                                tint = if (canSend) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CallBridgeMessageBubble(message: MessageEntity) {
    val isOutbound = !message.isIncoming
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOutbound) Alignment.End else Alignment.Start
    ) {
        // Bubble
        Box(
            modifier = Modifier
                .widthIn(max = 285.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isOutbound) 18.dp else 4.dp,
                        bottomEnd = if (isOutbound) 4.dp else 18.dp
                    )
                )
                .then(
                    if (isOutbound) {
                        Modifier.background(
                            Brush.verticalGradient(CallBridgeBlueGradient)
                        )
                    } else {
                        Modifier.background(
                            if (isDark) CallBridgeIncomingDark else CallBridgeIncomingLight
                        )
                    }
                )
                .padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            Text(
                text = message.body,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                color = if (isOutbound) Color.White else if (isDark) Color.White else Color.Black
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Receipt Status (Delivered or Timestamp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Text(
                text = FormatUtils.formatTimestamp(message.timestamp),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            if (isOutbound) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "• Delivered",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = CallBridgeBlue.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
