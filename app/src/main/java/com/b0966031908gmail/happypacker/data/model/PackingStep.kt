package com.b0966031908gmail.happypacker.data.model

data class PackingStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val audioText: String,
    val imageResId: Int? = null  // 圖片資源 ID（可選）
)