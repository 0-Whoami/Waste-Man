package com.example.wasteman.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wasteman.components.FilterPill
import com.example.wasteman.components.ReportCard
import com.example.wasteman.components.ReportDetailDialog
import com.example.wasteman.core.ReportStatus
import com.example.wasteman.core.ReportTimeFilter
import com.example.wasteman.core.WasteCategory
import com.example.wasteman.core.WasteReportItem
import com.example.wasteman.state.DEMO_NOW_MILLIS
import com.example.wasteman.theme.AlertRed
import com.example.wasteman.theme.Black
import com.example.wasteman.theme.EcoDarkGreen
import com.example.wasteman.theme.EcoGreen
import com.example.wasteman.theme.wasteManFieldColors

@Composable
fun MyReportsContent(
    reports: List<WasteReportItem>,
    resolveThumbnail: (WasteReportItem) -> ImageBitmap? = { null },
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

    val reportsSnapshot = reports.toList()

    val activeDetailReport = remember(selectedReportForDetail, reportsSnapshot) {
        selectedReportForDetail?.let { current -> reportsSnapshot.find { it.id == current.id } ?: current }
    }

    val activeFilterCount = remember(selectedStatusFilter, selectedCategoryFilter, selectedTimeFilter) {
        listOf(
            selectedTimeFilter != ReportTimeFilter.ALL,
            selectedStatusFilter != null,
            selectedCategoryFilter != null
        ).count { it }
    }

    val filteredReports = remember(
        reportsSnapshot,
        selectedStatusFilter,
        selectedCategoryFilter,
        selectedTimeFilter,
        searchQuery,
        isAdmin,
        referenceNowMillis
    ) {
        val query = searchQuery.trim().lowercase()
        reportsSnapshot.filter { report ->
            val matchesStatus = (selectedStatusFilter == null) || (report.status == selectedStatusFilter)
            val matchesCategory = (selectedCategoryFilter == null) || (report.category == selectedCategoryFilter)

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

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isCompact = maxWidth < 680.dp
        var filtersExpanded by remember(isCompact) { mutableStateOf(!isCompact) }

        Column(
            modifier = Modifier.fillMaxSize().padding(if (isCompact) 14.dp else 20.dp),
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
                        fontSize = if (isCompact) 20.sp else 24.sp,
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
                    AnimatedVisibility(
                        visible = activeFilterCount > 0 || searchQuery.isNotBlank(),
                        enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.88f),
                        exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.88f)
                    ) {
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = when {
                                isCompact && isAdmin -> "Search name, phone, location..."
                                isCompact -> "Search type, location..."
                                isAdmin -> "Search name, phone, type, time, description, location..."
                                else -> "Search name, type, time, description, location..."
                            },
                            color = Black.copy(alpha = 0.45f),
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = EcoDarkGreen, modifier = Modifier.size(20.dp))
                    },
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
                    modifier = Modifier.weight(1f)
                )

                FilterToggleChip(
                    expanded = filtersExpanded,
                    activeFilterCount = activeFilterCount,
                    compact = isCompact,
                    onClick = { filtersExpanded = !filtersExpanded }
                )
            }

            AnimatedVisibility(
                visible = filtersExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 2.dp, vertical = 3.dp),
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 2.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterPill(text = "All Status", selected = selectedStatusFilter == null) {
                            selectedStatusFilter = null
                        }
                        ReportStatus.entries.forEach { status ->
                            FilterPill(text = status.label, selected = selectedStatusFilter == status) {
                                selectedStatusFilter = status
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 2.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterPill(text = "All Types", selected = selectedCategoryFilter == null) {
                            selectedCategoryFilter = null
                        }
                        WasteCategory.entries.forEach { category ->
                            FilterPill(text = category.displayName, selected = selectedCategoryFilter == category) {
                                selectedCategoryFilter = category
                            }
                        }
                    }
                }
            }

            val reportsContentState = when {
                isLoading -> 0
                filteredReports.isEmpty() -> 1
                else -> 2
            }

            AnimatedContent(
                targetState = reportsContentState,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
                label = "reportsListTransition"
            ) { state ->
                when (state) {
                    0 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                color = EcoGreen,
                                strokeWidth = 3.5.dp,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "Loading reports...",
                                color = EcoDarkGreen,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    1 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.History, contentDescription = null, tint = EcoDarkGreen.copy(alpha = 0.5f), modifier = Modifier.size(44.dp))
                            Text("No matching reports found", color = Black.copy(alpha = 0.65f), fontWeight = FontWeight.Medium)
                        }
                    }

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                    ) {
                        items(filteredReports, key = { it.id }) { report ->
                            Box(modifier = Modifier.animateItem()) {
                                ReportCard(
                                    report = report,
                                    thumbnail = resolveThumbnail(report),
                                    isAdmin = isAdmin,
                                    onStatusChange = { newStatus -> onStatusUpdate(report.id, newStatus) },
                                    onClick = { selectedReportForDetail = report }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    activeDetailReport?.let { report ->
        ReportDetailDialog(
            report = report,
            thumbnail = resolveThumbnail(report),
            isAdmin = isAdmin,
            onStatusUpdate = { newStatus -> onStatusUpdate(report.id, newStatus) },
            onDismiss = { selectedReportForDetail = null }
        )
    }
}

@Composable
private fun FilterToggleChip(
    expanded: Boolean,
    activeFilterCount: Int,
    compact: Boolean,
    onClick: () -> Unit
) {
    val highlighted = expanded || activeFilterCount > 0
    val containerColor = if (highlighted) EcoGreen else Color.White.copy(alpha = 0.85f)
    val contentColor = if (highlighted) Color.White else EcoDarkGreen

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = if (highlighted) null else BorderStroke(1.dp, Black.copy(alpha = 0.18f)),
        modifier = Modifier
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (compact) 10.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(Icons.Default.Tune, contentDescription = "Toggle filters", tint = contentColor, modifier = Modifier.size(18.dp))
            val label = when {
                compact && activeFilterCount > 0 -> "$activeFilterCount"
                compact -> "Filters"
                activeFilterCount > 0 -> "Filters ($activeFilterCount)"
                else -> "Filters"
            }
            Text(
                text = label,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                softWrap = false
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
