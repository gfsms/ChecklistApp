package com.tudominio.checklistapp.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tudominio.checklistapp.ui.screens.*
import com.tudominio.checklistapp.ui.viewmodels.NewInspectionViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Definición de las rutas de navegación de la aplicación.
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Home : Screen("home_screen")
    object NewInspection : Screen("new_inspection_screen")
    object History : Screen("history_screen")
    object Dashboard : Screen("dashboard_screen")
    object Debug : Screen("debug_screen") // New Debug Screen
    object Camera : Screen("camera_screen/{questionId}") {
        fun createRoute(questionId: String) = "camera_screen/$questionId"
    }
    object Photos : Screen("photos_screen/{questionId}") {
        fun createRoute(questionId: String) = "photos_screen/$questionId"
    }
    object Drawing : Screen("drawing_screen/{photoUri}/{photoId}/{questionId}") {
        fun createRoute(photoUri: String, photoId: String, questionId: String): String {
            val encodedUri = URLEncoder.encode(photoUri, StandardCharsets.UTF_8.toString())
            return "drawing_screen/$encodedUri/$photoId/$questionId"
        }
    }
}

/**
 * Configura el grafo de navegación de la aplicación.
 */
@Composable
fun SetupNavGraph(
    navController: NavHostController
) {
    // ViewModel compartido para todas las pantallas
    val viewModel: NewInspectionViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // Pantalla de Splash
        composable(route = Screen.Splash.route) {
            SplashScreen(onSplashFinished = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }

        // Pantalla Principal (Home)
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToNewInspection = {
                    navController.navigate(Screen.NewInspection.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToDebug = {
                    navController.navigate(Screen.Debug.route)
                },
                viewModel = viewModel
            )
        }

        // Pantalla de Nueva Inspección
        composable(route = Screen.NewInspection.route) {
            NewInspectionScreen(
                onNavigateBack = { navController.navigateUp() },
                onInspectionCompleted = { navController.navigate(Screen.Home.route) },
                viewModel = viewModel,
                onNavigateToCamera = { questionId: String ->
                    navController.navigate(Screen.Camera.createRoute(questionId))
                },
                onNavigateToPhotos = { questionId: String ->
                    navController.navigate(Screen.Photos.createRoute(questionId))
                }
            )
        }

        // Pantalla de Historial
        composable(route = Screen.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route)
                }
            )
        }

        // Dashboard
        composable(route = Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateBack = { navController.navigateUp() },
                inspection = viewModel.inspection.takeIf { it.isCompleted }
            )
        }

        // Debug Screen
        composable(route = Screen.Debug.route) {
            DebugScreen(
                onNavigateBack = { navController.navigateUp() },
                newInspectionViewModel = viewModel
            )
        }

        // Pantalla de Cámara
        composable(
            route = Screen.Camera.route,
            arguments = listOf(navArgument("questionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val questionId = backStackEntry.arguments?.getString("questionId") ?: ""

            CameraScreen(
                onPhotoTaken = { uri ->
                    viewModel.addPhotoToQuestion(questionId, uri.toString())
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla de Gestión de Fotos
        composable(
            route = Screen.Photos.route,
            arguments = listOf(navArgument("questionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val questionId = backStackEntry.arguments?.getString("questionId") ?: ""
            val question = viewModel.getQuestionById(questionId)

            question?.let { q ->
                PhotoScreen(
                    photos = q.answer?.photos ?: emptyList(),
                    onAddPhoto = {
                        navController.navigate(Screen.Camera.createRoute(questionId))
                    },
                    onDeletePhoto = { photo ->
                        viewModel.removePhotoFromQuestion(questionId, photo)
                    },
                    onEditPhoto = { photo ->
                        navController.navigate(
                            Screen.Drawing.createRoute(
                                photoUri = photo.uri,
                                photoId = photo.id,
                                questionId = questionId
                            )
                        )
                    },
                    onSaveChanges = {
                        navController.popBackStack()
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // Pantalla de Dibujo sobre foto
        composable(
            route = Screen.Drawing.route,
            arguments = listOf(
                navArgument("photoUri") { type = NavType.StringType },
                navArgument("photoId") { type = NavType.StringType },
                navArgument("questionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedPhotoUri = backStackEntry.arguments?.getString("photoUri") ?: ""
            val photoUri = URLDecoder.decode(encodedPhotoUri, StandardCharsets.UTF_8.toString())
            val photoId = backStackEntry.arguments?.getString("photoId") ?: ""
            val questionId = backStackEntry.arguments?.getString("questionId") ?: ""

            DrawingScreen(
                photoUri = photoUri,
                onDrawingFinished = { drawingUri ->
                    viewModel.updatePhotoWithDrawing(questionId, photoId, drawingUri)
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}