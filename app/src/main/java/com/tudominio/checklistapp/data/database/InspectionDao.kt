package com.tudominio.checklistapp.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InspectionDao {
    // Insert operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: InspectionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InspectionItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: InspectionQuestionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity)

    // Delete operations
    @Query("DELETE FROM inspections WHERE id = :inspectionId")
    suspend fun deleteInspection(inspectionId: String)

    @Query("DELETE FROM inspection_items WHERE inspectionId = :inspectionId")
    suspend fun deleteItemsForInspection(inspectionId: String)

    @Query("DELETE FROM inspection_questions WHERE id = :questionId")
    suspend fun deleteQuestion(questionId: String)

    @Query("DELETE FROM photos WHERE questionId = :questionId")
    suspend fun deletePhotosForQuestion(questionId: String)

    // Basic query to get all inspections
    @Query("SELECT * FROM inspections ORDER BY date DESC")
    fun getAllInspections(): Flow<List<InspectionEntity>>

    // Get inspections as a list (non-Flow version)
    @Query("SELECT * FROM inspections ORDER BY date DESC")
    suspend fun getAllInspectionsList(): List<InspectionEntity>

    // Query to get a single inspection by ID
    @Query("SELECT * FROM inspections WHERE id = :inspectionId")
    suspend fun getInspectionById(inspectionId: String): InspectionEntity

    // Query to get all items for an inspection
    @Query("SELECT * FROM inspection_items WHERE inspectionId = :inspectionId")
    suspend fun getItemsForInspection(inspectionId: String): List<InspectionItemEntity>

    // Query to get all questions for an item
    @Query("SELECT * FROM inspection_questions WHERE itemId = :itemId")
    suspend fun getQuestionsForItem(itemId: String): List<InspectionQuestionEntity>

    // Query to get all photos for a question
    @Query("SELECT * FROM photos WHERE questionId = :questionId")
    suspend fun getPhotosForQuestion(questionId: String): List<PhotoEntity>

    // Search inspections by equipment ID or inspector
    @Query("SELECT * FROM inspections WHERE equipment LIKE '%' || :searchQuery || '%' OR inspector LIKE '%' || :searchQuery || '%' ORDER BY date DESC")
    suspend fun searchInspections(searchQuery: String): List<InspectionEntity>

    // Filter inspections by conformity level
    @Query("SELECT * FROM inspections WHERE conformityPercentage >= :minPercentage AND conformityPercentage <= :maxPercentage ORDER BY date DESC")
    suspend fun getInspectionsByConformity(minPercentage: Float, maxPercentage: Float): List<InspectionEntity>

    // Query to find recurring non-conformities
    @Query("""
        SELECT q.* FROM inspection_questions q
        JOIN inspection_items i ON q.itemId = i.id
        JOIN inspections insp ON i.inspectionId = insp.id
        WHERE q.text LIKE '%' || :questionText || '%'
        AND i.name LIKE '%' || :itemName || '%'
        AND insp.equipment LIKE '%' || :equipment || '%'
        AND insp.id != :currentInspectionId
        AND q.isConform = 0
        ORDER BY insp.date DESC
        LIMIT 10
    """)
    suspend fun findSimilarNonConformities(
        questionText: String,
        itemName: String,
        equipment: String,
        currentInspectionId: String
    ): List<InspectionQuestionEntity>
}