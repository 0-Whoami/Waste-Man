package com.example.wasteman.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.wasteman.core.ReportStatus
import com.example.wasteman.core.WasteReportItem
import com.example.wasteman.theme.Black
import com.example.wasteman.theme.EcoDarkGreen
import com.example.wasteman.theme.EcoGreen
import com.example.wasteman.theme.EcoLightGreen

@Composable
fun ReportCard(
    report: WasteReportItem,
    thumbnail: ImageBitmap? = null,
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
                if (thumbnail != null) {
                    Image(
                        bitmap = thumbnail,
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
                        AdminStatusDropdownChip(
                            currentStatus = report.status,
                            onStatusSelected = onStatusChange
                        )
                    } else {
                        StatusBadge(status = report.status)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Black.copy(alpha = 0.55f),
                        modifier = Modifier.size(13.dp)
                    )
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
fun AdminStatusDropdownChip(
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
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Change status",
                tint = EcoDarkGreen,
                modifier = Modifier.size(18.dp)
            )
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
fun ReportDetailDialog(
    report: WasteReportItem,
    thumbnail: ImageBitmap? = null,
    isAdmin: Boolean,
    onStatusUpdate: (ReportStatus) -> Unit,
    onDismiss: () -> Unit
) {
    val detailAspectRatio = remember(thumbnail) { computeClampedAspectRatio(thumbnail) }

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
                        if (thumbnail != null) {
                            Image(
                                bitmap = thumbnail,
                                contentDescription = report.category.displayName,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = EcoDarkGreen.copy(alpha = 0.45f),
                                    modifier = Modifier.size(44.dp)
                                )
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
fun DetailMetaRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, tint = EcoDarkGreen, modifier = Modifier.size(18.dp))
        Text("$label:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Black.copy(alpha = 0.7f))
        Text(value, fontSize = 13.sp, color = Black, modifier = Modifier.weight(1f))
    }
}
