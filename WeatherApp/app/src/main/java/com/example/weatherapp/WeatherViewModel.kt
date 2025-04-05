package com.example.weatherapp

import android.content.Context
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class WeatherViewModel : ViewModel() {
    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Loading)
    val weatherState: StateFlow<WeatherState> = _weatherState

    // Fetch weather by city name
    fun fetchWeatherByCity(city: String) {
        viewModelScope.launch {
            _weatherState.value = WeatherState.Loading
            try {
                val response = RetrofitClient.weatherApiService.getWeatherByCity(
                    city = city,
                    apiKey = "2eba4f893dcb1064055f6ea7b29656a3" // Thay bằng API key của bạn
                )
                handleResponse(response)
            } catch (e: Exception) {
                _weatherState.value = WeatherState.Error("Error: ${e.message}")
            }
        }
    }

    // Fetch weather by location
    fun fetchWeatherByLocation(context: Context) {
        viewModelScope.launch {
            _weatherState.value = WeatherState.Loading
            try {
                val locationHelper = LocationHelper(context)
                val location = locationHelper.getCurrentLocation()
                val response = RetrofitClient.weatherApiService.getWeatherByLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    apiKey = "2eba4f893dcb1064055f6ea7b29656a3" // Thay bằng API key của bạn
                )
                handleResponse(response)
            } catch (e: Exception) {
                _weatherState.value = WeatherState.Error("Error: ${e.message}")
            }
        }
    }

    private fun handleResponse(response: Response<API>) {
        if (response.isSuccessful) {
            response.body()?.let {
                _weatherState.value = WeatherState.Success(it)
            } ?: run {
                _weatherState.value = WeatherState.Error("No data")
            }
        } else {
            _weatherState.value = WeatherState.Error("API error: ${response.message()}")
        }
    }
}

sealed class WeatherState {
    object Loading : WeatherState()
    data class Success(val data: API) : WeatherState()
    data class Error(val message: String) : WeatherState()
}

@Composable
fun CustomSwitch(
    width: Dp = 60.dp,              // Chiều rộng của Switch
    height: Dp = 30.dp,             // Chiều cao của Switch
    thumbSize: Dp = 30.dp,
    cornerRadius: Dp = 15.5.dp,// Kích thước của nút tròn (thumb)
    isChecked: Boolean,             // Trạng thái bật/tắt
    onCheckedChange: (Boolean) -> Unit, // Callback khi thay đổi trạng thái
    checkedGradient: Brush = Brush.horizontalGradient(
        colors = listOf(Color(0xFF2B4485), Color(0xFF87CEEB),Color(0xFFADDDFF), Color(0xFFEDF6FF))
        // Gradient khi bật
    ),
    uncheckedGradient: Brush = Brush.horizontalGradient(
        colors = listOf(Color(0xFFC8D7FF), Color(0xFFDCF0FF)) // Gradient khi tắt
    ),
    checkedThumbGradient: Brush = Brush.verticalGradient(colors = listOf(Color(0xFFC8D7FF), Color(0xFFDCF0FF))), // Màu nút tròn khi bật
    uncheckedThumbGradient: Brush = Brush.horizontalGradient(colors = listOf(Color(0xFF2B4485), Color(0xFF87CEEB),Color(0xFFADDDFF), Color(0xFFEDF6FF))) // Màu nút tròn khi tắt
) {
    // Animation cho vị trí của nút tròn
    val thumbPosition by animateDpAsState(
        targetValue = if (isChecked) width - thumbSize else 0.dp,
        label = "Thumb Animation"
    )


    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            //.clip(androidx.compose.foundation.shape.RoundedCornerShape(height / 2))
            .background(
                brush = if (isChecked) checkedGradient else uncheckedGradient,
                shape = RoundedCornerShape(cornerRadius)
            )
            .clickable { onCheckedChange(!isChecked) } // Xử lý sự kiện click
    ) {
        // Nút tròn (thumb)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = thumbPosition) // Di chuyển nút tròn theo trạng thái
                .size(thumbSize)
                .background(
                    brush = if (isChecked) checkedThumbGradient else uncheckedThumbGradient,
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        )
    }
}

@Composable
fun BackgroundSwitch() {
    var isChecked by remember { mutableStateOf(false) }
    CustomSwitch(
        isChecked = isChecked,
        onCheckedChange = { isChecked = it },
        width = 65.dp,          // Tùy chỉnh chiều rộng
        height = 30.dp,         // Tùy chỉnh chiều cao
        thumbSize = 30.dp,      // Tùy chỉnh kích thước nút tròn
        checkedGradient = Brush.horizontalGradient(
            colors = listOf(Color(0xFF2B4485), Color(0xFFAFCAFF)) // Gradient xanh
        ),
        uncheckedGradient = Brush.horizontalGradient(
            colors = listOf(Color(0xff25F9DF), Color(0xffFFEEB2)) // Gradient xám
        ),
        checkedThumbGradient =  Brush.verticalGradient(
            colors = listOf(Color(0xFFC8D7FF), Color(0xFFDCF0FF)) // Màu xanh đậm khi bật
        ), // Màu xanh đậm khi bật
        uncheckedThumbGradient = Brush.horizontalGradient(
            colors = listOf(Color(0xffFED060), Color(0xffFAA96F)) // Màu xám khi tắt
        )
    )
}


