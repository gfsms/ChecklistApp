// app/src/main/java/com/tudominio/checklistapp/data/database/InspectionDao.kt
package com.tudominio.checklistapp.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InspectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: InspectionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InspectionItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: InspectionQuestionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity)

    @Transaction
    @Query("SELECT * FROM inspections ORDER BY date DESC")
    fun getAllInspections(): Flow<List<InspectionEntity>>

    @Query("SELECT * FROM inspection_items WHERE inspectionId = :inspectionId")
    suspend fun getItemsForInspection(inspectionId: String): List<InspectionItemEntity>

    @Query("SELECT * FROM inspection_questions WHERE itemId = :itemId")
    suspend fun getQuestionsForItem(itemId: String): List<InspectionQuestionEntity>

    @Query("SELECT * FROM photos WHERE questionId = :questionId")
    suspend fun getPhotosForQuestion(questionId: String): List<PhotoEntity>

    @Query("SELECT * FROM inspection_questions WHERE isConform = 0 AND text = :questionText AND itemId IN (SELECT id FROM inspection_items WHERE name = :itemName AND inspectionId IN (SELECT id FROM inspections WHERE equipment = :equipment AND id != :currentInspectionId))")
    suspend fun findSimilarNonConformities(questionText: String, itemName: String, equipment: String, currentInspectionId: String): List<InspectionQuestionEntity>

    @Query("SELECT * FROM inspections WHERE id = :inspectionId")
    suspend fun getInspectionById(inspectionId: String): InspectionEntity
}