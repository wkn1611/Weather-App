package com.mi3mien.weatherapp
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.text.Normalizer
import java.util.*
import java.text.SimpleDateFormat

// Ánh xạ cho các trường hợp đặc biệt (tỉnh không được API hỗ trợ trực tiếp)
val specialCityMapping = mapOf(
    "nghe an" to "vinh",
    "ho chi minh" to "Ho Chi Minh City",
    "hai phong" to "Hai Phong",
    "thanh hoa" to "Thanh Hoa"
)

class WeatherViewModel : ViewModel() {
    private companion object {
        const val API_KEY = "373d0eaeeda5364b19ff00b986d60613"
    }

    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Loading)
    val weatherState: StateFlow<WeatherState> = _weatherState

    private val _cityWeatherStates = MutableStateFlow<Map<String, WeatherState>>(emptyMap())
    val cityWeatherStates: StateFlow<Map<String, WeatherState>> = _cityWeatherStates

    private val _forecastState = MutableStateFlow<ForecastState>(ForecastState.Loading)
    val forecastState: StateFlow<ForecastState> = _forecastState

    private val _dailyWeatherData = MutableStateFlow<Map<String, DailyWeatherInfo>>(emptyMap())
    val dailyWeatherData: StateFlow<Map<String, DailyWeatherInfo>> = _dailyWeatherData

    // Hàm chuẩn hóa tên thành phố (bỏ dấu, chuyển thành chữ thường)
    fun normalizeCityName(city: String): String {
        return Normalizer.normalize(city, Normalizer.Form.NFD)
            .replace("\\p{M}".toRegex(), "")
            .lowercase()
            .trim()
    }

    private fun mapWeatherIcon(iconCode: String): Int {
        return when (iconCode) {
            "01d", "01n" -> R.drawable.sunny
            "02d", "02n" -> R.drawable.clouds
            "03d", "03n", "04d", "04n" -> R.drawable.clouds
            "09d", "09n", "10d", "10n" -> R.drawable.rain
            "11d", "11n" -> R.drawable.storm
            else -> R.drawable.clouds
        }
    }

    fun fetchWeatherByCity(city: String) {
        val normalizedCity = normalizeCityName(city)
        val apiCityName = specialCityMapping[normalizedCity] ?: normalizedCity
        Log.d("WeatherViewModel", "Fetching weather for normalized city: $apiCityName (original: $city)")

        viewModelScope.launch {
            val currentStates = _cityWeatherStates.value.toMutableMap()
            currentStates[apiCityName] = WeatherState.Loading
            _cityWeatherStates.value = currentStates
            _weatherState.value = WeatherState.Loading

            try {
                val response = RetrofitClient.weatherApiService.getWeatherByCity(
                    city = apiCityName,
                    units = "metric",
                    apiKey = API_KEY
                )
                if (response.isSuccessful) {
                    response.body()?.let {
                        Log.d("WeatherViewModel", "Success for $apiCityName: ${it.name}, Temp: ${it.main.temp}")
                    } ?: Log.w("WeatherViewModel", "No body for $apiCityName")
                } else {
                    Log.e("WeatherViewModel", "API error for $apiCityName: ${response.code()} - ${response.message()}")
                }
                handleWeatherResponse(apiCityName, response)
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Network error for $apiCityName: ${e.message}", e)
                val updatedStates = _cityWeatherStates.value.toMutableMap()
                updatedStates[apiCityName] = WeatherState.Error("Network error: ${e.message}")
                _cityWeatherStates.value = updatedStates
                _weatherState.value = WeatherState.Error("Network error: ${e.message}")
            }
        }
    }

    fun fetchWeatherByLocation(context: Context) {
        viewModelScope.launch {
            try {
                val locationHelper = LocationHelper(context)
                val location = locationHelper.getCurrentLocation()

                val currentStates = _cityWeatherStates.value.toMutableMap()
                val locationKey = "CurrentLocation"
                currentStates[locationKey] = WeatherState.Loading
                _cityWeatherStates.value = currentStates
                _weatherState.value = WeatherState.Loading

                val response = RetrofitClient.weatherApiService.getWeatherByLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    units = "metric",
                    apiKey = API_KEY
                )
                handleWeatherResponse(locationKey, response)

                val forecastResponse = RetrofitClient.weatherApiService.getWeatherForecast(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    units = "metric",
                    apiKey = API_KEY
                )
                handleForecastResponse(forecastResponse)
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Network error for location: ${e.message}", e)
                val updatedStates = _cityWeatherStates.value.toMutableMap()
                updatedStates["CurrentLocation"] = WeatherState.Error("Network error: ${e.message}")
                _cityWeatherStates.value = updatedStates
                _weatherState.value = WeatherState.Error("Network error: ${e.message}")
                _forecastState.value = ForecastState.Error("Network error: ${e.message}")
            }
        }
    }

    private fun handleWeatherResponse(city: String, response: Response<WeatherResponse>) {
        val updatedStates = _cityWeatherStates.value.toMutableMap()
        if (response.isSuccessful) {
            response.body()?.let {
                Log.d("WeatherViewModel", "Success for $city: ${it.name}, Temp: ${it.main.temp}")
                updatedStates[city] = WeatherState.Success(it)
                _weatherState.value = WeatherState.Success(it)

                // Thêm dữ liệu thời tiết ngày hiện tại vào dailyWeatherData
                val currentDate = Calendar.getInstance()
                val dateKey = "${currentDate.get(Calendar.YEAR)}-${currentDate.get(Calendar.MONTH) + 1}-${currentDate.get(Calendar.DAY_OF_MONTH)}"
                val currentWeather = DailyWeatherInfo(
                    condition = it.weather.firstOrNull()?.main ?: "Unknown",
                    temp = it.main.temp.toInt(),
                    icon = mapWeatherIcon(it.weather.firstOrNull()?.icon ?: "01d")
                )
                val updatedDailyWeather = _dailyWeatherData.value.toMutableMap()
                updatedDailyWeather[dateKey] = currentWeather
                _dailyWeatherData.value = updatedDailyWeather
            } ?: run {
                Log.e("WeatherViewModel", "No data received for $city")
                updatedStates[city] = WeatherState.Error("No data received from API")
                _weatherState.value = WeatherState.Error("No data received from API")
            }
        } else {
            Log.e("WeatherViewModel", "API error for $city: ${response.code()} - ${response.message()}")
            updatedStates[city] = WeatherState.Error("API error: ${response.code()} - ${response.message()}")
            _weatherState.value = WeatherState.Error("API error: ${response.code()} - ${response.message()}")
        }
        _cityWeatherStates.value = updatedStates
    }

    private fun handleForecastResponse(response: Response<ForecastResponse>) {
        if (response.isSuccessful) {
            response.body()?.let { forecastData ->
                // Nhóm dữ liệu dự báo cho 4 ngày tiếp theo (tổng cộng 5 ngày kể cả ngày hiện tại)
                val dailyWeatherMap = mutableMapOf<String, DailyWeatherInfo>()
                val groupedByDay = forecastData.list
                    .groupBy { data ->
                        val date = Calendar.getInstance().apply {
                            timeInMillis = data.dt * 1000
                        }
                        "${date.get(Calendar.YEAR)}-${date.get(Calendar.MONTH) + 1}-${date.get(Calendar.DAY_OF_MONTH)}"
                    }

                // Lấy dữ liệu cho 4 ngày tiếp theo (bỏ qua ngày hiện tại vì đã thêm từ weatherState)
                val today = Calendar.getInstance()
                for (i in 1..4) {
                    val nextDay = today.clone() as Calendar
                    nextDay.add(Calendar.DAY_OF_MONTH, i)
                    val dateKey = "${nextDay.get(Calendar.YEAR)}-${nextDay.get(Calendar.MONTH) + 1}-${nextDay.get(Calendar.DAY_OF_MONTH)}"
                    val dayData = groupedByDay[dateKey]?.firstOrNull()
                    if (dayData != null) {
                        dailyWeatherMap[dateKey] = DailyWeatherInfo(
                            condition = dayData.weather.firstOrNull()?.main ?: "Unknown",
                            temp = dayData.main.temp.toInt(),
                            icon = mapWeatherIcon(dayData.weather.firstOrNull()?.icon ?: "01d")
                        )
                    }
                }

                // Kết hợp dữ liệu ngày hiện tại và 4 ngày tiếp theo
                val currentDailyWeather = _dailyWeatherData.value.toMutableMap()
                currentDailyWeather.putAll(dailyWeatherMap)
                _dailyWeatherData.value = currentDailyWeather

                // Cập nhật forecastState với danh sách dự báo 5 ngày
                val dailyForecast = processDailyForecast(forecastData.list)
                _forecastState.value = ForecastState.Success(dailyForecast)
            } ?: run {
                Log.e("WeatherViewModel", "No forecast data available")
                _forecastState.value = ForecastState.Error("No forecast data available")
            }
        } else {
            Log.e("WeatherViewModel", "API error for forecast: ${response.code()} - ${response.message()}")
            _forecastState.value = ForecastState.Error("API error: ${response.code()} - ${response.message()}")
        }
    }

    private fun processDailyForecast(forecastList: List<ForecastItem>): List<DailyForecast> {
        val dailyData = forecastList
            .groupBy { data ->
                val date = Date(data.dt * 1000)
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            }
            .mapValues { entry ->
                val firstEntry = entry.value.first()
                DailyForecast(
                    date = Date(firstEntry.dt * 1000),
                    tempMin = entry.value.minOf { it.main.temp },
                    tempMax = entry.value.maxOf { it.main.temp },
                    icon = mapWeatherIcon(firstEntry.weather.firstOrNull()?.icon ?: "01d"),
                    condition = firstEntry.weather.firstOrNull()?.main ?: "Unknown"
                )
            }
            .values
            .toList()
            .sortedBy { it.date }
            .take(5) // Chỉ lấy 5 ngày (bao gồm ngày hiện tại)

        val calendar = Calendar.getInstance()
        return (0 until 5).map { index ->
            dailyData.getOrNull(index) ?: DailyForecast(
                date = calendar.apply { add(Calendar.DAY_OF_MONTH, index) }.time,
                tempMin = 0.0,
                tempMax = 0.0,
                icon = R.drawable.sunny,
                condition = "Unknown"
            )
        }
    }
}

sealed class WeatherState {
    object Loading : WeatherState()
    data class Success(val data: WeatherResponse) : WeatherState()
    data class Error(val message: String) : WeatherState()
}

sealed class ForecastState {
    object Loading : ForecastState()
    data class Success(val forecast: List<DailyForecast>) : ForecastState()
    data class Error(val message: String) : ForecastState()
}

data class DailyForecast(
    val date: Date,
    val tempMin: Double,
    val tempMax: Double,
    val icon: Int,
    val condition: String
)

data class DailyWeatherInfo(
    val condition: String,
    val temp: Int,
    val icon: Int
)

