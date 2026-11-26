package com.example.wasteman.core

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json

val WasteManJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

fun createWasteManHttpClient(): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(WasteManJson)
    }
}

class KtorWasteManBackend(
    private val client: HttpClient,
    baseUrl: String = "http://localhost:8080"
) : WasteManBackend {

    private val normalizedBaseUrl: String = baseUrl.trimEnd('/')
    private var authToken: String? = null

    fun setAuthToken(token: String?) {
        authToken = token?.takeIf { it.isNotBlank() }
    }

    fun clearAuthToken() {
        authToken = null
    }

    fun resolveUrl(pathOrUrl: String): String =
        if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
            pathOrUrl
        } else {
            "$normalizedBaseUrl/${pathOrUrl.trimStart('/')}"
        }

    override suspend fun sendOtpOrLogin(phoneNumber: String): Result<UserSession> = safeCall {
        val response = client.post("$normalizedBaseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(phoneNumber = phoneNumber))
        }
        ensureSuccess(response)
        val session: UserSession = response.body()
        setAuthToken(session.token)
        session
    }

    override suspend fun submitReport(draft: WasteReportDraft): Result<WasteReportItem> = safeCall {
        val response = client.post("$normalizedBaseUrl/reports") {
            applyAuthHeader()
            contentType(ContentType.Application.Json)
            setBody(draft)
        }
        ensureSuccess(response)
        response.body()
    }

    override suspend fun fetchReports(isAdmin: Boolean): Result<List<WasteReportItem>> = safeCall {
        val url = if (isAdmin) {
            "$normalizedBaseUrl/reports?scope=admin"
        } else {
            "$normalizedBaseUrl/reports"
        }
        val response = client.get(url) {
            applyAuthHeader()
        }
        ensureSuccess(response)
        val items: List<WasteReportItem> = response.body()
        if (isAdmin) items else items.map { it.sanitizedForPublic() }
    }

    override suspend fun fetchMyReports(): Result<List<WasteReportItem>> = safeCall {
        val response = client.get("$normalizedBaseUrl/reports?mine=true") {
            applyAuthHeader()
        }
        ensureSuccess(response)
        val items: List<WasteReportItem> = response.body()
        items.map { it.sanitizedForPublic() }
    }

    override suspend fun updateReportStatus(reportId: String, newStatus: ReportStatus): Result<Unit> = safeCall {
        val response = client.patch("$normalizedBaseUrl/reports/$reportId/status") {
            applyAuthHeader()
            contentType(ContentType.Application.Json)
            setBody(UpdateReportStatusRequest(status = newStatus))
        }
        ensureSuccess(response)
    }

    override suspend fun fetchDashboardStats(): Result<IssueStats> = safeCall {
        val response = client.get("$normalizedBaseUrl/stats") {
            applyAuthHeader()
        }
        ensureSuccess(response)
        response.body()
    }

    suspend fun fetchImageBytes(imageUrl: String): ByteArray? = runCatching {
        val response = client.get(resolveUrl(imageUrl)) {
            applyAuthHeader()
        }
        if (response.status.isSuccess()) response.body<ByteArray>() else null
    }.getOrNull()

    private fun HttpRequestBuilder.applyAuthHeader() {
        authToken?.let { token ->
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    private suspend fun ensureSuccess(response: HttpResponse) {
        if (!response.status.isSuccess()) {
            val detail = runCatching { response.bodyAsText() }.getOrNull().orEmpty()
            throw IllegalStateException("HTTP ${response.status.value}: ${detail.ifBlank { response.status.description }}")
        }
    }

    private inline fun <T> safeCall(block: () -> T): Result<T> = try {
        Result.success(block())
    } catch (ce: CancellationException) {
        throw ce
    } catch (t: Throwable) {
        Result.failure(t)
    }
}
