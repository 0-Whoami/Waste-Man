package com.example.wasteman.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wasteman.theme.Black
import com.example.wasteman.theme.EcoDarkGreen
import com.example.wasteman.theme.EcoGreen
import com.example.wasteman.theme.EcoLightGreen
import com.example.wasteman.theme.wasteManFieldColors
import com.example.wasteman.theme.wasteManPrimaryButtonColors

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
                    colors = wasteManPrimaryButtonColors(),
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
