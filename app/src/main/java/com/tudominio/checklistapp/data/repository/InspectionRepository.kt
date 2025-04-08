package com.tudominio.checklistapp.data.repository

import android.util.Log
import com.tudominio.checklistapp.data.database.*
import com.tudominio.checklistapp.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.UUID

/**
 * Repository implementation that connects the ViewModel with the database.
 * Provides methods to save, retrieve, and manage inspection data.
 */
open class InspectionRepository(private val inspectionDao: InspectionDao) {
    private val TAG = "InspectionRepository"

    // Get all inspections as a Flow (reactive stream)
    val allInspections: Flow<List<Inspection>> = try {
        inspectionDao.getAllInspections()
            .map { entities ->
                entities.map { it.toInspectionModel() }
            }
            .catch { e ->
                logError("Error in allInspections flow", e)
                emit(emptyList())
            }
    } catch (e: Exception) {
        logError("Failed to create allInspections flow", e)
        flowOf(emptyList())
    }

    // Get all inspections as a suspending function (for one-time operations)
    suspend fun getAllInspectionsList(): List<Inspection> {
        return try {
            val inspectionEntities = inspectionDao.getAllInspectionsList()
            inspectionEntities.map { entity ->
                entity.toInspectionModel()
            }
        } catch (e: Exception) {
            logError("Error getting all inspections list", e)
            emptyList()
        }
    }

    // Save a complete inspection to the database
    suspend fun saveInspection(inspection: Inspection): Boolean {
        return try {
            Log.d(TAG, "Starting to save inspection: ${inspection.id}")

            // Calculate conformity percentage
            val totalQuestions = inspection.items.sumOf { it.questions.size }
            val answeredQuestions = inspection.items.sumOf { item ->
                item.questions.count { it.answer != null }
            }
            val conformQuestions = inspection.items.sumOf { item ->
                item.questions.count { question ->
                    question.answer?.isConform == true
                }
            }

            val conformityPercentage = if (answeredQuestions > 0) {
                (conformQuestions.toFloat() / answeredQuestions) * 100
            } else {
                0f
            }

            // Save main inspection entity
            val inspectionEntity = InspectionEntity(
                id = inspection.id,
                equipment = inspection.equipment,
                inspector = inspection.inspector,
                supervisor = inspection.supervisor,
                horometer = inspection.horometer,
                date = inspection.date,
                isCompleted = inspection.isCompleted,
                conformityPercentage = conformityPercentage
            )
            inspectionDao.insertInspection(inspectionEntity)
            Log.d(TAG, "Saved inspection entity: ${inspectionEntity.id}")

            // Save items, questions and photos
            inspection.items.forEach { item ->
                val itemEntity = InspectionItemEntity(
                    id = item.id,
                    inspectionId = inspection.id,
                    name = item.name
                )
                inspectionDao.insertItem(itemEntity)
                Log.d(TAG, "Saved item: ${itemEntity.id}")

                item.questions.forEach { question ->
                    val questionEntity = InspectionQuestionEntity(
                        id = question.id,
                        itemId = item.id,
                        text = question.text,
                        isConform = question.answer?.isConform,
                        comment = question.answer?.comment
                    )
                    inspectionDao.insertQuestion(questionEntity)
                    Log.d(TAG, "Saved question: ${questionEntity.id}")

                    question.answer?.photos?.forEach { photo ->
                        val photoEntity = PhotoEntity(
                            id = photo.id,
                            questionId = question.id,
                            uri = photo.uri,
                            hasDrawings = photo.hasDrawings,
                            drawingUri = photo.drawingUri,
                            timestamp = photo.timestamp
                        )
                        inspectionDao.insertPhoto(photoEntity)
                        Log.d(TAG, "Saved photo: ${photoEntity.id}")
                    }
                }
            }

            Log.d(TAG, "Successfully saved inspection: ${inspection.id}")
            true
        } catch (e: Exception) {
            logError("Error saving inspection: ${e.message}", e)
            false
        }
    }

    // Get a specific inspection by ID
    suspend fun getInspectionById(inspectionId: String): Inspection? {
        return try {
            // Get base inspection data
            val inspectionEntity = inspectionDao.getInspectionById(inspectionId)
            inspectionEntity.toInspectionModel()
        } catch (e: Exception) {
            logError("Error getting inspection by ID: ${e.message}", e)
            null
        }
    }

    // Get full inspection with all its relations (items, questions, photos)
    suspend fun getFullInspection(inspectionId: String): Inspection? {
        return try {
            // Get base inspection entity
            val inspectionEntity = inspectionDao.getInspectionById(inspectionId)

            // Get related items
            val itemEntities = inspectionDao.getItemsForInspection(inspectionId)

            // Map items and their relations
            val items = itemEntities.map { itemEntity ->
                val questionEntities = inspectionDao.getQuestionsForItem(itemEntity.id)

                val questions = questionEntities.map { questionEntity ->
                    val photoEntities = inspectionDao.getPhotosForQuestion(questionEntity.id)

                    val photos = photoEntities.map { photoEntity ->
                        Photo(
                            id = photoEntity.id,
                            uri = photoEntity.uri,
                            hasDrawings = photoEntity.hasDrawings,
                            drawingUri = photoEntity.drawingUri,
                            timestamp = photoEntity.timestamp
                        )
                    }

                    val answer = if (questionEntity.isConform != null) {
                        Answer(
                            isConform = questionEntity.isConform,
                            comment = questionEntity.comment ?: "",
                            photos = photos,
                            timestamp = LocalDateTime.now() // Using current time since we don't store answer timestamp
                        )
                    } else null

                    InspectionQuestion(
                        id = questionEntity.id,
                        text = questionEntity.text,
                        answer = answer
                    )
                }

                InspectionItem(
                    id = itemEntity.id,
                    name = itemEntity.name,
                    questions = questions
                )
            }

            Inspection(
                id = inspectionEntity.id,
                equipment = inspectionEntity.equipment,
                inspector = inspectionEntity.inspector,
                supervisor = inspectionEntity.supervisor,
                horometer = inspectionEntity.horometer,
                date = inspectionEntity.date,
                items = items,
                isCompleted = inspectionEntity.isCompleted
            )
        } catch (e: Exception) {
            logError("Error getting full inspection", e)
            null
        }
    }

    // Delete an inspection and all its related data
    suspend fun deleteInspection(inspectionId: String): Boolean {
        return try {
            inspectionDao.deleteInspection(inspectionId)
            true
        } catch (e: Exception) {
            logError("Error deleting inspection: ${e.message}", e)
            false
        }
    }


    // Test database connection
    suspend fun testDatabaseConnection(): Boolean {
        return try {
            Log.d(TAG, "Testing database connection...")
            val inspections = inspectionDao.getAllInspectionsList()
            Log.d(TAG, "Database connection test successful. Found ${inspections.size} inspections.")
            true
        } catch (e: Exception) {
            logError("Database connection test failed", e)
            false
        }
    }

    // Get debugging string for inspections count
    fun getInspectionsDebugInfo(): String {
        return try {
            "Repository initialized, ready to fetch inspections"
        } catch (e: Exception) {
            "Error in repository: ${e.message}"
        }
    }

    // Helper extension function to convert InspectionEntity to Inspection model
    private fun InspectionEntity.toInspectionModel(): Inspection {
        return Inspection(
            id = this.id,
            equipment = this.equipment,
            inspector = this.inspector,
            supervisor = this.supervisor,
            horometer = this.horometer,
            date = this.date,
            isCompleted = this.isCompleted,
            items = emptyList() // Items will be loaded separately if needed
        )
    }

    /**
     * Method for logging errors that can be overridden by subclasses
     */
    open fun logError(message: String, e: Throwable? = null) {
        if (e != null) {
            Log.e(TAG, message, e)
        } else {
            Log.e(TAG, message)
        }
    }
}