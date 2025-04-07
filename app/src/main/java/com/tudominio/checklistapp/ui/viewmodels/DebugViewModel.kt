package com.tudominio.checklistapp.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tudominio.checklistapp.data.database.AppDatabase
import com.tudominio.checklistapp.data.database.InspectionEntity
import com.tudominio.checklistapp.data.repository.DebugRepository
import kotlinx.coroutines.launch

/**
 * A simplified ViewModel to debug database access issues
 */
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
        val database = AppDatabase.getDatabase(application)
        repository = DebugRepository(database)

        // Test database access immediately
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