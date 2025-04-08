package com.tudominio.checklistapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tudominio.checklistapp.ui.screens.*
import com.tudominio.checklistapp.ui.viewmodels.NewInspectionViewModel
import com.tudominio.checklistapp.ui.viewmodels.PostInspectionViewModel
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
    object EquipmentSelection : Screen("equipment_selection_screen")
    object PostInspection : Screen("post_inspection_screen/{controlInspectionId}") {
        fun createRoute(controlInspectionId: String) = "post_inspection_screen/$controlInspectionId"
    }
    object History : Screen("history_screen")
    object Dashboard : Screen("dashboard_screen")
    object Debug : Screen("debug_screen") // Debug Screen
    object InspectionDetail : Screen("inspection_detail_screen/{inspectionId}") {
        fun createRoute(inspectionId: String) = "inspection_detail_screen/$inspectionId"
    }
    object Camera : Screen("camera_screen/{questionId}/{viewModelType}") {
        fun createRoute(questionId: String, viewModelType: String) = "camera_screen/$questionId/$viewModelType"
    }
    object Photos : Screen("photos_screen/{questionId}/{viewModelType}") {
        fun createRoute(questionId: String, viewModelType: String) = "photos_screen/$questionId/$viewModelType"
    }
    object Drawing : Screen("drawing_screen/{photoUri}/{photoId}/{questionId}/{viewModelType}") {
        fun createRoute(photoUri: String, photoId: String, questionId: String, viewModelType: String): String {
            val encodedUri = URLEncoder.encode(photoUri, StandardCharsets.UTF_8.toString())
            return "drawing_screen/$encodedUri/$photoId/$questionId/$viewModelType"
        }
    }
}

// Constants for viewModelType parameter
const val VIEW_MODEL_NEW = "new"
const val VIEW_MODEL_POST = "post"

/**
 * Configura el grafo de navegación de la aplicación.
 */
@Composable
fun SetupNavGraph(
    navController: NavHostController
) {
    // ViewModels for screens
    val newViewModel: NewInspectionViewModel = viewModel()
    val postViewModel: PostInspectionViewModel = viewModel()

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
                onNavigateToPostInspection = {
                    navController.navigate(Screen.EquipmentSelection.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToDebug = {
                    navController.navigate(Screen.Debug.route)
                },
                viewModel = newViewModel
            )
        }

        // Pantalla de selección de equipo para post-inspección
        composable(route = Screen.EquipmentSelection.route) {
            EquipmentSelectionScreen(
                onNavigateBack = { navController.navigateUp() },
                onSelectEquipment = { controlInspectionId ->
                    navController.navigate(Screen.PostInspection.createRoute(controlInspectionId))
                },
                viewModel = postViewModel
            )
        }

        // Pantalla de inspección post-intervención
        composable(
            route = Screen.PostInspection.route,
            arguments = listOf(navArgument("controlInspectionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val controlInspectionId = backStackEntry.arguments?.getString("controlInspectionId") ?: ""

            PostInspectionScreen(
                controlInspectionId = controlInspectionId,
                onNavigateBack = { navController.navigateUp() },
                onInspectionCompleted = {
                    // Go back to home when completed, don't leave post inspection in backstack
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.EquipmentSelection.route) { inclusive = true }
                    }
                },
                onNavigateToCamera = { questionId: String ->
                    // Save the current state before navigating to camera
                    postViewModel.saveCurrentStageForCamera()
                    navController.navigate(Screen.Camera.createRoute(questionId, VIEW_MODEL_POST))
                },
                onNavigateToPhotos = { questionId: String ->
                    // Save the current state before navigating to photos
                    postViewModel.saveCurrentStageForCamera()
                    navController.navigate(Screen.Photos.createRoute(questionId, VIEW_MODEL_POST))
                },
                viewModel = postViewModel
            )
        }

        // Pantalla de Nueva Inspección (Control de Inicio)
        composable(route = Screen.NewInspection.route) {
            NewInspectionScreen(
                onNavigateBack = { navController.navigateUp() },
                onInspectionCompleted = { navController.navigate(Screen.Home.route) },
                viewModel = newViewModel,
                onNavigateToCamera = { questionId: String ->
                    navController.navigate(Screen.Camera.createRoute(questionId, VIEW_MODEL_NEW))
                },
                onNavigateToPhotos = { questionId: String ->
                    navController.navigate(Screen.Photos.createRoute(questionId, VIEW_MODEL_NEW))
                }
            )
        }

        // Pantalla de Historial
        composable(route = Screen.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route)
                },
                onNavigateToDetail = { inspectionId ->
                    navController.navigate(Screen.InspectionDetail.createRoute(inspectionId))
                }
            )
        }

        // Detalle de Inspección
        composable(
            route = Screen.InspectionDetail.route,
            arguments = listOf(navArgument("inspectionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val inspectionId = backStackEntry.arguments?.getString("inspectionId") ?: ""
            InspectionDetailScreen(
                inspectionId = inspectionId,
                onNavigateBack = { navController.navigateUp() },
                onExport = {
                    navController.navigate(Screen.Dashboard.route)
                }
            )
        }

        // Dashboard
        composable(route = Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateBack = { navController.navigateUp() },
                inspection = newViewModel.inspection.takeIf { it.isCompleted }
            )
        }

        // Debug Screen
        composable(route = Screen.Debug.route) {
            DebugScreen(
                onNavigateBack = { navController.navigateUp() },
                newInspectionViewModel = newViewModel
            )
        }

        // Pantalla de Cámara
        composable(
            route = Screen.Camera.route,
            arguments = listOf(
                navArgument("questionId") { type = NavType.StringType },
                navArgument("viewModelType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val questionId = backStackEntry.arguments?.getString("questionId") ?: ""
            val viewModelType = backStackEntry.arguments?.getString("viewModelType") ?: VIEW_MODEL_NEW

            CameraScreen(
                onPhotoTaken = { uri ->
                    // Choose the correct viewModel based on viewModelType
                    if (viewModelType == VIEW_MODEL_NEW) {
                        newViewModel.addPhotoToQuestion(questionId, uri.toString())
                    } else {
                        postViewModel.addPhotoToQuestion(questionId, uri.toString())
                        // Restore the previous stage after taking a photo
                        postViewModel.restoreStageAfterCamera()
                    }
                    navController.popBackStack()
                },
                onNavigateBack = {
                    // For post-inspection, we need to restore the stage after canceling too
                    if (viewModelType == VIEW_MODEL_POST) {
                        postViewModel.restoreStageAfterCamera()
                    }
                    navController.popBackStack()
                }
            )
        }

        // Pantalla de Gestión de Fotos
        composable(
            route = Screen.Photos.route,
            arguments = listOf(
                navArgument("questionId") { type = NavType.StringType },
                navArgument("viewModelType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val questionId = backStackEntry.arguments?.getString("questionId") ?: ""
            val viewModelType = backStackEntry.arguments?.getString("viewModelType") ?: VIEW_MODEL_NEW

            // Get question from appropriate viewModel
            val question = if (viewModelType == VIEW_MODEL_NEW) {
                newViewModel.getQuestionById(questionId)
            } else {
                postViewModel.getQuestionById(questionId)
            }

            question?.let { q ->
                PhotoScreen(
                    photos = q.answer?.photos ?: emptyList(),
                    onAddPhoto = {
                        navController.navigate(Screen.Camera.createRoute(questionId, viewModelType))
                    },
                    onDeletePhoto = { photo ->
                        if (viewModelType == VIEW_MODEL_NEW) {
                            newViewModel.removePhotoFromQuestion(questionId, photo)
                        } else {
                            postViewModel.removePhotoFromQuestion(questionId, photo)
                        }
                    },
                    onEditPhoto = { photo ->
                        navController.navigate(
                            Screen.Drawing.createRoute(
                                photoUri = photo.uri,
                                photoId = photo.id,
                                questionId = questionId,
                                viewModelType = viewModelType
                            )
                        )
                    },
                    onSaveChanges = {
                        // Restore stage after returning to the main flow from photos screen
                        if (viewModelType == VIEW_MODEL_POST) {
                            postViewModel.restoreStageAfterCamera()
                        }
                        navController.popBackStack()
                    },
                    onNavigateBack = {
                        // Restore stage after canceling too
                        if (viewModelType == VIEW_MODEL_POST) {
                            postViewModel.restoreStageAfterCamera()
                        }
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
                navArgument("questionId") { type = NavType.StringType },
                navArgument("viewModelType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedPhotoUri = backStackEntry.arguments?.getString("photoUri") ?: ""
            val photoUri = URLDecoder.decode(encodedPhotoUri, StandardCharsets.UTF_8.toString())
            val photoId = backStackEntry.arguments?.getString("photoId") ?: ""
            val questionId = backStackEntry.arguments?.getString("questionId") ?: ""
            val viewModelType = backStackEntry.arguments?.getString("viewModelType") ?: VIEW_MODEL_NEW

            DrawingScreen(
                photoUri = photoUri,
                onDrawingFinished = { drawingUri ->
                    if (viewModelType == VIEW_MODEL_NEW) {
                        newViewModel.updatePhotoWithDrawing(questionId, photoId, drawingUri)
                    } else {
                        postViewModel.updatePhotoWithDrawing(questionId, photoId, drawingUri)
                        // Restore stage when returning to the main flow
                        postViewModel.restoreStageAfterCamera()
                    }
                    navController.popBackStack()
                },
                onNavigateBack = {
                    // Restore stage when canceling too
                    if (viewModelType == VIEW_MODEL_POST) {
                        postViewModel.restoreStageAfterCamera()
                    }
                    navController.popBackStack()
                }
            )
        }
    }
}