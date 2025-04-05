package com.example.weatherapp

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

// Data class chứa thông số thời tiết
data class WeatherDetails(
    val cityName: String = "N/A",
    val temperature: String = "Updating...", // Thay đổi thành String để hiển thị "Updating..."
    val humidity: Int = 0,
    val weatherType: String = "Unknown",
    val date: String = "N/A",
    val isLoading: Boolean = false
)

// Hàm logic lấy thông số thời tiết
fun getWeatherDetails(weatherState: WeatherState): WeatherDetails {
    return when (weatherState) {
        is WeatherState.Loading -> {
            WeatherDetails(isLoading = true)
        }
        is WeatherState.Success -> {
            val weatherData = weatherState.data
            if (weatherData.list.isNotEmpty()) {
                val firstForecast = weatherData.list[0]
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = dateFormat.parse(firstForecast.dt_txt)
                val displayDateFormat = SimpleDateFormat("EEE d MMM", Locale.getDefault())
                val formattedDate = date?.let { displayDateFormat.format(it) } ?: "N/A"

                WeatherDetails(
                    cityName = weatherData.city.name,
                    temperature = "${firstForecast.main.temp.toInt()}°F", // Định dạng nhiệt độ
                    humidity = firstForecast.main.humidity,
                    weatherType = firstForecast.weather[0].main,
                    date = formattedDate,
                    isLoading = false
                )
            } else {
                WeatherDetails() // Trả về giá trị mặc định nếu không có dữ liệu
            }
        }
        is WeatherState.Error -> {
            WeatherDetails() // Trả về giá trị mặc định nếu có lỗi
        }
    }
}

// Hàm ánh xạ loại thời tiết với icon
fun getWeatherIcon(forecast: Item0?): Int {
    return if (forecast != null) {
        when (forecast.weather[0].main) {
            "Clear" -> R.drawable.sunny
            "Clouds" -> R.drawable.clouds
            "Rain" -> R.drawable.rain
            "Thunderstorm" -> R.drawable.storm
            else -> R.drawable.sunny // Mặc định
        }
    } else {
        R.drawable.sunny // Mặc định nếu không có dữ liệu
    }
}

@Composable
fun MainScreen(viewModel: WeatherViewModel = viewModel()) {
    val weatherState by viewModel.weatherState.collectAsState()
    val context = LocalContext.current
    val weatherDetails = getWeatherDetails(weatherState)

    // State to track if permission is granted
    var isLocationPermissionGranted by remember { mutableStateOf(false) }

    // Launcher to request location permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isLocationPermissionGranted = isGranted
        if (isGranted) {
            viewModel.fetchWeatherByLocation(context)
        } else {
            // Handle permission denied: fetch weather for a default city
            viewModel.fetchWeatherByCity("Hanoi")
        }
    }

    // Fetch weather for current location when the screen is first composed
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // Lấy firstForecast từ weatherState
    val firstForecast = if (weatherState is WeatherState.Success && (weatherState as WeatherState.Success).data.list.isNotEmpty()) {
        (weatherState as WeatherState.Success).data.list[0]
    } else {
        null
    }

    // Lấy weatherIcon từ firstForecast (nếu có)
    val weatherIcon = getWeatherIcon(firstForecast)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(Color(0xFF87CEEB), Color(0xFF2B4485))
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .background(Color.White)
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            BackgroundSwitch()
        }

        Card(
            modifier = Modifier
                .width(355.dp)
                .height(170.dp)
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF3B82F6))
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        if (weatherDetails.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(64.dp),
                                color = Color.White
                            )
                        } else {
                            Image(
                                painter = painterResource(id = weatherIcon),
                                contentDescription = "Weather Icon",
                                modifier = Modifier.size(64.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = weatherDetails.cityName,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = weatherDetails.date,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = weatherDetails.temperature,
                        color = Color.White,
                        fontSize = if (weatherDetails.temperature == "Updating...") 24.sp else 48.sp, // Giảm kích thước chữ khi đang cập nhật
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .alpha(if (weatherDetails.temperature == "Updating...") 0.7f else 1f) // Làm mờ khi đang cập nhật
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .width(355.dp)
                .height(210.dp)
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.LightGray)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "AIR QUALITY",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WeatherDetailItem(icon = "🌬️", label = "WIND", value = "7km/h")
                    WeatherDetailItem(icon = "💧", label = "DIOXIDE", value = "0.9")
                    WeatherDetailItem(icon = "☔", label = "RAIN", value = "25%")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WeatherDetailItem(icon = "🌅", label = "02", value = "3")
                    WeatherDetailItem(icon = "🌡️", label = "TEMP", value = "26.5")
                    WeatherDetailItem(icon = "☀️", label = "SUN", value = "50")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .height(230.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.LightGray)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ForecastItem(day = "YES", date = "01/03", temp = "27°C", icon = "☁️", backgroundColor = Color(0xFFF87171))
                ForecastItem(day = "TOD", date = "02/03", temp = "29°C", icon = "🌥️", backgroundColor = Color(0xFF60A5FA))
                ForecastItem(day = "TMR", date = "03/03", temp = "30°C", icon = "🌧️", backgroundColor = Color(0xFF4ADE80))
                ForecastItem(day = "TMR", date = "04/03", temp = "26°C", icon = "🌧️", backgroundColor = Color(0xFF4ADE80))
            }
        }
    }
}

@Composable
fun WeatherDetailItem(icon: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 24.sp)
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
        Text(text = value, color = Color.Black, fontSize = 16.sp)
    }
}

@Composable
fun ForecastItem(day: String, date: String, temp: String, icon: String, backgroundColor: Color) {
    Card(
        modifier = Modifier
            .width(60.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(22.dp)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(top = 20.dp, start = 13.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = day, color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = date, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = temp, color = Color.White, fontSize = 16.sp)
        }
    }
}

@Composable
fun CustomSwitchExample() {
    var checked by remember { mutableStateOf(false) }
    Switch(
        checked = checked,
        onCheckedChange = { checked = it }
    )
}

@Composable
fun WeatherScreenKT(viewModel: WeatherViewModel = viewModel()) {
    MainScreen(viewModel = viewModel)
}