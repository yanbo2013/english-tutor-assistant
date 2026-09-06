package com.example.myapplication.data.local.mapper

import com.example.myapplication.data.local.entity.SessionEntity
import com.example.myapplication.domain.model.Session
import com.example.myapplication.domain.model.TextSegment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * SessionEntity → Session 领域模型 映射器
 *
 * Room 实体中的 selectedSegments 是 JSON 字符串，
 * 需要反序列化为 List<TextSegment>
 */
private val gson = Gson()
private val textSegmentListType = object : TypeToken<List<TextSegment>>() {}.type

fun SessionEntity.toDomain(): Session {
    val segments: List<TextSegment> = try {
        if (selectedSegments.isBlank()) {
            emptyList()
        } else {
            gson.fromJson<List<TextSegment>>(selectedSegments, textSegmentListType) ?: emptyList()
        }
    } catch (_: Exception) {
        emptyList()
    }

    return Session(
        id = id,
        timestamp = timestamp,
        imageUrl = imageUrl,
        recognizedText = recognizedText,
        segments = segments,
        sessionType = sessionType,
        completedAt = completedAt
    )
}

/**
 * Session 领域模型 → SessionEntity
 */
fun Session.toEntity(): SessionEntity {
    val segmentsJson = try {
        gson.toJson(segments)
    } catch (_: Exception) {
        "[]"
    }

    return SessionEntity(
        id = id,
        timestamp = timestamp,
        imageUrl = imageUrl,
        recognizedText = recognizedText,
        selectedSegments = segmentsJson,
        sessionType = sessionType,
        completedAt = completedAt
    )
}
