package com.example.weatherapp

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherapp.ui.sourceSans3
import java.text.SimpleDateFormat
import java.util.*

// Data class chứa thông số thời tiết
data class WeatherDetails(
    val cityName: String = "N/A",
    val temperature: String = "Updating...",
    val humidity: Int = 0,
    val weatherType: String = "Unknown",
    val date: String = "N/A",
    val isLoading: Boolean = false,
    val windSpeed: Double = 0.0,
    val visibility: Int = 0,
    val sunrise: Long = 0,
    val sunset: Long = 0,
    val tempMin: Double = 0.0,
    val tempMax: Double = 0.0
)

// Hàm logic lấy thông số thời tiết từ WeatherResponse
fun getWeatherDetails(weatherState: WeatherState): WeatherDetails {
    return when (weatherState) {
        is WeatherState.Loading -> {
            WeatherDetails(isLoading = true)
        }
        is WeatherState.Success -> {
            val weatherData = weatherState.data
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = Date(weatherData.dt * 1000L)
            val displayDateFormat = SimpleDateFormat("EEE d MMM", Locale.getDefault())
            val formattedDate = displayDateFormat.format(date)

            WeatherDetails(
                cityName = weatherData.name,
                temperature = "${weatherData.main.temp.toInt()}°C",
                humidity = weatherData.main.humidity,
                weatherType = weatherData.weather.firstOrNull()?.main ?: "Unknown",
                date = formattedDate,
                isLoading = false,
                windSpeed = weatherData.wind.speed,
                visibility = weatherData.visibility,
                sunrise = weatherData.sys.sunrise,
                sunset = weatherData.sys.sunset,
                tempMin = weatherData.main.tempMin,
                tempMax = weatherData.main.tempMax
            )
        }
        is WeatherState.Error -> {
            WeatherDetails()
        }
    }
}

// Hàm ánh xạ loại thời tiết với icon từ WeatherResponse
fun getWeatherIcon(weather: WeatherResponse?): Int {
    val weatherType = weather?.weather?.firstOrNull()?.main ?: "Unknown"
    return when (weatherType) {
        "Clear" -> R.drawable.sunny
        "Clouds" -> R.drawable.clouds
        "Rain" -> R.drawable.rain
        "Thunderstorm" -> R.drawable.storm
        else -> R.drawable.sunny
    }
}

@Composable
fun MainScreen(viewModel: WeatherViewModel = viewModel(), onNavigateToWeatherScreen: () -> Unit) {
    val weatherState by viewModel.weatherState.collectAsState()
    val forecastState by viewModel.forecastState.collectAsState()
    val context = LocalContext.current
    val weatherDetails = getWeatherDetails(weatherState)

    val activeGradient = Brush.linearGradient(
        colors = listOf(Color(0xff0A2876), Color(0xffAFCAFF)),
        start = androidx.compose.ui.geometry.Offset(4f, 4f),
        end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    val defaultGradient = Brush.linearGradient(
        colors = listOf(Color(0xff1CD9C3), Color(0xffEDD685)),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    // Gradient tùy chỉnh cho Card chứa ForecastItem
    val forecastCardActiveGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFAFCAFF), Color(0xFF7AAFFF)),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    val forecastCardDefaultGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFFEEB2), Color(0xFFFFF7D9)),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    var isSwitchOn by remember { mutableStateOf(false) }
    val currentGradient = if (isSwitchOn) activeGradient else defaultGradient
    val forecastCardGradient = if (isSwitchOn) forecastCardActiveGradient else forecastCardDefaultGradient

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
            viewModel.fetchWeatherByCity("Hanoi")
        }
    }

    // Fetch weather for current location when the screen is first composed
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // Lấy weatherIcon từ weatherState
    val weatherIcon = getWeatherIcon(if (weatherState is WeatherState.Success) (weatherState as WeatherState.Success).data else null)

    // Lấy danh sách dự báo từ forecastState: ngày hiện tại + 3 ngày tiếp theo (tổng 4 ngày)
    val forecastList = when (forecastState) {
        is ForecastState.Success -> {
            (forecastState as ForecastState.Success).forecast.drop(1).take(4)
        }
        else -> emptyList()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(currentGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.padding(20.dp)
            ) {
                Spacer(modifier = Modifier.weight(1f))
                BackgroundSwitch(
                    isChecked = isSwitchOn,
                    onCheckedChange = { isSwitchOn = it }
                )
            }

            Card(
                modifier = Modifier
                    .width(355.dp)
                    .height(170.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(colors = listOf(Color(0xff5BEBF0), Color(0xff2468E8)))),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .height(150.dp)
                                .width(150.dp)
                        ) {
                            if (weatherDetails.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(150.dp),
                                    color = Color.White
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = weatherIcon),
                                    contentDescription = "Weather Icon",
                                    modifier = Modifier.size(170.dp)
                                )
                            }
                        }
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = weatherDetails.temperature,
                                color = Color.White,
                                fontSize = if (weatherDetails.temperature == "Updating...") 24.sp else 48.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = sourceSans3,
                                modifier = Modifier
                                    .alpha(if (weatherDetails.temperature == "Updating...") 0.7f else 1f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = weatherDetails.cityName,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = sourceSans3
                            )
                            Text(
                                text = weatherDetails.date,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                fontFamily = sourceSans3
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .width(355.dp)
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xffffffff))
            ) {
                Column(
                    modifier = Modifier
                        .padding(10.dp)
                        .height(200.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "AIR QUALITY",
                        color = Color.Black,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = sourceSans3
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        WeatherDetailItem(
                            image = painterResource(id = R.drawable.wind),
                            label = "WIND",
                            "${weatherDetails.windSpeed} m/s"
                        )
                        WeatherDetailItem(
                            image = painterResource(id = R.drawable.dioxide),
                            label = "HUMIDITY",
                            "${weatherDetails.humidity}%"
                        )
                        WeatherDetailItem(
                            image = painterResource(id = R.drawable.sunrise11),
                            label = "SUNRISE",
                            value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(weatherDetails.sunrise * 1000L))
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        WeatherDetailItem(
                            image = painterResource(id = R.drawable.hightemp),
                            label = "TEMP MAX",
                            "${weatherDetails.tempMax}°C"
                        )
                        WeatherDetailItem(
                            image = painterResource(id = R.drawable.lowtemp),
                            label = "TEMP MIN",
                            "${weatherDetails.tempMin}°C"
                        )
                        WeatherDetailItem(
                            image = painterResource(id = R.drawable.sunset),
                            label = "SUNSET",
                            value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(weatherDetails.sunset * 1000L))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .height(260.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(forecastCardGradient),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 15.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (forecastList.isEmpty()) {
                        repeat(4) {
                            ForecastItem(
                                day = "N/A",
                                date = "N/A",
                                temp = "N/A",
                                icon = painterResource(id = R.drawable.sunny)
                            )
                        }
                    } else {
                        forecastList.forEach { forecast ->
                            val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                            val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
                            ForecastItem(
                                day = dayFormat.format(forecast.date),
                                date = dateFormat.format(forecast.date),
                                temp = "${forecast.tempMax.toInt()}°C",
                                icon = painterResource(id = forecast.icon)
                            )
                        }
                    }
                }
            }
        }

        CustomBottomNavigationBar(
            modifier = Modifier
                .height(45.dp)
                .align(Alignment.BottomCenter),
            onLocationClick = {
                if (isLocationPermissionGranted) {
                    viewModel.fetchWeatherByLocation(context)
                } else {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            },
            onMenuClick = onNavigateToWeatherScreen
        )
    }
}

@Composable
fun WeatherDetailItem(image: Painter, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painter = image, contentDescription = label, modifier = Modifier.size(40.dp))
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 10.sp,
            fontFamily = sourceSans3
        )
        Text(
            text = value,
            color = Color.Black,
            fontSize = 14.sp,
            fontFamily = sourceSans3
        )
    }
}

@Composable
fun ForecastItem(day: String, date: String, temp: String, icon: Painter) {
    Card(
        modifier = Modifier
            .width(55.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xffFB834F), Color(0xffFFE100))
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day,
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = sourceSans3
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = date,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontFamily = sourceSans3
            )
            Spacer(modifier = Modifier.height(12.dp))
            Image(
                painter = icon,
                contentDescription = "Forecast Icon",
                modifier = Modifier.size(62.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = temp,
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = sourceSans3
            )
        }
    }
}

@Composable
fun BackgroundSwitch(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    CustomSwitch(
        isChecked = isChecked,
        onCheckedChange = onCheckedChange,
        width = 65.dp,
        height = 30.dp,
        thumbSize = 30.dp,
        checkedGradient = Brush.horizontalGradient(
            colors = listOf(Color(0xFF2B4485), Color(0xFFAFCAFF))
        ),
        uncheckedGradient = Brush.horizontalGradient(
            colors = listOf(Color(0xff25F9DF), Color(0xffFFEEB2))
        ),
        checkedThumbGradient = Brush.verticalGradient(
            colors = listOf(Color(0xFFC8D7FF), Color(0xFFDCF0FF))
        ),
        uncheckedThumbGradient = Brush.horizontalGradient(
            colors = listOf(Color(0xffFED060), Color(0xffFAA96F))
        )
    )
}

@Composable
fun CustomBottomNavigationBar(
    modifier: Modifier = Modifier,
    onLocationClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(Color(0xFFFFF5E1))
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onLocationClick) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = Color.Black
                )
            }
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.Black
                )
            }
        }
    }
}