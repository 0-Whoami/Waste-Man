package com.example.wasteman.state

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import com.example.wasteman.core.IssueStats
import com.example.wasteman.core.NewsAnnouncement
import com.example.wasteman.core.NewsIconType
import com.example.wasteman.core.ReportStatus
import com.example.wasteman.core.UserSession
import com.example.wasteman.core.WasteCategory
import com.example.wasteman.core.WasteManBackend
import com.example.wasteman.core.WasteReportDraft
import com.example.wasteman.core.WasteReportItem
import com.example.wasteman.platform.createDemoBitmap
import com.example.wasteman.theme.EcoGreen
import kotlinx.coroutines.delay

const val DEMO_NOW_MILLIS = 1_764_126_180_000L
private const val HOUR_MILLIS = 3_600_000L
private const val DAY_MILLIS = 86_400_000L

class DemoWasteManBackend : WasteManBackend {
    private var nextReportNumber = 106
    private var currentPhone = "+919876543210"

    private val demoReports = mutableListOf(
        WasteReportItem(
            id = "REP-105",
            reporterName = "",
            reporterPhone = "+919876543210",
            category = WasteCategory.OVERFLOWING_BINS,
            description = "Municipal dumpster near Sector 5 bus stop has been overflowing onto the footpath for 3 days.",
            location = "Sector 5, Main Market",
            timestamp = "2h ago",
            createdAtMillis = DEMO_NOW_MILLIS - (2 * HOUR_MILLIS),
            status = ReportStatus.PENDING
        ),
        WasteReportItem(
            id = "REP-104",
            reporterName = "Priya Nair",
            reporterPhone = "+919123456780",
            category = WasteCategory.PLASTIC_WASTE,
            description = "Large pile of single-use plastic bags and PET bottles dumped beside the storm drain.",
            location = "Canal Road, Block B",
            timestamp = "Yesterday",
            createdAtMillis = DEMO_NOW_MILLIS - (30 * HOUR_MILLIS),
            status = ReportStatus.IN_PROGRESS
        ),
        WasteReportItem(
            id = "REP-103",
            reporterName = "",
            reporterPhone = "+919876543210",
            category = WasteCategory.E_WASTE,
            description = "Broken monitors, circuit boards, and tangled cables left near the north park gate.",
            location = "Greenview Park North",
            timestamp = "5 days ago",
            createdAtMillis = DEMO_NOW_MILLIS - (5 * DAY_MILLIS),
            status = ReportStatus.SOLVED
        ),
        WasteReportItem(
            id = "REP-102",
            reporterName = "",
            reporterPhone = "+919988776655",
            category = WasteCategory.LITTERING,
            description = "Scattered food cartons and packaging outside Metro Gate 2.",
            location = "Central Metro Gate 2",
            timestamp = "18 days ago",
            createdAtMillis = DEMO_NOW_MILLIS - (18 * DAY_MILLIS),
            status = ReportStatus.SOLVED
        ),
        WasteReportItem(
            id = "REP-101",
            reporterName = "Meera Sen",
            reporterPhone = "+919811223344",
            category = WasteCategory.HAZARDOUS_WASTE,
            description = "Discarded paint cans and chemical drums behind the industrial warehouse.",
            location = "Plot 14, East Industrial Zone",
            timestamp = "Oct 12, 2026",
            createdAtMillis = DEMO_NOW_MILLIS - (45 * DAY_MILLIS),
            status = ReportStatus.SOLVED
        )
    )

    val demoThumbnails: MutableMap<String, ImageBitmap> = mutableMapOf(
        "REP-105" to createDemoBitmap(Color(0xFF8D6E63), width = 160, height = 100),
        "REP-104" to createDemoBitmap(Color(0xFF42A5F5), width = 120, height = 150),
        "REP-103" to createDemoBitmap(Color(0xFF78909C), width = 180, height = 100),
        "REP-101" to createDemoBitmap(Color(0xFFE57373), width = 140, height = 110)
    )

    fun defaultNews(): List<NewsAnnouncement> = listOf(
        NewsAnnouncement(
            id = "1",
            icon = NewsIconType.RECYCLING,
            heading = "E-Waste Drive",
            content = "Drop off old electronics at Sector 5 Community Center this Sunday for certified recycling."
        ),
        NewsAnnouncement(
            id = "2",
            icon = NewsIconType.CAMPAIGN,
            heading = "Zero-Plastic Week",
            content = "Local markets are switching to compostable bags. Report illegal plastic dumping instantly."
        ),
        NewsAnnouncement(
            id = "3",
            icon = NewsIconType.VOLUNTEER,
            heading = "76% Resolved!",
            content = "Thanks to citizen reports, 76 waste hotspots were cleared across the city this month."
        )
    )

    override suspend fun sendOtpOrLogin(phoneNumber: String): Result<UserSession> {
        delay(350)
        currentPhone = phoneNumber
        val isAdmin = phoneNumber.endsWith("0000") || phoneNumber.endsWith("3210")
        return Result.success(
            UserSession(
                phoneNumber = phoneNumber,
                isAdmin = isAdmin,
                token = "demo-token"
            )
        )
    }

    override suspend fun submitReport(draft: WasteReportDraft): Result<WasteReportItem> {
        delay(300)
        val id = "REP-${nextReportNumber++}"
        val created = WasteReportItem(
            id = id,
            reporterName = draft.reporterName,
            reporterPhone = draft.reporterPhone,
            category = draft.category,
            description = draft.description,
            location = draft.location,
            timestamp = "Just now",
            createdAtMillis = DEMO_NOW_MILLIS,
            status = ReportStatus.PENDING,
            imageBase64 = draft.imageBase64
        )
        demoReports.add(0, created)
        if (draft.imageBase64 == null) {
            demoThumbnails[id] = createDemoBitmap(EcoGreen, width = 160, height = 100)
        }
        return Result.success(created)
    }

    override suspend fun fetchReports(isAdmin: Boolean): Result<List<WasteReportItem>> =
        Result.success(
            if (isAdmin) demoReports.toList() else demoReports.map { it.sanitizedForPublic() }
        )

    override suspend fun fetchMyReports(): Result<List<WasteReportItem>> {
        val matching = if (currentPhone.isBlank()) {
            demoReports
        } else {
            demoReports.filter { it.reporterPhone == currentPhone }
        }
        return Result.success(matching.map { it.sanitizedForPublic() })
    }

    override suspend fun updateReportStatus(reportId: String, newStatus: ReportStatus): Result<Unit> {
        delay(100)
        val idx = demoReports.indexOfFirst { it.id == reportId }
        if (idx != -1) {
            demoReports[idx] = demoReports[idx].copy(status = newStatus)
        }
        return Result.success(Unit)
    }

    override suspend fun fetchDashboardStats(): Result<IssueStats> {
        val total = 95 + demoReports.size
        val solved = 73 + demoReports.count { it.status == ReportStatus.SOLVED }
        return Result.success(IssueStats(total = total, solved = solved))
    }
}
