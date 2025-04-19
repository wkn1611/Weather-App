package com.example.weatherapp.ui

import android.R.attr.onClick
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.weatherapp.R
import com.example.weatherapp.model.WeatherModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.weatherapp.WeatherState
import com.example.weatherapp.WeatherViewModel
import com.example.weatherapp.getWeatherDetails
import com.example.weatherapp.getWeatherIcon

// Font tùy chỉnh
val sourceSans3 = FontFamily(
    Font(R.font.ss3_regu, FontWeight.Normal),
    Font(R.font.ss3_bold, FontWeight.Bold)
)

@Composable
fun WeatherScreen(onNavigateToHomeScreen: () -> Unit, viewModel: WeatherViewModel = viewModel()) {
    val weatherState by viewModel.weatherState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val defaultCities = listOf("Hanoi", "Ho Chi Minh", "Da Nang", "Hue")
    val weatherStates = remember { mutableStateListOf<WeatherState>() }

    // Gọi API cho 4 thành phố mặc định khi màn hình được tạo
    LaunchedEffect(Unit) {
        weatherStates.clear()
        defaultCities.forEach { city ->
            viewModel.fetchWeatherByCity(city)
            weatherStates.add(WeatherState.Loading) // Thêm trạng thái Loading ban đầu
        }
    }

    // Cập nhật weatherStates khi weatherState thay đổi
    LaunchedEffect(weatherState) {
        val currentState = weatherState // Biến tạm để tránh lỗi smart cast
        val city = when (currentState) {
            is WeatherState.Success -> currentState.data.city.name
            is WeatherState.Error -> {
                val loadingIndex = weatherStates.indexOfFirst { it is WeatherState.Loading }
                if (loadingIndex != -1) defaultCities[loadingIndex] else return@LaunchedEffect
            }
            else -> return@LaunchedEffect
        }
        val index = defaultCities.indexOf(city)
        if (index != -1 && index < weatherStates.size) {
            weatherStates[index] = currentState
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF3A7BD5), Color(0xFF00D2FF))
                )
            )
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Thanh tiêu đề
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "Weather",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 42.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onNavigateToHomeScreen) {
                    Icon(Icons.Default.MoreHoriz, contentDescription = "Menu", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Thanh tìm kiếm
            SearchBar(searchQuery = searchQuery, onQueryChange = { searchQuery = it })

            Spacer(modifier = Modifier.height(16.dp))

            // Hiển thị danh sách thời tiết
            when {
                weatherStates.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                else -> {
                    val filteredData = weatherStates.mapIndexed { index, state ->
                        if (state is WeatherState.Success && !state.data.city.name.contains(searchQuery, ignoreCase = true)) {
                            WeatherState.Error("Filtered out") // Tạm thời đánh dấu để bỏ qua
                        } else {
                            state
                        }
                    }.filter { it !is WeatherState.Error || it.message != "Filtered out" }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
                    ) {
                        items(filteredData) { state ->
                            when (state) {
                                is WeatherState.Success -> {
                                    val details = getWeatherDetails(state) // Từ MainScreen.kt
                                    WeatherCard(
                                        weather = WeatherModel(
                                            city = details.cityName,
                                            temperature = details.temperature,
                                            highTemp = details.tempMax.toString(),
                                            lowTemp = details.tempMin.toString(),
                                            icon = getWeatherIcon(state.data.list.firstOrNull()) // Từ MainScreen.kt
                                        )
                                    )
                                }
                                is WeatherState.Error -> {
                                    Text(
                                        text = state.message,
                                        color = Color.White,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                }
                                is WeatherState.Loading -> {
                                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(searchQuery: String, onQueryChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(40.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFF8A65), Color(0xFFE57373))
                )
            )
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = "SEARCH FOR A CITY",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = sourceSans3
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )
        }
    }
}

@Composable
fun WeatherCard(weather: WeatherModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(160.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 28.dp, bottomEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.weather_background),
                    contentDescription = "Weather Background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = weather.temperature,
                            fontSize = 32.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "H: ${weather.highTemp}° L: ${weather.lowTemp}°",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = weather.city,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .align(Alignment.CenterVertically)
                            .offset(x = 40.dp)
                    ) {
                        Image(
                            painter = painterResource(id = weather.icon),
                            contentDescription = "Weather Icon",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWeatherScreen() {
    val navController = rememberNavController()
    val viewModel: WeatherViewModel = viewModel() // Thêm dòng này
    WeatherScreen(navController = navController, viewModel = viewModel) //Cập nhật dòng này
}