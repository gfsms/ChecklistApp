// app/src/main/java/com/tudominio/checklistapp/data/database/AppDatabase.kt
package com.tudominio.checklistapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        InspectionEntity::class,
        InspectionItemEntity::class,
        InspectionQuestionEntity::class,
        PhotoEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inspectionDao(): InspectionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "checklist_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}