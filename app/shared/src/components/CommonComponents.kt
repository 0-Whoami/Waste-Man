package com.example.wasteman.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wasteman.core.ReportStatus
import com.example.wasteman.theme.Black
import com.example.wasteman.theme.EcoDarkGreen
import com.example.wasteman.theme.EcoGreen
import com.example.wasteman.theme.EcoLightGreen

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
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
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

@Composable
fun FilterPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
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

@Composable
fun StatusBadge(status: ReportStatus) {
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
fun FieldHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Black)
        Text(text = subtitle, fontSize = 12.sp, color = Black.copy(alpha = 0.6f))
    }
}

fun computeClampedAspectRatio(bitmap: ImageBitmap?): Float? =
    bitmap?.let { bmp ->
        if (bmp.height > 0) (bmp.width.toFloat() / bmp.height.toFloat()).coerceIn(0.75f, 2.2f)
        else 16f / 9f
    }
