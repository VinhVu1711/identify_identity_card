//package com.vinh.identify_identity_card.ui.scan
//
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.PickVisualMediaRequest
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.WindowInsets
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Button
//import androidx.compose.material3.Card
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.LinearProgressIndicator
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ScanUploadBothScreen(
//    vm: ScanViewModel,
//    onGoResult: () -> Unit,
//    onBack: () -> Unit
//) {
//    val context = LocalContext.current
//
//    val pickFront =
//        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
//            if (uri != null) vm.setFront(uri)
//        }
//    val pickBack = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
//        if (uri != null) vm.setBack(uri)
//    }
//
//    Scaffold (
//        topBar = {
//            TopAppBar(
//                title = { Text("Upload CCCD (2 mặt)") },
//                windowInsets = WindowInsets(0),
//                navigationIcon = { IconButton(onClick = onBack) { Text("←") } }
//            )
//        }
//    ) { padding ->
//        Column(
//            Modifier.fillMaxSize().padding(padding).padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            Card {
//                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                    Text("Mặt trước", style = MaterialTheme.typography.titleMedium)
//                    Text(vm.frontUri?.toString() ?: "Chưa chọn", style = MaterialTheme.typography.bodySmall)
//                    Button(
//                        onClick = {
//                            pickFront.launch(
//                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
//                            )
//                        },
//                        modifier = Modifier.fillMaxWidth()
//                    ) { Text("Chọn ảnh mặt trước") }
//                }
//            }
//
//            Card {
//                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                    Text("Mặt sau", style = MaterialTheme.typography.titleMedium)
//                    Text(vm.backUri?.toString() ?: "Chưa chọn", style = MaterialTheme.typography.bodySmall)
//                    Button(
//                        onClick = {
//                            pickBack.launch(
//                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
//                            )
//                        },
//                        modifier = Modifier.fillMaxWidth()
//                    ) { Text("Chọn ảnh mặt sau") }
//                }
//            }
//
//            if (vm.isLoading) {
//                LinearProgressIndicator(Modifier.fillMaxWidth())
//                Text("Đang trích xuất...")
//            }
//
//            vm.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
//
//            val canConfirm = vm.frontUri != null && vm.backUri != null && !vm.isLoading
//
//            Button(
//                onClick = { vm.scanFrontBack(context, onSuccess = onGoResult) },
//                enabled = canConfirm,
//                modifier = Modifier.fillMaxWidth()
//            ) { Text("Xác nhận & Trích xuất") }
//        }
//    }
//}
