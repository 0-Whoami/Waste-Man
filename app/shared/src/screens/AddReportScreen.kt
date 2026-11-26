package com.example.wasteman.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wasteman.components.FieldHeader
import com.example.wasteman.components.WasteManDropdownItem
import com.example.wasteman.components.WasteManDropdownMenu
import com.example.wasteman.components.computeClampedAspectRatio
import com.example.wasteman.core.WasteCategory
import com.example.wasteman.core.WasteReportDraft
import com.example.wasteman.core.toBase64String
import com.example.wasteman.platform.ReportPlatformHandler
import com.example.wasteman.theme.Black
import com.example.wasteman.theme.EcoDarkGreen
import com.example.wasteman.theme.EcoGreen
import com.example.wasteman.theme.wasteManFieldColors
import com.example.wasteman.theme.wasteManPrimaryButtonColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddReportContent(
    platformHandler: ReportPlatformHandler,
    defaultReporterName: String = "",
    reporterPhone: String = "",
    isSubmitting: Boolean = false,
    onSubmitReport: (draft: WasteReportDraft, localPreview: ImageBitmap?) -> Unit,
    modifier: Modifier = Modifier
) {
    var reporterName by remember(defaultReporterName) { mutableStateOf(defaultReporterName) }
    var selectedCategory by remember { mutableStateOf<WasteCategory?>(null) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var imagePreview by remember { mutableStateOf<ImageBitmap?>(null) }
    var isFetchingGps by remember { mutableStateOf(false) }
    var isOpeningMap by remember { mutableStateOf(false) }

    val fieldColors = wasteManFieldColors()
    val previewAspectRatio = remember(imagePreview) { computeClampedAspectRatio(imagePreview) }

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

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                itemVerticalAlignment = Alignment.CenterVertically
            ) {
                FieldHeader(title = "Location", subtitle = "Where is the waste located?")

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LocationActionChip(
                        icon = Icons.Default.MyLocation,
                        label = if (isFetchingGps) "Locating..." else "Use GPS",
                        isLoading = isFetchingGps,
                        enabled = !isFetchingGps,
                        onClick = {
                            isFetchingGps = true
                            platformHandler.fetchCurrentLocation { fetched ->
                                location = fetched
                                isFetchingGps = false
                            }
                        }
                    )
                    LocationActionChip(
                        icon = Icons.Default.Map,
                        label = "Google Maps",
                        isLoading = isOpeningMap,
                        enabled = !isOpeningMap,
                        onClick = {
                            isOpeningMap = true
                            platformHandler.launchMapLocationPicker(
                                onLocationPicked = { picked ->
                                    location = picked
                                    isOpeningMap = false
                                },
                                onDismiss = {
                                    isOpeningMap = false
                                }
                            )
                        }
                    )
                }
            }

            val isLocationLoading = isFetchingGps || isOpeningMap
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                placeholder = {
                    Text("Enter street, landmark, or pick on Google Maps", color = Black.copy(alpha = 0.45f))
                },
                leadingIcon = {
                    AnimatedContent(
                        targetState = isLocationLoading,
                        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(140)) },
                        label = "locationIconLoading"
                    ) { loading ->
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = EcoDarkGreen
                            )
                        } else {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = EcoDarkGreen)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )
        }

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

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            FieldHeader(title = "Photo Evidence", subtitle = "Attach a clear photo of the issue")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
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
                AnimatedContent(
                    targetState = imagePreview,
                    transitionSpec = {
                        (fadeIn(tween(220)) + scaleIn(initialScale = 0.96f)) togetherWith fadeOut(tween(150))
                    },
                    label = "photoEvidenceTransition"
                ) { preview ->
                    if (preview != null) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                bitmap = preview,
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
                            imageBase64 = imageBytes?.takeIf { it.isNotEmpty() }?.toBase64String()
                        ),
                        imagePreview
                    )
                }
            },
            enabled = isFormValid,
            shape = RoundedCornerShape(50),
            colors = wasteManPrimaryButtonColors(),
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
private fun LocationActionChip(
    icon: ImageVector,
    label: String,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = EcoGreen.copy(alpha = 0.15f),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = EcoDarkGreen
                )
            } else {
                Icon(icon, contentDescription = null, tint = EcoDarkGreen, modifier = Modifier.size(14.dp))
            }
            Text(label, color = EcoDarkGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
