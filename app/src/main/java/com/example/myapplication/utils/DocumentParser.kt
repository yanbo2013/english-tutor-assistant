package com.example.myapplication.utils

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xslf.usermodel.XMLSlideShow
import java.io.File
import java.io.FileOutputStream

/**
 * 文档解析帮助类
 * 支持 PDF、Word(.docx)、PowerPoint(.pptx)
 */
class DocumentParser(private val context: Context) {
    
    /**
     * 解析文档，提取文本内容
     */
    fun parseDocument(uri: Uri): Result<String> {
        return try {
            val fileName = getFileName(uri)
            val text = when {
                fileName.endsWith(".pdf", ignoreCase = true) -> parsePdf(uri)
                fileName.endsWith(".docx", ignoreCase = true) || fileName.endsWith(".doc", ignoreCase = true) -> parseWord(uri)
                fileName.endsWith(".pptx", ignoreCase = true) || fileName.endsWith(".ppt", ignoreCase = true) -> parsePowerPoint(uri)
                else -> throw IllegalArgumentException("不支持的文件格式: $fileName")
            }
            
            Result.success(text)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * 解析 PDF 文件
     */
    private fun parsePdf(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("无法打开文件")
        
        return inputStream.use { stream ->
            val document = PDDocument.load(stream)
            try {
                val stripper = PDFTextStripper()
                stripper.getText(document)
            } finally {
                document.close()
            }
        }
    }
    
    /**
     * 解析 Word 文件 (.docx)
     */
    private fun parseWord(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("无法打开文件")
        
        return inputStream.use { stream ->
            val document = XWPFDocument(stream)
            try {
                val paragraphs = document.paragraphs.mapNotNull { it.text }
                paragraphs.joinToString("\n\n")
            } finally {
                document.close()
            }
        }
    }
    
    /**
     * 解析 PowerPoint 文件 (.pptx)
     */
    private fun parsePowerPoint(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("无法打开文件")
        
        return inputStream.use { stream ->
            val slideShow = XMLSlideShow(stream)
            try {
                val slides = slideShow.slides
                val textList = mutableListOf<String>()
                
                for (slide in slides) {
                    for (shape in slide.shapes) {
                        if (shape is org.apache.poi.xslf.usermodel.XSLFTextShape) {
                            val text = shape.text
                            if (text.isNotBlank()) {
                                textList.add(text)
                            }
                        }
                    }
                }
                
                textList.joinToString("\n\n")
            } finally {
                slideShow.close()
            }
        }
    }
    
    /**
     * 获取文件名
     */
    private fun getFileName(uri: Uri): String {
        var fileName = ""
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    fileName = it.getString(displayNameIndex)
                }
            }
        }
        return fileName.ifBlank { uri.lastPathSegment ?: "unknown" }
    }
    
    /**
     * 将 URI 复制到应用内部存储
     */
    fun copyUriToInternalStorage(uri: Uri, fileName: String): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return null
            
            val outputFile = File(context.filesDir, "documents/$fileName")
            outputFile.parentFile?.mkdirs()
            
            FileOutputStream(outputFile).use { output ->
                inputStream.use { input ->
                    input.copyTo(output)
                }
            }
            
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
