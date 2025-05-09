package com.mi3mien.weatherapp.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mi3mien.weatherapp.WeatherResponse
import com.mi3mien.weatherapp.WeatherState
import com.mi3mien.weatherapp.WeatherViewModel
import com.mi3mien.weatherapp.model.WeatherModel
import com.mi3mien.weatherapp.sourceSans3
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import com.mi3mien.weatherapp.R

// Ánh xạ giữa tên chuẩn hóa và tên tiếng Việt
val cityNameMapping = mapOf(
    "ha noi" to "Hà Nội",
    "ho chi minh" to "Hồ Chí Minh",
    "can tho" to "Cần Thơ",
    "hue" to "Huế",
    "vinh" to "Nghệ An",
    "hai phong" to "Hải Phòng",
    "thanh hoa" to "Thanh Hóa",
    "new york" to "New York" // Thêm ánh xạ cho New York
)

// Ánh xạ cho các trường hợp đặc biệt (tỉnh/thành phố không được API hỗ trợ trực tiếp)
val specialCityMapping = mapOf(
    "nghe an" to "vinh",
    "ho chi minh" to "Ho Chi Minh City",
    "hai phong" to "Hai Phong",
    "thanh hoa" to "Thanh Hoa",
    "new york" to "New York" // Thêm ánh xạ cho New York
)

@Composable
fun WeatherScreen(
    onNavigateToHomeScreen: () -> Unit,
    viewModel: WeatherViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    navController: NavController = rememberNavController()
) {
    val cityWeatherStates by viewModel.cityWeatherStates.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val defaultCities = listOf("Hà Nội", "Hồ Chí Minh", "Cần Thơ", "Huế").map { viewModel.normalizeCityName(it) }
    var searchResultCity by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Đảm bảo tải tất cả thành phố mặc định khi khởi động
    LaunchedEffect(Unit) {
        defaultCities.forEachIndexed { index, city ->
            delay(index * 500L)
            val apiCityName = specialCityMapping[city] ?: city
            viewModel.fetchWeatherByCity(apiCityName)
            Log.d("WeatherScreen", "Fetching weather for: $apiCityName")
        }
    }

    // Xử lý tìm kiếm
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            val normalizedQuery = viewModel.normalizeCityName(searchQuery)
            val finalQuery = normalizedQuery
            val apiCityName = specialCityMapping[finalQuery] ?: finalQuery
            Log.d("WeatherScreen", "Searching for: $apiCityName (from $searchQuery)")
            viewModel.fetchWeatherByCity(apiCityName)
            searchResultCity = apiCityName.lowercase() // Chuẩn hóa về chữ thường
        } else {
            searchResultCity = null
        }
    }

    // Ghi log trạng thái để debug
    LaunchedEffect(cityWeatherStates) {
        Log.d("WeatherScreen", "cityWeatherStates: $cityWeatherStates")
        val hoChiMinhState = cityWeatherStates["ho chi minh city"]
        if (hoChiMinhState != null) {
            Log.d("WeatherScreen", "Ho Chi Minh City state: $hoChiMinhState")
        }
    }

    // Xử lý lỗi và hiển thị snackbar
    LaunchedEffect(cityWeatherStates) {
        val citiesToCheck = if (searchResultCity != null) listOf(searchResultCity!!) else defaultCities.map { specialCityMapping[it] ?: it }
        citiesToCheck.forEach { city ->
            val state = cityWeatherStates[city.lowercase()]
            if (state is WeatherState.Error) {
                val displayCityName = if (city == searchResultCity) searchQuery else cityNameMapping[city.lowercase()] ?: city
                snackbarHostState.showSnackbar("Failed to load weather for $displayCityName: ${state.message}")
            }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigateBack() }) {
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
                    fontFamily = sourceSans3,
                    modifier = Modifier.padding(start = 28.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onNavigateToHomeScreen) {
                    Icon(Icons.Default.MoreHoriz, contentDescription = "Menu", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SearchBar(searchQuery = searchQuery, onQueryChange = { searchQuery = it })

            Spacer(modifier = Modifier.height(16.dp))

            if (cityWeatherStates.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                val citiesToShow = if (searchResultCity != null && cityWeatherStates[searchResultCity]?.let { it is WeatherState.Success } == true) {
                    listOf(searchResultCity!!)
                } else {
                    defaultCities.map { specialCityMapping[it] ?: it }
                }

                val filteredData = citiesToShow.mapNotNull { city ->
                    val state = cityWeatherStates[city.lowercase()] ?: return@mapNotNull null
                    if (state is WeatherState.Success) {
                        city.lowercase() to state
                    } else {
                        if (state is WeatherState.Error) {
                            Log.e("WeatherScreen", "Error for city $city: ${state.message}")
                        }
                        null
                    }
                }

                LaunchedEffect(filteredData) {
                    Log.d("WeatherScreen", "filteredData: $filteredData")
                }

                if (filteredData.isEmpty()) {
                    Text(
                        text = "No results found",
                        color = Color.White,
                        fontFamily = sourceSans3,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
                    ) {
                        items(filteredData) { (city, state) ->
                            val weather = state.data
                            WeatherCard(
                                weather = WeatherModel(
                                    city = cityNameMapping[city] ?: weather.name,
                                    temperature = "${weather.main.temp.toInt()}°C",
                                    highTemp = weather.main.tempMax.roundToInt().toString(),
                                    lowTemp = weather.main.tempMin.roundToInt().toString(),
                                    icon = getWeatherIcon(weather)
                                )
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun SearchBar(searchQuery: String, onQueryChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFF8A65), Color(0xFFE57373))
                )
            )
            .padding(horizontal = 12.dp)
    ) {
        TextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
            placeholder = {
                Text(
                    text = "SEARCH FOR A CITY",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = sourceSans3
                )
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        )
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
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentWidth()
                    ) {
                        Text(
                            text = weather.temperature,
                            fontSize = 32.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontFamily = sourceSans3
                        )
                        Text(
                            text = "H: ${weather.highTemp}° L: ${weather.lowTemp}°",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontFamily = sourceSans3
                        )
                        Text(
                            text = weather.city,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = sourceSans3,
                            color = Color.White,
                            maxLines = 1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.Start)
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

fun getWeatherIcon(weather: WeatherResponse?): Int {
    val iconCode = weather?.weather?.firstOrNull()?.icon ?: "01d"
    return when (iconCode) {
        "01d", "01n" -> R.drawable.sunny
        "02d", "02n" -> R.drawable.clouds
        "03d", "03n", "04d", "04n" -> R.drawable.clouds
        "09d", "09n", "10d", "10n" -> R.drawable.rain
        "11d", "11n" -> R.drawable.storm
        else -> R.drawable.clouds
    }
}