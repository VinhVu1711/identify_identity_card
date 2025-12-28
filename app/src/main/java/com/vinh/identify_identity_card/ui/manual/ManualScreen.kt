package com.vinh.identify_identity_card.ui.manual

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vinh.identify_identity_card.domain.model.DocumentType
import com.vinh.identify_identity_card.domain.model.Gender
import com.vinh.identify_identity_card.domain.model.toDisplayVi
import com.vinh.identify_identity_card.ui.components.DatePickerField
import com.vinh.identify_identity_card.ui.components.DateTarget
import com.vinh.identify_identity_card.ui.components.GenderDropdown
import com.vinh.identify_identity_card.ui.components.NationalityDropdown
import com.vinh.identify_identity_card.utils.toDdMmYyyy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualScreen(vm: ManualViewModel, onBack: () -> Unit) {
    val selected = vm.selected
    val msg = vm.message

    val scrollState = rememberScrollState()

    val nationalityOptions = remember {
        listOf("Vietnam", "Korea", "Japan", "USA", "China", "Other")
    }
    val genderOptions = remember { listOf("Nam", "Nữ", "Khác") }

    // ✅ Snackbar (giữ nguyên)
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(msg) {
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            vm.clearMessage()
        }
    }

    // --- DatePicker dialog control (giữ nguyên) ---
    var openDatePicker by remember { mutableStateOf(false) }
    var dateTarget by remember { mutableStateOf(DateTarget.DOB) }

    fun openPicker(target: DateTarget) {
        dateTarget = target
        openDatePicker = true
    }

    val currentMillis = when (selected) {
        DocumentType.IDENTITYCARD -> {
            val f = vm.identityCardForm
            when (dateTarget) {
                DateTarget.DOB -> f.dateOfBirth
                DateTarget.ISSUE -> f.issueDate
                DateTarget.EXPIRY -> f.expiryDate
            }
        }
        DocumentType.PASSPORT -> {
            val f = vm.passportForm
            when (dateTarget) {
                DateTarget.DOB -> f.dateOfBirth
                DateTarget.ISSUE -> f.issueDate
                DateTarget.EXPIRY -> f.expiryDate
            }
        }
    }

    if (openDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = currentMillis)
        DatePickerDialog(
            onDismissRequest = { openDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        if (selected == DocumentType.IDENTITYCARD) {
                            when (dateTarget) {
                                DateTarget.DOB -> {
                                    vm.identityCardDob(millis)
                                    vm.onBlur(ManualField.IDENTITYCARD_DOB) // ✅ validate
                                }
                                DateTarget.ISSUE -> {
                                    vm.identityCardIssueDate(millis)
                                    vm.onBlur(ManualField.IDENTITYCARD_ISSUE_DATE) // ✅
                                }
                                DateTarget.EXPIRY -> {
                                    vm.identityCardExpiryDate(millis)
                                    vm.onBlur(ManualField.IDENTITYCARD_EXPIRY_DATE) // ✅
                                }
                            }
                        } else {
                            when (dateTarget) {
                                DateTarget.DOB -> {
                                    vm.passportDob(millis)
                                    vm.onBlur(ManualField.PASSPORT_DOB) // ✅
                                }
                                DateTarget.ISSUE -> {
                                    vm.passportIssueDate(millis)
                                    vm.onBlur(ManualField.PASSPORT_ISSUE_DATE) // ✅
                                }
                                DateTarget.EXPIRY -> {
                                    vm.passportExpiryDate(millis)
                                    vm.onBlur(ManualField.PASSPORT_EXPIRY_DATE) // ✅
                                }
                            }
                        }
                    }
                    openDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { openDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Manual Input") },
                windowInsets = WindowInsets(0),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterChip(
                        selected = selected == DocumentType.IDENTITYCARD,
                        onClick = { vm.select(DocumentType.IDENTITYCARD) },
                        label = { Text("IdentityCard") }
                    )
                    FilterChip(
                        selected = selected == DocumentType.PASSPORT,
                        onClick = { vm.select(DocumentType.PASSPORT) },
                        label = { Text("Passport") }
                    )
                }

                if (selected == DocumentType.IDENTITYCARD) {
                    val f = vm.identityCardForm

                    // --- IDENTITYCARD_ID error ---
                    val idErr = vm.errors[ManualField.IDENTITYCARD_ID]
                    val idTouched = vm.isTouched(ManualField.IDENTITYCARD_ID)

                    OutlinedTextField(
                        value = f.idNumber.orEmpty(),
                        onValueChange = vm::identityCardId,
                        label = { Text("Số căn cước") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { st ->
                                if (!st.isFocused) vm.onBlur(ManualField.IDENTITYCARD_ID)
                            },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = idTouched && idErr != null,
                        supportingText = {
                            if (idTouched && idErr != null) Text(idErr)
                        }
                    )

                    // --- IDENTITYCARD_NAME ---
                    val nameErr = vm.errors[ManualField.IDENTITYCARD_NAME]
                    val nameTouched = vm.isTouched(ManualField.IDENTITYCARD_NAME)

                    OutlinedTextField(
                        value = f.fullName.orEmpty(),
                        onValueChange = vm::identityCardName,
                        label = { Text("Họ và tên") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { st ->
                                if (!st.isFocused) vm.onBlur(ManualField.IDENTITYCARD_NAME)
                            },
                        isError = nameTouched && nameErr != null,
                        supportingText = {
                            if (nameTouched && nameErr != null) Text(nameErr)
                        }
                    )

                    // --- IDENTITYCARD_DOB ---
                    val dobErr = vm.errors[ManualField.IDENTITYCARD_DOB]
                    val dobTouched = vm.isTouched(ManualField.IDENTITYCARD_DOB)

                    DatePickerField("Ngày sinh", f.dateOfBirth?.toDdMmYyyy().orEmpty()) {
                        openPicker(DateTarget.DOB)
                    }
                    if (dobTouched && dobErr != null) {
                        Text(dobErr, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    // --- IDENTITYCARD_GENDER ---
                    val gErr = vm.errors[ManualField.IDENTITYCARD_GENDER]
                    val gTouched = vm.isTouched(ManualField.IDENTITYCARD_GENDER)

                    GenderDropdown(
                        label = "Giới tính",
                        valueText = f.gender?.toDisplayVi().orEmpty(),
                        options = genderOptions,
                        onSelected = { t ->
                            vm.identityCardGender(
                                when (t) {
                                    "Nam" -> Gender.MALE
                                    "Nữ" -> Gender.FEMALE
                                    else -> Gender.OTHER
                                }
                            )
                            vm.onBlur(ManualField.IDENTITYCARD_GENDER) // ✅ validate after select
                        }
                    )
                    if (gTouched && gErr != null) {
                        Text(gErr, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    // --- IDENTITYCARD_NATIONALITY ---
                    val natErr = vm.errors[ManualField.IDENTITYCARD_NATIONALITY]
                    val natTouched = vm.isTouched(ManualField.IDENTITYCARD_NATIONALITY)

                    NationalityDropdown(
                        label = "Quốc tịch",
                        valueText = f.nationality.orEmpty(),
                        options = nationalityOptions,
                        onSelected = {
                            vm.identityCardNationality(it)
                            vm.onBlur(ManualField.IDENTITYCARD_NATIONALITY) // ✅
                        }
                    )
                    if (natTouched && natErr != null) {
                        Text(natErr, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    // --- IDENTITYCARD_ORIGIN ---
                    val originErr = vm.errors[ManualField.IDENTITYCARD_ORIGIN]
                    val originTouched = vm.isTouched(ManualField.IDENTITYCARD_ORIGIN)

                    OutlinedTextField(
                        value = f.placeOfOrigin.orEmpty(),
                        onValueChange = vm::identityCardOrigin,
                        label = { Text("Quê quán") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { st ->
                                if (!st.isFocused) vm.onBlur(ManualField.IDENTITYCARD_ORIGIN)
                            },
                        isError = originTouched && originErr != null,
                        supportingText = {
                            if (originTouched && originErr != null) Text(originErr)
                        }
                    )

                    // --- IDENTITYCARD_RESIDENCE ---
                    val resErr = vm.errors[ManualField.IDENTITYCARD_RESIDENCE]
                    val resTouched = vm.isTouched(ManualField.IDENTITYCARD_RESIDENCE)

                    OutlinedTextField(
                        value = f.placeOfResidence.orEmpty(),
                        onValueChange = vm::identityCardResidence,
                        label = { Text("Nơi thường trú") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { st ->
                                if (!st.isFocused) vm.onBlur(ManualField.IDENTITYCARD_RESIDENCE)
                            },
                        isError = resTouched && resErr != null,
                        supportingText = {
                            if (resTouched && resErr != null) Text(resErr)
                        }
                    )

                    // --- IDENTITYCARD_ISSUE_DATE ---
                    val issueErr = vm.errors[ManualField.IDENTITYCARD_ISSUE_DATE]
                    val issueTouched = vm.isTouched(ManualField.IDENTITYCARD_ISSUE_DATE)

                    DatePickerField("Ngày cấp", f.issueDate?.toDdMmYyyy().orEmpty()) {
                        openPicker(DateTarget.ISSUE)
                    }
                    if (issueTouched && issueErr != null) {
                        Text(issueErr, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    // --- IDENTITYCARD_EXPIRY_DATE ---
                    val expErr = vm.errors[ManualField.IDENTITYCARD_EXPIRY_DATE]
                    val expTouched = vm.isTouched(ManualField.IDENTITYCARD_EXPIRY_DATE)

                    DatePickerField("Ngày hết hạn", f.expiryDate?.toDdMmYyyy().orEmpty()) {
                        openPicker(DateTarget.EXPIRY)
                    }
                    if (expTouched && expErr != null) {
                        Text(expErr, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    // --- IDENTITYCARD_ISSUE_PLACE ---
                    val placeErr = vm.errors[ManualField.IDENTITYCARD_ISSUE_PLACE]
                    val placeTouched = vm.isTouched(ManualField.IDENTITYCARD_ISSUE_PLACE)

                    OutlinedTextField(
                        value = f.issuePlace.orEmpty(),
                        onValueChange = vm::identityCardIssuePlace,
                        label = { Text("Nơi cấp") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { st ->
                                if (!st.isFocused) vm.onBlur(ManualField.IDENTITYCARD_ISSUE_PLACE)
                            },
                        isError = placeTouched && placeErr != null,
                        supportingText = {
                            if (placeTouched && placeErr != null) Text(placeErr)
                        }
                    )

                } else {
                    val f = vm.passportForm

                    // --- PASS_ID ---
                    val idErr = vm.errors[ManualField.PASSPORT_ID]
                    val idTouched = vm.isTouched(ManualField.PASSPORT_ID)

                    OutlinedTextField(
                        value = f.idNumber.orEmpty(),
                        onValueChange = vm::passportId,
                        label = { Text("Số căn cước") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { st ->
                                if (!st.isFocused) vm.onBlur(ManualField.PASSPORT_ID)
                            },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = idTouched && idErr != null,
                        supportingText = {
                            if (idTouched && idErr != null) Text(idErr)
                        }
                    )

                    // --- PASS_NAME ---
                    val nameErr = vm.errors[ManualField.PASSPORT_NAME]
                    val nameTouched = vm.isTouched(ManualField.PASSPORT_NAME)

                    OutlinedTextField(
                        value = f.fullName.orEmpty(),
                        onValueChange = vm::passportName,
                        label = { Text("Họ và tên") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { st ->
                                if (!st.isFocused) vm.onBlur(ManualField.PASSPORT_NAME)
                            },
                        isError = nameTouched && nameErr != null,
                        supportingText = {
                            if (nameTouched && nameErr != null) Text(nameErr)
                        }
                    )

                    // --- PASS_DOB ---
                    val dobErr = vm.errors[ManualField.PASSPORT_DOB]
                    val dobTouched = vm.isTouched(ManualField.PASSPORT_DOB)

                    DatePickerField("Ngày sinh", f.dateOfBirth?.toDdMmYyyy().orEmpty()) {
                        openPicker(DateTarget.DOB)
                    }
                    if (dobTouched && dobErr != null) {
                        Text(dobErr, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    // --- PASS_GENDER ---
                    val gErr = vm.errors[ManualField.PASSPORT_GENDER]
                    val gTouched = vm.isTouched(ManualField.PASSPORT_GENDER)

                    GenderDropdown(
                        label = "Giới tính",
                        valueText = f.gender?.toDisplayVi().orEmpty(),
                        options = genderOptions,
                        onSelected = { t ->
                            vm.passportGender(
                                when (t) {
                                    "Nam" -> Gender.MALE
                                    "Nữ" -> Gender.FEMALE
                                    else -> Gender.OTHER
                                }
                            )
                            vm.onBlur(ManualField.PASSPORT_GENDER)
                        }
                    )
                    if (gTouched && gErr != null) {
                        Text(gErr, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    // --- PASS_NATIONALITY ---
                    val natErr = vm.errors[ManualField.PASSPORT_NATIONALITY]
                    val natTouched = vm.isTouched(ManualField.PASSPORT_NATIONALITY)

                    NationalityDropdown(
                        label = "Quốc tịch",
                        valueText = f.nationality.orEmpty(),
                        options = nationalityOptions,
                        onSelected = {
                            vm.passportNationality(it)
                            vm.onBlur(ManualField.PASSPORT_NATIONALITY)
                        }
                    )
                    if (natTouched && natErr != null) {
                        Text(natErr, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    // --- PASS_PASSPORT_NO ---
                    val passNoErr = vm.errors[ManualField.PASSPORT_NO]
                    val passNoTouched = vm.isTouched(ManualField.PASSPORT_NO)

                    OutlinedTextField(
                        value = f.passportNumber.orEmpty(),
                        onValueChange = vm::passportNo,
                        label = { Text("Số hộ chiếu") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { st ->
                                if (!st.isFocused) vm.onBlur(ManualField.PASSPORT_NO)
                            },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                        isError = passNoTouched && passNoErr != null,
                        supportingText = {
                            if (passNoTouched && passNoErr != null) Text(passNoErr)
                        }
                    )

                    // --- PASS_ISSUE_DATE ---
                    val issueErr = vm.errors[ManualField.PASSPORT_ISSUE_DATE]
                    val issueTouched = vm.isTouched(ManualField.PASSPORT_ISSUE_DATE)

                    DatePickerField("Ngày cấp", f.issueDate?.toDdMmYyyy().orEmpty()) {
                        openPicker(DateTarget.ISSUE)
                    }
                    if (issueTouched && issueErr != null) {
                        Text(issueErr, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    // --- PASS_EXPIRY_DATE ---
                    val expErr = vm.errors[ManualField.PASSPORT_EXPIRY_DATE]
                    val expTouched = vm.isTouched(ManualField.PASSPORT_EXPIRY_DATE)

                    DatePickerField("Ngày hết hạn", f.expiryDate?.toDdMmYyyy().orEmpty()) {
                        openPicker(DateTarget.EXPIRY)
                    }
                    if (expTouched && expErr != null) {
                        Text(expErr, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    // --- PASS_AUTHORITY ---
                    val authErr = vm.errors[ManualField.PASSPORT_AUTHORITY]
                    val authTouched = vm.isTouched(ManualField.PASSPORT_AUTHORITY)

                    OutlinedTextField(
                        value = f.issuingAuthority.orEmpty(),
                        onValueChange = vm::passportAuthority,
                        label = { Text("Cơ quan cấp") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { st ->
                                if (!st.isFocused) vm.onBlur(ManualField.PASSPORT_AUTHORITY)
                            },
                        isError = authTouched && authErr != null,
                        supportingText = {
                            if (authTouched && authErr != null) Text(authErr)
                        }
                    )
                }
            }

            Button(
                onClick = vm::submit,
                enabled = vm.canSubmit, // ✅ disable nếu thiếu/invalid
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .imePadding()
            ) {
                Text("Submit & Save")
            }
        }
    }
}
