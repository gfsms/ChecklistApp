package com.tudominio.checklistapp.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tudominio.checklistapp.ChecklistApplication
import com.tudominio.checklistapp.data.model.Inspection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the history screen that interacts with the repository
 * to load and display inspection history.
 */
class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "HistoryViewModel"

    // Repository instance from the Application class
    private val repository = (application as ChecklistApplication).repository

    // Using StateFlow for list of inspections to observe changes
    private val _inspections = MutableStateFlow<List<Inspection>>(emptyList())
    val inspections: StateFlow<List<Inspection>> = _inspections.asStateFlow()

    // State for the selected inspection
    var selectedInspection by mutableStateOf<Inspection?>(null)
        private set

    // Loading state
    var isLoading by mutableStateOf(true)
        private set

    // Error state
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Search and filter states
    var searchQuery by mutableStateOf("")
        private set

    var selectedFilter by mutableStateOf("Todos")
        private set

    init {
        loadInspections()
    }

    /**
     * Load all inspections from the repository
     */
    fun loadInspections() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                Log.d(TAG, "Starting to load inspections...")

                // First check database connectivity
                val isConnected = try {
                    withContext(Dispatchers.IO) {
                        Log.d(TAG, "Testing database connection...")
                        val result = repository.testDatabaseConnection()
                        Log.d(TAG, "Database connection test result: $result")
                        result
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Database connection test failed: ${e.message}", e)
                    false
                }

                if (!isConnected) {
                    Log.e(TAG, "Database connection test failed, can't load inspections")
                    isLoading = false
                    errorMessage = "No se pudo conectar a la base de datos. Verifique la conexión y vuelva a intentarlo."
                    return@launch
                }

                // Collect inspections from Flow
                try {
                    Log.d(TAG, "Setting up flow collection...")

                    // Use direct list loading for simpler debugging first
                    val list = withContext(Dispatchers.IO) {
                        repository.getAllInspectionsList()
                    }

                    Log.d(TAG, "Directly loaded ${list.size} inspections")

                    // Now set up the flow
                    repository.allInspections
                        .catch { e ->
                            Log.e(TAG, "Error in inspection flow: ${e.message}", e)
                            errorMessage = "Error al recibir datos: ${e.message}"
                            emit(emptyList())
                        }
                        .collectLatest { inspectionList ->
                            Log.d(TAG, "Flow emitted ${inspectionList.size} inspections")
                            val filteredList = filterInspections(inspectionList, searchQuery, selectedFilter)
                            Log.d(TAG, "After filtering: ${filteredList.size} inspections")
                            _inspections.value = filteredList
                            isLoading = false
                        }
                } catch (e: Exception) {
                    Log.e(TAG, "Error setting up collection: ${e.message}", e)
                    errorMessage = "Error al configurar la colección de datos: ${e.message}"
                    isLoading = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading inspections: ${e.message}", e)
                errorMessage = "Error al cargar las inspecciones: ${e.message}"
                isLoading = false
            }
        }
    }

    /**
     * Update search query and filter inspections
     */
    fun updateSearchQuery(query: String) {
        searchQuery = query
        filterCurrentInspections()
    }

    /**
     * Update filter option and filter inspections
     */
    fun updateFilter(filter: String) {
        selectedFilter = filter
        filterCurrentInspections()
    }

    /**
     * Filter inspections based on current search query and filter option
     */
    private fun filterCurrentInspections() {
        viewModelScope.launch {
            try {
                val allInspections = withContext(Dispatchers.IO) {
                    repository.getAllInspectionsList()
                }
                val filteredList = filterInspections(allInspections, searchQuery, selectedFilter)
                _inspections.value = filteredList
            } catch (e: Exception) {
                Log.e(TAG, "Error filtering inspections: ${e.message}", e)
                errorMessage = "Error al filtrar las inspecciones: ${e.message}"
            }
        }
    }

    /**
     * Helper function to filter inspections
     */
    private fun filterInspections(
        inspections: List<Inspection>,
        query: String,
        filter: String
    ): List<Inspection> {
        // First apply search query
        var filteredList = if (query.isNotEmpty()) {
            inspections.filter {
                it.equipment.contains(query, ignoreCase = true) ||
                        it.inspector.contains(query, ignoreCase = true)
            }
        } else {
            inspections
        }

        // Then apply conformity filter
        filteredList = when (filter) {
            "Alta Conformidad" -> {
                // Calculate conformity percentage for each inspection
                filteredList.filter { inspection ->
                    calculateConformityPercentage(inspection) >= 90f
                }
            }
            "Media Conformidad" -> {
                filteredList.filter { inspection ->
                    val conformity = calculateConformityPercentage(inspection)
                    conformity >= 70f && conformity < 90f
                }
            }
            "Baja Conformidad" -> {
                filteredList.filter { inspection ->
                    calculateConformityPercentage(inspection) < 70f
                }
            }
            else -> filteredList // "Todos"
        }

        return filteredList
    }

    /**
     * Calculate conformity percentage for an inspection
     */
    private fun calculateConformityPercentage(inspection: Inspection): Float {
        val totalQuestions = inspection.items.sumOf { it.questions.size }
        val answeredQuestions = inspection.items.sumOf { item ->
            item.questions.count { it.answer != null }
        }
        val conformQuestions = inspection.items.sumOf { item ->
            item.questions.count { question ->
                question.answer?.isConform == true
            }
        }

        return if (answeredQuestions > 0) {
            (conformQuestions.toFloat() / answeredQuestions) * 100
        } else {
            0f
        }
    }

    /**
     * Load the full details of a specific inspection
     */
    fun loadInspectionDetails(inspectionId: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val inspection = withContext(Dispatchers.IO) {
                    repository.getFullInspection(inspectionId)
                }

                selectedInspection = inspection
                isLoading = false
            } catch (e: Exception) {
                Log.e(TAG, "Error loading inspection details: ${e.message}", e)
                errorMessage = "Error al cargar los detalles de la inspección: ${e.message}"
                isLoading = false
            }
        }
    }

    /**
     * Delete an inspection
     */
    fun deleteInspection(inspectionId: String) {
        viewModelScope.launch {
            isLoading = true

            try {
                val success = withContext(Dispatchers.IO) {
                    repository.deleteInspection(inspectionId)
                }

                if (success) {
                    // Refresh the list
                    if (selectedInspection?.id == inspectionId) {
                        selectedInspection = null
                    }

                    filterCurrentInspections()
                } else {
                    errorMessage = "No se pudo eliminar la inspección"
                }
                isLoading = false
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting inspection: ${e.message}", e)
                errorMessage = "Error al eliminar la inspección: ${e.message}"
                isLoading = false
            }
        }
    }

    /**
     * Clear the selected inspection
     */
    fun clearSelectedInspection() {
        selectedInspection = null
    }

    /**
     * Clear any error message
     */
    fun clearError() {
        errorMessage = null
    }
}