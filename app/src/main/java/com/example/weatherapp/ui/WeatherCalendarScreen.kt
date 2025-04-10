package com.example.weatherapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf

@Composable
fun WeatherCalendarScreen(navController: NavController) {
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
    // State for current month and year
    val currentDate = remember { mutableStateOf(Calendar.getInstance()) }
    val calendar = currentDate.value

    // Get the month and year
    val month = calendar.get(Calendar.MONTH) + 1 // MONTH is 0-indexed
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
                    painter = painterResource(id = R.drawable.clouds),
                    contentDescription = "Weather Icon",
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "Cloudy", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "27° C", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
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

            // Navigation arrows under "August 2025"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { moveToPreviousMonth()},
                    modifier = Modifier.size(32.dp).padding(end = 2.dp) // nhỏ lại
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous"
                    )
                }
                IconButton(
                    onClick = { moveToNextMonth()},
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = "Next"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            // Update calendar view after month change
            CalendarView(month = month, year = year)
        }
    }
}

@Composable
fun CalendarView(month: Int, year: Int) {
    val daysOfWeek = listOf("MO", "TU", "WE", "TH", "FR", "SA", "SU")
    val daysInMonth = getDaysInMonth(month, year)
    val startDayOffset = getStartDayOffset(month, year)
    val days = List(startDayOffset) { 0 } + (1..daysInMonth).toList()
    val filledDays = days + List((7 - days.size % 7) % 7) { 0 } // Bù cho đủ hàng cuối
    val weeks = filledDays.chunked(7)
    // Get current day
    val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
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
                        val isToday = day == currentDay
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(4.dp)) // Changed to square shape with rounded corners
                                .background(if (isToday) Color.Black else Color.Transparent), // Black background for today
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = if (isToday) Color.White else Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
// Get number of days in a specific month
fun getDaysInMonth(month: Int, year: Int): Int {
    val calendar = Calendar.getInstance()
    calendar.set(year, month - 1, 1) // Month is 0-indexed in Calendar
    return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}

// Get the starting day of the week (offset) for a specific month
fun getStartDayOffset(month: Int, year: Int): Int {
    val calendar = Calendar.getInstance()
    calendar.set(year, month - 1, 1)
    return calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY // Offset based on Monday start
}
@Preview(showBackground = true)
@Composable
fun PreviewWeatherCalendarScreen() {
    WeatherCalendarScreen(navController = rememberNavController())
}
