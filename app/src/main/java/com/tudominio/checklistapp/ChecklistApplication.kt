package com.tudominio.checklistapp

import android.app.Application
import android.util.Log
import com.tudominio.checklistapp.data.database.AppDatabase
import com.tudominio.checklistapp.data.database.InspectionDao
import com.tudominio.checklistapp.data.repository.InspectionRepository

/**
 * Application class that initializes database and repository.
 * This provides a single source of truth for the database and repository instances.
 */
class ChecklistApplication : Application() {
    private val TAG = "ChecklistApplication"

    // Lazy initialization of database
    val database by lazy {
        try {
            Log.d(TAG, "Initializing database...")
            val db = AppDatabase.getDatabase(this)
            Log.d(TAG, "Database initialized successfully")
            db
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing database: ${e.message}", e)
            null
        }
    }

    // Lazy initialization of DAO
    private val dao: InspectionDao? by lazy {
        try {
            Log.d(TAG, "Getting DAO...")
            val result = database?.inspectionDao()
            if (result != null) {
                Log.d(TAG, "DAO obtained successfully")
            } else {
                Log.e(TAG, "DAO is null")
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error getting DAO: ${e.message}", e)
            null
        }
    }

    // Repository with safe initialization
    val repository by lazy {
        Log.d(TAG, "Initializing repository...")
        dao?.let {
            Log.d(TAG, "Creating real repository with DAO")
            InspectionRepository(it)
        } ?: run {
            Log.e(TAG, "Creating dummy repository (DAO was null)")
            DummyRepository()
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application initialized")

        // Trigger database initialization at startup to detect issues early
        val dbInitialized = database != null
        Log.d(TAG, "Database initialized on startup: $dbInitialized")
    }

    /**
     * Fallback repository in case the real one can't be initialized
     * This prevents crashes when the database isn't available
     */
    private inner class DummyRepository : InspectionRepository(EmptyDao()) {
        // This will just log errors instead of crashing
        override fun logError(message: String, e: Throwable?) {
            Log.e(TAG, "DummyRepository: $message", e)
        }
    }

    /**
     * Empty DAO implementation for fallback
     */
    private inner class EmptyDao : InspectionDao {
        override suspend fun insertInspection(inspection: com.tudominio.checklistapp.data.database.InspectionEntity) {
            Log.e(TAG, "EmptyDao: insertInspection called but not available")
        }

        override suspend fun insertItem(item: com.tudominio.checklistapp.data.database.InspectionItemEntity) {
            Log.e(TAG, "EmptyDao: insertItem called but not available")
        }

        override suspend fun insertQuestion(question: com.tudominio.checklistapp.data.database.InspectionQuestionEntity) {
            Log.e(TAG, "EmptyDao: insertQuestion called but not available")
        }

        override suspend fun insertPhoto(photo: com.tudominio.checklistapp.data.database.PhotoEntity) {
            Log.e(TAG, "EmptyDao: insertPhoto called but not available")
        }

        override suspend fun deleteInspection(inspectionId: String) {
            Log.e(TAG, "EmptyDao: deleteInspection called but not available")
        }

        override suspend fun deleteItemsForInspection(inspectionId: String) {
            Log.e(TAG, "EmptyDao: deleteItemsForInspection called but not available")
        }

        override suspend fun deleteQuestion(questionId: String) {
            Log.e(TAG, "EmptyDao: deleteQuestion called but not available")
        }

        override suspend fun deletePhotosForQuestion(questionId: String) {
            Log.e(TAG, "EmptyDao: deletePhotosForQuestion called but not available")
        }

        override fun getAllInspections(): kotlinx.coroutines.flow.Flow<List<com.tudominio.checklistapp.data.database.InspectionEntity>> {
            Log.e(TAG, "EmptyDao: getAllInspections called but not available")
            return kotlinx.coroutines.flow.flowOf(emptyList())
        }

        override suspend fun getAllInspectionsList(): List<com.tudominio.checklistapp.data.database.InspectionEntity> {
            Log.e(TAG, "EmptyDao: getAllInspectionsList called but not available")
            return emptyList()
        }

        override suspend fun getInspectionById(inspectionId: String): com.tudominio.checklistapp.data.database.InspectionEntity {
            Log.e(TAG, "EmptyDao: getInspectionById called but not available")
            throw IllegalStateException("Database not available")
        }

        override suspend fun getItemsForInspection(inspectionId: String): List<com.tudominio.checklistapp.data.database.InspectionItemEntity> {
            Log.e(TAG, "EmptyDao: getItemsForInspection called but not available")
            return emptyList()
        }

        override suspend fun getQuestionsForItem(itemId: String): List<com.tudominio.checklistapp.data.database.InspectionQuestionEntity> {
            Log.e(TAG, "EmptyDao: getQuestionsForItem called but not available")
            return emptyList()
        }

        override suspend fun getPhotosForQuestion(questionId: String): List<com.tudominio.checklistapp.data.database.PhotoEntity> {
            Log.e(TAG, "EmptyDao: getPhotosForQuestion called but not available")
            return emptyList()
        }

        override suspend fun searchInspections(searchQuery: String): List<com.tudominio.checklistapp.data.database.InspectionEntity> {
            Log.e(TAG, "EmptyDao: searchInspections called but not available")
            return emptyList()
        }

        override suspend fun getInspectionsByConformity(minPercentage: Float, maxPercentage: Float): List<com.tudominio.checklistapp.data.database.InspectionEntity> {
            Log.e(TAG, "EmptyDao: getInspectionsByConformity called but not available")
            return emptyList()
        }

        override suspend fun findSimilarNonConformities(questionText: String, itemName: String, equipment: String, currentInspectionId: String): List<com.tudominio.checklistapp.data.database.InspectionQuestionEntity> {
            Log.e(TAG, "EmptyDao: findSimilarNonConformities called but not available")
            return emptyList()
        }
    }
}