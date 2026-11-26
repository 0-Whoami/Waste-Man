package com.example.wasteman.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val EcoGreen = Color(0xFF4FB828)
val EcoLightGreen = Color(0xFFF1E4F3)
val EcoDarkGreen = Color(0xFF386C0B)
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
fun wasteManFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = EcoGreen,
    unfocusedBorderColor = Black.copy(alpha = 0.22f),
    cursorColor = EcoDarkGreen,
    focusedTextColor = Black,
    unfocusedTextColor = Black,
    focusedContainerColor = Color.White.copy(alpha = 0.9f),
    unfocusedContainerColor = Color.White.copy(alpha = 0.78f)
)

@Composable
fun wasteManPrimaryButtonColors(): ButtonColors = ButtonDefaults.buttonColors(
    containerColor = EcoGreen,
    contentColor = Color.White,
    disabledContainerColor = EcoGreen.copy(alpha = 0.4f),
    disabledContentColor = Color.White.copy(alpha = 0.7f)
)

@Composable
fun WasteManTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = EcoGreen,
            secondary = EcoLightGreen,
            error = AlertRed
        ),
        typography = WasteManTypography,
        content = content
    )
}
