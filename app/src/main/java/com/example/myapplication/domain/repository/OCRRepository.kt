package com.example.myapplication.domain.repository

import android.graphics.Bitmap
import com.example.myapplication.data.remote.model.OCRResponse

/**
 * OCR 识别仓库接口
 */
interface OCRRepository {
    
    /**
     * 识别图片中的文字
     */
    suspend fun recognizeText(bitmap: Bitmap): Result<OCRResponse>
    
    /**
     * 从图片文件识别文字
     */
    suspend fun recognizeTextFromPath(imagePath: String): Result<OCRResponse>
}
