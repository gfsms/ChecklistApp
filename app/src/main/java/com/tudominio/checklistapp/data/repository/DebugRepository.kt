package com.tudominio.checklistapp.data.repository

import android.util.Log
import com.tudominio.checklistapp.data.database.AppDatabase
import com.tudominio.checklistapp.data.database.InspectionEntity
import com.tudominio.checklistapp.data.model.Inspection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A simplified repository to debug database access issues
 */
class DebugRepository(private val db: AppDatabase) {

    private val TAG = "DebugRepository"

    /**
     * Simple function to test if we can access the database
     */
    suspend fun testDatabaseAccess(): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                // Simplest possible query
                val count = db.inspectionDao().getAllInspectionsList().size
                Log.d(TAG, "Database accessed successfully. Found $count inspections.")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error accessing database: ${e.message}", e)
            false
        }
    }

    /**
     * Get all inspections as a list, with error handling
     */
    suspend fun getSafeInspectionsList(): List<InspectionEntity> {
        return try {
            withContext(Dispatchers.IO) {
                val result = db.inspectionDao().getAllInspectionsList()
                Log.d(TAG, "Retrieved ${result.size} inspections")
                result
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting inspections list: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get a specific inspection by ID, with error handling
     */
    suspend fun getSafeInspectionById(id: String): InspectionEntity? {
        return try {
            withContext(Dispatchers.IO) {
                val result = db.inspectionDao().getInspectionById(id)
                Log.d(TAG, "Retrieved inspection: $id")
                result
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting inspection $id: ${e.message}", e)
            null
        }
    }

    /**
     * Save a basic inspection with minimal data
     */
    suspend fun saveBasicInspection(inspection: Inspection): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                // Create a simplified entity with just the basic info
                val entity = InspectionEntity(
                    id = inspection.id,
                    equipment = inspection.equipment,
                    inspector = inspection.inspector,
                    supervisor = inspection.supervisor,
                    horometer = inspection.horometer,
                    date = inspection.date,
                    isCompleted = inspection.isCompleted,
                    conformityPercentage = calculateConformityPercentage(inspection)
                )

                // Insert just the inspection record without relations
                db.inspectionDao().insertInspection(entity)
                Log.d(TAG, "Saved basic inspection: ${inspection.id}")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving inspection: ${e.message}", e)
            false
        }
    }

    /**
     * Calculate the conformity percentage for an inspection
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
}