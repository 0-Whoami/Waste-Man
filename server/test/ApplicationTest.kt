package com.example.wasteman.server

import com.example.wasteman.core.KtorWasteManBackend
import com.example.wasteman.core.ReportStatus
import com.example.wasteman.core.WasteCategory
import com.example.wasteman.core.WasteManJson
import com.example.wasteman.core.WasteReportDraft
import com.example.wasteman.core.toBase64String
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ApplicationTest {

    @Test
    fun publicReportsStripReporterPhoneWhileAdminIncludesIt() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                json(WasteManJson)
            }
        }
        val backend = KtorWasteManBackend(client, baseUrl = "")

        // Unauthenticated / public fetch must never expose phone numbers
        val publicReports = backend.fetchReports(isAdmin = false).getOrThrow()
        assertTrue(publicReports.isNotEmpty())
        assertTrue(publicReports.all { it.reporterPhone.isEmpty() })

        // Even requesting admin scope without an admin token must still strip phone numbers
        val unauthAdminReports = backend.fetchReports(isAdmin = true).getOrThrow()
        assertTrue(unauthAdminReports.all { it.reporterPhone.isEmpty() })

        // Citizen login (non-admin phone)
        val citizenSession = backend.sendOtpOrLogin("+919111222333").getOrThrow()
        assertFalse(citizenSession.isAdmin)

        // Citizen still cannot read other users' phone numbers or update status
        val citizenReports = backend.fetchReports(isAdmin = true).getOrThrow()
        assertTrue(citizenReports.all { it.reporterPhone.isEmpty() })
        val citizenUpdate = backend.updateReportStatus(publicReports.first().id, ReportStatus.SOLVED)
        assertTrue(citizenUpdate.isFailure)

        // Admin login
        val adminSession = backend.sendOtpOrLogin("+919876543210").getOrThrow()
        assertTrue(adminSession.isAdmin)
        val adminReports = backend.fetchReports(isAdmin = true).getOrThrow()
        assertTrue(adminReports.any { it.reporterPhone.isNotBlank() })
    }

    @Test
    fun submitReportWithImageAndAdminStatusUpdateUpdatesStats() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                json(WasteManJson)
            }
        }
        val backend = KtorWasteManBackend(client, baseUrl = "")

        val statsBefore = backend.fetchDashboardStats().getOrThrow()

        // Submit report as citizen with a tiny PNG header payload
        backend.sendOtpOrLogin("+919444555666").getOrThrow()
        val pngStub = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
        val created = backend.submitReport(
            WasteReportDraft(
                reporterName = "Aarav",
                reporterPhone = "+919444555666",
                category = WasteCategory.PLASTIC_WASTE,
                description = "Plastic bottles stacked near park entrance",
                location = "City Park Gate 1",
                imageBase64 = pngStub.toBase64String()
            )
        ).getOrThrow()

        assertEquals(ReportStatus.PENDING, created.status)
        assertNotNull(created.imageUrl)
        assertEquals("", created.reporterPhone)

        // Uploaded image is retrievable
        val uploadedBytes = backend.fetchImageBytes(created.imageUrl!!)
        assertNotNull(uploadedBytes)
        assertEquals(pngStub.size, uploadedBytes.size)

        // Stats reflect the newly added pending report
        val statsAfterCreate = backend.fetchDashboardStats().getOrThrow()
        assertEquals(statsBefore.total + 1, statsAfterCreate.total)
        assertEquals(statsBefore.solved, statsAfterCreate.solved)

        // Admin updates status to SOLVED
        backend.sendOtpOrLogin("+919876543210").getOrThrow()
        backend.updateReportStatus(created.id, ReportStatus.SOLVED).getOrThrow()

        val statsAfterSolve = backend.fetchDashboardStats().getOrThrow()
        assertEquals(statsBefore.solved + 1, statsAfterSolve.solved)
    }
}
