package com.example.weatherapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.weatherapp.ui.HomeScreen
import com.example.weatherapp.ui.WeatherScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController() // Create NavHostController with the correct type
    NavHost(navController = navController, startDestination = "HomeScreen") {
        composable("homeScreen") {
            HomeScreen(navController)
        }
        composable("weatherScreen") {
            WeatherScreen(navController)
        }
    }
}
