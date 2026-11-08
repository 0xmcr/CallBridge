package com.callbridge.personal.ui.dialpad

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callbridge.personal.ui.theme.CallGreen
import com.callbridge.personal.ui.theme.StatusWarning
import com.callbridge.personal.ui.util.DtmfPlayer
import com.callbridge.personal.ui.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialPadScreen(
    initialNumber: String = "",
    dialSoundEnabled: Boolean = true,
    onNavigateBack: () -> Unit,
    onStartCall: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    // Internal state stores only the 10 national digits (e.g. "2025550143")
    var nationalDigits by remember {
        mutableStateOf(FormatUtils.extractNationalDigits(initialNumber))
    }

    var clipboardDigits by remember { mutableStateOf<String?>(null) }

    // Check clipboard for auto-detecting US phone numbers
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val clipText = clipboardManager.getText()?.text
        if (!clipText.isNullOrBlank()) {
            val extracted = FormatUtils.extractNationalDigits(clipText)
            if (extracted.length >= 7) {
                clipboardDigits = extracted
            }
        }
    }

    val validation = remember(nationalDigits) {
        FormatUtils.validateNanp(nationalDigits)
    }

    val locationHint = remember(nationalDigits) {
        FormatUtils.getAreaCodeLocation(nationalDigits)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dial Pad", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Display Number Area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                // Large Formatted Telephone Number Display
                Text(
                    text = validation.formatted,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = if (nationalDigits.length > 7) 32.sp else 36.sp,
                        letterSpacing = 1.sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Real-time Validity Indicator Pill
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = when {
                        validation.isValid -> CallGreen.copy(alpha = 0.15f)
                        nationalDigits.length == 10 -> StatusWarning.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        if (validation.isValid) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = CallGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = validation.message,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = when {
                                validation.isValid -> CallGreen
                                nationalDigits.length == 10 -> StatusWarning
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                // Live US City & State Location Hint
                if (!locationHint.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = locationHint,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Smart Clipboard Paste Pill
                if (nationalDigits.isEmpty() && !clipboardDigits.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable {
                            nationalDigits = clipboardDigits!!
                            clipboardDigits = null
                            if (dialSoundEnabled) DtmfPlayer.playTone('1')
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentPaste,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Paste ${FormatUtils.formatNanpDisplay(clipboardDigits!!)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // Keypad (4 rows)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val keys = listOf(
                    listOf("1" to "", "2" to "ABC", "3" to "DEF"),
                    listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
                    listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
                    listOf("*" to "", "0" to "+", "#" to "")
                )

                keys.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { (digit, sub) ->
                            DialKeyButton(
                                digit = digit,
                                sub = sub,
                                onClick = {
                                    if (dialSoundEnabled) {
                                        DtmfPlayer.playTone(digit.first())
                                    }
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

                                    // Strictly limit to 10 national digits
                                    if (digit in "0".."9") {
                                        if (nationalDigits.length < 10) {
                                            nationalDigits += digit
                                        } else {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Bottom Actions Row: Spacer | Call FAB | Backspace Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Invisible balance box
                Box(modifier = Modifier.size(68.dp))

                // Primary Call Button (Glowing Emerald)
                FloatingActionButton(
                    onClick = {
                        if (validation.isValid) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onStartCall(validation.e164)
                        }
                    },
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    containerColor = if (validation.isValid) CallGreen else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (validation.isValid) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = if (validation.isValid) 6.dp else 0.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = "Place Call",
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Backspace Button (Quick Tap to Delete, Long Press to Clear All)
                Box(
                    modifier = Modifier.size(68.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (nationalDigits.isNotEmpty()) {
                        BackspaceKey(
                            onDelete = {
                                if (nationalDigits.isNotEmpty()) {
                                    if (dialSoundEnabled) DtmfPlayer.playDeleteTone()
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    nationalDigits = nationalDigits.dropLast(1)
                                }
                            },
                            onClearAll = {
                                if (dialSoundEnabled) DtmfPlayer.playDeleteTone()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                nationalDigits = ""
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialKeyButton(
    digit: String,
    sub: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = CircleShape
            ),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = digit,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (sub.isNotEmpty()) {
                Text(
                    text = sub,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    ),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BackspaceKey(
    onDelete: () -> Unit,
    onClearAll: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .combinedClickable(
                onClick = onDelete,
                onLongClick = onClearAll
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Backspace,
            contentDescription = "Delete digit or hold to clear",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}
