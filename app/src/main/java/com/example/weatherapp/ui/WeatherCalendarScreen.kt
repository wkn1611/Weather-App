package com.example.weatherapp.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.weatherapp.R
import java.util.*
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherapp.WeatherViewModel
import android.Manifest

@Composable
fun WeatherCalendarScreen(navController: NavController, viewModel: WeatherViewModel = viewModel()) {
    val context = LocalContext.current
    val weatherState by viewModel.weatherState.collectAsState()

    // State for location permission
    var isLocationPermissionGranted by remember { mutableStateOf(false) }
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

    // Request location permission when screen is composed
    LaunchedEffect(Unit) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF3A7BD5), Color(0xFF00D2FF))))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with "Calendar" title centered
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack, // Using default back arrow icon
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Calender",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp)) // To keep balance with the back button
        }

        Spacer(modifier = Modifier.height(16.dp))

        ForecastCard()

        Spacer(modifier = Modifier.height(16.dp))

        CalendarCard()
    }
}

@Composable
fun ForecastCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFE0F7FA), Color(0xFFB2EBF2))
                )
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Partly Cloudy", color = Color.Black, fontSize = 16.sp)
            Text(text = "August, 10th 2024", color = Color.Gray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(8.dp))

            val forecastData = listOf(
                "Monday" to R.drawable.storm,
                "Tuesday" to R.drawable.rain,
                "Wednesday" to R.drawable.rain,
                "Thursday" to R.drawable.clouds,
                "Friday" to R.drawable.storm,
                "Saturday" to R.drawable.sunny,
                "Sunday" to R.drawable.clouds
            )

            LazyColumn {
                items(forecastData) { (day, icon) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = icon),
                            contentDescription = day,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = day,
                            color = Color.Black,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(text = "68° / 22°", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarCard() {
    // State for current month, year, and selected day
    val currentDate = remember { mutableStateOf(Calendar.getInstance()) }
    val selectedDate = remember { mutableStateOf(Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, get(Calendar.DAY_OF_MONTH)) }) }
    val calendar = currentDate.value

    // Get the month and year
    val month = calendar.get(Calendar.MONTH) + 1
    val year = calendar.get(Calendar.YEAR)

    // Handle previous and next buttons
    fun moveToNextMonth() {
        calendar.add(Calendar.MONTH, 1)
        currentDate.value = calendar.clone() as Calendar
    }

    fun moveToPreviousMonth() {
        calendar.add(Calendar.MONTH, -1)
        currentDate.value = calendar.clone() as Calendar
    }

    // Dữ liệu thời tiết mẫu cho các ngày
    data class WeatherInfo(val condition: String, val temp: Int, val icon: Int)

    val weatherData = remember {
        mapOf(
            // Dữ liệu mẫu: bạn có thể thay bằng dữ liệu API
            "2025-04-15" to WeatherInfo("Sunny", 27, R.drawable.sunny),
            "2025-04-16" to WeatherInfo("Cloudy", 24, R.drawable.clouds),
            "2025-04-17" to WeatherInfo("Rain", 22, R.drawable.rain),
            "2025-04-18" to WeatherInfo("Storm", 20, R.drawable.storm),
            "2025-04-19" to WeatherInfo("Sunny", 26, R.drawable.sunny),
            // Thêm các ngày khác nếu cần
            "2025-04-20" to WeatherInfo("Cloudy", 25, R.drawable.clouds)
        )
    }

    // Lấy thông tin thời tiết cho ngày được chọn
    val selectedWeather = weatherData[
        "${selectedDate.value.get(Calendar.YEAR)}-${selectedDate.value.get(Calendar.MONTH) + 1}-${selectedDate.value.get(Calendar.DAY_OF_MONTH)}"
    ] ?: WeatherInfo("Unknown", 0, R.drawable.sunny) // Mặc định nếu không có dữ liệu

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Weather Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = selectedWeather.icon),
                    contentDescription = "Weather Icon",
                    modifier = Modifier.size(60.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = selectedWeather.condition,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${selectedWeather.temp}°C",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = year.toString(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    )
                }
            }

            // Navigation arrows under "Month Year"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { moveToPreviousMonth() },
                    modifier = Modifier.size(32.dp).padding(end = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous"
                    )
                }
                IconButton(
                    onClick = { moveToNextMonth() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = "Next"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            CalendarView(
                month = month,
                year = year,
                selectedDay = selectedDate.value.get(Calendar.DAY_OF_MONTH),
                onDaySelected = { day ->
                    val newDate = Calendar.getInstance().apply {
                        set(year, month - 1, day)
                    }
                    selectedDate.value = newDate
                }
            )
        }
    }
}

@Composable
fun CalendarView(
    month: Int,
    year: Int,
    selectedDay: Int,
    onDaySelected: (Int) -> Unit
) {
    val daysOfWeek = listOf("MO", "TU", "WE", "TH", "FR", "SA", "SU")
    val daysInMonth = getDaysInMonth(month, year)
    val startDayOffset = getStartDayOffset(month, year)
    val days = List(startDayOffset) { 0 } + (1..daysInMonth).toList()
    val filledDays = days + List((7 - days.size % 7) % 7) { 0 }
    val weeks = filledDays.chunked(7)

    Column {
        // Header: Days of Week
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            daysOfWeek.forEach {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(40.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar Days
        weeks.forEach { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { day ->
                    if (day == 0) {
                        Spacer(modifier = Modifier.size(40.dp))
                    } else {
                        val isSelected = day == selectedDay
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color.Black else Color.Transparent)
                                .clickable { onDaySelected(day) }, // Thêm khả năng nhấn
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = if (isSelected) Color.White else Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getDaysInMonth(month: Int, year: Int): Int {
    val calendar = Calendar.getInstance()
    calendar.set(year, month - 1, 1)
    return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}

fun getStartDayOffset(month: Int, year: Int): Int {
    val calendar = Calendar.getInstance()
    calendar.set(year, month - 1, 1)
    return (calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
}
@Preview(showBackground = true)
@Composable
fun PreviewWeatherCalendarScreen() {
    WeatherCalendarScreen(navController = rememberNavController())
}
