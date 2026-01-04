//package com.vinh.identify_identity_card.ui.scan
//
//import android.net.Uri
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.platform.LocalContext
//import com.vinh.identify_identity_card.utils.createTempImageFile
//import com.vinh.identify_identity_card.utils.createTempImageUri
//import android.util.Log
//private const val TAG = "ScanCapture"
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ScanCaptureScreen(
//    vm: ScanViewModel,
//    source: ScanSource,
//    onGoResult: () -> Unit,
//    onBack: () -> Unit
//) {
//    val context = LocalContext.current
//
//    // Gallery
////    val pickLauncher = rememberLauncherForActivityResult(
////        ActivityResultContracts.GetContent()
////    ) { uri: Uri? ->
////        if (uri != null) vm.scan(context, uri, onSuccess = onGoResult)
////    }
//    val pickLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.PickVisualMedia()
//    ) { uri: Uri? ->
//        Log.d(TAG, "Picker result uri=$uri")
//        if (uri != null) vm.scanFrontBack(context, uri, onSuccess = onGoResult)
//        else Log.w(TAG, "Picker canceled (uri=null)")
//    }
//
//
//    // Camera
//    var cameraUri by remember { mutableStateOf<Uri?>(null) }
//    val cameraLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.TakePicture()
//    ) { ok ->
//        Log.d(TAG, "Camera result ok=$ok cameraUri=$cameraUri")
//        if (ok) cameraUri?.let { vm.scan(context, it, onSuccess = onGoResult) }
//        else Log.w(TAG, "Camera canceled or failed")
//    }
//
//    // Auto trigger khi vào màn này
//    LaunchedEffect(source) {
//        if (source == ScanSource.GALLERY) pickLauncher.launch(
//            androidx.activity.result.PickVisualMediaRequest(
//                ActivityResultContracts.PickVisualMedia.ImageOnly
//            )
//        )
//
//        else {
//            val file = createTempImageFile(context)
//            val uri = createTempImageUri(context, file)
//            cameraUri = uri
//            cameraLauncher.launch(uri)
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Capture / Upload") },
//                windowInsets = WindowInsets(0),
//                navigationIcon = { IconButton(onClick = onBack) { Text("←") } }
//            )
//        }
//    ) { padding ->
//        Column(
//            Modifier
//                .fillMaxSize()
//                .padding(padding)
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            if (vm.isLoading) {
//                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
//                Text("Đang trích xuất thông tin...", style = MaterialTheme.typography.bodyMedium)
//            }
//
//            vm.error?.let { err ->
//                Card {
//                    Column(Modifier.padding(12.dp)) {
//                        Text("Lỗi", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
//                        Spacer(Modifier.height(6.dp))
//                        Text(err)
//                        Spacer(Modifier.height(12.dp))
//
//                        Button(
//                            onClick = {
//                                vm.clearError()
//                                // chụp/chọn lại
//                                if (source == ScanSource.GALLERY) pickLauncher.launch(
//                                    androidx.activity.result.PickVisualMediaRequest(
//                                        ActivityResultContracts.PickVisualMedia.ImageOnly
//                                    )
//                                )
//
//                                else {
//                                    val file = createTempImageFile(context)
//                                    val uri = createTempImageUri(context, file)
//                                    cameraUri = uri
//                                    cameraLauncher.launch(uri)
//                                }
//                            },
//                            enabled = !vm.isLoading,
//                            modifier = Modifier.fillMaxWidth()
//                        ) { Text("Chụp/Chọn lại") }
//                    }
//                }
//            }
//
//            if (!vm.isLoading && vm.error == null) {
//                Text("Đang mở ${if (source == ScanSource.CAMERA) "camera" else "thư viện"}...")
//            }
//        }
//    }
//}
