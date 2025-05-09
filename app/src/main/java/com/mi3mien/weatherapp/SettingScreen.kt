package com.mi3mien.weatherapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mi3mien.weatherapp.sourceSans3

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2196F3),
                        Color(0xFF4FC3F7)
                    )
                )
            )
            .padding(16.dp)
    ) {
        // Nút Back và tiêu đề "Settings" trong cùng một Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { onNavigateBack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = sourceSans3,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        // Units Section
        Text(
            text = "Units",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            fontFamily = sourceSans3,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
                    .padding(start = 8.dp, end = 8.dp)
            ) {
                // Weather Unit Toggle
                var weatherUnit by remember { mutableStateOf("C") }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Weather",
                        fontSize = 16.sp,
                        color = Color.Black,
                        fontFamily = sourceSans3
                    )
                    Row(
                        modifier = Modifier
                            .background(
                                color = Color(0xFFBBDEFB),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(4.dp)
                    ) {
                        ToggleButton(
                            text = "C",
                            isSelected = weatherUnit == "C",
                            onClick = { weatherUnit = "C" }
                        )
                        ToggleButton(
                            text = "F",
                            isSelected = weatherUnit == "F",
                            onClick = { weatherUnit = "F" }
                        )
                    }
                }

                // Wind Unit Toggle
                var windUnit by remember { mutableStateOf("MIL") }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Wind",
                        fontSize = 16.sp,
                        color = Color.Black,
                        fontFamily = sourceSans3
                    )
                    Row(
                        modifier = Modifier
                            .background(
                                color = Color(0xFFBBDEFB),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(4.dp)
                    ) {
                        ToggleButton(
                            text = "MIL",
                            isSelected = windUnit == "MIL",
                            onClick = { windUnit = "MIL" }
                        )
                        ToggleButton(
                            text = "Km",
                            isSelected = windUnit == "Km",
                            onClick = { windUnit = "Km" }
                        )
                    }
                }
            }
        }

        // Apps Section
        Text(
            text = "Apps",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            fontFamily = sourceSans3,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
        ) {
            Column {
                SettingItem("WeatherPro")
                SettingItem("Share")
                SettingItem("About the weather app")
                SettingItem("Join with us")
                SettingItem("Mobile data limit")
            }
        }

        // Feedback Section
        Text(
            text = "Review",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            fontFamily = sourceSans3,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(), // Đảm bảo Card mở rộng theo nội dung
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Feedback",
                        fontSize = 16.sp,
                        color = Color.Black,
                        fontFamily = sourceSans3
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Arrow Forward",
                        tint = Color.Black
                    )
                }
                Text(
                    text = "Tell us about the experience with the app",
                    fontSize = 12.sp, // Giảm fontSize từ 14.sp xuống 12.sp
                    color = Color.Gray,
                    fontFamily = sourceSans3,
                    modifier = Modifier.padding(top = 2.dp) // Giảm padding từ 4.dp xuống 2.dp
                )
            }
        }
    }
}

@Composable
fun ToggleButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF2196F3) else Color.Transparent,
            contentColor = if (isSelected) Color.White else Color.Black
        ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontFamily = sourceSans3
        )
    }
}

@Composable
fun SettingItem(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle click */ }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (title) {
                    "WeatherPro" -> Icons.Default.Settings
                    "Share" -> Icons.Default.Share
                    "About the weather app" -> Icons.Default.Info
                    "Join with us" -> Icons.Default.Person
                    else -> Icons.Default.DataUsage
                },
                contentDescription = title,
                tint = Color(0xFF2196F3),
                modifier = Modifier.padding(end = 16.dp)
            )
            Text(
                text = title,
                fontSize = 16.sp,
                color = Color.Black,
                fontFamily = sourceSans3
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Arrow Forward",
            tint = Color.Black
        )
    }
}