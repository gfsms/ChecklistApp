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
import com.tudominio.checklistapp.data.model.Inspection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    // Using composition mutableStateOf instead of LiveData
    var isLoading by mutableStateOf(true)
        private set

    var inspections by mutableStateOf<List<InspectionEntity>>(emptyList())
        private set

    var selectedInspection by mutableStateOf<Inspection?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadInspections()
    }

    fun loadInspections() {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                withContext(Dispatchers.IO) {
                    val inspectionsList = db.inspectionDao().getAllInspectionsList()
                    withContext(Dispatchers.Main) {
                        inspections = inspectionsList
                    }
                }
            } catch (e: Exception) {
                Log.e("HistoryViewModel", "Error loading inspections", e)
                errorMessage = "Error loading inspections: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun loadInspectionDetails(inspectionId: String) {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                withContext(Dispatchers.IO) {
                    val inspection = db.inspectionDao().getInspectionById(inspectionId)
                    val items = db.inspectionDao().getItemsForInspection(inspectionId)

                    // Build a simplified inspection object with minimal data
                    val inspectionWithItems = Inspection(
                        id = inspection.id,
                        equipment = inspection.equipment,
                        inspector = inspection.inspector,
                        supervisor = inspection.supervisor,
                        horometer = inspection.horometer,
                        date = inspection.date,
                        isCompleted = inspection.isCompleted,
                        items = emptyList() // We'll just show basic info for now
                    )

                    withContext(Dispatchers.Main) {
                        selectedInspection = inspectionWithItems
                    }
                }
            } catch (e: Exception) {
                Log.e("HistoryViewModel", "Error loading inspection details", e)
                errorMessage = "Error loading details: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearSelectedInspection() {
        selectedInspection = null
    }

    fun clearError() {
        errorMessage = null
    }
}