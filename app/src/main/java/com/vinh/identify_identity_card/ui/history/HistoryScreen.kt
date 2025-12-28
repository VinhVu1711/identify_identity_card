package com.vinh.identify_identity_card.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vinh.identify_identity_card.data.local.HistoryEntity
import com.vinh.identify_identity_card.domain.model.IdentityCardInfo
import com.vinh.identify_identity_card.domain.model.PassportInfo
import com.vinh.identify_identity_card.utils.toDdMmYyyy
import androidx.compose.foundation.layout.WindowInsets

import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(vm: HistoryViewModel, onBack: ()-> Unit) {
    val list by vm.items.collectAsState()

    Scaffold(

        topBar = {
            TopAppBar(
                title = { Text("History") },
                windowInsets = WindowInsets(0),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Spacer(Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(list) { item -> HistoryCard(item) }
            }
        }
    }
}

@Composable
private fun HistoryCard(item: HistoryEntity) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Type: ${item.docType} • Mode: ${item.mode}", style = MaterialTheme.typography.labelLarge)

            if (item.docType == "IdentityCard") {
                val info = remember(item.dataJson) {
                    runCatching { Json.decodeFromString(IdentityCardInfo.serializer(), item.dataJson) }.getOrNull()
                }
                Text(info?.fullName ?: "Unknown", style = MaterialTheme.typography.titleMedium)
                Text("Identity Card Number: ${info?.idNumber.orEmpty()}")
                Text("Date of birth: ${info?.dateOfBirth?.toDdMmYyyy().orEmpty()}")
                Text("Issue: ${info?.issueDate?.toDdMmYyyy().orEmpty()} • Exp: ${info?.expiryDate?.toDdMmYyyy().orEmpty()}")
            } else {
                val info = remember(item.dataJson) {
                    runCatching { Json.decodeFromString(PassportInfo.serializer(), item.dataJson) }.getOrNull()
                }
                Text(info?.fullName ?: "Unknown", style = MaterialTheme.typography.titleMedium)
                Text("Passport: ${info?.passportNumber.orEmpty()}")
                Text("DOB: ${info?.dateOfBirth?.toDdMmYyyy().orEmpty()}")
                Text("Issue: ${info?.issueDate?.toDdMmYyyy().orEmpty()} • Exp: ${info?.expiryDate?.toDdMmYyyy().orEmpty()}")
            }
        }
    }
}
