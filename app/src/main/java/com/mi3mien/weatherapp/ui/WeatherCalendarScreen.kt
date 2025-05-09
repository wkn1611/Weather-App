package com.mi3mien.weatherapp.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import com.mi3mien.weatherapp.DailyWeatherInfo
import com.mi3mien.weatherapp.WeatherState
import com.mi3mien.weatherapp.WeatherViewModel
import com.mi3mien.weatherapp.sourceSans3
import com.mi3mien.weatherapp.R

@Composable
fun WeatherCalendarScreen(
    onNavigateBack: () -> Unit,
    viewModel: WeatherViewModel = viewModel()
) {
    val context = LocalContext.current
    val weatherState by viewModel.weatherState.collectAsState()
    val forecastState by viewModel.forecastState.collectAsState()
    val dailyWeatherData by viewModel.dailyWeatherData.collectAsState()

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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Calendar",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = sourceSans3,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        ForecastCard(weatherState, dailyWeatherData)

        Spacer(modifier = Modifier.height(16.dp))

        CalendarCard(dailyWeatherData)
    }
}

@Composable
fun ForecastCard(weatherState: WeatherState, dailyWeatherData: Map<String, DailyWeatherInfo>) {
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
            // Display current location
            val locationName = when (weatherState) {
                is WeatherState.Success -> weatherState.data.name
                is WeatherState.Error -> "Error: ${weatherState.message}"
                is WeatherState.Loading -> "Loading..."
            }
            Text(
                text = locationName,
                color = Color.Black,
                fontSize = 16.sp,
                fontFamily = sourceSans3
            )

            // Display current date
            val currentDate = Calendar.getInstance().time
            val dateFormat = SimpleDateFormat("MMMM, d, yyyy", Locale.getDefault())
            Text(
                text = dateFormat.format(currentDate),
                color = Color.Gray,
                fontSize = 14.sp,
                fontFamily = sourceSans3
            )

            Spacer(modifier = Modifier.height(4.dp)) // Giảm từ 8.dp xuống 4.dp

            // Hiển thị thời tiết cho 7 ngày từ Thứ Hai đến Chủ Nhật
            val calendar = Calendar.getInstance()
            // Tìm ngày Thứ Hai của tuần hiện tại
            while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                calendar.add(Calendar.DAY_OF_MONTH, -1)
            }

            // Tạo danh sách 7 ngày từ Thứ Hai đến Chủ Nhật
            val daysOfWeek = (0 until 7).map { offset ->
                val dayCalendar = calendar.clone() as Calendar
                dayCalendar.add(Calendar.DAY_OF_MONTH, offset)
                dayCalendar
            }

            val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
            val dateKeyFormat = SimpleDateFormat("yyyy-M-d", Locale.getDefault())

            LazyColumn {
                items(daysOfWeek) { day ->
                    val dateKey = dateKeyFormat.format(day.time)
                    val weatherInfo = dailyWeatherData[dateKey] ?: DailyWeatherInfo(
                        condition = "Unknown",
                        temp = 0,
                        icon = R.drawable.sunny
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp), // Giảm từ 6.dp xuống 4.dp
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = weatherInfo.icon),
                            contentDescription = weatherInfo.condition,
                            modifier = Modifier.size(20.dp) // Giảm từ 24.dp xuống 20.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = dayFormat.format(day.time),
                            color = Color.Black,
                            fontSize = 14.sp, // Giảm từ 16.sp xuống 14.sp
                            fontFamily = sourceSans3,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${weatherInfo.temp}°C",
                            color = Color.Gray,
                            fontSize = 12.sp, // Giảm từ 14.sp xuống 12.sp
                            fontFamily = sourceSans3
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun CalendarCard(dailyWeatherData: Map<String, DailyWeatherInfo>) {
    val currentDate = remember { mutableStateOf(Calendar.getInstance()) }
    val today = Calendar.getInstance()
    val selectedDate = remember {
        mutableStateOf(today.apply { set(Calendar.DAY_OF_MONTH, get(Calendar.DAY_OF_MONTH)) })
    }

    // Tính toán month và year trực tiếp từ currentDate để đảm bảo cập nhật ngay
    val month by derivedStateOf { currentDate.value.get(Calendar.MONTH) + 1 }
    val year by derivedStateOf { currentDate.value.get(Calendar.YEAR) }

    // Hàm di chuyển tháng và cập nhật selectedDate
    fun moveToNextMonth() {
        currentDate.value.add(Calendar.MONTH, 1)
        // Đặt lại selectedDate về ngày 1 của tháng mới
        val newSelectedDate = currentDate.value.clone() as Calendar
        newSelectedDate.set(Calendar.DAY_OF_MONTH, 1)
        selectedDate.value = newSelectedDate
    }

    fun moveToPreviousMonth() {
        currentDate.value.add(Calendar.MONTH, -1)
        // Đặt lại selectedDate về ngày 1 của tháng mới
        val newSelectedDate = currentDate.value.clone() as Calendar
        newSelectedDate.set(Calendar.DAY_OF_MONTH, 1)
        selectedDate.value = newSelectedDate
    }

    // Định dạng key cho ngày được chọn
    val dateFormat = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
    val selectedDateKey = dateFormat.format(selectedDate.value.time)

    // Lấy thông tin thời tiết cho ngày được chọn
    val selectedWeather = dailyWeatherData[selectedDateKey] ?: DailyWeatherInfo(
        condition = "Data not available",
        temp = 0,
        icon = R.drawable.sunny
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .height(460.dp) // Tăng từ 440.dp lên 460.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = selectedWeather.condition,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = sourceSans3,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${selectedWeather.temp}°C",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = sourceSans3
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.width(IntrinsicSize.Min)
                ) {
                    Text(
                        text = currentDate.value.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = sourceSans3,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = year.toString(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = sourceSans3,
                        textAlign = TextAlign.End
                    )
                }
            }

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
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous"
                    )
                }
                IconButton(
                    onClick = { moveToNextMonth() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            daysOfWeek.forEach {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = sourceSans3,
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        weeks.forEach { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { day ->
                    if (day == 0) {
                        Spacer(modifier = Modifier.size(32.dp))
                    } else {
                        val isSelected = day == selectedDay
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color.Black else Color.Transparent)
                                .clickable { onDaySelected(day) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = sourceSans3,
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