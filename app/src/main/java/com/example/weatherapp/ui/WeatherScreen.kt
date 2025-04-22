package com.example.weatherapp.ui

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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.weatherapp.R
import com.example.weatherapp.WeatherResponse
import com.example.weatherapp.WeatherState
import com.example.weatherapp.WeatherViewModel
import com.example.weatherapp.model.WeatherModel
import com.example.weatherapp.normalizeCityName
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

// Font tùy chỉnh
val sourceSans3 = FontFamily(
    Font(R.font.ss3_regu, FontWeight.Normal),
    Font(R.font.ss3_bold, FontWeight.Bold)
)

// Ánh xạ giữa tên chuẩn hóa và tên tiếng Việt
val cityNameMapping = mapOf(
    "Hanoi" to "Hà Nội",
    "Ho Chi Minh" to "Hồ Chí Minh",
    "Can Tho" to "Cần Thơ", // Thêm Cần Thơ
    "Hue" to "Huế",
    "Vinh" to "Nghệ An"
)

// Ánh xạ cho các trường hợp đặc biệt (tỉnh/thành phố không được API hỗ trợ trực tiếp)
val specialCityMapping = mapOf(
    "Nghe An" to "Vinh"
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
    // Thay "Đà Nẵng" bằng "Cần Thơ"
    val defaultCities = listOf("Hà Nội", "Hồ Chí Minh", "Cần Thơ", "Huế").map { normalizeCityName(it) }
    var searchResultCity by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        defaultCities.forEachIndexed { index, city ->
            delay(index * 500L)
            val apiCityName = specialCityMapping[city] ?: city
            viewModel.fetchWeatherByCity(apiCityName)
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            val normalizedQuery = normalizeCityName(searchQuery)
            val apiCityName = specialCityMapping[normalizedQuery] ?: normalizedQuery
            viewModel.fetchWeatherByCity(apiCityName)
            searchResultCity = apiCityName
        } else {
            searchResultCity = null
        }
    }

    LaunchedEffect(cityWeatherStates) {
        Log.d("WeatherScreen", "cityWeatherStates: $cityWeatherStates")
    }

    LaunchedEffect(cityWeatherStates) {
        val citiesToCheck = if (searchResultCity != null) listOf(searchResultCity!!) else defaultCities
        citiesToCheck.forEach { city ->
            val state = cityWeatherStates[city]
            if (state is WeatherState.Error) {
                val displayCityName = if (city == searchResultCity) searchQuery else cityNameMapping[city] ?: city
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
                    modifier = Modifier.padding(start = 42.dp)
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
                    defaultCities
                }

                val filteredData = citiesToShow.mapNotNull { city ->
                    val state = cityWeatherStates[city] ?: return@mapNotNull null
                    if (state is WeatherState.Success) {
                        city to state
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
                                    city = cityNameMapping[city] ?: if (city == searchResultCity) searchQuery else city,
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
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
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