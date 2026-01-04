package com.vinh.identify_identity_card.ui.scan
import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vinh.identify_identity_card.domain.model.DocumentType
import com.vinh.identify_identity_card.utils.createTempImageFile
import com.vinh.identify_identity_card.utils.createTempImageUri
import kotlinx.coroutines.launch
enum class UploadSlot { FRONT, BACK, PASSPORT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanUploadScreen(
    vm: ScanViewModel,
    onGoResult: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // slot nào đang được bấm "+"
    var activeSlot by remember { mutableStateOf<UploadSlot?>(null) }

    // bottom sheet open/close
    var openPickerSheet by remember { mutableStateOf(false) }

    // Photo picker (gallery)
    val pickLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            when (activeSlot) {
                UploadSlot.FRONT -> vm.setFront(uri)
                UploadSlot.BACK -> vm.setBack(uri)
                UploadSlot.PASSPORT -> vm.setPassport(uri)
                null -> Unit
            }
        }
        openPickerSheet = false
        activeSlot = null
    }

    // Camera (TakePicture)
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        if (ok) {
            cameraUri?.let { uri ->
                when (activeSlot) {
                    UploadSlot.FRONT -> vm.setFront(uri)
                    UploadSlot.BACK -> vm.setBack(uri)
                    UploadSlot.PASSPORT -> vm.setPassport(uri)
                    null -> Unit
                }
            }
        }
        openPickerSheet = false
        activeSlot = null
    }

    // ---- UI ----
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Photo") },
                windowInsets = WindowInsets(0),
                navigationIcon = { IconButton(onClick = onBack) { Text("←") } }
            )
        },
        bottomBar = {
            val canConfirm = when (vm.selectedDocType) {
                DocumentType.IDENTITYCARD  -> vm.frontUri != null && vm.backUri != null
                DocumentType.PASSPORT -> vm.passportUri != null
            }

            Button(
                onClick = {
                    if (vm.selectedDocType == DocumentType.IDENTITYCARD) {
                        vm.scanFrontBack(context, onSuccess = onGoResult)
                    } else {
                        vm.scanPassportOne(context, onSuccess = onGoResult)
                    }
                },
                enabled = canConfirm && !vm.isLoading,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) { Text("Xác nhận & Trích xuất") }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Filter
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = vm.selectedDocType == DocumentType.IDENTITYCARD,
                    onClick = { vm.selectDocType(DocumentType.IDENTITYCARD) },
                    label = { Text("CCCD") }
                )
                FilterChip(
                    selected = vm.selectedDocType == DocumentType.PASSPORT,
                    onClick = { vm.selectDocType(DocumentType.PASSPORT) },
                    label = { Text("Passport") }
                )
            }

            if (vm.isLoading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Text("Đang trích xuất...")
            }

            vm.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            // Containers
            if (vm.selectedDocType == DocumentType.IDENTITYCARD) {
                UploadCard(
                    title = "Mặt trước",
                    picked = vm.frontUri != null,
                    onAdd = {
                        activeSlot = UploadSlot.FRONT
                        openPickerSheet = true
                    }
                )
                UploadCard(
                    title = "Mặt sau",
                    picked = vm.backUri != null,
                    onAdd = {
                        activeSlot = UploadSlot.BACK
                        openPickerSheet = true
                    }
                )
            } else {
                UploadCard(
                    title = "Ảnh Passport",
                    picked = vm.passportUri != null,
                    onAdd = {
                        activeSlot = UploadSlot.PASSPORT
                        openPickerSheet = true
                    }
                )
            }
        }
    }

    // ---- BottomSheet chọn Camera/Gallery ----
    if (openPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                openPickerSheet = false
                activeSlot = null
            }
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Chọn nguồn ảnh", style = MaterialTheme.typography.titleMedium)

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        // Camera: tạo temp uri rồi launch
                        val file = createTempImageFile(context)
                        val uri = createTempImageUri(context, file)
                        cameraUri = uri
                        cameraLauncher.launch(uri)
                    }
                ) { Text("Chọn Camera") }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        pickLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                ) { Text("Chọn từ Thư viện") }

                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun UploadCard(
    title: String,
    picked: Boolean,
    onAdd: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(if (picked) "Đã chọn ảnh" else "Chưa chọn", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onAdd) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}


