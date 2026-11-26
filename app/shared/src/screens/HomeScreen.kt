package com.example.wasteman.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.example.wasteman.components.FilterPill
import com.example.wasteman.components.ReportCard
import com.example.wasteman.components.ReportDetailDialog
import com.example.wasteman.core.IssueStats
import com.example.wasteman.core.NewsAnnouncement
import com.example.wasteman.core.NewsIconType
import com.example.wasteman.core.WasteReportItem
import com.example.wasteman.theme.Black
import com.example.wasteman.theme.EcoDarkGreen
import com.example.wasteman.theme.EcoGreen
import com.example.wasteman.theme.EcoLightGreen
import kotlinx.coroutines.launch
import kotlin.math.round

@Composable
fun HomeScreen(
    issueStats: IssueStats,
    newsItems: List<NewsAnnouncement>,
    recentReports: List<WasteReportItem>,
    resolveThumbnail: (WasteReportItem) -> ImageBitmap? = { null },
    isCompact: Boolean,
    isLoading: Boolean = false,
    onReportClick: () -> Unit,
    onViewAllClick: () -> Unit
) {
    val columns = if (isCompact) 1 else 2
    val topCardHeight = if (isCompact) 210.dp else 235.dp
    var selectedReportForDetail by remember { mutableStateOf<WasteReportItem?>(null) }

    AnimatedContent(
        targetState = isLoading && recentReports.isEmpty(),
        transitionSpec = { fadeIn(tween(240)) togetherWith fadeOut(tween(180)) },
        label = "homeLoadingTransition"
    ) { showInitialLoading ->
        if (showInitialLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        color = EcoGreen,
                        strokeWidth = 3.5.dp,
                        modifier = Modifier.size(42.dp)
                    )
                    Text(
                        text = "Loading WasteMan...",
                        color = EcoDarkGreen,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
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
                        color = Color.White.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, Black.copy(alpha = 0.07f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(topCardHeight)
                            .shadow(
                                elevation = 3.dp,
                                shape = RoundedCornerShape(22.dp),
                                ambientColor = Black.copy(alpha = 0.09f),
                                spotColor = Black.copy(alpha = 0.13f)
                            )
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
                    Box(modifier = Modifier.animateItem()) {
                        ReportCard(
                            report = report,
                            thumbnail = resolveThumbnail(report),
                            isAdmin = false,
                            onStatusChange = {},
                            onClick = { selectedReportForDetail = report }
                        )
                    }
                }
            }
        }
    }

    selectedReportForDetail?.let { report ->
        ReportDetailDialog(
            report = report,
            thumbnail = resolveThumbnail(report),
            isAdmin = false,
            onStatusUpdate = {},
            onDismiss = { selectedReportForDetail = null }
        )
    }
}

@Composable
fun IssueStatsGaugeCard(issueStats: IssueStats, isCompact: Boolean) {
    var hasAppeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        hasAppeared = true
    }

    val targetPercent = if (hasAppeared && issueStats.total > 0) {
        issueStats.solved.toFloat() / issueStats.total
    } else {
        0f
    }
    val animatedPercent by animateFloatAsState(
        targetValue = targetPercent,
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "statsPercent"
    )
    val animatedTotal by animateIntAsState(
        targetValue = if (hasAppeared) issueStats.total else 0,
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "statsTotal"
    )
    val animatedSolved by animateIntAsState(
        targetValue = if (hasAppeared) issueStats.solved else 0,
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "statsSolved"
    )
    val animatedPending by animateIntAsState(
        targetValue = if (hasAppeared) issueStats.pending else 0,
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "statsPending"
    )

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
                    sweepAngle = 180f * animatedPercent,
                    useCenter = false,
                    style = Stroke(strokeWidthPx, cap = StrokeCap.Round),
                    topLeft = topLeft,
                    size = arcSize
                )
            }

            val dynamicFont = (gaugeDiameter / 150.dp) * 28.sp
            Text(
                text = "${round(animatedPercent * 100).toInt()}%",
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
            StatLegendRow(color = Color.LightGray, text = "Submitted : $animatedTotal", isCompact = isCompact)
            StatLegendRow(color = EcoGreen, text = "Solved : $animatedSolved", isCompact = isCompact)
            StatLegendRow(color = Color(0xFFFFA000), text = "Pending : $animatedPending", isCompact = isCompact)
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
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Black.copy(alpha = 0.10f),
                spotColor = Black.copy(alpha = 0.14f)
            )
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
                    Icon(
                        imageVector = item.icon.toImageVector(),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = EcoLightGreen
                    )
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

private fun NewsIconType.toImageVector(): ImageVector = when (this) {
    NewsIconType.RECYCLING -> Icons.Default.Recycling
    NewsIconType.CAMPAIGN -> Icons.Default.Campaign
    NewsIconType.VOLUNTEER -> Icons.Default.VolunteerActivism
    NewsIconType.ALERT -> Icons.Default.Warning
}
