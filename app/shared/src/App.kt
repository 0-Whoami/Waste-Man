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
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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
import kotlinx.coroutines.launch
import org.jetbrains.compose.reload.DevelopmentEntryPoint
import org.w3c.dom.Text
import kotlin.math.round

val EcoGreen = Color(0xFF4fb828)
val EcoLightGreen = Color(0xFFf1e4f3)
val EcoDarkGreen = Color(0xFF386c0b)

val Black = Color(0xFF191102)
val AlertRed = Color(0xFFC62828)

@Composable
@Preview
@DevelopmentEntryPoint
fun App() {
    val repository = remember { WasteManRepository() }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = EcoGreen,
            secondary = EcoLightGreen,
            error = AlertRed
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Screen2()
            //HomeScreen(repository)
        }
    }
}


@Composable
fun HeaderBar(isCompact: Boolean, state: AppState) {
    Row(
        modifier = Modifier.padding(5.dp).defaultMinSize(minHeight = 100.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Eco, "", modifier = Modifier.size(45.dp), tint = EcoLightGreen)
        Spacer(Modifier.width(10.dp))
        Text("WasteMan", fontWeight = FontWeight.Bold, fontSize = 35.sp, color = EcoLightGreen)
        Spacer(Modifier.weight(1f))
        NavPill(
            active = true,
            onClick = {},
            icon = Icons.Default.Person,
            label = if (isCompact) "" else (if (state.loginNum.isBlank()) "Login" else "Logout")
        )
        Spacer(Modifier.width(10.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NavPill(active: Boolean, onClick: () -> Unit, icon: ImageVector, label: String) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pressScale"
    )
    val eScale by animateFloatAsState(
        targetValue = if (hovered) 1.5f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "hoverElevation"
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.scale(scale).shadow(
            5.dp * eScale,
            RoundedCornerShape(20.dp),
            spotColor = EcoLightGreen,
            ambientColor = EcoLightGreen
        ).background(EcoLightGreen, RoundedCornerShape(20.dp)).height(40.dp).padding(10.dp)
            .onClick(interactionSource = interactionSource, onClick = onClick)
    ) {
        val color = if (active) EcoGreen else Color.Gray
        Icon(icon, "", tint = color)
        if (label.isNotBlank()) Text(label, color = color)
    }
}

enum class AppTab {
    HOME, REPORT, ISSUES, ADMIN, LOGIN
}

class AppState {
    var activeTab by mutableStateOf(AppTab.HOME)
    var isAdmin by mutableStateOf(false)
    var loginNum by mutableStateOf("")
}

@Composable
fun NavBar(state: AppState) {
    val scrollState = rememberScrollState()
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(10.dp).horizontalScroll(scrollState)
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
            icon = Icons.Default.Report,
            label = "Add Report"
        )
        NavPill(
            active = state.activeTab == AppTab.ISSUES,
            onClick = { state.activeTab = AppTab.ISSUES },
            icon = Icons.Default.History,
            label = "My Reports"
        )
        if (state.isAdmin) NavPill(
            active = state.activeTab == AppTab.ADMIN,
            onClick = { state.activeTab = AppTab.ADMIN },
            icon = Icons.Default.AdminPanelSettings,
            label = "My Reports"
        )
    }
}

class IssueStats(val total: Int = 0, val solved: Int = 0) {
    val pending: Int
        get() = total - solved
}

@Composable
fun foo(color: Color, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(20.dp).background(color = color, RoundedCornerShape(5.dp)))
        Text(text = text, fontSize = 18.sp, color = Black)
    }
}

@Composable
fun IssueCard(issueStats: IssueStats? = IssueStats(100, 76)) {
    if (issueStats != null) {
        val percent =
            if (issueStats.total == 0) 0f else issueStats.solved.toFloat() / issueStats.total
        Row(
            modifier = Modifier.padding(10.dp).widthIn(max = 500.dp).aspectRatio(2.75f)
        ) {
            BoxWithConstraints(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier.weight(1f).aspectRatio(2f)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width / 10
                    val offset = Offset(width / 2, width / 2)
                    drawArc(
                        Color.LightGray,
                        -180f,
                        180f,
                        false,
                        style = Stroke(width, cap = StrokeCap.Round),
                        topLeft = offset,
                        size = Size(size.width - width, size.width - width)
                    )
                    drawArc(
                        EcoGreen,
                        -180f,
                        180 * percent,
                        false,
                        style = Stroke(width, cap = StrokeCap.Round),
                        topLeft = offset,
                        size = Size(size.width - width, size.width - width)
                    )
                }
                val fontSize = (maxWidth / 150.dp) * 35.sp
                Text("${round(percent * 100).toInt()}%", color = Black, fontSize = fontSize)
            }
            Spacer(Modifier.width(10.dp))
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                foo(Color.LightGray, "Submitted : ${issueStats.total}")
                foo(EcoGreen, "Solved : ${issueStats.solved}")
            }
        }
    }

}

@Composable
fun Card(icon: ImageVector, heading: String, content: String) {
    Column {
        Row(
            modifier = Modifier.padding(5.dp).defaultMinSize(minHeight = 100.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, "", modifier = Modifier.size(45.dp), tint = EcoLightGreen)
            Spacer(Modifier.width(10.dp))
            Text(heading, fontWeight = FontWeight.Bold, fontSize = 35.sp, color = EcoLightGreen)
        }
        Text(content, color = EcoLightGreen)
    }
}

class CardItem(
    val icon: ImageVector,
    val heading: String,
    val content: String
)

@Composable
fun AutoAdvancingCarousel(
    items: List<CardItem>,
    modifier: Modifier = Modifier,
    autoAdvanceDurationMillis: Int = 5000
) {
    if (items.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { items.size })
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()
    val progress = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    // Handles timer and automatic page advancement
    LaunchedEffect(pagerState.currentPage, isDragged) {
        if (isDragged) {
            progress.snapTo(0f)
            return@LaunchedEffect
        }

        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = autoAdvanceDurationMillis,
                easing = LinearEasing
            )
        )

        val nextPage = (pagerState.currentPage + 1) % items.size
        pagerState.animateScrollToPage(nextPage)
    }

    Column(
        modifier = modifier.fillMaxWidth().background(EcoGreen, RoundedCornerShape(20.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val item = items[page]
            Card(
                icon = item.icon,
                heading = item.heading,
                content = item.content
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, _ ->
                val isSelected = pagerState.currentPage == index
                val indicatorWidth by animateDpAsState(
                    targetValue = if (isSelected) 36.dp else 6.dp,
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
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
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

class HomeScreenState {
    var issueStats: IssueStats? by mutableStateOf(null)
    var newsItems = mutableStateListOf<CardItem>()
}

@Composable
fun HomeScreen(homeScreenState: HomeScreenState) {
    IssueCard(homeScreenState.issueStats)
    AutoAdvancingCarousel(homeScreenState.newsItems)
}

enum class WasteCategory(val displayName: String) {
    OVERFLOWING_BINS("Overflowing Bins"),
    LITTERING("Littering"),
    PLASTIC_WASTE("Plastic Waste"),
    E_WASTE("E-Waste"),
    ORGANIC_WASTE("Organic Waste"),
    HAZARDOUS_WASTE("Hazardous Waste"),
    OTHER("Other")
}

class WasteReportDraft(
    val category: WasteCategory,
    val description: String,
    val location: String,
    val imageBytes: ByteArray?
)

class ReportPlatformHandler {
    fun launchImagePicker(onImagePicked: (bytes: ByteArray, bitmap: ImageBitmap) -> Unit) {

    }

    fun fetchCurrentLocation(onLocationFetched: (addressOrCoords: String) -> Unit) {

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReportContent(
    platformHandler: ReportPlatformHandler = ReportPlatformHandler(),
    onSubmitReport: (WasteReportDraft) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<WasteCategory?>(null) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var imagePreview by remember { mutableStateOf<ImageBitmap?>(null) }

    val scrollState = rememberScrollState()

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = EcoGreen,
        unfocusedBorderColor = Black.copy(alpha = 0.25f),
        cursorColor = EcoDarkGreen,
        focusedTextColor = Black,
        unfocusedTextColor = Black,
        focusedContainerColor = Color.White.copy(alpha = 0.85f),
        unfocusedContainerColor = Color.White.copy(alpha = 0.7f)
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(28.dp),
        color = EcoLightGreen
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Add Report",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Black
            )

            // 1. Category Dropdown
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
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select Category",
                                tint = EcoDarkGreen
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = fieldColors,
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        WasteCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = category.displayName,
                                        color = if (category == selectedCategory) EcoDarkGreen else Black,
                                        fontWeight = if (category == selectedCategory) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    selectedCategory = category
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // 2. Location Input + Explicit GPS Action
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
                                platformHandler.fetchCurrentLocation { fetched ->
                                    location = fetched
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = EcoDarkGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Use GPS",
                                color = EcoDarkGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    placeholder = {
                        Text(
                            "Enter street, nearby landmark, or tap Use GPS",
                            color = Black.copy(alpha = 0.45f)
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = EcoDarkGreen)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 3. Description Input
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldHeader(title = "Description", subtitle = "Provide details to help cleanup crews")

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = {
                        Text(
                            "Describe severity, approximate size, or hazards...",
                            color = Black.copy(alpha = 0.45f)
                        )
                    },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 4. Image Picker & Preview
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldHeader(title = "Photo Evidence", subtitle = "Attach a clear photo of the issue")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.7f))
                        .border(
                            BorderStroke(1.5.dp, EcoDarkGreen.copy(alpha = 0.35f)),
                            RoundedCornerShape(16.dp)
                        )
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
                            contentScale = ContentScale.Crop,
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
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove photo",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Upload photo",
                                tint = EcoDarkGreen,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Tap to select or upload photo",
                                color = EcoDarkGreen,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "JPG, PNG supported",
                                color = Black.copy(alpha = 0.45f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 5. Submit Button
            val isFormValid = selectedCategory != null && description.isNotBlank() && location.isNotBlank()

            Button(
                onClick = {
                    selectedCategory?.let { category ->
                        onSubmitReport(
                            WasteReportDraft(
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Submit Report",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FieldHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Black
        )
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = Black.copy(alpha = 0.6f)
        )
    }
}
enum class ReportStatus(val label: String) {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    SOLVED("Solved")
}

data class WasteReportItem(
    val id: String,
    val reporterName: String = "Citizen",
    val category: WasteCategory,
    val description: String,
    val location: String,
    val timestamp: String,
    val status: ReportStatus,
    val thumbnail: ImageBitmap? = null
)

/**
 * KMP Repository/Data Interface: Implement in commonMain (e.g., via Ktor client)
 * to handle report clicks and admin status updates.
 */
interface MyReportsHandler {
    fun onReportClick(report: WasteReportItem) {}
    fun onStatusUpdate(reportId: String, newStatus: ReportStatus) {}
    fun onRefresh() {}
}

@Composable
fun MyReportsContent(
    reports: List<WasteReportItem>,
    handler: MyReportsHandler,
    isAdmin: Boolean = false,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var selectedStatusFilter by remember { mutableStateOf<ReportStatus?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<WasteCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedReportForDetail by remember { mutableStateOf<WasteReportItem?>(null) }

    // Keep open popup synchronized if parent list updates status
    val activeDetailReport = remember(selectedReportForDetail, reports) {
        selectedReportForDetail?.let { current ->
            reports.find { it.id == current.id } ?: current
        }
    }

    val filteredReports = remember(reports, selectedStatusFilter, selectedCategoryFilter, searchQuery) {
        val query = searchQuery.trim().lowercase()
        reports.filter { report ->
            val matchesStatus = selectedStatusFilter == null || report.status == selectedStatusFilter
            val matchesCategory = selectedCategoryFilter == null || report.category == selectedCategoryFilter
            val matchesQuery = query.isEmpty() ||
                    report.id.lowercase().contains(query) ||
                    report.reporterName.lowercase().contains(query) ||
                    report.category.displayName.lowercase().contains(query) ||
                    report.timestamp.lowercase().contains(query) ||
                    report.description.lowercase().contains(query) ||
                    report.location.lowercase().contains(query)

            matchesStatus && matchesCategory && matchesQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Heading + Admin Badge + Count Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isAdmin) "All Reports" else "My Reports",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black
                )
                if (isAdmin) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = EcoDarkGreen
                    ) {
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

            Surface(
                shape = RoundedCornerShape(50),
                color = EcoGreen.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "${filteredReports.size} Total",
                    color = EcoDarkGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Multi-Field Search Bar (Name, Type, Time, Description, Location, ID)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = "Search name, type, time, description, location...",
                    color = Black.copy(alpha = 0.45f),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = EcoDarkGreen,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = Black.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EcoGreen,
                unfocusedBorderColor = Black.copy(alpha = 0.2f),
                cursorColor = EcoDarkGreen,
                focusedTextColor = Black,
                unfocusedTextColor = Black,
                focusedContainerColor = Color.White.copy(alpha = 0.85f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.7f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Status Filter Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterPill(
                text = "All Status",
                selected = selectedStatusFilter == null,
                onClick = { selectedStatusFilter = null }
            )
            ReportStatus.entries.forEach { status ->
                FilterPill(
                    text = status.label,
                    selected = selectedStatusFilter == status,
                    onClick = { selectedStatusFilter = status }
                )
            }
        }

        // Category Type Filter Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterPill(
                text = "All Types",
                selected = selectedCategoryFilter == null,
                onClick = { selectedCategoryFilter = null }
            )
            WasteCategory.entries.forEach { category ->
                FilterPill(
                    text = category.displayName,
                    selected = selectedCategoryFilter == category,
                    onClick = { selectedCategoryFilter = category }
                )
            }
        }

        // Reports List / Loading / Empty State
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EcoGreen)
                }
            }

            filteredReports.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = EcoDarkGreen.copy(alpha = 0.5f),
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "No matching reports found",
                            color = Black.copy(alpha = 0.65f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(filteredReports, key = { it.id }) { report ->
                        ReportCard(
                            report = report,
                            isAdmin = isAdmin,
                            onStatusChange = { newStatus ->
                                handler.onStatusUpdate(report.id, newStatus)
                            },
                            onClick = {
                                selectedReportForDetail = report
                                handler.onReportClick(report)
                            }
                        )
                    }
                }
            }
        }
    }

    // Maximized Detailed Popup Dialog on Card Click
    activeDetailReport?.let { report ->
        ReportDetailDialog(
            report = report,
            isAdmin = isAdmin,
            onStatusUpdate = { newStatus ->
                handler.onStatusUpdate(report.id, newStatus)
            },
            onDismiss = { selectedReportForDetail = null }
        )
    }
}

@Composable
private fun ReportCard(
    report: WasteReportItem,
    isAdmin: Boolean,
    onStatusChange: (ReportStatus) -> Unit,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.8f),
        border = BorderStroke(1.dp, Black.copy(alpha = 0.08f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail Preview
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(EcoLightGreen),
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
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = EcoDarkGreen.copy(alpha = 0.45f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Report Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
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
                    Spacer(modifier = Modifier.width(6.dp))

                    if (isAdmin) {
                        AdminStatusDropdownChip(
                            currentStatus = report.status,
                            onStatusSelected = onStatusChange
                        )
                    } else {
                        StatusBadge(status = report.status)
                    }
                }

                // Reporter Name (highlighted when admin views all reports)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Black.copy(alpha = 0.55f),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "${report.reporterName} • #${report.id}",
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = EcoDarkGreen,
                        modifier = Modifier.size(14.dp)
                    )
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
private fun StatusBadge(status: ReportStatus) {
    val (bgColor, textColor, icon) = when (status) {
        ReportStatus.SOLVED -> Triple(
            EcoGreen.copy(alpha = 0.18f),
            EcoDarkGreen,
            Icons.Default.CheckCircle
        )
        ReportStatus.IN_PROGRESS -> Triple(
            Color(0xFFFFA000).copy(alpha = 0.18f),
            Color(0xFF8D5400),
            Icons.Default.Schedule
        )
        ReportStatus.PENDING -> Triple(
            Black.copy(alpha = 0.1f),
            Black.copy(alpha = 0.7f),
            Icons.Default.Schedule
        )
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = status.label,
                color = textColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = report.category.displayName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Black
                        )
                        Text(
                            text = "Report ID: #${report.id}",
                            fontSize = 12.sp,
                            color = Black.copy(alpha = 0.6f)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Black.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close details",
                            tint = Black
                        )
                    }
                }

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Maximized Photo Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White.copy(alpha = 0.65f)),
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
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = EcoDarkGreen.copy(alpha = 0.45f),
                                    modifier = Modifier.size(44.dp)
                                )
                                Text(
                                    text = "No photo attached",
                                    fontSize = 13.sp,
                                    color = Black.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    // Metadata Card
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DetailMetaRow(
                                icon = Icons.Default.Person,
                                label = "Reported By",
                                value = report.reporterName
                            )
                            DetailMetaRow(
                                icon = Icons.Default.LocationOn,
                                label = "Location",
                                value = report.location
                            )
                            DetailMetaRow(
                                icon = Icons.Default.Schedule,
                                label = "Submitted",
                                value = report.timestamp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Current Status:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Black.copy(alpha = 0.7f)
                                )
                                StatusBadge(status = report.status)
                            }
                        }
                    }

                    // Full Description
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Issue Description",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Black
                            )
                            Text(
                                text = report.description,
                                fontSize = 14.sp,
                                color = Black.copy(alpha = 0.85f),
                                lineHeight = 20.sp
                            )
                        }
                    }

                    // Admin Status Control Panel
                    if (isAdmin) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = EcoGreen.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, EcoDarkGreen.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Admin: Update Resolution Status",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EcoDarkGreen
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ReportStatus.entries.forEach { status ->
                                        val isCurrent = report.status == status
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = if (isCurrent) EcoGreen else Color.White,
                                            border = if (isCurrent) null else BorderStroke(
                                                1.dp,
                                                Black.copy(alpha = 0.2f)
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(50))
                                                .clickable { onStatusUpdate(status) }
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.padding(vertical = 10.dp)
                                            ) {
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
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable { expanded = true }
        ) {
            StatusBadge(status = currentStatus)
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Change status",
                tint = EcoDarkGreen,
                modifier = Modifier.size(18.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            ReportStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = status.label,
                            fontWeight = if (status == currentStatus) FontWeight.Bold else FontWeight.Normal,
                            color = if (status == currentStatus) EcoDarkGreen else Black
                        )
                    },
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
private fun DetailMetaRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = EcoDarkGreen,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "$label:",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Black.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = Black,
            modifier = Modifier.weight(1f)
        )
    }
}
@Composable
private fun FilterPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) EcoGreen else Color.White.copy(alpha = 0.65f),
        border = if (selected) null else BorderStroke(1.dp, Black.copy(alpha = 0.15f)),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Black.copy(alpha = 0.75f),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}
@Composable
fun Screen2() {
    val state = remember { AppState() }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxWidth < 600.dp
        Column(
            modifier = Modifier.fillMaxSize().safeContentPadding()
                .background(EcoGreen).padding(10.dp)
        ) {
            HeaderBar(isCompact, state)
            NavBar(state)
            Column(
                modifier = Modifier.shadow(
                    5.dp,
                    RoundedCornerShape(20.dp),
                    spotColor = EcoLightGreen,
                    ambientColor = EcoLightGreen
                )
                    .background(EcoLightGreen, RoundedCornerShape(20.dp))
                    .fillMaxSize()
            ) {
                when (state.activeTab) {
                    AppTab.HOME -> HomeScreen()
                    AppTab.REPORT -> AddReportContent()
                    AppTab.ISSUES -> MyReportsContent()
                    AppTab.ADMIN -> MyReportsContent(isAdmin = state.isAdmin)
                    AppTab.LOGIN -> LoginScreen()
                }
            }
        }
    }
}