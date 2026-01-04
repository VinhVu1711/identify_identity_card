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
import android.util.Log
private const val TAG = "ScanVM"
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

//    fun selectDocType(type: DocumentType) { selectedDocType = type }

    fun setImage(uri: Uri) { imageUri = uri }

    fun clearError() { error = null }
//    var uiHint by mutableStateOf<String?>(null)
//        private set
//
//    fun setUiHint(msg: String) { uiHint = msg }
//    fun clearUiHint() { uiHint = null }
var frontUri by mutableStateOf<Uri?>(null)
    private set
    var backUri by mutableStateOf<Uri?>(null)
        private set

    fun setFront(uri: Uri) { frontUri = uri }
    fun setBack(uri: Uri) { backUri = uri }
    fun scanFrontBack(context: Context, onSuccess: () -> Unit) {
        val fUri = frontUri ?: run { error = "Chưa chọn mặt trước"; return }
        val bUri = backUri ?: run { error = "Chưa chọn mặt sau"; return }

        viewModelScope.launch {
            try {
                isLoading = true
                error = null

                val fMime = context.contentResolver.getType(fUri) ?: "image/jpeg"
                val bMime = context.contentResolver.getType(bUri) ?: "image/jpeg"

                val fFile = withContext(Dispatchers.IO) { uriToTempFile(context, fUri) }
                val bFile = withContext(Dispatchers.IO) { uriToTempFile(context, bUri) }

                Log.d("ScanVM", "📸 Front uri=$fUri")
                Log.d("ScanVM", "📸 Back uri=$bUri")
                Log.d("ScanVM", "Front mime=$fMime Back mime=$bMime")
                Log.d("ScanVM", "Front file=${fFile.length()} bytes")
                Log.d("ScanVM", "Back file=${bFile.length()} bytes")

                Log.d("ScanVM", "📤 Calling scanRepo.scanCccdFrontBack()")

                val raw = withContext(Dispatchers.IO) {
                    scanRepo.scanCccdFrontBack(fFile, fMime, bFile, bMime)
                }
                Log.d("ScanVM", "📥 RAW RESPONSE:\n${raw.take(1500)}")


                val dto = json.decodeFromString(IdentityCardScanDto.serializer(), raw)

                val mapped = IdentityCardInfo(
                    idNumber = dto.idNumber,
                    fullName = dto.fullName,
                    dateOfBirth = parseDdMmYyyyToMillis(dto.dateOfBirth),
                    gender = dto.gender,
                    nationality = dto.nationality,
                    placeOfOrigin = dto.placeOfOrigin,
                    placeOfResidence = dto.placeOfResidence,
                    issueDate = parseDdMmYyyyToMillis(dto.issueDate),
                    expiryDate = parseDdMmYyyyToMillis(dto.expiryDate),
                    issuePlace = dto.issuePlace
                )

                // Giờ bạn có thể check "critical" thôi
                if (mapped.idNumber.isNullOrBlank() || mapped.fullName.isNullOrBlank()) {
                    error = "Không đọc được trường quan trọng. Hãy chụp rõ hơn."
                    return@launch
                }

                identityCardForm = mapped
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

    var passportUri by mutableStateOf<Uri?>(null)
        private set


    fun setPassport(uri: Uri?) { passportUri = uri }

    fun clearPickedImages() {
        frontUri = null
        backUri = null
        passportUri = null
    }
    fun selectDocType(type: DocumentType) {
        selectedDocType = type
        clearPickedImages()
    }
    fun scanPassportOne(context: Context, onSuccess: () -> Unit) {
        val pUri = passportUri ?: run { error = "Chưa chọn ảnh passport"; return }

        viewModelScope.launch {
            try {
                isLoading = true
                error = null

                val mime = context.contentResolver.getType(pUri) ?: "image/jpeg"
                val file = withContext(Dispatchers.IO) { uriToTempFile(context, pUri) }

                val raw = withContext(Dispatchers.IO) {
                    scanRepo.scanPassportOne(file, mime)
                }

                val dto = json.decodeFromString(PassportScanDto.serializer(), raw)

                val mapped = PassportInfo(
                    idNumber = dto.idNumber,
                    fullName = dto.fullName,
                    dateOfBirth = parseDdMmYyyyToMillis(dto.dateOfBirth),
                    gender = dto.gender,
                    nationality = dto.nationality,
                    passportNumber = dto.passportNumber,
                    issueDate = parseDdMmYyyyToMillis(dto.issueDate),
                    expiryDate = parseDdMmYyyyToMillis(dto.expiryDate),
                    issuingAuthority = dto.issuingAuthority
                )

                passportForm = mapped
                onSuccess()
            } catch (e: Exception) {
                error = "Scan thất bại: ${e.message}"
            } finally {
                isLoading = false
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
