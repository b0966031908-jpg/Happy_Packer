package com.b0966031908gmail.happypacker.data.model

/**
 * 客人資料（升級版）
 */
data class Customer(
    val emoji: String,        // 客人頭像 emoji
    val name: String,         // 客人名字
    val wantedSock: Sock,     // 想要的襪子
    val quantity: Int = 1     // 👈 新增：要買幾雙（1-3）
) {
    /**
     * 客人說的話
     */
    fun getSpeech(): String {
        return if (quantity == 1) {
            "我要${wantedSock.colorName}襪子"
        } else {
            "我要 ${quantity} 雙${wantedSock.colorName}襪子"
        }
    }

    /**
     * 總價
     */
    fun getTotalPrice(): Int = wantedSock.price * quantity

    companion object {
        // 可用的客人頭像
        private val customerEmojis = listOf("👦", "👧", "👴", "👵", "👨", "👩")

        /**
         * 創建隨機客人
         */
        fun createRandomCustomer(quantity: Int = 1, price: Int = 30): Customer {
            val emoji = customerEmojis.random()
            val sock = Sock.getRandomSock().copy(price = price)  // 使用指定價格
            val name = when(emoji) {
                "👦" -> "小明"
                "👧" -> "小美"
                "👴" -> "王爺爺"
                "👵" -> "李奶奶"
                "👨" -> "陳先生"
                "👩" -> "林小姐"
                else -> "客人"
            }
            return Customer(emoji, name, sock, quantity)
        }
    }
}