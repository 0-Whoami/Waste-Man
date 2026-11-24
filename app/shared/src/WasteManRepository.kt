package com.example.wasteman

import androidx.compose.runtime.*

class WasteManRepository {
    var currentUserRole by mutableStateOf<UserRole?>(null)
    var currentPhone by mutableStateOf("")

    val complaints = mutableStateListOf(
        Complaint(
            id = "c1",
            title = "Overflowing Garbage Bin at Central Market",
            category = IssueCategory.OVERFLOWING_BINS,
            description = "Commercial waste bin overflowing with food packaging and plastic waste. Emitting foul odor near food stalls.",
            area = "Market Square East",
            street = "14th Commercial Main Road",
            landmark = "Opposite Central Bus Stop, Gate 3",
            pincode = "560001",
            locationDetails = "14th Commercial Main Road, Opposite Central Bus Stop Gate 3, Market Square East (PIN: 560001)",
            latitude = 12.9716,
            longitude = 77.5946,
            imageUrl = "https://images.unsplash.com/photo-1530587191325-3db32d826c18?w=800",
            status = ComplaintStatus.SUBMITTED,
            reporterPhone = "9876543210",
            timestamp = 1710000000000L
        ),
        Complaint(
            id = "c2",
            title = "Hazardous Biomedical Syringes in Alley",
            category = IssueCategory.HAZARDOUS_WASTE,
            description = "Unsecured medical waste box with discarded needles and blood vials found in alley behind clinic.",
            area = "North Ward Health Corridor",
            street = "Barton Clinic Road",
            landmark = "Behind City General Hospital, Alleyway 4",
            pincode = "560012",
            locationDetails = "Barton Clinic Road, Behind City General Hospital Alley 4, North Ward (PIN: 560012)",
            latitude = 12.9810,
            longitude = 77.6005,
            imageUrl = "https://images.unsplash.com/photo-1584744982491-665216d55ab8?w=800",
            status = ComplaintStatus.IN_PROGRESS,
            reporterPhone = "9811122233",
            assignedTeam = "Biohazard Response Team Alpha",
            timestamp = 1710000000000L
        ),
        Complaint(
            id = "c3",
            title = "Dumped Electronics & CRT Monitors",
            category = IssueCategory.E_WASTE,
            description = "Old computer monitors, mercury lamps, and lead batteries dumped along park boundary wall.",
            area = "Greenwood Residential Park",
            street = "Oak Tree Avenue, 2nd Cross",
            landmark = "Next to Greenwood Park West Gate",
            pincode = "560034",
            locationDetails = "Oak Tree Avenue 2nd Cross, Next to Greenwood Park West Gate (PIN: 560034)",
            latitude = 12.9550,
            longitude = 77.5800,
            imageUrl = "https://images.unsplash.com/photo-1550989460-0adf9ea622e2?w=800",
            status = ComplaintStatus.RESOLVED,
            reporterPhone = "9822233344",
            assignedTeam = "E-Waste Recycling Fleet 2",
            timestamp = 1710000000000L
        )
    )

    val newsFeedItems = mutableStateListOf(
        NewsFeedItem(
            id = "f1",
            title = "⚠️ CRITICAL ALERT: Biohazard Waste in North Ward Alleyways",
            content = "Unsafe biomedical waste reported behind City Hospital on Barton Clinic Road. Sanitation emergency team dispatched.",
            type = NewsType.CRITICAL_ALERT,
            safetyInstructions = "DO NOT APPROACH OR TOUCH. Maintain 10m distance. Report any additional sightings with exact street landmarks.",
            area = "North Ward",
            timestamp = 1710000000000L
        ),
        NewsFeedItem(
            id = "f2",
            title = "♻️ Municipal Waste Segregation Directive: Wet vs Dry Waste",
            content = "Keep organic kitchen waste (Green Bin) strictly separate from dry plastic & glass recyclables (Blue Bin).",
            type = NewsType.AWARENESS_TIP,
            imageUrl = "https://images.unsplash.com/photo-1532996122724-e3c354a0b15b?w=800",
            timestamp = 1710000000000L
        ),
        NewsFeedItem(
            id = "f3",
            title = "🌱 Community E-Waste Drive at Greenwood Park",
            content = "Bring expired batteries, old phones, and laptops to Greenwood West Gate on Saturday for free eco-certified recycling.",
            type = NewsType.COMMUNITY_NEWS,
            area = "Greenwood Suburbs",
            timestamp = 1710000000000L
        )
    )

    fun login(phoneInput: String): Boolean {
        val clean = phoneInput.filter { it.isDigit() }
        if (clean.length < 10) return false
        currentPhone = clean
        currentUserRole = if (clean == "9999999999" || clean == "0000000000" || clean.endsWith("99")) {
            UserRole.ADMINISTRATOR
        } else {
            UserRole.RESIDENT
        }
        return true
    }

    fun logout() {
        currentUserRole = null
        currentPhone = ""
    }

    fun reportIssue(
        title: String,
        category: IssueCategory,
        description: String,
        area: String,
        street: String?,
        landmark: String?,
        pincode: String?,
        locationDetails: String,
        latitude: Double?,
        longitude: Double?,
        imageUrl: String?,
        imageData: String?
    ) {
        val formattedLocation = listOfNotNull(
            street.takeIf { !it.isNullOrBlank() },
            landmark.takeIf { !it.isNullOrBlank() },
            area.takeIf { it.isNotBlank() },
            pincode?.let { "PIN: $it" }
        ).joinToString(", ").ifBlank { locationDetails }

        val newComplaint = Complaint(
            id = "c_${1710000000000L + complaints.size}",
            title = title,
            category = category,
            description = description,
            area = area,
            street = street,
            landmark = landmark,
            pincode = pincode,
            locationDetails = formattedLocation,
            latitude = latitude,
            longitude = longitude,
            imageUrl = imageUrl.takeIf { !it.isNullOrBlank() } ?: defaultImageForCategory(category),
            imageData = imageData,
            status = ComplaintStatus.SUBMITTED,
            reporterPhone = currentPhone.ifBlank { "9876543210" },
            timestamp = 1710000000000L
        )
        complaints.add(0, newComplaint)
    }

    fun updateComplaintStatus(id: String, newStatus: ComplaintStatus, assignedTeam: String?) {
        val index = complaints.indexOfFirst { it.id == id }
        if (index != -1) {
            val c = complaints[index]
            complaints[index] = c.copy(status = newStatus, assignedTeam = assignedTeam ?: c.assignedTeam)
        }
    }

    private fun defaultImageForCategory(category: IssueCategory): String {
        return when (category) {
            IssueCategory.OVERFLOWING_BINS -> "https://images.unsplash.com/photo-1530587191325-3db32d826c18?w=800"
            IssueCategory.HAZARDOUS_WASTE -> "https://images.unsplash.com/photo-1584744982491-665216d55ab8?w=800"
            IssueCategory.E_WASTE -> "https://images.unsplash.com/photo-1550989460-0adf9ea622e2?w=800"
            else -> "https://images.unsplash.com/photo-1611288883610-acf4c16cd6bc?w=800"
        }
    }
}
