package com.callbridge.personal.ui.activecall

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callbridge.personal.calling.CallUiState
import com.callbridge.personal.ui.theme.CallGreen
import com.callbridge.personal.ui.theme.CallRed
import com.callbridge.personal.ui.util.DtmfPlayer
import com.callbridge.personal.ui.util.FormatUtils
import kotlinx.coroutines.delay

@Composable
fun ActiveCallScreen(
    destination: String,
    contactName: String = "",
    callState: CallUiState,
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    isHeld: Boolean = false,
    activeCallNote: String = "",
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleHold: () -> Unit = {},
    onSendDtmfDigit: (String) -> Unit = {},
    onUpdateNote: (String) -> Unit = {},
    onEndCall: () -> Unit
) {
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var showInCallKeypad by remember { mutableStateOf(false) }
    var showNotesSheet by remember { mutableStateOf(false) }
    var enteredDtmfDigits by remember { mutableStateOf("") }
    var noteText by remember(activeCallNote) { mutableStateOf(activeCallNote) }

    // Resolve location of US destination
    val locationHint = remember(destination) {
        FormatUtils.getAreaCodeLocation(destination)
    }

    // Live timer ticking strictly on Confirmed Connection (pauses if held)
    LaunchedEffect(callState, isHeld) {
        if (callState is CallUiState.Connected) {
            while (true) {
                delay(1000L)
                if (!isHeld) {
                    val diff = (System.currentTimeMillis() - callState.connectedAtMillis) / 1000
                    elapsedSeconds = if (diff > 0) diff else 0
                }
            }
        } else {
            elapsedSeconds = 0L
        }
    }

    // Auto-return when call ends or fails
    LaunchedEffect(callState) {
        if (callState is CallUiState.Ended) {
            delay(1500L)
            onEndCall()
        }
    }

    // Infinite pulse animation for active audio
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )

    val isCallActive = callState is CallUiState.Connected
    val isConnecting = callState is CallUiState.Connecting || callState is CallUiState.FetchingToken || callState is CallUiState.Ringing

    val stateText = when {
        isHeld -> "Call On Hold"
        callState is CallUiState.Idle -> "Idle"
        callState is CallUiState.RequestingPermission -> "Requesting Audio Permission..."
        callState is CallUiState.FetchingToken -> "Authenticating Twilio VoIP..."
        callState is CallUiState.Connecting -> "Connecting to US Telecom..."
        callState is CallUiState.Ringing -> "Ringing..."
        callState is CallUiState.Connected -> "Connected (HD PSTN Voice)"
        callState is CallUiState.Ending -> "Ending Call..."
        callState is CallUiState.Ended -> "Call Ended (${callState.durationSeconds}s)"
        callState is CallUiState.Failed -> "Call Failed: ${callState.userMessage}"
        else -> "Calling..."
    }

    // ==========================================
    // In-Call DTMF Keypad Dialog (IVR Support)
    // ==========================================
    if (showInCallKeypad) {
        AlertDialog(
            onDismissRequest = { showInCallKeypad = false },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Touch-Tone Keypad (DTMF)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    if (enteredDtmfDigits.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = enteredDtmfDigits,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val keyRows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("*", "0", "#")
                    )

                    keyRows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            row.forEach { key ->
                                Surface(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            enteredDtmfDigits += key
                                            DtmfPlayer.playTone(key[0])
                                            onSendDtmfDigit(key)
                                        },
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = key,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInCallKeypad = false }) {
                    Text("Close")
                }
            }
        )
    }

    // ==========================================
    // In-Call Quick Scratchpad / Notes Dialog
    // ==========================================
    if (showNotesSheet) {
        AlertDialog(
            onDismissRequest = { showNotesSheet = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.EditNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("In-Call Quick Note", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Notes are saved directly to this call's log upon disconnecting:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = {
                            noteText = it
                            onUpdateNote(it)
                        },
                        placeholder = { Text("Jot down notes, client requirements, addresses...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotesSheet = false }) {
                    Text("Done")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 36.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Telephony Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isCallActive) CallGreen else MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HD Voice • 24-bit Opus • BD ➔ US",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Center: Avatar with Audio Wave Ripple & Contact Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                // Pulsing Avatar Box
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(140.dp)
                ) {
                    if (isCallActive && !isHeld) {
                        // Outer ripple wave 1
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha))
                        )
                        // Outer ripple wave 2
                        Box(
                            modifier = Modifier
                                .size(98.dp)
                                .scale(pulseScale * 0.85f)
                                .clip(CircleShape)
                                .background(CallGreen.copy(alpha = pulseAlpha * 0.7f))
                        )
                    }

                    // Main Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .background(
                                if (isHeld) MaterialTheme.colorScheme.errorContainer
                                else MaterialTheme.colorScheme.primaryContainer
                            )
                            .border(
                                width = 3.dp,
                                color = if (isHeld) CallRed else if (isCallActive) CallGreen else MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier.size(52.dp),
                            tint = if (isHeld) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Recipient Name
                Text(
                    text = contactName.ifEmpty { destination },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (contactName.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = destination,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Location Hint (e.g. "Washington, DC")
                if (!locationHint.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📍 $locationHint",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Call State Status Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isConnecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                    }

                    Text(
                        text = stateText,
                        style = MaterialTheme.typography.titleMedium,
                        color = when {
                            isHeld -> Color(0xFFF59E0B) // Amber
                            callState is CallUiState.Failed -> CallRed
                            callState is CallUiState.Connected -> CallGreen
                            else -> MaterialTheme.colorScheme.primary
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Live Running Duration Counter
                if (callState is CallUiState.Connected) {
                    val minutes = elapsedSeconds / 60
                    val seconds = elapsedSeconds % 60
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
                        fontWeight = FontWeight.Bold,
                        color = if (isHeld) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // ==========================================
            // Comprehensive 6-Button In-Call Grid
            // ==========================================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                // Controls Row 1: Mute | Keypad | Speaker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 1. Mute Button
                    CallControlButton(
                        icon = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                        label = if (isMuted) "Unmute" else "Mute",
                        isActive = isMuted,
                        enabled = isCallActive,
                        onClick = onToggleMute
                    )

                    // 2. In-Call Keypad (DTMF for IVR)
                    CallControlButton(
                        icon = Icons.Filled.Dialpad,
                        label = "Keypad",
                        isActive = showInCallKeypad,
                        enabled = isCallActive,
                        onClick = { showInCallKeypad = true }
                    )

                    // 3. Speakerphone
                    CallControlButton(
                        icon = Icons.Filled.VolumeUp,
                        label = "Speaker",
                        isActive = isSpeakerOn,
                        enabled = isCallActive || callState is CallUiState.Ringing,
                        onClick = onToggleSpeaker
                    )
                }

                // Controls Row 2: Hold | Quick Notes | Bluetooth/Audio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 4. Hold Call
                    CallControlButton(
                        icon = if (isHeld) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                        label = if (isHeld) "Unhold" else "Hold",
                        isActive = isHeld,
                        enabled = isCallActive,
                        onClick = onToggleHold
                    )

                    // 5. In-Call Scratchpad / Note
                    CallControlButton(
                        icon = Icons.Filled.EditNote,
                        label = if (noteText.isNotEmpty()) "Notes (1)" else "Notes",
                        isActive = noteText.isNotEmpty(),
                        enabled = isCallActive,
                        onClick = { showNotesSheet = true }
                    )

                    // 6. Bluetooth / Headset Status
                    CallControlButton(
                        icon = Icons.Filled.Bluetooth,
                        label = "Audio",
                        isActive = false,
                        enabled = true,
                        onClick = onToggleSpeaker
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Big Red End Call Floating Button
                FloatingActionButton(
                    onClick = onEndCall,
                    modifier = Modifier.size(76.dp),
                    shape = CircleShape,
                    containerColor = CallRed,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Filled.CallEnd,
                        contentDescription = "End Call",
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CallControlButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape),
            shape = CircleShape,
            color = when {
                isActive -> MaterialTheme.colorScheme.primary
                enabled -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            }
        ) {
            IconButton(
                onClick = onClick,
                enabled = enabled
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = when {
                        isActive -> MaterialTheme.colorScheme.onPrimary
                        enabled -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    },
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
