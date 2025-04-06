// app/src/main/java/com/tudominio/checklistapp/ui/viewmodels/HistoryViewModel.kt
package com.tudominio.checklistapp.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tudominio.checklistapp.data.database.AppDatabase
import com.tudominio.checklistapp.data.database.InspectionEntity
import com.tudominio.checklistapp.data.model.Inspection
import com.tudominio.checklistapp.data.repository.InspectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: InspectionRepository
    private val _inspections = MutableStateFlow<List<InspectionEntity>>(emptyList())
    val inspections: StateFlow<List<InspectionEntity>> = _inspections

    private val _selectedInspection = MutableStateFlow<Inspection?>(null)
    val selectedInspection: StateFlow<Inspection?> = _selectedInspection

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        val inspectionDao = AppDatabase.getDatabase(application).inspectionDao()
        repository = InspectionRepository(inspectionDao)

        viewModelScope.launch {
            repository.allInspections.collectLatest {
                _inspections.value = it
            }
        }
    }

    fun loadInspectionDetails(inspectionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val inspection = repository.getFullInspection(inspectionId)
                _selectedInspection.value = inspection
            } catch (e: Exception) {
                // Manejar error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSelectedInspection() {
        _selectedInspection.value = null
    }
}