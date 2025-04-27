package com.example.weatherapp.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherapp.WeatherState
import com.example.weatherapp.WeatherViewModel
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigateToCalendarScreen: () -> Unit,
    onNavigateToSettingsScreen: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: WeatherViewModel = viewModel()
) {
    val context = LocalContext.current
    val weatherState by viewModel.weatherState.collectAsState()
    var isLocationPermissionGranted by remember { mutableStateOf(false) }

    // Yêu cầu quyền truy cập vị trí
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isLocationPermissionGranted = isGranted
        if (isGranted) {
            viewModel.fetchWeatherByLocation(context)
        }
    }

    // Gọi fetchWeatherByLocation khi màn hình được tạo
    LaunchedEffect(Unit) {
        permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // Xác định vị trí để hiển thị
    val userLocation = when (weatherState) {
        is WeatherState.Success -> {
            val city = (weatherState as WeatherState.Success).data.name
            "$city, ${countryNameFromCode((weatherState as WeatherState.Success).data.sys.country ?: "Unknown")}"
        }
        is WeatherState.Error -> "Hanoi, Vietnam"
        is WeatherState.Loading -> "Loading..."
    }

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
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start // Căn trái tất cả các thành phần
        ) {
            // 🖙 Back Button + Location
            Column(
                horizontalAlignment = Alignment.Start // Căn trái
            ) {
                IconButton(onClick = { onNavigateBack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))

                // 🌍 Current Location
                Column(
                    horizontalAlignment = Alignment.Start // Căn trái
                ) {
                    Text(
                        text = "Current location",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        fontFamily = sourceSans3
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start // Căn trái
                    ) {
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
                        Text(
                            text = userLocation,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontFamily = sourceSans3,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 📅 Menu Items
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start // Căn trái
            ) {
                MenuItem(Icons.Outlined.Event, "Calender") {
                    onNavigateToCalendarScreen()
                }
            }

            // ⚙️ Settings & Share App
            Column(
                modifier = Modifier.padding(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.Start // Căn trái
            ) {
                Text(
                    text = "Settings",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = sourceSans3,
                    modifier = Modifier.clickable { onNavigateToSettingsScreen() }
                )
                Text(
                    text = "Share this app",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = sourceSans3,
                    modifier = Modifier.clickable { }
                )
            }
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
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Start // Căn trái các thành phần trong Row
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
        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontFamily = sourceSans3
        )
    }
}

// Hàm chuyển đổi mã quốc gia thành tên quốc gia
fun countryNameFromCode(countryCode: String): String {
    return try {
        val locale = Locale("", countryCode)
        locale.displayCountry
    } catch (e: Exception) {
        "Unknown"
    }
}