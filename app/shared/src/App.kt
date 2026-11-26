package com.example.wasteman

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wasteman.components.NavPill
import com.example.wasteman.screens.AddReportContent
import com.example.wasteman.screens.HomeScreen
import com.example.wasteman.screens.LoginScreen
import com.example.wasteman.screens.MyReportsContent
import com.example.wasteman.state.AppState
import com.example.wasteman.state.AppTab
import com.example.wasteman.theme.Black
import com.example.wasteman.theme.EcoGreen
import com.example.wasteman.theme.EcoLightGreen
import com.example.wasteman.theme.WasteManTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.reload.DevelopmentEntryPoint

@Composable
@Preview
@DevelopmentEntryPoint
fun App() {
    WasteManTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            WasteManAppScreen()
        }
    }
}

@Composable
fun WasteManAppScreen(state: AppState = remember { AppState() }) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (!state.useDemoBackend) {
            state.refreshFromBackend(fallbackToDemoOnFailure = true)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxWidth < 680.dp

        AnimatedContent(
            targetState = state.activeTab == AppTab.LOGIN,
            transitionSpec = {
                (fadeIn(tween(240)) + slideInVertically { it / 28 }) togetherWith fadeOut(tween(160))
            },
            label = "rootAuthTransition"
        ) { isLogin ->
            if (isLogin) {
                LoginScreen(
                    isLoading = state.isLoading,
                    onContinueWithPhone = { fullPhone ->
                        scope.launch { state.login(fullPhone) }
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
                                state.logout()
                            }
                        }
                    )

                    NavBar(state = state)

                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(24.dp),
                                ambientColor = Black.copy(alpha = 0.12f),
                                spotColor = Black.copy(alpha = 0.16f)
                            ),
                        shape = RoundedCornerShape(24.dp),
                        color = EcoLightGreen
                    ) {
                        AnimatedContent(
                            targetState = state.activeTab,
                            transitionSpec = {
                                (fadeIn(tween(220)) + slideInVertically { it / 28 }) togetherWith fadeOut(tween(150))
                            },
                            label = "tabContentTransition"
                        ) { tab ->
                            when (tab) {
                                AppTab.HOME -> HomeScreen(
                                    issueStats = state.issueStats,
                                    newsItems = state.newsItems,
                                    recentReports = state.reports.take(4),
                                    resolveThumbnail = state::resolveThumbnail,
                                    isCompact = isCompact,
                                    isLoading = state.isLoading,
                                    onReportClick = { state.activeTab = AppTab.REPORT },
                                    onViewAllClick = { state.activeTab = AppTab.ISSUES }
                                )

                                AppTab.REPORT -> AddReportContent(
                                    platformHandler = state.platformHandler,
                                    defaultReporterName = state.savedReporterName,
                                    reporterPhone = state.loginNum,
                                    isSubmitting = state.isLoading,
                                    onSubmitReport = { draft, localPreview ->
                                        scope.launch { state.submitReport(draft, localPreview) }
                                    }
                                )

                                AppTab.ISSUES -> MyReportsContent(
                                    reports = if (state.useDemoBackend) {
                                        state.reports.filter {
                                            state.loginNum.isBlank() || it.reporterPhone == state.loginNum
                                        }
                                    } else {
                                        state.myReports
                                    },
                                    resolveThumbnail = state::resolveThumbnail,
                                    isAdmin = false,
                                    isLoading = state.isLoading,
                                    onStatusUpdate = { _, _ -> }
                                )

                                AppTab.ADMIN -> MyReportsContent(
                                    reports = state.reports,
                                    resolveThumbnail = state::resolveThumbnail,
                                    isAdmin = state.isAdmin,
                                    isLoading = state.isLoading,
                                    onStatusUpdate = { reportId, newStatus ->
                                        scope.launch { state.updateReportStatus(reportId, newStatus) }
                                    }
                                )

                                AppTab.LOGIN -> Unit
                            }
                        }
                    }
                }
            }
        }
    }
}

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
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 6.dp, vertical = 8.dp)
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
