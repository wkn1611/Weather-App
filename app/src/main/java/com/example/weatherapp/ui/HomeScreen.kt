package com.example.weatherapp.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.shape.CircleShape

@Composable
fun HomeScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF3A7BD5), Color(0xFF00D2FF))
                )
            )
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 🖙 Back Button + Location
            Column {
                CustomBackButton(navController)
                Spacer(modifier = Modifier.height(14.dp))

                // 🌍 Current Location
                Column {
                    Text(
                        text = "Current location",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp)) // Dịch xuống dưới
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.2f), shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = "Location",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Berlin, Germany", color = Color.White, fontSize = 20.sp)
                    }
                }
            }

            // 📅 Menu Items (Canh giữa)
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MenuItem(Icons.Outlined.Event, "Calender") {
                    navController.navigate("calendar_screen")
                }
                MenuItem(Icons.Outlined.NotificationsNone, "Notifications") {
                    navController.navigate("notifications_screen")
                }
            }

            // ⚙️ Settings & Share App (Đặt sát dưới)
            Column(
                modifier = Modifier.padding(bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Settings",
                    color = Color.White,
                    fontSize = 20.sp,
                    modifier = Modifier.clickable { }
                )
                Text(
                    "Share this app",
                    color = Color.White,
                    fontSize = 20.sp,
                    modifier = Modifier.clickable { }
                )
            }
        }
    }
}

@Composable
fun CustomBackButton(navController: NavController) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { navController.popBackStack() }
            .padding(vertical = 12.dp)
    ) {
        Canvas(modifier = Modifier.size(13.dp, 12.dp)) {
            drawLine(
                color = Color.White,
                start = Offset(size.width * 0.8f, 0f),
                end = Offset(0f, size.height / 2),
                strokeWidth = 6f
            )
            drawLine(
                color = Color.White,
                start = Offset(0f, size.height / 2),
                end = Offset(size.width * 0.8f, size.height),
                strokeWidth = 6f
            )
        }
    }
}

@Composable
fun MenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.2f), shape = CircleShape)
        ) {
            Icon(icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, color = Color.White, fontSize = 20.sp)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewMainScreen() {
    HomeScreen(navController = rememberNavController())
}
