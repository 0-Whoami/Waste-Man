package com.example.wasteman

enum class UserRole {
    RESIDENT, ADMINISTRATOR
}

enum class IssueCategory(val displayName: String, val isCritical: Boolean = false) {
    OVERFLOWING_BINS("Overflowing Bins", false),
    LITTERING("Littering / Illegal Dumping", false),
    PLASTIC_WASTE("Plastic Waste", false),
    E_WASTE("E-Waste / Electronics", false),
    HAZARDOUS_WASTE("Hazardous / Biomedical Waste", true),
    CONSTRUCTION_DEBRIS("Construction Debris", false)
}

enum class ComplaintStatus(val displayName: String) {
    SUBMITTED("Submitted"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved")
}

enum class NewsType {
    CRITICAL_ALERT, AWARENESS_TIP, COMMUNITY_NEWS
}

data class Complaint(
    val id: String,
    val title: String,
    val category: IssueCategory,
    val description: String,
    val area: String,
    val street: String? = null,
    val landmark: String? = null,
    val pincode: String? = null,
    val locationDetails: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val imageUrl: String? = null,
    val imageData: String? = null,
    val status: ComplaintStatus = ComplaintStatus.SUBMITTED,
    val reporterPhone: String,
    val assignedTeam: String? = null,
    val timestamp: Long = 1710000000000L
)

data class NewsFeedItem(
    val id: String,
    val title: String,
    val content: String,
    val type: NewsType,
    val safetyInstructions: String? = null,
    val area: String? = null,
    val imageUrl: String? = null,
    val timestamp: Long = 1710000000000L
)

data class SummaryStats(
    val totalComplaints: Int,
    val submittedCount: Int,
    val inProgressCount: Int,
    val resolvedCount: Int,
    val areaBreakdown: Map<String, Int>
)
