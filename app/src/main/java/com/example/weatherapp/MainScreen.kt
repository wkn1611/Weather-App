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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
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
    val isLoading: Boolean = false,
    val windSpeed: Double = 0.0,
    val visibility: Int = 0,
    val sunrise: Int = 0,
    val sunset: Int = 0,
    val tempMin: Double = 0.0,
    val tempMax: Double = 0.0
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
                    temperature = "${firstForecast.main.temp.toInt()}°F",
                    humidity = firstForecast.main.humidity,
                    weatherType = firstForecast.weather[0].main,
                    date = formattedDate,
                    isLoading = false,
                    windSpeed = firstForecast.wind.speed, // Lấy tốc độ gió
                    visibility = firstForecast.visibility, // Lấy tầm nhìn
                    sunrise = weatherData.city.sunrise,   // Lấy giờ mặt trời mọc
                    sunset = weatherData.city.sunset,     // Lấy giờ mặt trời lặn
                    tempMin = firstForecast.main.temp_min, // Lấy nhiệt độ thấp nhất
                    tempMax = firstForecast.main.temp_max  // Lấy nhiệt độ cao nhất
                )
            } else {
                WeatherDetails() // Trả về mặc định nếu không có dữ liệu
            }
        }
        is WeatherState.Error -> {
            WeatherDetails() // Trả về mặc định nếu có lỗi
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

    val activeGradient = Brush.linearGradient(
        colors = listOf(Color(0xff0A2876), Color(0xffAFCAFF)),
        start = androidx.compose.ui.geometry.Offset(4f, 4f),
        end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    // Gradient khi Switch bật
    val defaultGradient = Brush.linearGradient(
        colors = listOf(Color(0xff1CD9C3), Color(0xffEDD685)),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    var isSwitchOn by remember { mutableStateOf(false) }
    val currentGradient = if (isSwitchOn) activeGradient else defaultGradient

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

    // Lấy danh sách dự báo: ngày hiện tại + 4 ngày tiếp theo
    val forecastList = if (weatherState is WeatherState.Success) {
        (weatherState as WeatherState.Success).data.list
            .groupBy {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.dt * 1000L))
            }
            .map { it.value.first() } // Lấy một bản ghi mỗi ngày
            .take(4) // Lấy 5 ngày (ngày hiện tại + 4 ngày sau)
    } else emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(currentGradient),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                //.background(Color.White)
                .padding(20.dp)
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
                containerColor = Color.Transparent // Đặt màu trong suốt để gradient hiển thị
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
                    Column {
                        Box{
                        if (weatherDetails.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(200.dp),
                                color = Color.White
                            )
                        } else {
                            Image(
                                painter = painterResource(id = weatherIcon),
                                contentDescription = "Weather Icon",
                                modifier = Modifier.size(150.dp).offset(y = (-30).dp)
                            )
                        }
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
                .height(270.dp)
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xffffffff))
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .height(250.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "AIR QUALITY",
                    color = Color.Black,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WeatherDetailItem(image = painterResource(id = R.drawable.wind), label = "WIND", "${weatherDetails.windSpeed} m/s")
                    WeatherDetailItem(image = painterResource(id = R.drawable.dioxide), label = "HUMIDITY" ,"${weatherDetails.humidity}%")
                    WeatherDetailItem(image = painterResource(id = R.drawable.sunrise11), label = "SUNRISE", value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(weatherDetails.sunrise * 1000L))
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WeatherDetailItem(image = painterResource(id = R.drawable.hightemp), label = "TEMP MAX", "${weatherDetails.tempMax}°C")
                    WeatherDetailItem(image = painterResource(id = R.drawable.lowtemp), label = "TEMP MIN", "${weatherDetails.tempMin}°C")
                    WeatherDetailItem(image = painterResource(id = R.drawable.sunset), label = "SUNSET", value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(weatherDetails.sunset * 1000L)))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .height(250.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xffffffff))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 25.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (forecastList.isEmpty()) {
                    repeat(5) {
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
                            day = dayFormat.format(Date(forecast.dt * 1000L)),
                            date = dateFormat.format(Date(forecast.dt * 1000L)),
                            temp = "${forecast.main.temp.toInt()}°C",
                            icon = painterResource(id = getWeatherIcon(forecast))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherDetailItem(image: Painter, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painter = image, contentDescription = label, modifier = Modifier.size(46.dp))
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
        Text(text = value, color = Color.Black, fontSize = 16.sp)
    }
}

@Composable
fun ForecastItem(day: String, date: String, temp: String, icon: Painter) {
    Card(
        modifier = Modifier
            .width(60.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xffFB834F), Color(0xffFFE100))
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent // Đặt màu trong suốt để gradient hiển thị
        ),
        elevation = CardDefaults.cardElevation(0.dp)
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
            Image(painter = icon, contentDescription = "Forecast Icon", modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = temp, color = Color.White, fontSize = 16.sp)
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

