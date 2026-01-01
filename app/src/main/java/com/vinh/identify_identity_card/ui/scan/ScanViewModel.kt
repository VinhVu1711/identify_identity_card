package com.vinh.identify_identity_card.ui.scan

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vinh.identify_identity_card.data.repo.HistoryRepository
import com.vinh.identify_identity_card.data.repo.ScanRepository
import com.vinh.identify_identity_card.domain.model.*
import com.vinh.identify_identity_card.utils.parseDdMmYyyyToMillis
import com.vinh.identify_identity_card.utils.uriToTempFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ScanViewModel(
    private val scanRepo: ScanRepository,
    private val historyRepo: HistoryRepository
) : ViewModel() {

    var selectedDocType by mutableStateOf(DocumentType.IDENTITYCARD)
        private set

    var imageUri by mutableStateOf<Uri?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var identityCardForm by mutableStateOf(IdentityCardInfo())
        private set

    var passportForm by mutableStateOf(PassportInfo())
        private set

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    fun selectDocType(type: DocumentType) { selectedDocType = type }

    fun setImage(uri: Uri) { imageUri = uri }

    fun clearError() { error = null }
//    var uiHint by mutableStateOf<String?>(null)
//        private set
//
//    fun setUiHint(msg: String) { uiHint = msg }
//    fun clearUiHint() { uiHint = null }

    fun scan(context: Context, uri: Uri, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                error = null
                isLoading = true
                imageUri = uri

                val file = withContext(Dispatchers.IO) { uriToTempFile(context, uri) }

                val raw = withContext(Dispatchers.IO) { scanRepo.scan(selectedDocType, file) }

                if (selectedDocType == DocumentType.IDENTITYCARD) {
                    val dto = json.decodeFromString(IdentityCardScanDto.serializer(), raw)

                    val mapped = IdentityCardInfo(
                        idNumber = dto.idNumber,
                        fullName = dto.fullName,
                        dateOfBirth = parseDdMmYyyyToMillis(dto.dateOfBirth),
                        gender = dto.gender,
                        nationality = dto.nationality,
                        placeOfOrigin = dto.placeOfOrigin,
                        placeOfResidence = dto.placeOfResidence,
                        expiryDate = parseDdMmYyyyToMillis(dto.expiryDate),
                        issueDate = parseDdMmYyyyToMillis(dto.issueDate),
                        issuePlace = dto.issuePlace
                    )

                    // ✅ check “đủ thông tin”
                    if (!isIdentityCardComplete(mapped)) {
                        error = "Không trích xuất đủ thông tin. Hãy chụp lại ảnh rõ nét hơn."
                        return@launch
                    }

                    identityCardForm = mapped
                } else {
                    val dto = json.decodeFromString(PassportScanDto.serializer(), raw)

                    val mapped = PassportInfo(
                        idNumber = dto.idNumber,
                        fullName = dto.fullName,
                        dateOfBirth = parseDdMmYyyyToMillis(dto.dateOfBirth),
                        gender = dto.gender,
                        nationality = dto.nationality,
                        passportNumber = dto.passportNumber,
                        expiryDate = parseDdMmYyyyToMillis(dto.expiryDate),
                        issueDate = parseDdMmYyyyToMillis(dto.issueDate),
                        issuingAuthority = dto.issuingAuthority
                    )

                    if (!isPassportComplete(mapped)) {
                        error = "Không trích xuất đủ thông tin. Hãy chụp lại ảnh rõ nét hơn."
                        return@launch
                    }

                    passportForm = mapped
                }

                onSuccess()
            } catch (e: Exception) {
                error = "Scan thất bại: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun saveToHistory() {
        viewModelScope.launch {
            try {
                val uriStr = imageUri?.toString()
                if (selectedDocType == DocumentType.IDENTITYCARD) {
                    historyRepo.insertScan(
                        docType = "IdentityCard",
                        dataJson = Json.encodeToString(identityCardForm),
                        imageUri = uriStr
                    )
                } else {
                    historyRepo.insertScan(
                        docType = "Passport",
                        dataJson = Json.encodeToString(passportForm),
                        imageUri = uriStr
                    )
                }
            } catch (e: Exception) {
                error = "Lưu thất bại: ${e.message}"
            }
        }
    }

    private fun isIdentityCardComplete(f: IdentityCardInfo): Boolean {
        return !f.idNumber.isNullOrBlank()
                && !f.fullName.isNullOrBlank()
                && f.dateOfBirth != null
                && f.gender != null
                && !f.nationality.isNullOrBlank()
                && !f.placeOfOrigin.isNullOrBlank()
                && !f.placeOfResidence.isNullOrBlank()
                && f.issueDate != null
                && f.expiryDate != null
                && !f.issuePlace.isNullOrBlank()
    }

    private fun isPassportComplete(f: PassportInfo): Boolean {
        return !f.idNumber.isNullOrBlank()
                && !f.fullName.isNullOrBlank()
                && f.dateOfBirth != null
                && f.gender != null
                && !f.nationality.isNullOrBlank()
                && !f.passportNumber.isNullOrBlank()
                && f.issueDate != null
                && f.expiryDate != null
                && !f.issuingAuthority.isNullOrBlank()
    }
}
