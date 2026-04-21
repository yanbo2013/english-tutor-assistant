package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 练习记录实体类
 */
@Entity(
    tableName = "records",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class RecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val sentenceIndex: Int,
    val originalText: String,
    val studentAudioPath: String?,
    val recognizedText: String?,
    val accuracyScore: Float?,
    val fluencyScore: Float?,
    val completenessScore: Float?,
    val feedback: String?,
    val createdAt: Long = System.currentTimeMillis()
)
