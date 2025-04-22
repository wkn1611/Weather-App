import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.weatherapp.MainScreen
import com.example.weatherapp.ui.HomeScreen
import com.example.weatherapp.ui.WeatherCalendarScreen
import com.example.weatherapp.ui.WeatherScreen

@Composable
fun WeatherAppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "mainScreen" // Điểm bắt đầu là MainScreen
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
                onNavigateBack = {navController.popBackStack()}
            )
        }
        composable("homeScreen") {
            HomeScreen(
                onNavigateToCalendarScreen = {
                    navController.navigate("weatherCalendarScreen")
                },
                onNavigateBack = {navController.popBackStack()}
            )
        }
        composable("weatherCalendarScreen") {
            WeatherCalendarScreen(
                onNavigateBack = {
                    navController.popBackStack() // Quay lại màn hình trước đó (HomeScreen)
                }
            )
        }
    }
}