package com.example.wasteman.server

import com.example.wasteman.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

val adminPhones = setOf("9999999999", "0000000000", "1234567890")

val sampleComplaints = mutableListOf(
    Complaint(
        id = "c1",
        title = "Overflowing Garbage Bin at Market Square",
        category = IssueCategory.OVERFLOWING_BINS,
        description = "Bin is spilling plastic wrappers and food waste onto the sidewalk, causing bad odor.",
        area = "Market Area",
        locationDetails = "Near Central Bus Stop",
        latitude = 12.9716,
        longitude = 77.5946,
        imageUrl = "https://images.unsplash.com/photo-1530587191325-3db32d826c18?w=400",
        status = ComplaintStatus.SUBMITTED,
        reporterPhone = "9876543210",
        timestamp = 1710000000000L
    ),
    Complaint(
        id = "c2",
        title = "Hazardous Medical Waste Near Clinic",
        category = IssueCategory.HAZARDOUS_WASTE,
        description = "Unsecured vials and used syringes found near back alley. Extremely dangerous for children!",
        area = "North Ward",
        locationDetails = "Behind City Health Clinic",
        latitude = 12.9810,
        longitude = 77.6005,
        imageUrl = "https://images.unsplash.com/photo-1584744982491-665216d55ab8?w=400",
        status = ComplaintStatus.IN_PROGRESS,
        reporterPhone = "9811122233",
        assignedTeam = "Biohazard Response Team A",
        timestamp = 1710000000000L
    ),
    Complaint(
        id = "c3",
        title = "Discarded Old Televisions & Batteries",
        category = IssueCategory.E_WASTE,
        description = "CRT monitors and lead-acid batteries dumped illegally next to the park.",
        area = "Greenwood Suburbs",
        locationDetails = "Oak Tree Corner",
        latitude = 12.9550,
        longitude = 77.5800,
        imageUrl = "https://images.unsplash.com/photo-1550989460-0adf9ea622e2?w=400",
        status = ComplaintStatus.RESOLVED,
        reporterPhone = "9822233344",
        assignedTeam = "E-Waste Recycling Unit",
        timestamp = 1710000000000L
    )
)

val sampleFeed = mutableListOf(
    NewsFeedItem(
        id = "f1",
        title = "⚠️ CRITICAL HAZARD: Biohazard Waste Alert in North Ward",
        content = "Unsafe disposal of medical syringes and vials reported in North Ward alleyways. Sanitation teams dispatched.",
        type = NewsType.CRITICAL_ALERT,
        safetyInstructions = "DO NOT TOUCH or approach. Keep children away. Report exact location immediately.",
        area = "North Ward",
        timestamp = 1710000000000L
    ),
    NewsFeedItem(
        id = "f2",
        title = "♻️ Waste Segregation Drive: Wet vs Dry Waste",
        content = "Separate organic food waste (green bin) from recyclables (blue bin) to help reduce municipal landfill overflow by 40%.",
        type = NewsType.AWARENESS_TIP,
        imageUrl = "https://images.unsplash.com/photo-1532996122724-e3c354a0b15b?w=400",
        timestamp = 1710000000000L
    ),
    NewsFeedItem(
        id = "f3",
        title = "🌱 Community E-Waste Collection Drive This Weekend",
        content = "Drop off old phones, batteries, and chargers at Central Park from 9 AM to 5 PM for safe eco-recycling.",
        type = NewsType.COMMUNITY_NEWS,
        area = "Greenwood Suburbs",
        timestamp = 1710000000000L
    )
)

fun Application.module() {
    routing {
        get("/") {
            call.respondText("WasteMan Backend Real-Time API is running.")
        }

        // Live News & Hazard Feed
        get("/api/feed") {
            val jsonList = sampleFeed.joinToString(prefix = "[", postfix = "]") { f ->
                """{"id":"${f.id}","title":"${f.title}","content":"${f.content}","type":"${f.type.name}","safetyInstructions":${f.safetyInstructions?.let { "\"$it\"" } ?: "null"},"area":${f.area?.let { "\"$it\"" } ?: "null"},"imageUrl":${f.imageUrl?.let { "\"$it\"" } ?: "null"},"timestamp":${f.timestamp}}"""
            }
            call.respondText(jsonList, ContentType.Application.Json)
        }

        // Login endpoint
        post("/api/login") {
            try {
                val phone = call.parameters["phone"] ?: call.receiveParameters()["phone"] ?: "9876543210"
                val cleanPhone = phone.filter { it.isDigit() }
                val role = if (adminPhones.contains(cleanPhone) || cleanPhone.endsWith("99")) {
                    UserRole.ADMINISTRATOR
                } else {
                    UserRole.RESIDENT
                }
                call.respondText("""{"role":"$role","phone":"$cleanPhone","message":"Login successful"}""", ContentType.Application.Json)
            } catch (_: Exception) {
                call.respondText("""{"role":"RESIDENT","phone":"9876543210","message":"Default login"}""", ContentType.Application.Json)
            }
        }

        // Get complaints
        get("/api/complaints") {
            val phone = call.request.queryParameters["phone"]
            val area = call.request.queryParameters["area"]
            val status = call.request.queryParameters["status"]

            var filtered = sampleComplaints.toList()
            if (!phone.isNullOrBlank()) {
                filtered = filtered.filter { it.reporterPhone == phone }
            }
            if (!area.isNullOrBlank()) {
                filtered = filtered.filter { it.area.contains(area, ignoreCase = true) }
            }
            if (!status.isNullOrBlank()) {
                filtered = filtered.filter { it.status.name.equals(status, ignoreCase = true) }
            }

            val jsonList = filtered.joinToString(prefix = "[", postfix = "]") { c ->
                """{"id":"${c.id}","title":"${c.title}","category":"${c.category.name}","description":"${c.description}","area":"${c.area}","locationDetails":"${c.locationDetails}","latitude":${c.latitude ?: "null"},"longitude":${c.longitude ?: "null"},"imageUrl":${c.imageUrl?.let { "\"$it\"" } ?: "null"},"imageData":${c.imageData?.let { "\"$it\"" } ?: "null"},"status":"${c.status.name}","reporterPhone":"${c.reporterPhone}","assignedTeam":${c.assignedTeam?.let { "\"$it\"" } ?: "null"},"timestamp":${c.timestamp}}"""
            }
            call.respondText(jsonList, ContentType.Application.Json)
        }

        // Submit complaint
        post("/api/complaints") {
            try {
                val params = call.receiveParameters()
                val title = params["title"] ?: "Waste Issue"
                val categoryStr = params["category"] ?: "OVERFLOWING_BINS"
                val description = params["description"] ?: ""
                val area = params["area"] ?: "Downtown"
                val locationDetails = params["locationDetails"] ?: ""
                val lat = params["latitude"]?.toDoubleOrNull()
                val lng = params["longitude"]?.toDoubleOrNull()
                val imageUrl = params["imageUrl"]
                val imageData = params["imageData"]
                val phone = params["reporterPhone"] ?: "9876543210"

                val category = try { IssueCategory.valueOf(categoryStr) } catch (_: Exception) { IssueCategory.OVERFLOWING_BINS }
                val newId = "c_${System.currentTimeMillis()}"
                val newComplaint = Complaint(
                    id = newId,
                    title = title,
                    category = category,
                    description = description,
                    area = area,
                    locationDetails = locationDetails,
                    latitude = lat,
                    longitude = lng,
                    imageUrl = imageUrl,
                    imageData = imageData,
                    status = ComplaintStatus.SUBMITTED,
                    reporterPhone = phone,
                    timestamp = System.currentTimeMillis()
                )
                sampleComplaints.add(0, newComplaint)
                call.respondText("""{"success":true,"id":"$newId","message":"Complaint submitted successfully"}""", ContentType.Application.Json)
            } catch (e: Exception) {
                call.respondText("""{"success":false,"message":"${e.message}"}""", ContentType.Application.Json)
            }
        }

        // Update status
        post("/api/complaints/{id}/status") {
            val id = call.parameters["id"]
            val params = call.receiveParameters()
            val newStatusStr = params["status"] ?: "IN_PROGRESS"
            val team = params["assignedTeam"]

            val complaint = sampleComplaints.find { it.id == id }
            if (complaint != null) {
                val idx = sampleComplaints.indexOf(complaint)
                val newStatus = try { ComplaintStatus.valueOf(newStatusStr) } catch (_: Exception) { complaint.status }
                val updated = complaint.copy(
                    status = newStatus,
                    assignedTeam = team ?: complaint.assignedTeam
                )
                sampleComplaints[idx] = updated
                call.respondText("""{"success":true,"message":"Complaint status updated"}""", ContentType.Application.Json)
            } else {
                call.respondText("""{"success":false,"message":"Complaint not found"}""", status = HttpStatusCode.NotFound, contentType = ContentType.Application.Json)
            }
        }

        // Summary stats
        get("/api/summary") {
            val total = sampleComplaints.size
            val submitted = sampleComplaints.count { it.status == ComplaintStatus.SUBMITTED }
            val inProgress = sampleComplaints.count { it.status == ComplaintStatus.IN_PROGRESS }
            val resolved = sampleComplaints.count { it.status == ComplaintStatus.RESOLVED }
            val areaMap = sampleComplaints.groupBy { it.area }.mapValues { it.value.size }

            val areaJson = areaMap.entries.joinToString(prefix = "{", postfix = "}") { "\"${it.key}\":${it.value}" }
            val json = """{"totalComplaints":$total,"submittedCount":$submitted,"inProgressCount":$inProgress,"resolvedCount":$resolved,"areaBreakdown":$areaJson}"""
            call.respondText(json, ContentType.Application.Json)
        }
    }
}
