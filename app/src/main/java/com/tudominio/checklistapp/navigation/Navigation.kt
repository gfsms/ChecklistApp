package com.tudominio.checklistapp.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tudominio.checklistapp.ui.screens.CameraScreen
import com.tudominio.checklistapp.ui.screens.DrawingScreen
import com.tudominio.checklistapp.ui.screens.HistoryScreen
import com.tudominio.checklistapp.ui.screens.HomeScreen
import com.tudominio.checklistapp.ui.screens.NewInspectionScreen
import com.tudominio.checklistapp.ui.screens.PhotoScreen
import com.tudominio.checklistapp.ui.screens.SplashScreen
import com.tudominio.checklistapp.ui.viewmodels.NewInspectionViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Definición de las rutas de navegación de la aplicación.
 * Cada pantalla tiene una ruta única que se utiliza para la navegación.
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Home : Screen("home_screen")
    object NewInspection : Screen("new_inspection_screen")
    object History : Screen("history_screen")
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
 * Configura el grafo de navegación de la aplicación con todas las rutas disponibles.
 * Define cómo se conectan las diferentes pantallas entre sí.
 */
@Composable
fun SetupNavGraph(
    navController: NavHostController
) {
    // Crear un ViewModel compartido para todas las pantallas
    val viewModel: NewInspectionViewModel = viewModel()

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

        // Pantalla de Historial (por implementar)
        composable(route = Screen.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.navigateUp() }
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
                    // Al tomar la foto, actualizamos el viewModel y volvemos a la pantalla anterior
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
                        // Navegamos a la pantalla de cámara
                        navController.navigate(Screen.Camera.createRoute(questionId))
                    },
                    onDeletePhoto = { photo ->
                        viewModel.removePhotoFromQuestion(questionId, photo)
                    },
                    onEditPhoto = { photo ->
                        // Navegamos a la pantalla de dibujo
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
                    // Al terminar el dibujo, actualizamos la foto en el viewModel
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