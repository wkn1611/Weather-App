package com.mi3mien.weatherapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mi3mien.weatherapp.ui.WeatherScreen
import com.mi3mien.weatherapp.MainScreen
import com.mi3mien.weatherapp.SettingsScreen
import com.mi3mien.weatherapp.ui.HomeScreen
import com.mi3mien.weatherapp.ui.WeatherCalendarScreen

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