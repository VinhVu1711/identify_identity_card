package com.vinh.identify_identity_card.ui.scan

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanEntryScreen(
    vm: ScanViewModel,
    onNextToCapture: (source: ScanSource) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Permission strings
    val galleryPerm = remember {
        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_IMAGES
        else Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var pendingSource by remember { mutableStateOf<ScanSource?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val camOk = result[Manifest.permission.CAMERA] == true
        val galOk = result[galleryPerm] == true

        when (pendingSource) {
            ScanSource.CAMERA -> if (camOk) onNextToCapture(ScanSource.CAMERA)
            else Unit
            ScanSource.GALLERY -> if (galOk) onNextToCapture(ScanSource.GALLERY)
            else Unit
            null -> Unit
        }
        pendingSource = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Document") },
                windowInsets = WindowInsets(0),
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text("Chọn loại giấy tờ:")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = vm.selectedDocType == com.vinh.identify_identity_card.domain.model.DocumentType.IDENTITYCARD,
                    onClick = { vm.selectDocType(com.vinh.identify_identity_card.domain.model.DocumentType.IDENTITYCARD) },
                    label = { Text("CCCD") }
                )
                FilterChip(
                    selected = vm.selectedDocType == com.vinh.identify_identity_card.domain.model.DocumentType.PASSPORT,
                    onClick = { vm.selectDocType(com.vinh.identify_identity_card.domain.model.DocumentType.PASSPORT) },
                    label = { Text("Hộ chiếu") }
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    pendingSource = ScanSource.CAMERA
                    permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                }
            ) { Text("Dùng Camera") }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    pendingSource = null
                    onNextToCapture(ScanSource.GALLERY)
                }
            ) { Text("Chọn từ Thư viện") }

            Text(
                "Sau khi cấp quyền, bạn sẽ chụp/chọn ảnh. Ảnh rõ nét, không lóa, đủ khung giấy tờ sẽ cho kết quả tốt nhất.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

enum class ScanSource { CAMERA, GALLERY }
