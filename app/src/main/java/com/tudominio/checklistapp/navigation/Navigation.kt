package com.tudominio.checklistapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tudominio.checklistapp.ui.screens.HomeScreen
import com.tudominio.checklistapp.ui.screens.NewInspectionScreen
import com.tudominio.checklistapp.ui.screens.SplashScreen

/**
 * Definición de las rutas de navegación de la aplicación.
 * Cada pantalla tiene una ruta única que se utiliza para la navegación.
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Home : Screen("home_screen")
    object NewInspection : Screen("new_inspection_screen")
    object History : Screen("history_screen")
    // Se añadirán más pantallas según avancemos
}

/**
 * Configura el grafo de navegación de la aplicación con todas las rutas disponibles.
 * Define cómo se conectan las diferentes pantallas entre sí.
 */
@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // Pantalla de Splash
        composable(route = Screen.Splash.route) {
            SplashScreen(onSplashFinished = {
                // Navegar a la pantalla principal cuando termine el splash
                navController.navigate(Screen.Home.route) {
                    // Elimina la pantalla splash del stack de navegación
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }

        // Pantalla Principal (Home)
        composable(route = Screen.Home.route) {
            HomeScreen(
                // Navegación a Nueva Inspección
                onNavigateToNewInspection = {
                    navController.navigate(Screen.NewInspection.route)
                },
                // Navegación a Historial
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                }
            )
        }

        // Pantalla de Nueva Inspección
        composable(route = Screen.NewInspection.route) {
            NewInspectionScreen(
                onNavigateBack = { navController.navigateUp() },
                onInspectionCompleted = { navController.navigate(Screen.Home.route) }
            )
        }

        // Pantalla de Historial (por implementar)
        composable(route = Screen.History.route) {
            // Implementaremos esta pantalla más adelante
            // Por ahora, simplemente mostrará un placeholder
            SplashScreen(onSplashFinished = { navController.navigateUp() })
        }
    }
}