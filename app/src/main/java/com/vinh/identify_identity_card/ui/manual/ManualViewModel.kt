package com.vinh.identify_identity_card.ui.manual

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vinh.identify_identity_card.data.repo.HistoryRepository
import com.vinh.identify_identity_card.domain.model.DocumentType
import com.vinh.identify_identity_card.domain.model.Gender
import com.vinh.identify_identity_card.domain.model.IdentityCardInfo
import com.vinh.identify_identity_card.domain.model.PassportInfo
import com.vinh.identify_identity_card.utils.normalizeUpper
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar

class ManualViewModel(
    private val repo: HistoryRepository
) : ViewModel() {

    var selected by mutableStateOf(DocumentType.IDENTITYCARD)
        private set

    var identityCardForm by mutableStateOf(IdentityCardInfo())
        private set

    var passportForm by mutableStateOf(PassportInfo())
        private set

    // message dùng snackbar bên UI
    var message by mutableStateOf<String?>(null)
        private set

    // error map + touched map
    private val _errors = mutableStateMapOf<ManualField, String>()
    val errors: Map<ManualField, String> get() = _errors

    private val _touched = mutableStateMapOf<ManualField, Boolean>()
    fun isTouched(field: ManualField) = _touched[field] == true

    // Track: user có tự set expiry chưa (để không overwrite)
    private var IDENTITYCARDExpiryManuallySet by mutableStateOf(false)

    fun select(type: DocumentType) {
        selected = type
        // optional: reset touched/errors khi đổi tab
        // _errors.clear(); _touched.clear()
    }

    // -----------------------------
    // SETTERS (có filter input)
    // -----------------------------
    fun identityCardId(v: String) {
        val onlyDigits = v.filter { it.isDigit() }
        identityCardForm = identityCardForm.copy(idNumber = onlyDigits)
    }

    fun identityCardName(v: String) { identityCardForm = identityCardForm.copy(fullName = v) }

    fun identityCardDob(v: Long) {
        identityCardForm = identityCardForm.copy(dateOfBirth = v)

        // ✅ auto set expiry = DOB + 25 years (default)
        if (!IDENTITYCARDExpiryManuallySet) {
            identityCardForm = identityCardForm.copy(expiryDate = addYears(v, 25))
        }
    }

    fun identityCardGender(v: Gender) { identityCardForm = identityCardForm.copy(gender = v) }
    fun identityCardNationality(v: String) { identityCardForm = identityCardForm.copy(nationality = v) }
    fun identityCardOrigin(v: String) { identityCardForm = identityCardForm.copy(placeOfOrigin = v) }
    fun identityCardResidence(v: String) { identityCardForm = identityCardForm.copy(placeOfResidence = v) }
    fun identityCardIssueDate(v: Long) { identityCardForm = identityCardForm.copy(issueDate = v) }

    fun identityCardExpiryDate(v: Long) {
        IDENTITYCARDExpiryManuallySet = true
        identityCardForm = identityCardForm.copy(expiryDate = v)
    }

    fun identityCardIssuePlace(v: String) { identityCardForm = identityCardForm.copy(issuePlace = v) }

    // Passport setters
    fun passportId(v: String) {
        val onlyDigits = v.filter { it.isDigit() }
        passportForm = passportForm.copy(idNumber = onlyDigits)
    }

    fun passportName(v: String) { passportForm = passportForm.copy(fullName = v) }
    fun passportDob(v: Long) { passportForm = passportForm.copy(dateOfBirth = v) }
    fun passportGender(v: Gender) { passportForm = passportForm.copy(gender = v) }
    fun passportNationality(v: String) { passportForm = passportForm.copy(nationality = v) }

    fun passportNo(v: String) {
        // format: 1 uppercase letter + 7 digits (8 chars)
        val cleaned = v.uppercase()
        // cho nhập thoải mái, validate khi blur
        passportForm = passportForm.copy(passportNumber = cleaned)
    }

    fun passportIssueDate(v: Long) { passportForm = passportForm.copy(issueDate = v) }
    fun passportExpiryDate(v: Long) { passportForm = passportForm.copy(expiryDate = v) }
    fun passportAuthority(v: String) { passportForm = passportForm.copy(issuingAuthority = v) }

    // -----------------------------
    // BLUR VALIDATION entrypoint
    // gọi khi user rời ô
    // -----------------------------
    fun onBlur(field: ManualField) {
        _touched[field] = true
        validateField(field)
    }

    private fun setError(field: ManualField, msg: String?) {
        if (msg.isNullOrBlank()) _errors.remove(field) else _errors[field] = msg
    }

    private fun validateField(field: ManualField) {
        when (field) {
            // IDENTITYCARD required + format
            ManualField.IDENTITYCARD_ID -> {
                val id = identityCardForm.idNumber.orEmpty()
                when {
                    id.isBlank() -> setError(field, "Bắt buộc")
                    !id.all { it.isDigit() } -> setError(field, "Chỉ được nhập số")
                    id.length != 14 -> setError(field, "IDENTITYCARD phải gồm 14 chữ số")
                    else -> setError(field, null)
                }
            }

            ManualField.IDENTITYCARD_NAME ->
                setError(field, if (identityCardForm.fullName.isNullOrBlank()) "Bắt buộc" else null)

            ManualField.IDENTITYCARD_DOB -> {
                val dob = identityCardForm.dateOfBirth
                if (dob == null) {
                    setError(field, "Bắt buộc")
                } else {
                    val ok = isAtLeastYearsOld(dob, 14)
                    setError(field, if (!ok) "Phải từ 14 tuổi trở lên" else null)
                }
            }

            ManualField.IDENTITYCARD_GENDER ->
                setError(field, if (identityCardForm.gender == null) "Bắt buộc" else null)

            ManualField.IDENTITYCARD_NATIONALITY ->
                setError(field, if (identityCardForm.nationality.isNullOrBlank()) "Bắt buộc" else null)

            ManualField.IDENTITYCARD_ORIGIN ->
                setError(field, if (identityCardForm.placeOfOrigin.isNullOrBlank()) "Bắt buộc" else null)

            ManualField.IDENTITYCARD_RESIDENCE ->
                setError(field, if (identityCardForm.placeOfResidence.isNullOrBlank()) "Bắt buộc" else null)

            ManualField.IDENTITYCARD_ISSUE_DATE ->
                setError(field, if (identityCardForm.issueDate == null) "Bắt buộc" else null)

            ManualField.IDENTITYCARD_EXPIRY_DATE ->
                setError(field, if (identityCardForm.expiryDate == null) "Bắt buộc" else null)

            ManualField.IDENTITYCARD_ISSUE_PLACE ->
                setError(field, if (identityCardForm.issuePlace.isNullOrBlank()) "Bắt buộc" else null)

            // PASSPORT required + format
            ManualField.PASSPORT_ID -> {
                val id = passportForm.idNumber.orEmpty()
                if (id.isBlank()) setError(field, "Bắt buộc")
                else if (!id.all { it.isDigit() }) setError(field, "Chỉ được nhập số")
                else setError(field, null)
            }

            ManualField.PASSPORT_NAME ->
                setError(field, if (passportForm.fullName.isNullOrBlank()) "Bắt buộc" else null)

            ManualField.PASSPORT_DOB -> {
                val dob = passportForm.dateOfBirth
                if (dob == null) setError(field, "Bắt buộc")
                else {
                    val ok = isAtLeastYearsOld(dob, 0) // passport không yêu cầu 14 (bạn không nói)
                    setError(field, if (!ok) "Ngày sinh không hợp lệ" else null)
                }
            }

            ManualField.PASSPORT_GENDER ->
                setError(field, if (passportForm.gender == null) "Bắt buộc" else null)

            ManualField.PASSPORT_NATIONALITY ->
                setError(field, if (passportForm.nationality.isNullOrBlank()) "Bắt buộc" else null)

            ManualField.PASSPORT_NO -> {
                val p = passportForm.passportNumber.orEmpty()
                val ok = Regex("^[A-Z][0-9]{7}$").matches(p)
                when {
                    p.isBlank() -> setError(field, "Bắt buộc")
                    !ok -> setError(field, "Hộ chiếu: 1 chữ in hoa + 7 chữ số (VD: B1234567)")
                    else -> setError(field, null)
                }
            }

            ManualField.PASSPORT_ISSUE_DATE ->
                setError(field, if (passportForm.issueDate == null) "Bắt buộc" else null)

            ManualField.PASSPORT_EXPIRY_DATE ->
                setError(field, if (passportForm.expiryDate == null) "Bắt buộc" else null)

            ManualField.PASSPORT_AUTHORITY ->
                setError(field, if (passportForm.issuingAuthority.isNullOrBlank()) "Bắt buộc" else null)
        }
    }

    // -----------------------------
    // SUBMIT enable
    // -----------------------------
    val canSubmit: Boolean
        get() = if (selected == DocumentType.IDENTITYCARD) {
            // bắt buộc tất cả + không có error liên quan IDENTITYCARD
            isIDENTITYCARDComplete() && _errors.keys.none { it.name.startsWith("IDENTITYCARD") }
        } else {
            isPassportComplete() && _errors.keys.none { it.name.startsWith("PASS") }
        }

    private fun isIDENTITYCARDComplete(): Boolean {
        val f = identityCardForm
        return !f.idNumber.isNullOrBlank() &&
                !f.fullName.isNullOrBlank() &&
                f.dateOfBirth != null &&
                f.gender != null &&
                !f.nationality.isNullOrBlank() &&
                !f.placeOfOrigin.isNullOrBlank() &&
                !f.placeOfResidence.isNullOrBlank() &&
                f.issueDate != null &&
                f.expiryDate != null &&
                !f.issuePlace.isNullOrBlank()
    }

    private fun isPassportComplete(): Boolean {
        val f = passportForm
        return !f.idNumber.isNullOrBlank() &&
                !f.fullName.isNullOrBlank() &&
                f.dateOfBirth != null &&
                f.gender != null &&
                !f.nationality.isNullOrBlank() &&
                !f.passportNumber.isNullOrBlank() &&
                f.issueDate != null &&
                f.expiryDate != null &&
                !f.issuingAuthority.isNullOrBlank()
    }

    fun submit() {
        // chặn nếu invalid
        if (!canSubmit) {
            message = "Vui lòng kiểm tra các trường bắt buộc."
            // mark all required as touched + validate để hiện error
            touchAndValidateAllRequired()
            return
        }

        viewModelScope.launch {
            when (selected) {
                DocumentType.IDENTITYCARD -> {
                    val normalized = identityCardForm.copy(
                        fullName = normalizeUpper(identityCardForm.fullName),
                        issuePlace = normalizeUpper(identityCardForm.issuePlace),
                    )
                    repo.insertManual("IdentityCard", Json.encodeToString(normalized))
                }

                DocumentType.PASSPORT -> {
                    val normalized = passportForm.copy(
                        fullName = normalizeUpper(passportForm.fullName),
                        issuingAuthority = normalizeUpper(passportForm.issuingAuthority),
                    )
                    repo.insertManual("Passport", Json.encodeToString(normalized))
                }
            }
            message = "Saved to history"
            clearAll()
        }
    }

    fun clearMessage() { message = null }

    private fun clearAll() {
        identityCardForm = IdentityCardInfo()
        passportForm = PassportInfo()
        IDENTITYCARDExpiryManuallySet = false
        _errors.clear()
        _touched.clear()
    }

    private fun touchAndValidateAllRequired() {
        val fields = if (selected == DocumentType.IDENTITYCARD) {
            listOf(
                ManualField.IDENTITYCARD_ID, ManualField.IDENTITYCARD_NAME, ManualField.IDENTITYCARD_DOB,
                ManualField.IDENTITYCARD_GENDER, ManualField.IDENTITYCARD_NATIONALITY, ManualField.IDENTITYCARD_ORIGIN,
                ManualField.IDENTITYCARD_RESIDENCE, ManualField.IDENTITYCARD_ISSUE_DATE, ManualField.IDENTITYCARD_EXPIRY_DATE,
                ManualField.IDENTITYCARD_ISSUE_PLACE
            )
        } else {
            listOf(
                ManualField.PASSPORT_ID, ManualField.PASSPORT_NAME, ManualField.PASSPORT_DOB,
                ManualField.PASSPORT_GENDER, ManualField.PASSPORT_NATIONALITY, ManualField.PASSPORT_NO,
                ManualField.PASSPORT_ISSUE_DATE, ManualField.PASSPORT_EXPIRY_DATE, ManualField.PASSPORT_AUTHORITY
            )
        }
        fields.forEach {
            _touched[it] = true
            validateField(it)
        }
    }

    // -----------------------------
    // Helpers
    // -----------------------------
    private fun addYears(millis: Long, years: Int): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        cal.add(Calendar.YEAR, years)
        return cal.timeInMillis
    }

    private fun isAtLeastYearsOld(dobMillis: Long, years: Int): Boolean {
        val now = Calendar.getInstance()
        val dob = Calendar.getInstance().apply { timeInMillis = dobMillis }
        var age = now.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        val nowDayOfYear = now.get(Calendar.DAY_OF_YEAR)
        val dobDayOfYear = dob.get(Calendar.DAY_OF_YEAR)
        if (nowDayOfYear < dobDayOfYear) age -= 1
        return age >= years
    }
}
