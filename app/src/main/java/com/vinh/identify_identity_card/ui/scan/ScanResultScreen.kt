package com.vinh.identify_identity_card.ui.scan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vinh.identify_identity_card.domain.model.DocumentType
import com.vinh.identify_identity_card.domain.model.Gender
import com.vinh.identify_identity_card.domain.model.toDisplayVi
import com.vinh.identify_identity_card.utils.toDdMmYyyy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen(
    vm: ScanViewModel,
    onSavedGoHistory: () -> Unit,
    onBack: () -> Unit
) {
    val scroll = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(vm.error) {
        vm.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Scan Result") },
                windowInsets = WindowInsets(0),
                navigationIcon = { IconButton(onClick = onBack) { Text("←") } }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    vm.saveToHistory()
                    onSavedGoHistory()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) { Text("Save") }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (vm.selectedDocType == DocumentType.IDENTITYCARD) {
                val f = vm.identityCardForm

                OutlinedTextField(f.idNumber.orEmpty(), {}, label = { Text("Số CCCD") }, modifier = Modifier.fillMaxWidth(), readOnly = true)
                OutlinedTextField(f.fullName.orEmpty(), {}, label = { Text("Họ và tên") }, modifier = Modifier.fillMaxWidth(), readOnly = true)
                OutlinedTextField(f.dateOfBirth?.toDdMmYyyy().orEmpty(), {}, label = { Text("Ngày sinh") }, modifier = Modifier.fillMaxWidth(), readOnly = true)
                OutlinedTextField(f.gender?.toDisplayVi().orEmpty(), {}, label = { Text("Giới tính") }, modifier = Modifier.fillMaxWidth(), readOnly = true)
                OutlinedTextField(f.nationality.orEmpty(), {}, label = { Text("Quốc tịch") }, modifier = Modifier.fillMaxWidth(), readOnly = true)
                OutlinedTextField(f.placeOfOrigin.orEmpty(), {}, label = { Text("Quê quán") }, modifier = Modifier.fillMaxWidth(), readOnly = true)
                OutlinedTextField(f.placeOfResidence.orEmpty(), {}, label = { Text("Nơi thường trú") }, modifier = Modifier.fillMaxWidth(), readOnly = true)
                OutlinedTextField(f.issueDate?.toDdMmYyyy().orEmpty(), {}, label = { Text("Ngày cấp") }, modifier = Modifier.fillMaxWidth(), readOnly = true)
                OutlinedTextField(f.expiryDate?.toDdMmYyyy().orEmpty(), {}, label = { Text("Ngày hết hạn") }, modifier = Modifier.fillMaxWidth(), readOnly = true)
                OutlinedTextField(f.issuePlace.orEmpty(), {}, label = { Text("Nơi cấp") }, modifier = Modifier.fillMaxWidth(), readOnly = true)

            } else {
                val f = vm.passportForm

                OutlinedTextField(f.idNumber.orEmpty(), {}, label = { Text("Số CCCD") }, modifier = Modifier.fillMaxWidth(), readOnly = true)
                OutlinedTextField(f.fullName.orEmpty(), {}, label = { Text("Họ và tên") }, modifier = Modifier.fillMaxWidth(), readOnly = true)
                OutlinedTextField(f.dateOfBirth?.toDdMmYyyy().orEmpty(), {}, label = { Text("Ngày sinh") }, modifier = Modifier.fillMaxWidth(), readOnly = true)
                OutlinedTextField(f.gender?.toDisplayVi().orEmpty(), {}, label = { Text("Giới tính") }, modifier = Modifier.fillMaxWidth(), readOnly = true)
                OutlinedTextField(f.nationality.orEmpty(), {}, label = { Text("Quốc tịch") }, modifier = Modifier.fillMaxWidth(), readOnly = true)
                OutlinedTextField(f.passportNumber.orEmpty(), {}, label = { Text("Số hộ chiếu") }, modifier = Modifier.fillMaxWidth(), readOnly = true)
                OutlinedTextField(f.issueDate?.toDdMmYyyy().orEmpty(), {}, label = { Text("Ngày cấp") }, modifier = Modifier.fillMaxWidth(), readOnly = true)
                OutlinedTextField(f.expiryDate?.toDdMmYyyy().orEmpty(), {}, label = { Text("Ngày hết hạn") }, modifier = Modifier.fillMaxWidth(), readOnly = true)
                OutlinedTextField(f.issuingAuthority.orEmpty(), {}, label = { Text("Cơ quan cấp") }, modifier = Modifier.fillMaxWidth(), readOnly = true)
            }
        }
    }
}
