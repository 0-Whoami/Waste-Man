package com.example.wasteman.server

import com.example.wasteman.core.IssueStats
import com.example.wasteman.core.LoginRequest
import com.example.wasteman.core.ReportStatus
import com.example.wasteman.core.UpdateReportStatusRequest
import com.example.wasteman.core.UserSession
import com.example.wasteman.core.WasteCategory
import com.example.wasteman.core.WasteReportDraft
import com.example.wasteman.core.WasteReportItem
import com.example.wasteman.core.WasteManJson
import com.example.wasteman.core.decodeBase64OrNull
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

fun main() {
    embeddedServer(Netty, port = 8081, host = "0.0.0.0") {
        module(persistToDisk = true)
    }.start(wait = true)
}

private val configuredAdminPhoneDigits = setOf(
    "9876543210",
    "0000000000",
    "9999999999",
    "1234567890"
)

private val sessionStore = ConcurrentHashMap<String, UserSession>()
private val reportLock = Any()
private val reportSequence = AtomicInteger(105)
private val uploadsDir = File("uploads").apply { mkdirs() }
private val reportsStorageFile = File("data/reports.json")

private val storedReports = mutableListOf(
    WasteReportItem(
        id = "REP-105",
        reporterName = "",
        reporterPhone = "+919876543210",
        category = WasteCategory.OVERFLOWING_BINS,
        description = "Municipal dumpster near Sector 5 bus stop has been overflowing onto the footpath for 3 days.",
        location = "Sector 5, Main Market",
        timestamp = "2h ago",
        createdAtMillis = System.currentTimeMillis() - (2 * 3_600_000L),
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
        createdAtMillis = System.currentTimeMillis() - (30 * 3_600_000L),
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
        createdAtMillis = System.currentTimeMillis() - (5 * 86_400_000L),
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
        createdAtMillis = System.currentTimeMillis() - (18 * 86_400_000L),
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
        createdAtMillis = System.currentTimeMillis() - (45 * 86_400_000L),
        status = ReportStatus.SOLVED
    )
)

fun Application.module(persistToDisk: Boolean = false) {
    if (persistToDisk) {
        loadPersistedReports()
    }

    install(ContentNegotiation) {
        json(WasteManJson)
    }

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Options)
    }

    routing {
        get("/") {
            call.respondText("WasteMan API is running.")
        }

        post("/auth/login") {
            val request = call.receive<LoginRequest>()
            val digits = request.phoneNumber.filter { it.isDigit() }
            if (digits.length < 10) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid phone number"))
                return@post
            }
            val lastTen = digits.takeLast(10)
            val normalizedPhone = if (request.phoneNumber.startsWith("+")) {
                request.phoneNumber.trim()
            } else {
                "+91$lastTen"
            }
            val isAdmin = configuredAdminPhoneDigits.contains(lastTen)
            val token = UUID.randomUUID().toString()
            val session = UserSession(
                phoneNumber = normalizedPhone,
                isAdmin = isAdmin,
                token = token
            )
            sessionStore[token] = session
            call.respond(HttpStatusCode.OK, session)
        }

        get("/stats") {
            val stats = synchronized(reportLock) {
                IssueStats(
                    total = storedReports.size,
                    solved = storedReports.count { it.status == ReportStatus.SOLVED }
                )
            }
            call.respond(HttpStatusCode.OK, stats)
        }

        get("/reports") {
            val session = call.bearerSession()
            val requestedScope = call.request.queryParameters["scope"]
            val mineOnly = call.request.queryParameters["mine"]?.toBooleanStrictOrNull() == true
            val includePrivatePhone = session?.isAdmin == true && requestedScope == "admin"

            val snapshot = synchronized(reportLock) {
                val baseList = if (mineOnly && session != null) {
                    val userDigits = session.phoneNumber.filter { it.isDigit() }.takeLast(10)
                    storedReports.filter { it.reporterPhone.filter { ch -> ch.isDigit() }.takeLast(10) == userDigits }
                } else {
                    storedReports.toList()
                }
                if (includePrivatePhone) {
                    baseList
                } else {
                    baseList.map { it.sanitizedForPublic() }
                }
            }
            call.respond(HttpStatusCode.OK, snapshot)
        }

        post("/reports") {
            val session = call.bearerSession()
            val draft = call.receive<WasteReportDraft>()
            if (draft.description.isBlank() || draft.location.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Description and location are required"))
                return@post
            }

            val id = "REP-${reportSequence.incrementAndGet()}"
            val imageUrl = draft.imageBase64?.decodeBase64OrNull()?.let { bytes ->
                saveUploadedImage(id, bytes)
            }

            val createdItem = WasteReportItem(
                id = id,
                reporterName = draft.reporterName.trim(),
                reporterPhone = session?.phoneNumber ?: draft.reporterPhone.trim(),
                category = draft.category,
                description = draft.description.trim(),
                location = draft.location.trim(),
                timestamp = "Just now",
                createdAtMillis = System.currentTimeMillis(),
                status = ReportStatus.PENDING,
                imageUrl = imageUrl
            )

            synchronized(reportLock) {
                storedReports.add(0, createdItem)
                if (persistToDisk) savePersistedReportsLocked()
            }

            val responseItem = if (session?.isAdmin == true) createdItem else createdItem.sanitizedForPublic()
            call.respond(HttpStatusCode.Created, responseItem)
        }

        patch("/reports/{id}/status") {
            val session = call.bearerSession()
            if (session == null) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Authentication required"))
                return@patch
            }
            if (!session.isAdmin) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Admin privileges required"))
                return@patch
            }

            val reportId = call.parameters["id"].orEmpty()
            val request = call.receive<UpdateReportStatusRequest>()

            val updated = synchronized(reportLock) {
                val index = storedReports.indexOfFirst { it.id == reportId }
                if (index == -1) {
                    null
                } else {
                    val next = storedReports[index].copy(status = request.status)
                    storedReports[index] = next
                    if (persistToDisk) savePersistedReportsLocked()
                    next
                }
            }

            if (updated == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Report $reportId not found"))
            } else {
                call.respond(HttpStatusCode.OK, updated)
            }
        }

        get("/uploads/{fileName}") {
            val fileName = call.parameters["fileName"].orEmpty()
            val target = File(uploadsDir, fileName).canonicalFile
            val root = uploadsDir.canonicalFile
            if (!target.path.startsWith(root.path) || !target.exists() || !target.isFile) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }
            val contentType = when (target.extension.lowercase()) {
                "png" -> ContentType.Image.PNG
                "webp" -> ContentType("image", "webp")
                else -> ContentType.Image.JPEG
            }
            call.respondBytes(target.readBytes(), contentType)
        }
    }
}

private fun ApplicationCall.bearerSession(): UserSession? {
    val authHeader = request.headers[HttpHeaders.Authorization] ?: return null
    if (!authHeader.startsWith("Bearer ", ignoreCase = true)) return null
    val token = authHeader.substringAfter(' ').trim()
    return sessionStore[token]
}

private fun saveUploadedImage(reportId: String, bytes: ByteArray): String? {
    if (bytes.isEmpty()) return null
    val ext = when {
        bytes.size >= 4 && bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() -> "png"
        bytes.size >= 4 && bytes[0] == 0x52.toByte() && bytes[1] == 0x49.toByte() -> "webp"
        else -> "jpg"
    }
    val fileName = "$reportId.$ext"
    File(uploadsDir, fileName).writeBytes(bytes)
    return "/uploads/$fileName"
}

private fun loadPersistedReports() {
    if (!reportsStorageFile.exists()) return
    runCatching {
        val loaded = WasteManJson.decodeFromString<List<WasteReportItem>>(reportsStorageFile.readText())
        if (loaded.isNotEmpty()) {
            synchronized(reportLock) {
                storedReports.clear()
                storedReports.addAll(loaded)
                val maxSeq = loaded.mapNotNull { it.id.removePrefix("REP-").toIntOrNull() }.maxOrNull() ?: 105
                reportSequence.set(maxOf(reportSequence.get(), maxSeq))
            }
        }
    }
}

private fun savePersistedReportsLocked() {
    runCatching {
        reportsStorageFile.parentFile?.mkdirs()
        reportsStorageFile.writeText(WasteManJson.encodeToString(storedReports.toList()))
    }
}
