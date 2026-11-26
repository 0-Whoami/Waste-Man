package com.example.wasteman

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.onClick
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.reload.DevelopmentEntryPoint
import kotlin.math.round

// ============================================================================
// THEME & TYPOGRAPHY (Montserrat-ready)
// ============================================================================

val EcoGreen = Color(0xFF4fb828)
val EcoLightGreen = Color(0xFFf1e4f3)
val EcoDarkGreen = Color(0xFF386c0b)
val Black = Color(0xFF191102)
val AlertRed = Color(0xFFC62828)

val MontserratFontFamily: FontFamily = FontFamily.SansSerif

val WasteManTypography = Typography(
    displaySmall = TextStyle(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Bold, fontSize = 34.sp),
    headlineMedium = TextStyle(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp),
    titleMedium = TextStyle(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Normal, fontSize = 15.sp),
    bodyMedium = TextStyle(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp),
    labelLarge = TextStyle(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp),
    labelSmall = TextStyle(fontFamily = MontserratFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
)

@Composable
private fun wasteManFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = EcoGreen,
    unfocusedBorderColor = Black.copy(alpha = 0.22f),
    cursorColor = EcoDarkGreen,
    focusedTextColor = Black,
    unfocusedTextColor = Black,
    focusedContainerColor = Color.White.copy(alpha = 0.9f),
    unfocusedContainerColor = Color.White.copy(alpha = 0.78f)
)

// ============================================================================
// CORE DOMAIN MODELS & KMP INTERFACES (Move to `:core` module when splitting)
// ============================================================================

enum class AppTab { HOME, REPORT, ISSUES, ADMIN, LOGIN }

enum class WasteCategory(val displayName: String) {
    OVERFLOWING_BINS("Overflowing Bins"),
    LITTERING("Littering"),
    PLASTIC_WASTE("Plastic Waste"),
    E_WASTE("E-Waste"),
    ORGANIC_WASTE("Organic Waste"),
    HAZARDOUS_WASTE("Hazardous Waste"),
    OTHER("Other")
}

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

data class WasteReportDraft(
    val reporterName: String,
    val reporterPhone: String,
    val category: WasteCategory,
    val description: String,
    val location: String,
    val imageBytes: ByteArray?
)

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
    val thumbnail: ImageBitmap? = null
) {
    fun displayReporter(isAdmin: Boolean): String = when {
        reporterName.isNotBlank() -> reporterName
        isAdmin && reporterPhone.isNotBlank() -> reporterPhone
        else -> "Citizen"
    }
}

data class IssueStats(val total: Int = 0, val solved: Int = 0) {
    val pending: Int get() = (total - solved).coerceAtLeast(0)
}

data class NewsAnnouncement(
    val id: String,
    val icon: ImageVector,
    val heading: String,
    val content: String
)

data class UserSession(
    val phoneNumber: String,
    val isAdmin: Boolean
)

interface ReportPlatformHandler {
    fun launchImagePicker(onImagePicked: (bytes: ByteArray, bitmap: ImageBitmap) -> Unit)
    fun fetchCurrentLocation(onLocationFetched: (addressOrCoords: String) -> Unit)
}

interface WasteManBackend {
    suspend fun sendOtpOrLogin(phoneNumber: String): Result<UserSession>
    suspend fun submitReport(draft: WasteReportDraft): Result<WasteReportItem>
    suspend fun fetchReports(isAdmin: Boolean): Result<List<WasteReportItem>>
    suspend fun updateReportStatus(reportId: String, newStatus: ReportStatus): Result<Unit>
    suspend fun fetchDashboardStats(): Result<IssueStats>
}

// ============================================================================
// APP STATE & DEMO IMPLEMENTATION (Test UI without backend)
// ============================================================================

private const val DEMO_NOW_MILLIS = 1_764_126_180_000L
private const val HOUR_MILLIS = 3_600_000L
private const val DAY_MILLIS = 86_400_000L

class AppState(
    val backend: WasteManBackend = DemoWasteManBackend(),
    val platformHandler: ReportPlatformHandler = DemoPlatformHandler()
) {
    var activeTab by mutableStateOf(AppTab.HOME)
    var isAdmin by mutableStateOf(true)
    var loginNum by mutableStateOf("+919876543210")
    var savedReporterName by mutableStateOf("")
    var isLoading by mutableStateOf(false)

    var issueStats by mutableStateOf(IssueStats(total = 100, solved = 76))
    val reports = mutableStateListOf<WasteReportItem>()
    val newsItems = mutableStateListOf<NewsAnnouncement>()

    init {
        loadDemoData()
    }

    private fun loadDemoData() {
        newsItems.addAll(
            listOf(
                NewsAnnouncement(
                    "1",
                    Icons.Default.Recycling,
                    "E-Waste Drive",
                    "Drop off old electronics at Sector 5 Community Center this Sunday for certified recycling."
                ),
                NewsAnnouncement(
                    "2",
                    Icons.Default.Campaign,
                    "Zero-Plastic Week",
                    "Local markets are switching to compostable bags. Report illegal plastic dumping instantly."
                ),
                NewsAnnouncement(
                    "3",
                    Icons.Default.VolunteerActivism,
                    "76% Resolved!",
                    "Thanks to citizen reports, 76 waste hotspots were cleared across the city this month."
                )
            )
        )

        reports.addAll(
            listOf(
                WasteReportItem(
                    id = "REP-105",
                    reporterName = "",
                    reporterPhone = "+919876543210",
                    category = WasteCategory.OVERFLOWING_BINS,
                    description = "Municipal dumpster near Sector 5 bus stop has been overflowing onto the footpath for 3 days.",
                    location = "Sector 5, Main Market",
                    timestamp = "2h ago",
                    createdAtMillis = DEMO_NOW_MILLIS - (2 * HOUR_MILLIS),
                    status = ReportStatus.PENDING,
                    thumbnail = createDemoBitmap(Color(0xFF8D6E63), width = 160, height = 100)
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
                    status = ReportStatus.IN_PROGRESS,
                    thumbnail = createDemoBitmap(Color(0xFF42A5F5), width = 120, height = 150)
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
                    status = ReportStatus.SOLVED,
                    thumbnail = createDemoBitmap(Color(0xFF78909C), width = 180, height = 100)
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
                    status = ReportStatus.SOLVED,
                    thumbnail = null
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
                    status = ReportStatus.SOLVED,
                    thumbnail = createDemoBitmap(Color(0xFFE57373), width = 140, height = 110)
                )
            )
        )
    }

    fun recalculateStats() {
        val total = 95 + reports.size
        val solved = 73 + reports.count { it.status == ReportStatus.SOLVED }
        issueStats = IssueStats(total = total, solved = solved)
    }
}

class DemoPlatformHandler : ReportPlatformHandler {
    override fun launchImagePicker(onImagePicked: (bytes: ByteArray, bitmap: ImageBitmap) -> Unit) {
        val demoBitmap = createDemoBitmap(EcoDarkGreen, width = 200, height = 120)
        onImagePicked(ByteArray(0), demoBitmap)
    }

    override fun fetchCurrentLocation(onLocationFetched: (addressOrCoords: String) -> Unit) {
        onLocationFetched("22.5726° N, 88.3639° E • Salt Lake Sector V")
    }
}

class DemoWasteManBackend : WasteManBackend {
    override suspend fun sendOtpOrLogin(phoneNumber: String): Result<UserSession> {
        delay(500)
        val admin = phoneNumber.endsWith("0000") || phoneNumber.endsWith("3210")
        return Result.success(
            UserSession(
                phoneNumber = phoneNumber,
                isAdmin = admin
            )
        )
    }

    override suspend fun submitReport(draft: WasteReportDraft): Result<WasteReportItem> {
        delay(400)
        return Result.success(
            WasteReportItem(
                id = "REP-${(106..999).random()}",
                reporterName = draft.reporterName,
                reporterPhone = draft.reporterPhone,
                category = draft.category,
                description = draft.description,
                location = draft.location,
                timestamp = "Just now",
                createdAtMillis = DEMO_NOW_MILLIS,
                status = ReportStatus.PENDING,
                thumbnail = createDemoBitmap(EcoGreen, width = 160, height = 100)
            )
        )
    }

    override suspend fun fetchReports(isAdmin: Boolean): Result<List<WasteReportItem>> =
        Result.success(emptyList())

    override suspend fun updateReportStatus(reportId: String, newStatus: ReportStatus): Result<Unit> {
        delay(100)
        return Result.success(Unit)
    }

    override suspend fun fetchDashboardStats(): Result<IssueStats> =
        Result.success(IssueStats(100, 76))
}

private fun createDemoBitmap(color: Color, width: Int = 120, height: Int = 120): ImageBitmap =
    ImageBitmap(width, height).apply {
        Canvas(this).drawRect(
            left = 0f,
            top = 0f,
            right = width.toFloat(),
            bottom = height.toFloat(),
            paint = Paint().apply { this.color = color }
        )
    }

// ============================================================================
// ENTRY POINT & ROOT SHELL
// ============================================================================

@Composable
@Preview
@DevelopmentEntryPoint
fun App() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = EcoGreen,
            secondary = EcoLightGreen,
            error = AlertRed
        ),
        typography = WasteManTypography
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            WasteManAppScreen()
        }
    }
}

@Composable
fun WasteManAppScreen(state: AppState = remember { AppState() }) {
    val scope = rememberCoroutineScope()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxWidth < 680.dp

        if (state.activeTab == AppTab.LOGIN) {
            LoginScreen(
                isLoading = state.isLoading,
                onContinueWithPhone = { fullPhone ->
                    scope.launch {
                        state.isLoading = true
                        state.backend.sendOtpOrLogin(fullPhone).onSuccess { session ->
                            state.loginNum = session.phoneNumber
                            state.isAdmin = session.isAdmin
                            state.activeTab = AppTab.HOME
                        }
                        state.isLoading = false
                    }
                },
                onBackToHome = { state.activeTab = AppTab.HOME }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(EcoGreen)
                    .safeContentPadding()
                    .padding(horizontal = if (isCompact) 10.dp else 20.dp, vertical = 10.dp)
            ) {
                HeaderBar(
                    isCompact = isCompact,
                    state = state,
                    onAuthClick = {
                        if (state.loginNum.isBlank()) {
                            state.activeTab = AppTab.LOGIN
                        } else {
                            state.loginNum = ""
                            state.savedReporterName = ""
                            state.isAdmin = false
                            state.activeTab = AppTab.LOGIN
                        }
                    }
                )

                NavBar(state = state)

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor = EcoLightGreen,
                            ambientColor = EcoLightGreen
                        ),
                    shape = RoundedCornerShape(24.dp),
                    color = EcoLightGreen
                ) {
                    val handleStatusUpdate: (String, ReportStatus) -> Unit = { reportId, newStatus ->
                        val idx = state.reports.indexOfFirst { it.id == reportId }
                        if (idx != -1) {
                            val previousReport = state.reports[idx]
                            state.reports[idx] = previousReport.copy(status = newStatus)
                            state.recalculateStats()

                            scope.launch {
                                state.backend.updateReportStatus(reportId, newStatus).onFailure {
                                    val rollbackIdx = state.reports.indexOfFirst { it.id == reportId }
                                    if (rollbackIdx != -1) {
                                        state.reports[rollbackIdx] = previousReport
                                        state.recalculateStats()
                                    }
                                }
                            }
                        }
                    }

                    when (state.activeTab) {
                        AppTab.HOME -> HomeScreen(
                            issueStats = state.issueStats,
                            newsItems = state.newsItems,
                            recentReports = state.reports.take(4),
                            isCompact = isCompact,
                            onReportClick = { state.activeTab = AppTab.REPORT },
                            onViewAllClick = { state.activeTab = AppTab.ISSUES }
                        )

                        AppTab.REPORT -> AddReportContent(
                            platformHandler = state.platformHandler,
                            defaultReporterName = state.savedReporterName,
                            reporterPhone = state.loginNum,
                            isSubmitting = state.isLoading,
                            onSubmitReport = { draft ->
                                if (draft.reporterName.isNotBlank()) {
                                    state.savedReporterName = draft.reporterName
                                }
                                scope.launch {
                                    state.isLoading = true
                                    state.backend.submitReport(draft).onSuccess { created ->
                                        state.reports.add(0, created)
                                        state.recalculateStats()
                                        state.activeTab = AppTab.ISSUES
                                    }
                                    state.isLoading = false
                                }
                            }
                        )

                        AppTab.ISSUES -> MyReportsContent(
                            reports = state.reports.filter {
                                state.loginNum.isBlank() || it.reporterPhone == state.loginNum
                            },
                            isAdmin = false,
                            isLoading = state.isLoading,
                            onStatusUpdate = { _, _ -> }
                        )

                        AppTab.ADMIN -> MyReportsContent(
                            reports = state.reports,
                            isAdmin = state.isAdmin,
                            isLoading = state.isLoading,
                            onStatusUpdate = handleStatusUpdate
                        )

                        AppTab.LOGIN -> Unit
                    }
                }
            }
        }
    }
}

// ============================================================================
// THEMED DROPDOWN MENU COMPONENTS (Rounded container + Rounded hover/selected)
// ============================================================================

@Composable
fun WasteManDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(18.dp))
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = modifier
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .border(1.dp, EcoDarkGreen.copy(alpha = 0.16f), RoundedCornerShape(18.dp))
                .padding(vertical = 4.dp)
        ) {
            content()
        }
    }
}

@Composable
fun WasteManDropdownItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    val itemBg = when {
        selected -> EcoGreen.copy(alpha = 0.16f)
        hovered -> EcoLightGreen.copy(alpha = 0.75f)
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(itemBg)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            color = if (selected) EcoDarkGreen else Black,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp
        )
        if (selected) {
            Spacer(Modifier.width(12.dp))
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = EcoDarkGreen,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ============================================================================
// HEADER & NAVIGATION BAR
// ============================================================================

@Composable
fun HeaderBar(
    isCompact: Boolean,
    state: AppState,
    onAuthClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Eco,
            contentDescription = "WasteMan Logo",
            modifier = Modifier.size(40.dp),
            tint = EcoLightGreen
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "WasteMan",
            fontWeight = FontWeight.Bold,
            fontSize = if (isCompact) 28.sp else 34.sp,
            color = EcoLightGreen
        )
        Spacer(Modifier.weight(1f))

        NavPill(
            active = true,
            onClick = onAuthClick,
            icon = Icons.Default.Person,
            label = when {
                isCompact -> ""
                state.loginNum.isBlank() -> "Login"
                else -> "Logout"
            }
        )
    }
}

@Composable
fun NavBar(state: AppState) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 8.dp)
            .horizontalScroll(rememberScrollState())
    ) {
        NavPill(
            active = state.activeTab == AppTab.HOME,
            onClick = { state.activeTab = AppTab.HOME },
            icon = Icons.Default.Home,
            label = ""
        )
        NavPill(
            active = state.activeTab == AppTab.REPORT,
            onClick = { state.activeTab = AppTab.REPORT },
            icon = Icons.Default.AddCircleOutline,
            label = "Add Report"
        )
        NavPill(
            active = state.activeTab == AppTab.ISSUES,
            onClick = { state.activeTab = AppTab.ISSUES },
            icon = Icons.Default.History,
            label = "My Reports"
        )
        if (state.isAdmin) {
            NavPill(
                active = state.activeTab == AppTab.ADMIN,
                onClick = { state.activeTab = AppTab.ADMIN },
                icon = Icons.Default.AdminPanelSettings,
                label = "Admin Panel"
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NavPill(
    active: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "pressScale"
    )
    val elevation by animateDpAsState(
        targetValue = if (hovered) 8.dp else 3.dp,
        label = "hoverElevation"
    )

    val contentColor = if (active) EcoGreen else Black.copy(alpha = 0.55f)

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .scale(scale)
            .shadow(elevation, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .background(EcoLightGreen)
            .height(40.dp)
            .padding(horizontal = if (label.isBlank()) 11.dp else 14.dp)
            .onClick(interactionSource = interactionSource, onClick = onClick)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(20.dp))
        if (label.isNotBlank()) {
            Text(
                text = label,
                color = contentColor,
                fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

// ============================================================================
// HOME SCREEN (Responsive Grid + Public Privacy Enforced)
// ============================================================================

@Composable
fun HomeScreen(
    issueStats: IssueStats,
    newsItems: List<NewsAnnouncement>,
    recentReports: List<WasteReportItem>,
    isCompact: Boolean,
    onReportClick: () -> Unit,
    onViewAllClick: () -> Unit
) {
    val columns = if (isCompact) 1 else 2
    val topCardHeight = if (isCompact) 210.dp else 235.dp
    var selectedReportForDetail by remember { mutableStateOf<WasteReportItem?>(null) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(if (isCompact) 16.dp else 24.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.White.copy(alpha = 0.88f),
                border = BorderStroke(1.dp, Black.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth().height(topCardHeight)
            ) {
                IssueStatsGaugeCard(issueStats = issueStats, isCompact = isCompact)
            }
        }

        item {
            AutoAdvancingCarousel(
                items = newsItems,
                modifier = Modifier.fillMaxWidth().height(topCardHeight)
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            if (isCompact) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Recent Community Activity",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterPill(text = "+ New Report", selected = true, onClick = onReportClick)
                        FilterPill(text = "View All", selected = false, onClick = onViewAllClick)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Community Activity",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterPill(text = "+ New Report", selected = true, onClick = onReportClick)
                        FilterPill(text = "View All", selected = false, onClick = onViewAllClick)
                    }
                }
            }
        }

        // Public feed always renders with isAdmin = false so phone numbers stay hidden
        items(recentReports.size, key = { recentReports[it].id }) { index ->
            val report = recentReports[index]
            ReportCard(
                report = report,
                isAdmin = false,
                onStatusChange = {},
                onClick = { selectedReportForDetail = report }
            )
        }
    }

    selectedReportForDetail?.let { report ->
        ReportDetailDialog(
            report = report,
            isAdmin = false,
            onStatusUpdate = {},
            onDismiss = { selectedReportForDetail = null }
        )
    }
}

@Composable
private fun IssueStatsGaugeCard(issueStats: IssueStats, isCompact: Boolean) {
    val percent = if (issueStats.total == 0) 0f else issueStats.solved.toFloat() / issueStats.total

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = if (isCompact) 14.dp else 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (isCompact) 12.dp else 24.dp)
    ) {
        BoxWithConstraints(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier
                .weight(1.05f)
                .fillMaxHeight()
        ) {
            val strokeWidthPx = with(LocalDensity.current) {
                (if (isCompact) 13.dp else 18.dp).toPx()
            }
            val maxDiameterByWidth = maxWidth - (if (isCompact) 14.dp else 20.dp)
            val maxDiameterByHeight = (maxHeight - 8.dp) * 2f
            val gaugeDiameter = minOf(maxDiameterByWidth, maxDiameterByHeight).coerceAtLeast(80.dp)

            Canvas(
                modifier = Modifier
                    .width(gaugeDiameter)
                    .height(gaugeDiameter / 2f)
                    .align(Alignment.Center)
            ) {
                val topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f)
                val arcSize = Size(
                    width = (size.width - strokeWidthPx).coerceAtLeast(10f),
                    height = ((size.height * 2f) - strokeWidthPx).coerceAtLeast(10f)
                )

                drawArc(
                    color = Color.LightGray.copy(alpha = 0.7f),
                    startAngle = -180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    style = Stroke(strokeWidthPx, cap = StrokeCap.Round),
                    topLeft = topLeft,
                    size = arcSize
                )
                drawArc(
                    color = EcoGreen,
                    startAngle = -180f,
                    sweepAngle = 180f * percent,
                    useCenter = false,
                    style = Stroke(strokeWidthPx, cap = StrokeCap.Round),
                    topLeft = topLeft,
                    size = arcSize
                )
            }

            val dynamicFont = (gaugeDiameter / 150.dp) * 28.sp
            Text(
                text = "${round(percent * 100).toInt()}%",
                color = Black,
                fontWeight = FontWeight.Bold,
                fontSize = dynamicFont,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = gaugeDiameter / 6f)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(if (isCompact) 10.dp else 14.dp, Alignment.CenterVertically)
        ) {
            StatLegendRow(color = Color.LightGray, text = "Submitted : ${issueStats.total}", isCompact = isCompact)
            StatLegendRow(color = EcoGreen, text = "Solved : ${issueStats.solved}", isCompact = isCompact)
            StatLegendRow(color = Color(0xFFFFA000), text = "Pending : ${issueStats.pending}", isCompact = isCompact)
        }
    }
}

@Composable
private fun StatLegendRow(color: Color, text: String, isCompact: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(if (isCompact) 14.dp else 16.dp)
                .background(color = color, RoundedCornerShape(4.dp))
        )
        Text(
            text = text,
            fontSize = if (isCompact) 13.sp else 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Black,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun AutoAdvancingCarousel(
    items: List<NewsAnnouncement>,
    modifier: Modifier = Modifier,
    autoAdvanceDurationMillis: Int = 5000
) {
    if (items.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { items.size })
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()
    var isPointerDown by remember { mutableStateOf(false) }

    val progress = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState.settledPage, isDragged, isPointerDown, items.size) {
        if (items.size <= 1 || isDragged || isPointerDown) {
            progress.snapTo(0f)
            return@LaunchedEffect
        }

        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = autoAdvanceDurationMillis, easing = LinearEasing)
        )

        val nextPage = (pagerState.settledPage + 1) % items.size
        pagerState.animateScrollToPage(
            page = nextPage,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(EcoGreen)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    isPointerDown = true
                    do {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                    } while (event.changes.any { it.pressed })
                    isPointerDown = false
                }
            }
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(0.dp),
            pageSpacing = 0.dp,
            beyondViewportPageCount = 1,
            userScrollEnabled = true,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
        ) { page ->
            val item = items[page]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(item.icon, contentDescription = null, modifier = Modifier.size(32.dp), tint = EcoLightGreen)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = item.heading,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = EcoLightGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = item.content,
                    color = EcoLightGreen.copy(alpha = 0.95f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, _ ->
                val isSelected = pagerState.currentPage == index
                val indicatorWidth by animateDpAsState(
                    targetValue = if (isSelected) 32.dp else 6.dp,
                    animationSpec = tween(durationMillis = 300),
                    label = "IndicatorWidth"
                )

                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .width(indicatorWidth)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                        .clickable {
                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                        }
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress.value)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// ADD REPORT TAB
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReportContent(
    platformHandler: ReportPlatformHandler,
    defaultReporterName: String = "",
    reporterPhone: String = "",
    isSubmitting: Boolean = false,
    onSubmitReport: (WasteReportDraft) -> Unit,
    modifier: Modifier = Modifier
) {
    var reporterName by remember(defaultReporterName) { mutableStateOf(defaultReporterName) }
    var selectedCategory by remember { mutableStateOf<WasteCategory?>(null) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var imagePreview by remember { mutableStateOf<ImageBitmap?>(null) }

    val fieldColors = wasteManFieldColors()

    val previewAspectRatio = remember(imagePreview) {
        imagePreview?.let { bmp ->
            if (bmp.height > 0) (bmp.width.toFloat() / bmp.height.toFloat()).coerceIn(0.75f, 2.2f)
            else 16f / 9f
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Add Report",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Black
        )

        // 1. Optional Reporter Name Field (Phone number is never exposed publicly)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            FieldHeader(
                title = "Your Name (Optional)",
                subtitle = "Shown publicly on the report; defaults to \"Citizen\" if left blank"
            )

            OutlinedTextField(
                value = reporterName,
                onValueChange = { reporterName = it },
                placeholder = {
                    Text("Enter your display name (optional)", color = Black.copy(alpha = 0.45f))
                },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = EcoDarkGreen) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 2. Category Dropdown
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            FieldHeader(title = "Waste Category", subtitle = "Select the type of waste issue")

            ExposedDropdownMenuBox(
                expanded = categoryDropdownExpanded,
                onExpandedChange = { categoryDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory?.displayName ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = {
                        Text(
                            "Choose category (e.g., Overflowing Bins, E-Waste)",
                            color = Black.copy(alpha = 0.45f)
                        )
                    },
                    trailingIcon = {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = EcoDarkGreen)
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors,
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )

                WasteManDropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false }
                ) {
                    WasteCategory.entries.forEach { category ->
                        WasteManDropdownItem(
                            text = category.displayName,
                            selected = category == selectedCategory,
                            onClick = {
                                selectedCategory = category
                                categoryDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // 3. Location Input + GPS Pill
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FieldHeader(title = "Location", subtitle = "Where is the waste located?")

                Surface(
                    shape = RoundedCornerShape(50),
                    color = EcoGreen.copy(alpha = 0.15f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable {
                            platformHandler.fetchCurrentLocation { fetched -> location = fetched }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = EcoDarkGreen, modifier = Modifier.size(14.dp))
                        Text("Use GPS", color = EcoDarkGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                placeholder = {
                    Text("Enter street, landmark, or tap Use GPS", color = Black.copy(alpha = 0.45f))
                },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = EcoDarkGreen) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 4. Description Input
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            FieldHeader(title = "Description", subtitle = "Provide details to help cleanup crews")

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = {
                    Text("Describe severity, approximate size, or hazards...", color = Black.copy(alpha = 0.45f))
                },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 5. Image Picker & Aspect-Ratio-Preserving Preview
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            FieldHeader(title = "Photo Evidence", subtitle = "Attach a clear photo of the issue")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (previewAspectRatio != null) Modifier.aspectRatio(previewAspectRatio)
                        else Modifier.height(170.dp)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.78f))
                    .border(BorderStroke(1.5.dp, EcoDarkGreen.copy(alpha = 0.35f)), RoundedCornerShape(16.dp))
                    .clickable {
                        platformHandler.launchImagePicker { bytes, bitmap ->
                            imageBytes = bytes
                            imagePreview = bitmap
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (imagePreview != null) {
                    Image(
                        bitmap = imagePreview!!,
                        contentDescription = "Selected issue photo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                    IconButton(
                        onClick = {
                            imagePreview = null
                            imageBytes = null
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(32.dp)
                            .background(Black.copy(alpha = 0.65f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Remove photo", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = EcoDarkGreen, modifier = Modifier.size(36.dp))
                        Text("Tap to select or upload photo", color = EcoDarkGreen, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("JPG, PNG supported", color = Black.copy(alpha = 0.45f), fontSize = 12.sp)
                    }
                }
            }
        }

        val isFormValid = selectedCategory != null && description.isNotBlank() && location.isNotBlank() && !isSubmitting

        Button(
            onClick = {
                selectedCategory?.let { category ->
                    onSubmitReport(
                        WasteReportDraft(
                            reporterName = reporterName.trim(),
                            reporterPhone = reporterPhone,
                            category = category,
                            description = description.trim(),
                            location = location.trim(),
                            imageBytes = imageBytes
                        )
                    )
                }
            },
            enabled = isFormValid,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = EcoGreen,
                contentColor = Color.White,
                disabledContainerColor = EcoGreen.copy(alpha = 0.4f),
                disabledContentColor = Color.White.copy(alpha = 0.7f)
            ),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.5.dp, modifier = Modifier.size(22.dp))
            } else {
                Text("Submit Report", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FieldHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Black)
        Text(text = subtitle, fontSize = 12.sp, color = Black.copy(alpha = 0.6f))
    }
}

// ============================================================================
// MY REPORTS & ADMIN PANEL TAB (Phone Numbers Strictly Restricted to Admin)
// ============================================================================

@Composable
fun MyReportsContent(
    reports: List<WasteReportItem>,
    isAdmin: Boolean = false,
    isLoading: Boolean = false,
    referenceNowMillis: Long = DEMO_NOW_MILLIS,
    onStatusUpdate: (reportId: String, newStatus: ReportStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStatusFilter by remember { mutableStateOf<ReportStatus?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<WasteCategory?>(null) }
    var selectedTimeFilter by remember { mutableStateOf(ReportTimeFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedReportForDetail by remember { mutableStateOf<WasteReportItem?>(null) }

    val activeDetailReport = remember(selectedReportForDetail, reports.toList()) {
        selectedReportForDetail?.let { current -> reports.find { it.id == current.id } ?: current }
    }

    val filteredReports = remember(
        reports.toList(),
        selectedStatusFilter,
        selectedCategoryFilter,
        selectedTimeFilter,
        searchQuery,
        isAdmin,
        referenceNowMillis
    ) {
        val query = searchQuery.trim().lowercase()
        reports.filter { report ->
            val matchesStatus = selectedStatusFilter == null || report.status == selectedStatusFilter
            val matchesCategory = selectedCategoryFilter == null || report.category == selectedCategoryFilter

            val matchesTime = when (selectedTimeFilter) {
                ReportTimeFilter.ALL -> true
                ReportTimeFilter.OLDER -> {
                    val thirtyDaysMillis = ReportTimeFilter.LAST_30_DAYS.maxAgeMillis ?: 0L
                    report.createdAtMillis > 0L && (referenceNowMillis - report.createdAtMillis) > thirtyDaysMillis
                }
                else -> {
                    val window = selectedTimeFilter.maxAgeMillis ?: Long.MAX_VALUE
                    report.createdAtMillis == 0L || (referenceNowMillis - report.createdAtMillis) <= window
                }
            }

            // Phone number matching is strictly gated behind `isAdmin`
            val matchesQuery = query.isEmpty() ||
                    report.id.lowercase().contains(query) ||
                    report.displayReporter(isAdmin).lowercase().contains(query) ||
                    (isAdmin && report.reporterPhone.lowercase().contains(query)) ||
                    report.category.displayName.lowercase().contains(query) ||
                    report.timestamp.lowercase().contains(query) ||
                    report.description.lowercase().contains(query) ||
                    report.location.lowercase().contains(query)

            matchesStatus && matchesCategory && matchesTime && matchesQuery
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isAdmin) "All Reports" else "My Reports",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black
                )
                if (isAdmin) {
                    Surface(shape = RoundedCornerShape(50), color = EcoDarkGreen) {
                        Text(
                            text = "ADMIN",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val hasActiveFilters = selectedStatusFilter != null ||
                        selectedCategoryFilter != null ||
                        selectedTimeFilter != ReportTimeFilter.ALL ||
                        searchQuery.isNotBlank()

                if (hasActiveFilters) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = AlertRed.copy(alpha = 0.12f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                selectedStatusFilter = null
                                selectedCategoryFilter = null
                                selectedTimeFilter = ReportTimeFilter.ALL
                                searchQuery = ""
                            }
                    ) {
                        Text(
                            text = "Reset",
                            color = AlertRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Surface(shape = RoundedCornerShape(50), color = EcoGreen.copy(alpha = 0.15f)) {
                    Text(
                        text = "${filteredReports.size} Total",
                        color = EcoDarkGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = if (isAdmin) {
                        "Search name, phone, type, time, description, location..."
                    } else {
                        "Search name, type, time, description, location..."
                    },
                    color = Black.copy(alpha = 0.45f),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EcoDarkGreen, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Black.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = wasteManFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        // 1. Time Filter Row
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReportTimeFilter.entries.forEach { timeFilter ->
                FilterPill(
                    text = timeFilter.label,
                    selected = selectedTimeFilter == timeFilter,
                    onClick = { selectedTimeFilter = timeFilter }
                )
            }
        }

        // 2. Status Filter Row
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterPill(text = "All Status", selected = selectedStatusFilter == null) { selectedStatusFilter = null }
            ReportStatus.entries.forEach { status ->
                FilterPill(text = status.label, selected = selectedStatusFilter == status) {
                    selectedStatusFilter = status
                }
            }
        }

        // 3. Category Filter Row
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterPill(text = "All Types", selected = selectedCategoryFilter == null) { selectedCategoryFilter = null }
            WasteCategory.entries.forEach { category ->
                FilterPill(text = category.displayName, selected = selectedCategoryFilter == category) {
                    selectedCategoryFilter = category
                }
            }
        }

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EcoGreen)
            }

            filteredReports.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.History, contentDescription = null, tint = EcoDarkGreen.copy(alpha = 0.5f), modifier = Modifier.size(44.dp))
                    Text("No matching reports found", color = Black.copy(alpha = 0.65f), fontWeight = FontWeight.Medium)
                }
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(filteredReports, key = { it.id }) { report ->
                    ReportCard(
                        report = report,
                        isAdmin = isAdmin,
                        onStatusChange = { newStatus -> onStatusUpdate(report.id, newStatus) },
                        onClick = { selectedReportForDetail = report }
                    )
                }
            }
        }
    }

    activeDetailReport?.let { report ->
        ReportDetailDialog(
            report = report,
            isAdmin = isAdmin,
            onStatusUpdate = { newStatus -> onStatusUpdate(report.id, newStatus) },
            onDismiss = { selectedReportForDetail = null }
        )
    }
}

@Composable
fun ReportCard(
    report: WasteReportItem,
    isAdmin: Boolean,
    onStatusChange: (ReportStatus) -> Unit,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, Black.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(76.dp).clip(RoundedCornerShape(12.dp)).background(EcoLightGreen),
                contentAlignment = Alignment.Center
            ) {
                if (report.thumbnail != null) {
                    Image(
                        bitmap = report.thumbnail,
                        contentDescription = report.category.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.Image, contentDescription = null, tint = EcoDarkGreen.copy(alpha = 0.45f), modifier = Modifier.size(28.dp))
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = report.category.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(6.dp))
                    if (isAdmin) {
                        AdminStatusDropdownChip(currentStatus = report.status, onStatusSelected = onStatusChange)
                    } else {
                        StatusBadge(status = report.status)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Black.copy(alpha = 0.55f), modifier = Modifier.size(13.dp))
                    Text(
                        text = "${report.displayReporter(isAdmin)} • #${report.id}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = report.description,
                    fontSize = 13.sp,
                    color = Black.copy(alpha = 0.78f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = EcoDarkGreen, modifier = Modifier.size(14.dp))
                    Text(
                        text = "${report.location} • ${report.timestamp}",
                        fontSize = 12.sp,
                        color = Black.copy(alpha = 0.55f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportDetailDialog(
    report: WasteReportItem,
    isAdmin: Boolean,
    onStatusUpdate: (ReportStatus) -> Unit,
    onDismiss: () -> Unit
) {
    val detailAspectRatio = remember(report.thumbnail) {
        report.thumbnail?.let { bmp ->
            if (bmp.height > 0) (bmp.width.toFloat() / bmp.height.toFloat()).coerceIn(0.75f, 2.2f)
            else 16f / 9f
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 560.dp)
                .fillMaxHeight(0.86f),
            shape = RoundedCornerShape(28.dp),
            color = EcoLightGreen,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(report.category.displayName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Black)
                        Text("Report ID: #${report.id}", fontSize = 12.sp, color = Black.copy(alpha = 0.6f))
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp).background(Black.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Black)
                    }
                }

                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (detailAspectRatio != null) Modifier.aspectRatio(detailAspectRatio)
                                else Modifier.height(180.dp)
                            )
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White.copy(alpha = 0.65f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (report.thumbnail != null) {
                            Image(
                                bitmap = report.thumbnail,
                                contentDescription = report.category.displayName,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = EcoDarkGreen.copy(alpha = 0.45f), modifier = Modifier.size(44.dp))
                                Text("No photo attached", fontSize = 13.sp, color = Black.copy(alpha = 0.5f))
                            }
                        }
                    }

                    Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.85f), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            DetailMetaRow(Icons.Default.Person, "Reported By", report.displayReporter(isAdmin))
                            if (isAdmin && report.reporterPhone.isNotBlank() && report.reporterName.isNotBlank()) {
                                DetailMetaRow(Icons.Default.Phone, "Contact", report.reporterPhone)
                            }
                            DetailMetaRow(Icons.Default.LocationOn, "Location", report.location)
                            DetailMetaRow(Icons.Default.Schedule, "Submitted", report.timestamp)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Current Status:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Black.copy(alpha = 0.7f))
                                StatusBadge(status = report.status)
                            }
                        }
                    }

                    Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.85f), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Issue Description", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Black)
                            Text(report.description, fontSize = 14.sp, color = Black.copy(alpha = 0.85f), lineHeight = 20.sp)
                        }
                    }

                    if (isAdmin) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = EcoGreen.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, EcoDarkGreen.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Admin: Update Resolution Status", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EcoDarkGreen)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ReportStatus.entries.forEach { status ->
                                        val isCurrent = report.status == status
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = if (isCurrent) EcoGreen else Color.White,
                                            border = if (isCurrent) null else BorderStroke(1.dp, Black.copy(alpha = 0.2f)),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(50))
                                                .clickable { onStatusUpdate(status) }
                                        ) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                                                Text(
                                                    text = status.label,
                                                    color = if (isCurrent) Color.White else Black,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminStatusDropdownChip(
    currentStatus: ReportStatus,
    onStatusSelected: (ReportStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clip(RoundedCornerShape(50)).clickable { expanded = true }
        ) {
            StatusBadge(status = currentStatus)
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Change status", tint = EcoDarkGreen, modifier = Modifier.size(18.dp))
        }

        WasteManDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ReportStatus.entries.forEach { status ->
                WasteManDropdownItem(
                    text = status.label,
                    selected = status == currentStatus,
                    onClick = {
                        onStatusSelected(status)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: ReportStatus) {
    val (bgColor, textColor, icon) = when (status) {
        ReportStatus.SOLVED -> Triple(EcoGreen.copy(alpha = 0.18f), EcoDarkGreen, Icons.Default.CheckCircle)
        ReportStatus.IN_PROGRESS -> Triple(Color(0xFFFFA000).copy(alpha = 0.18f), Color(0xFF8D5400), Icons.Default.Schedule)
        ReportStatus.PENDING -> Triple(Black.copy(alpha = 0.1f), Black.copy(alpha = 0.7f), Icons.Default.Schedule)
    }

    Surface(shape = RoundedCornerShape(50), color = bgColor) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(12.dp))
            Text(status.label, color = textColor, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
        }
    }
}

@Composable
private fun DetailMetaRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, tint = EcoDarkGreen, modifier = Modifier.size(18.dp))
        Text("$label:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Black.copy(alpha = 0.7f))
        Text(value, fontSize = 13.sp, color = Black, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun FilterPill(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) EcoGreen else Color.White.copy(alpha = 0.8f),
        border = if (selected) null else BorderStroke(1.dp, Black.copy(alpha = 0.15f)),
        modifier = Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Black.copy(alpha = 0.75f),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

// ============================================================================
// LOGIN SCREEN (Phone-only)
// ============================================================================

@Composable
fun LoginScreen(
    isLoading: Boolean = false,
    countryCode: String = "+91",
    onContinueWithPhone: (String) -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    var phoneNumber by remember { mutableStateOf("") }
    val isValidPhone = phoneNumber.length == 10

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EcoGreen)
            .safeContentPadding()
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(shape = CircleShape, color = EcoLightGreen, modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Eco, contentDescription = "WasteMan", tint = EcoGreen, modifier = Modifier.size(26.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Text("WasteMan", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = EcoLightGreen)
        }

        Surface(
            modifier = Modifier.widthIn(max = 440.dp).fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = EcoLightGreen
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Welcome Back", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Black)
                    Text(
                        "Enter your 10-digit phone number to report and track waste issues.",
                        fontSize = 14.sp,
                        color = Black.copy(alpha = 0.65f)
                    )
                }

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        if (digits.length <= 10) phoneNumber = digits
                    },
                    placeholder = { Text("9876543210", color = Black.copy(alpha = 0.35f)) },
                    leadingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = EcoDarkGreen, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(countryCode, fontWeight = FontWeight.Bold, color = Black, fontSize = 15.sp)
                            Spacer(Modifier.width(6.dp))
                            Box(Modifier.width(1.dp).height(20.dp).background(Black.copy(alpha = 0.2f)))
                        }
                    },
                    singleLine = true,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { if (isValidPhone && !isLoading) onContinueWithPhone("$countryCode$phoneNumber") }
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = wasteManFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { onContinueWithPhone("$countryCode$phoneNumber") },
                    enabled = isValidPhone && !isLoading,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EcoGreen,
                        contentColor = Color.White,
                        disabledContainerColor = EcoGreen.copy(alpha = 0.4f),
                        disabledContentColor = Color.White.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.5.dp, modifier = Modifier.size(22.dp))
                    } else {
                        Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Continue as Guest",
                        color = EcoDarkGreen,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable { onBackToHome() }.padding(4.dp)
                    )
                }
            }
        }

        Text(
            text = "Clean city, better tomorrow.",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = EcoLightGreen.copy(alpha = 0.9f)
        )
    }
}