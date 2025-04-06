package com.example.weatherapp.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.weatherapp.R
import com.example.weatherapp.model.WeatherModel
import com.example.weatherapp.model.getDummyWeatherData
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale


@Composable
fun WeatherScreen(navController: NavController) {
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
            horizontalAlignment = Alignment.CenterHorizontally // Căn giữa nội dung trong cột
        ) {
            // Thanh tiêu đề
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomBackButton(navController)
                Spacer(modifier = Modifier.weight(1f)) // Đẩy "Weather" vào giữa
                Text(
                    "Weather",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f)) // Giữ cân bằng khoảng cách
                IconButton(onClick = { }) {
                    Icon(Icons.Default.MoreHoriz, contentDescription = "Menu", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Thanh tìm kiếm
            SearchBar()

            Spacer(modifier = Modifier.height(16.dp))

            // Danh sách thời tiết
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(20.dp), // Tăng khoảng cách giữa các item
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp) // Thêm padding để không bị dính vào mép
            ) {
                items(getDummyWeatherData()) { weather ->
                    WeatherCard(weather)
                }
            }
        }
    }
}

@Composable
fun SearchBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f) // Chiều rộng giống với WeatherCard
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
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
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Search for a city",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun WeatherCard(weather: WeatherModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f) // Cùng chiều rộng với thanh tìm kiếm
            .height(160.dp) // Điều chỉnh chiều cao phù hợp
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 32.dp, bottomEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Ảnh nền
                Image(
                    painter = painterResource(id = R.drawable.weather_background),
                    contentDescription = "Weather Background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )

                // Nội dung trên card
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

                    // Icon thời tiết đặt sát mép phải
                    Box(
                        modifier = Modifier
                            .size(200.dp) // Kích thước cố định cho tất cả icon
                            .align(Alignment.CenterVertically)
                            .offset(x = 40.dp) // Đẩy icon ra sát mép phải
                    ) {
                        Image(
                            painter = painterResource(id = weather.icon),
                            contentDescription = "Weather Icon",
                            contentScale = ContentScale.Fit, // Đảm bảo icon không bị méo
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
    WeatherScreen(navController = rememberNavController())
}
