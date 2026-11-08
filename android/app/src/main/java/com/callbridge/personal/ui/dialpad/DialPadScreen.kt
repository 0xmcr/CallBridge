package com.callbridge.personal.ui.dialpad

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

    // Internal state stores national or international digits (e.g. "+88017...", "2025550143")
    var nationalDigits by remember {
        mutableStateOf(if (initialNumber.isNotBlank()) initialNumber else "")
    }

    var clipboardDigits by remember { mutableStateOf<String?>(null) }
    var showCountryPicker by remember { mutableStateOf(false) }

    // Resolve country dynamically from input
    val countryInfo = remember(nationalDigits) {
        FormatUtils.resolveCountryInfo(nationalDigits)
    }

    // Check clipboard for auto-detecting phone numbers
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val clipText = clipboardManager.getText()?.text
        if (!clipText.isNullOrBlank()) {
            val extracted = if (clipText.startsWith("+")) {
                clipText.replace("[^0-9+]".toRegex(), "")
            } else {
                FormatUtils.extractNationalDigits(clipText)
            }
            if (extracted.length >= 7) {
                clipboardDigits = extracted
            }
        }
    }

    val validation = remember(nationalDigits) {
        FormatUtils.validateGlobalE164(nationalDigits)
    }

    val locationHint = remember(nationalDigits) {
        if (!nationalDigits.startsWith("+") || nationalDigits.startsWith("+1")) {
            FormatUtils.getAreaCodeLocation(nationalDigits)
        } else {
            null
        }
    }

    if (showCountryPicker) {
        var countrySearch by remember { mutableStateOf("") }
        val filteredCountries = remember(countrySearch) {
            if (countrySearch.isBlank()) FormatUtils.SUPPORTED_COUNTRIES
            else FormatUtils.SUPPORTED_COUNTRIES.filter {
                it.name.contains(countrySearch.trim(), ignoreCase = true) ||
                        it.dialCode.contains(countrySearch.trim())
            }
        }

        AlertDialog(
            onDismissRequest = { showCountryPicker = false },
            title = {
                Text("Select Country (${FormatUtils.SUPPORTED_COUNTRIES.size} Supported)")
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = countrySearch,
                        onValueChange = { countrySearch = it },
                        placeholder = { Text("Search country or code...") },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (countrySearch.isNotEmpty()) {
                                IconButton(onClick = { countrySearch = "" }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                    ) {
                        items(filteredCountries, key = { it.name + it.dialCode }) { country ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        nationalDigits = country.dialCode
                                        showCountryPicker = false
                                    }
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = country.flag, fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = country.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = country.dialCode,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCountryPicker = false }) {
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(end = 12.dp)
                    ) {
                        Text("Dial Pad", fontWeight = FontWeight.Bold)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = CallGreen.copy(alpha = 0.15f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(CallGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "CallBridge Direct",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CallGreen
                                )
                            }
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
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Display Number Area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Modern curved input pill with Dynamic Flag Badge
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Dynamic Country Flag Badge (Clickable to switch)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { showCountryPicker = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(text = countryInfo.flag, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = countryInfo.dialCode,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "▾",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Formatted Number / Placeholder
                        val displayText = when {
                            nationalDigits.isEmpty() -> "Enter phone number..."
                            nationalDigits.startsWith("+") -> nationalDigits
                            else -> FormatUtils.formatNanpDisplay(nationalDigits)
                        }
                        Text(
                            text = displayText,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = if (nationalDigits.length > 11) 20.sp else if (nationalDigits.length > 7) 22.sp else 24.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = if (nationalDigits.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                        )
                    }
                }

                // Real-time Validity Indicator Pill (Shown only when typing)
                if (nationalDigits.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
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
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
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
                            val pasteLabel = if (clipboardDigits!!.startsWith("+")) clipboardDigits!! else FormatUtils.formatNanpDisplay(clipboardDigits!!)
                            Text(
                                text = "Paste $pasteLabel",
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
                verticalArrangement = Arrangement.spacedBy(10.dp),
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

                                    val maxLen = if (nationalDigits.startsWith("+")) 16 else 10
                                    if (nationalDigits.length < maxLen) {
                                        nationalDigits += digit
                                    } else {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                },
                                onLongClick = if (digit == "0") {
                                    {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (!nationalDigits.startsWith("+")) {
                                            nationalDigits = "+$nationalDigits"
                                        }
                                    }
                                } else null
                            )
                        }
                    }
                }
            }

            // Bottom Actions Row: Spacer | Call FAB | Backspace Button (Protected with Navigation Inset)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 14.dp, top = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Invisible balance box
                Box(modifier = Modifier.size(64.dp))

                // Primary Call Button (Glowing Emerald)
                FloatingActionButton(
                    onClick = {
                        if (validation.isValid) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val numberToCall = if (nationalDigits.startsWith("+")) nationalDigits else validation.e164
                            onStartCall(numberToCall)
                        }
                    },
                    modifier = Modifier.size(68.dp),
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
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Backspace Button (Quick Tap to Delete, Long Press to Clear All)
                Box(
                    modifier = Modifier.size(64.dp),
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DialKeyButton(
    digit: String,
    sub: String,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .size(70.dp)
            .clip(CircleShape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = CircleShape
            ),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
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
