package com.tudominio.checklistapp.data.repository

import com.tudominio.checklistapp.data.database.*
import com.tudominio.checklistapp.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

class InspectionRepository(private val inspectionDao: InspectionDao) {

    // Expose all inspections as a Flow
    val allInspections: Flow<List<InspectionEntity>> = inspectionDao.getAllInspections()

    // Save a complete inspection to the database
    suspend fun saveInspection(inspection: Inspection) {
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

        // Save items, questions and photos
        inspection.items.forEach { item ->
            val itemEntity = InspectionItemEntity(
                id = item.id,
                inspectionId = inspection.id,
                name = item.name
            )
            inspectionDao.insertItem(itemEntity)

            item.questions.forEach { question ->
                val questionEntity = InspectionQuestionEntity(
                    id = question.id,
                    itemId = item.id,
                    text = question.text,
                    isConform = question.answer?.isConform,
                    comment = question.answer?.comment
                )
                inspectionDao.insertQuestion(questionEntity)

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
                }
            }
        }
    }

    // Get a complete inspection with all its data
    suspend fun getFullInspection(inspectionId: String): Inspection {
        // Get base inspection data
        val inspectionEntity = inspectionDao.getInspectionById(inspectionId)
        val itemEntities = inspectionDao.getItemsForInspection(inspectionId)

        // Build the complete inspection object
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
                        timestamp = LocalDateTime.now() // Using current time as we don't store answer timestamp
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

        return Inspection(
            id = inspectionEntity.id,
            equipment = inspectionEntity.equipment,
            inspector = inspectionEntity.inspector,
            supervisor = inspectionEntity.supervisor,
            horometer = inspectionEntity.horometer,
            date = inspectionEntity.date,
            items = items,
            isCompleted = inspectionEntity.isCompleted
        )
    }

    // Find recurring non-conformities for warning
    suspend fun findRecurringNonConformities(
        questionText: String,
        itemName: String,
        equipment: String,
        currentInspectionId: String
    ): List<InspectionQuestionEntity> {
        return inspectionDao.findSimilarNonConformities(
            questionText, itemName, equipment, currentInspectionId
        )
    }
}