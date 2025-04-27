package com.example.weatherapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.weatherapp.MainScreen
import com.example.weatherapp.SettingsScreen
import com.example.weatherapp.ui.HomeScreen
import com.example.weatherapp.ui.WeatherCalendarScreen
import com.example.weatherapp.ui.WeatherScreen

@Composable
fun WeatherAppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "mainScreen"
    ) {
        composable("mainScreen") {
            MainScreen(
                onNavigateToWeatherScreen = {
                    navController.navigate("weatherScreen")
                }
            )
        }
        composable("weatherScreen") {
            WeatherScreen(
                onNavigateToHomeScreen = {
                    navController.navigate("homeScreen")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("homeScreen") {
            HomeScreen(
                onNavigateToCalendarScreen = {
                    navController.navigate("weatherCalendarScreen")
                },
                onNavigateToSettingsScreen = {
                    navController.navigate("settingsScreen")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("weatherCalendarScreen") {
            WeatherCalendarScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("settingsScreen") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() } // Thêm điều hướng quay lại
            )
        }
    }
}