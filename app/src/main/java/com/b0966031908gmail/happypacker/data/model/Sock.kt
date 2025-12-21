package com.b0966031908gmail.happypacker.data.model

/**
 * 襪子資料
 */
data class Sock(
    val id: Int,              // 編號
    val colorName: String,    // 顏色名稱（中文）
    val colorCode: String,    // 顏色代碼（例如 #FF6B9E）
    val price: Int            // 價格
) {
    companion object {
        /**
         * 取得所有襪子
         */
        fun getAllSocks(): List<Sock> = listOf(
            Sock(1, "紅色", "#FF5252", 30),
            Sock(2, "藍色", "#448AFF", 30),
            Sock(3, "黃色", "#FFD740", 30),
            Sock(4, "粉色", "#FF6B9E", 30),
            Sock(5, "綠色", "#69F0AE", 30),
            Sock(6, "紫色", "#E040FB", 30)
        )

        /**
         * 隨機取得一�雙襪子
         */
        fun getRandomSock(): Sock = getAllSocks().random()
    }
}