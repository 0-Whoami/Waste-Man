package com.example.wasteman.core

interface WasteManBackend {
    suspend fun sendOtpOrLogin(phoneNumber: String): Result<UserSession>
    suspend fun submitReport(draft: WasteReportDraft): Result<WasteReportItem>
    suspend fun fetchReports(isAdmin: Boolean): Result<List<WasteReportItem>>
    suspend fun fetchMyReports(): Result<List<WasteReportItem>> = fetchReports(isAdmin = false)
    suspend fun updateReportStatus(reportId: String, newStatus: ReportStatus): Result<Unit>
    suspend fun fetchDashboardStats(): Result<IssueStats>
}
