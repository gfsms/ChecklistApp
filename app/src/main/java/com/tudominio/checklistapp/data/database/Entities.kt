// app/src/main/java/com/tudominio/checklistapp/data/database/Entities.kt
package com.tudominio.checklistapp.data.database

import androidx.room.*
import java.time.LocalDateTime

@Entity(tableName = "inspections")
data class InspectionEntity(
    @PrimaryKey val id: String,
    val equipment: String,
    val inspector: String,
    val supervisor: String,
    val horometer: String,
    val date: LocalDateTime,
    val isCompleted: Boolean,
    val conformityPercentage: Float
)

@Entity(
    tableName = "inspection_items",
    foreignKeys = [
        ForeignKey(
            entity = InspectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["inspectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("inspectionId")]
)
data class InspectionItemEntity(
    @PrimaryKey val id: String,
    val inspectionId: String,
    val name: String
)

@Entity(
    tableName = "inspection_questions",
    foreignKeys = [
        ForeignKey(
            entity = InspectionItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("itemId")]
)
data class InspectionQuestionEntity(
    @PrimaryKey val id: String,
    val itemId: String,
    val text: String,
    val isConform: Boolean?,
    val comment: String?
)

@Entity(
    tableName = "photos",
    foreignKeys = [
        ForeignKey(
            entity = InspectionQuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("questionId")]
)
data class PhotoEntity(
    @PrimaryKey val id: String,
    val questionId: String,
    val uri: String,
    val hasDrawings: Boolean,
    val drawingUri: String?,
    val timestamp: LocalDateTime
)