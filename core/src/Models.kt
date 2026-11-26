package com.example.wasteman.core

import kotlinx.serialization.Serializable
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Serializable
enum class WasteCategory(val displayName: String) {
    OVERFLOWING_BINS("Overflowing Bins"),
    LITTERING("Littering"),
    PLASTIC_WASTE("Plastic Waste"),
    E_WASTE("E-Waste"),
    ORGANIC_WASTE("Organic Waste"),
    HAZARDOUS_WASTE("Hazardous Waste"),
    OTHER("Other")
}

@Serializable
enum class ReportStatus(val label: String) {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    SOLVED("Solved")
}

enum class ReportTimeFilter(val label: String, val maxAgeMillis: Long?) {
    ALL("All Time", null),
    TODAY("Today", 24L * 60 * 60 * 1000),
    LAST_7_DAYS("Last 7 Days", 7L * 24 * 60 * 60 * 1000),
    LAST_30_DAYS("Last 30 Days", 30L * 24 * 60 * 60 * 1000),
    OLDER("Older", null)
}

@Serializable
enum class NewsIconType {
    RECYCLING,
    CAMPAIGN,
    VOLUNTEER,
    ALERT
}

@Serializable
data class WasteReportDraft(
    val reporterName: String,
    val reporterPhone: String,
    val category: WasteCategory,
    val description: String,
    val location: String,
    val imageBase64: String? = null
)

@Serializable
data class WasteReportItem(
    val id: String,
    val reporterName: String = "",
    val reporterPhone: String = "",
    val category: WasteCategory,
    val description: String,
    val location: String,
    val timestamp: String,
    val createdAtMillis: Long = 0L,
    val status: ReportStatus,
    val imageUrl: String? = null,
    val imageBase64: String? = null
) {
    fun displayReporter(isAdmin: Boolean): String = when {
        reporterName.isNotBlank() -> reporterName
        isAdmin && reporterPhone.isNotBlank() -> reporterPhone
        else -> "Citizen"
    }

    fun sanitizedForPublic(): WasteReportItem =
        if (reporterPhone.isEmpty()) this else copy(reporterPhone = "")
}

@Serializable
data class IssueStats(
    val total: Int = 0,
    val solved: Int = 0
) {
    val pending: Int get() = (total - solved).coerceAtLeast(0)
}

@Serializable
data class NewsAnnouncement(
    val id: String,
    val icon: NewsIconType,
    val heading: String,
    val content: String
)

@Serializable
data class UserSession(
    val phoneNumber: String,
    val isAdmin: Boolean,
    val token: String = ""
)

@Serializable
data class LoginRequest(
    val phoneNumber: String
)

@Serializable
data class UpdateReportStatusRequest(
    val status: ReportStatus
)

@OptIn(ExperimentalEncodingApi::class)
fun ByteArray.toBase64String(): String = Base64.encode(this)

@OptIn(ExperimentalEncodingApi::class)
fun String.decodeBase64OrNull(): ByteArray? = runCatching {
    val clean = substringAfter("base64,", this).trim()
    if (clean.isEmpty()) null else Base64.decode(clean)
}.getOrNull()
