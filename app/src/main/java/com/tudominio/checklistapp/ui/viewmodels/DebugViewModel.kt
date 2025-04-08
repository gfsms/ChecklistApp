package com.tudominio.checklistapp.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tudominio.checklistapp.ChecklistApplication
import com.tudominio.checklistapp.data.database.AppDatabase
import com.tudominio.checklistapp.data.database.InspectionEntity
import com.tudominio.checklistapp.data.repository.DebugRepository
import kotlinx.coroutines.launch

class DebugViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "DebugViewModel"
    private val repository: DebugRepository

    // UI state
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var inspections by mutableStateOf<List<InspectionEntity>>(emptyList())
        private set

    var databaseAccessSuccessful by mutableStateOf<Boolean?>(null)
        private set

    init {
        // Safe database initialization with error handling
        val database = try {
            (application as ChecklistApplication).database
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get database from application: ${e.message}", e)
            null
        }

        // Initialize repository with null-safe approach
        repository = DebugRepository(database)

        // Test database access immediately but safely
        testDatabaseAccess()
    }

    fun testDatabaseAccess() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val result = repository.testDatabaseAccess()
                databaseAccessSuccessful = result

                if (result) {
                    Log.d(TAG, "Database access test succeeded")
                    loadInspections()
                } else {
                    Log.e(TAG, "Database access test failed")
                    errorMessage = "No se pudo acceder a la base de datos"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in database access test: ${e.message}", e)
                databaseAccessSuccessful = false
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    fun runDiagnostics() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val diagnosticResults = StringBuilder()

            // Check if Application class is providing repository
            val app = getApplication<ChecklistApplication>()
            diagnosticResults.append("App class: ${app::class.java.simpleName}\n")

            // Check if repository is available
            val repo = app.repository
            diagnosticResults.append("Repository available: ${repo != null}\n")

            // Check if database is available
            val db = app.database
            diagnosticResults.append("Database available: ${db != null}\n")

            // Check if DAO can be obtained
            val dao = db?.inspectionDao()
            diagnosticResults.append("DAO available: ${dao != null}\n")

            // If we reach here, show the results
            Log.d(TAG, "Diagnostics results:\n$diagnosticResults")
            errorMessage = "Resultados de diagnóstico:\n$diagnosticResults"

            isLoading = false
        }
    }
    fun resetDatabase() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val result = repository.resetDatabase()
                if (result) {
                    Log.d(TAG, "Database reset successful")
                    // Refresh database connection test
                    testDatabaseAccess()
                } else {
                    Log.e(TAG, "Database reset failed")
                    errorMessage = "No se pudo reiniciar la base de datos"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error resetting database: ${e.message}", e)
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    fun loadInspections() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                inspections = repository.getSafeInspectionsList()
                Log.d(TAG, "Loaded ${inspections.size} inspections")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading inspections: ${e.message}", e)
                errorMessage = "Error cargando inspecciones: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun saveCurrentInspection(viewModel: NewInspectionViewModel): Boolean {
        var success = false

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                success = repository.saveBasicInspection(viewModel.inspection)
                if (success) {
                    Log.d(TAG, "Saved inspection successfully")
                    loadInspections() // Refresh the list
                } else {
                    Log.e(TAG, "Failed to save inspection")
                    errorMessage = "No se pudo guardar la inspección"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving inspection: ${e.message}", e)
                errorMessage = "Error guardando inspección: ${e.message}"
            } finally {
                isLoading = false
            }
        }

        return success
    }

    fun clearErrorMessage() {
        errorMessage = null
    }
}