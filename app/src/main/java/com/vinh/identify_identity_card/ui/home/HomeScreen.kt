package com.vinh.identify_identity_card.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onManual: () -> Unit,
    onHistory: () -> Unit,
    onScan: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("ID Demo", style = MaterialTheme.typography.titleLarge)
        Button(onClick = onManual, modifier = Modifier.fillMaxWidth()) { Text("Manual Input") }
        Button(onClick = onScan, modifier = Modifier.fillMaxWidth()) { Text("Upload Photo") } //
        Button(onClick = onHistory, modifier = Modifier.fillMaxWidth()) { Text("History") }
    }
}
