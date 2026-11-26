package com.example.wasteman.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import com.example.wasteman.core.IssueStats
import com.example.wasteman.core.KtorWasteManBackend
import com.example.wasteman.core.NewsAnnouncement
import com.example.wasteman.core.ReportStatus
import com.example.wasteman.core.WasteManBackend
import com.example.wasteman.core.WasteReportDraft
import com.example.wasteman.core.WasteReportItem
import com.example.wasteman.core.createWasteManHttpClient
import com.example.wasteman.core.decodeBase64OrNull
import com.example.wasteman.platform.ReportPlatformHandler
import com.example.wasteman.platform.createPlatformHandler

enum class AppTab { HOME, REPORT, ISSUES, ADMIN, LOGIN }

class AppState(
    initialUseDemoBackend: Boolean = true,
    baseUrl: String = "http://localhost:8080",
    val platformHandler: ReportPlatformHandler = createPlatformHandler()
) {
    val demoBackend = DemoWasteManBackend()
    val ktorBackend = KtorWasteManBackend(createWasteManHttpClient(), baseUrl)

    var useDemoBackend by mutableStateOf(initialUseDemoBackend)
    val backend: WasteManBackend get() = if (useDemoBackend) demoBackend else ktorBackend

    var activeTab by mutableStateOf(AppTab.HOME)
    var isAdmin by mutableStateOf(initialUseDemoBackend)
    var loginNum by mutableStateOf(if (initialUseDemoBackend) "+919876543210" else "")
    var savedReporterName by mutableStateOf("")
    var isLoading by mutableStateOf(false)

    var issueStats by mutableStateOf(IssueStats(total = 100, solved = 76))
    val reports = mutableStateListOf<WasteReportItem>()
    val myReports = mutableStateListOf<WasteReportItem>()
    val newsItems = mutableStateListOf<NewsAnnouncement>()
    val thumbnailCache = mutableStateMapOf<String, ImageBitmap>()

    init {
        newsItems.addAll(demoBackend.defaultNews())
        if (initialUseDemoBackend) {
            loadInitialDemoSnapshot()
        }
    }

    private fun loadInitialDemoSnapshot() {
        thumbnailCache.putAll(demoBackend.demoThumbnails)
        loadDemoReports()
    }

    private fun loadDemoReports() {
        val initial = listOf(
            WasteReportItem(
                id = "REP-105",
                reporterName = "",
                reporterPhone = "+919876543210",
                category = com.example.wasteman.core.WasteCategory.OVERFLOWING_BINS,
                description = "Municipal dumpster near Sector 5 bus stop has been overflowing onto the footpath for 3 days.",
                location = "Sector 5, Main Market",
                timestamp = "2h ago",
                createdAtMillis = DEMO_NOW_MILLIS - (2 * 3_600_000L),
                status = ReportStatus.PENDING
            ),
            WasteReportItem(
                id = "REP-104",
                reporterName = "Priya Nair",
                reporterPhone = "+919123456780",
                category = com.example.wasteman.core.WasteCategory.PLASTIC_WASTE,
                description = "Large pile of single-use plastic bags and PET bottles dumped beside the storm drain.",
                location = "Canal Road, Block B",
                timestamp = "Yesterday",
                createdAtMillis = DEMO_NOW_MILLIS - (30 * 3_600_000L),
                status = ReportStatus.IN_PROGRESS
            ),
            WasteReportItem(
                id = "REP-103",
                reporterName = "",
                reporterPhone = "+919876543210",
                category = com.example.wasteman.core.WasteCategory.E_WASTE,
                description = "Broken monitors, circuit boards, and tangled cables left near the north park gate.",
                location = "Greenview Park North",
                timestamp = "5 days ago",
                createdAtMillis = DEMO_NOW_MILLIS - (5 * 86_400_000L),
                status = ReportStatus.SOLVED
            ),
            WasteReportItem(
                id = "REP-102",
                reporterName = "",
                reporterPhone = "+919988776655",
                category = com.example.wasteman.core.WasteCategory.LITTERING,
                description = "Scattered food cartons and packaging outside Metro Gate 2.",
                location = "Central Metro Gate 2",
                timestamp = "18 days ago",
                createdAtMillis = DEMO_NOW_MILLIS - (18 * 86_400_000L),
                status = ReportStatus.SOLVED
            ),
            WasteReportItem(
                id = "REP-101",
                reporterName = "Meera Sen",
                reporterPhone = "+919811223344",
                category = com.example.wasteman.core.WasteCategory.HAZARDOUS_WASTE,
                description = "Discarded paint cans and chemical drums behind the industrial warehouse.",
                location = "Plot 14, East Industrial Zone",
                timestamp = "Oct 12, 2026",
                createdAtMillis = DEMO_NOW_MILLIS - (45 * 86_400_000L),
                status = ReportStatus.SOLVED
            )
        )
        reports.clear()
        reports.addAll(initial)
        myReports.clear()
        myReports.addAll(initial.filter { it.reporterPhone == loginNum }.map { it.sanitizedForPublic() })
        recalculateLocalStats()
    }

    suspend fun refreshFromBackend(fallbackToDemoOnFailure: Boolean = true) {
        isLoading = true
        val reportsResult = backend.fetchReports(isAdmin)
        val statsResult = backend.fetchDashboardStats()

        if (reportsResult.isFailure && !useDemoBackend && fallbackToDemoOnFailure) {
            useDemoBackend = true
            loadInitialDemoSnapshot()
            isLoading = false
            return
        }

        reportsResult.onSuccess { fetched ->
            reports.clear()
            reports.addAll(fetched)
            fetched.forEach { cacheImageIfPresent(it) }
        }

        if (loginNum.isNotBlank()) {
            backend.fetchMyReports().onSuccess { mine ->
                myReports.clear()
                myReports.addAll(mine)
                mine.forEach { cacheImageIfPresent(it) }
            }
        } else {
            myReports.clear()
            myReports.addAll(reports.map { it.sanitizedForPublic() })
        }

        statsResult.onSuccess { issueStats = it }
        isLoading = false
    }

    suspend fun setBackendMode(useDemo: Boolean) {
        useDemoBackend = useDemo
        if (useDemo) {
            thumbnailCache.putAll(demoBackend.demoThumbnails)
        }
        refreshFromBackend(fallbackToDemoOnFailure = false)
    }

    suspend fun login(phoneNumber: String) {
        isLoading = true
        backend.sendOtpOrLogin(phoneNumber).onSuccess { session ->
            loginNum = session.phoneNumber
            isAdmin = session.isAdmin
            activeTab = AppTab.HOME
            refreshFromBackend(fallbackToDemoOnFailure = false)
        }
        isLoading = false
    }

    fun logout() {
        loginNum = ""
        savedReporterName = ""
        isAdmin = false
        ktorBackend.clearAuthToken()
        activeTab = AppTab.LOGIN
    }

    suspend fun submitReport(draft: WasteReportDraft, localPreview: ImageBitmap?) {
        if (draft.reporterName.isNotBlank()) {
            savedReporterName = draft.reporterName
        }
        isLoading = true
        backend.submitReport(draft).onSuccess { created ->
            if (localPreview != null) {
                thumbnailCache[created.id] = localPreview
            } else {
                cacheImageIfPresent(created)
            }
            reports.add(0, created)
            myReports.add(0, created.sanitizedForPublic())
            recalculateLocalStats()
            backend.fetchDashboardStats().onSuccess { issueStats = it }
            activeTab = AppTab.ISSUES
        }
        isLoading = false
    }

    suspend fun updateReportStatus(reportId: String, newStatus: ReportStatus) {
        val idx = reports.indexOfFirst { it.id == reportId }
        if (idx == -1) return

        val previous = reports[idx]
        reports[idx] = previous.copy(status = newStatus)
        val myIdx = myReports.indexOfFirst { it.id == reportId }
        if (myIdx != -1) {
            myReports[myIdx] = myReports[myIdx].copy(status = newStatus)
        }
        recalculateLocalStats()

        backend.updateReportStatus(reportId, newStatus).onFailure {
            val rollbackIdx = reports.indexOfFirst { it.id == reportId }
            if (rollbackIdx != -1) {
                reports[rollbackIdx] = previous
            }
            if (myIdx != -1 && myIdx < myReports.size) {
                myReports[myIdx] = myReports[myIdx].copy(status = previous.status)
            }
            recalculateLocalStats()
        }.onSuccess {
            backend.fetchDashboardStats().onSuccess { issueStats = it }
        }
    }

    fun resolveThumbnail(report: WasteReportItem): ImageBitmap? {
        thumbnailCache[report.id]?.let { return it }
        val base64Bytes = report.imageBase64?.decodeBase64OrNull()
        if (base64Bytes != null) {
            platformHandler.decodeBitmap(base64Bytes)?.let { decoded ->
                thumbnailCache[report.id] = decoded
                return decoded
            }
        }
        return null
    }

    private suspend fun cacheImageIfPresent(report: WasteReportItem) {
        if (thumbnailCache.containsKey(report.id)) return
        val base64Bytes = report.imageBase64?.decodeBase64OrNull()
        if (base64Bytes != null) {
            platformHandler.decodeBitmap(base64Bytes)?.let {
                thumbnailCache[report.id] = it
                return
            }
        }
        val imageUrl = report.imageUrl
        if (!useDemoBackend && !imageUrl.isNullOrBlank()) {
            ktorBackend.fetchImageBytes(imageUrl)?.let { bytes ->
                platformHandler.decodeBitmap(bytes)?.let { decoded ->
                    thumbnailCache[report.id] = decoded
                }
            }
        }
    }

    fun recalculateLocalStats() {
        val total = if (useDemoBackend) 95 + reports.size else reports.size
        val solved = if (useDemoBackend) {
            73 + reports.count { it.status == ReportStatus.SOLVED }
        } else {
            reports.count { it.status == ReportStatus.SOLVED }
        }
        issueStats = IssueStats(total = total, solved = solved)
    }
}
