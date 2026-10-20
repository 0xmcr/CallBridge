package com.callbridge.personal.ui.contacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.callbridge.personal.data.local.entity.ContactEntity
import com.callbridge.personal.data.repository.ContactsRepository
import com.callbridge.personal.ui.util.FormatUtils
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditContactScreen(
    contactId: String = "",
    contactsRepository: ContactsRepository,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val isEditing = contactId.isNotBlank()
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+1") }
    var company by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var originalCreatedAt by remember { mutableStateOf(System.currentTimeMillis()) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(contactId) {
        if (isEditing) {
            val existing = contactsRepository.getContactById(contactId).firstOrNull()
            if (existing != null) {
                name = existing.displayName
                phone = existing.phoneE164
                company = existing.company ?: ""
                notes = existing.notes ?: ""
                originalCreatedAt = existing.createdAt
            }
        }
    }

    val isFormValid = name.isNotBlank() && phone.replace("[^0-9]".toRegex(), "").length >= 10

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Contact" else "Add Contact") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name *") },
                placeholder = { Text("e.g. John Miller") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number (E.164) *") },
                placeholder = { Text("+1 (202) 555-0143") },
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                supportingText = { Text("US numbers should include +1 prefix") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = company,
                onValueChange = { company = it },
                label = { Text("Company (Optional)") },
                placeholder = { Text("e.g. Acme Corp") },
                leadingIcon = { Icon(Icons.Filled.Business, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes / Follow-up (Optional)") },
                placeholder = { Text("Meeting notes, timezone preferences, etc.") },
                leadingIcon = { Icon(Icons.Filled.Notes, contentDescription = null) },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!isFormValid || isSaving) return@Button
                    isSaving = true
                    scope.launch {
                        val normalizedPhone = FormatUtils.normalizePhoneNumber(phone)
                        val entity = ContactEntity(
                            id = if (isEditing) contactId else UUID.randomUUID().toString(),
                            displayName = name.trim(),
                            phoneE164 = normalizedPhone,
                            company = company.trim().ifEmpty { null },
                            notes = notes.trim().ifEmpty { null },
                            createdAt = if (isEditing) originalCreatedAt else System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        contactsRepository.saveContact(entity)
                        isSaving = false
                        onSaveSuccess()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid && !isSaving,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isEditing) "Update Contact" else "Save Contact")
                }
            }
        }
    }
}
