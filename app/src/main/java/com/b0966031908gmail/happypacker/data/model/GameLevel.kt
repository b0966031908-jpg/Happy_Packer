package com.b0966031908gmail.happypacker.data.model

/**
 * 遊戲關卡（升級版）
 */
data class GameLevel(
    val levelNumber: Int,         // 關卡編號 (1-9)
    val difficulty: Difficulty,   // 難度
    val customerCount: Int,       // 客人數量
    val questions: List<Question> // 題目列表
) {
    /**
     * 難度等級
     */
    enum class Difficulty {
        EASY,      // 簡單：選擇題
        MEDIUM,    // 中等：選擇題 + 多雙襪子
        HARD       // 困難：輸入答案 + 多雙襪子
    }

    /**
     * 問題
     */
    data class Question(
        val sockQuantity: Int,     // 襪子數量（1-3雙）
        val sockPrice: Int,        // 單價
        val paymentAmount: Int,    // 客人付的錢
        val timeLimit: Int         // 時間限制（秒）
    ) {
        // 總價
        val totalPrice: Int get() = sockPrice * sockQuantity

        // 應找零錢
        val correctChange: Int get() = paymentAmount - totalPrice
    }

    companion object {
        /**
         * 取得所有關卡
         */
        fun getAllLevels(): List<GameLevel> = listOf(
            // 第 1 關：簡單（單價30，付50-100）
            GameLevel(
                levelNumber = 1,
                difficulty = Difficulty.EASY,
                customerCount = 3,
                questions = listOf(
                    Question(sockQuantity = 1, sockPrice = 30, paymentAmount = 50, timeLimit = 30),
                    Question(sockQuantity = 1, sockPrice = 30, paymentAmount = 100, timeLimit = 25),
                    Question(sockQuantity = 1, sockPrice = 30, paymentAmount = 60, timeLimit = 20)
                )
            ),

            // 第 2 關：簡單（單價40，付50-100）
            GameLevel(
                levelNumber = 2,
                difficulty = Difficulty.EASY,
                customerCount = 3,
                questions = listOf(
                    Question(sockQuantity = 1, sockPrice = 40, paymentAmount = 100, timeLimit = 30),
                    Question(sockQuantity = 1, sockPrice = 40, paymentAmount = 50, timeLimit = 25),
                    Question(sockQuantity = 1, sockPrice = 40, paymentAmount = 80, timeLimit = 20)
                )
            ),

            // 第 3 關：簡單（單價50，付100）
            GameLevel(
                levelNumber = 3,
                difficulty = Difficulty.EASY,
                customerCount = 3,
                questions = listOf(
                    Question(sockQuantity = 1, sockPrice = 50, paymentAmount = 100, timeLimit = 30),
                    Question(sockQuantity = 1, sockPrice = 50, paymentAmount = 200, timeLimit = 25),
                    Question(sockQuantity = 1, sockPrice = 50, paymentAmount = 80, timeLimit = 20)
                )
            ),

            // 第 4 關：中等（2雙襪子）
            GameLevel(
                levelNumber = 4,
                difficulty = Difficulty.MEDIUM,
                customerCount = 3,
                questions = listOf(
                    Question(sockQuantity = 2, sockPrice = 30, paymentAmount = 100, timeLimit = 30),  // 60元，找40
                    Question(sockQuantity = 2, sockPrice = 40, paymentAmount = 100, timeLimit = 25),  // 80元，找20
                    Question(sockQuantity = 2, sockPrice = 25, paymentAmount = 100, timeLimit = 20)   // 50元，找50
                )
            ),

            // 第 5 關：中等（2-3雙）
            GameLevel(
                levelNumber = 5,
                difficulty = Difficulty.MEDIUM,
                customerCount = 3,
                questions = listOf(
                    Question(sockQuantity = 2, sockPrice = 35, paymentAmount = 100, timeLimit = 30),  // 70元，找30
                    Question(sockQuantity = 3, sockPrice = 30, paymentAmount = 100, timeLimit = 25),  // 90元，找10
                    Question(sockQuantity = 2, sockPrice = 45, paymentAmount = 200, timeLimit = 20)   // 90元，找110
                )
            ),

            // 第 6 關：中等（3雙）
            GameLevel(
                levelNumber = 6,
                difficulty = Difficulty.MEDIUM,
                customerCount = 3,
                questions = listOf(
                    Question(sockQuantity = 3, sockPrice = 40, paymentAmount = 200, timeLimit = 30),  // 120元，找80
                    Question(sockQuantity = 3, sockPrice = 25, paymentAmount = 100, timeLimit = 25),  // 75元，找25
                    Question(sockQuantity = 3, sockPrice = 50, paymentAmount = 200, timeLimit = 20)   // 150元，找50
                )
            ),

            // 第 7 關：困難（輸入答案 + 2雙）
            GameLevel(
                levelNumber = 7,
                difficulty = Difficulty.HARD,
                customerCount = 3,
                questions = listOf(
                    Question(sockQuantity = 2, sockPrice = 45, paymentAmount = 100, timeLimit = 30),  // 90元，找10
                    Question(sockQuantity = 2, sockPrice = 55, paymentAmount = 200, timeLimit = 25),  // 110元，找90
                    Question(sockQuantity = 2, sockPrice = 35, paymentAmount = 100, timeLimit = 20)   // 70元，找30
                )
            ),

            // 第 8 關：困難（輸入答案 + 3雙）
            GameLevel(
                levelNumber = 8,
                difficulty = Difficulty.HARD,
                customerCount = 3,
                questions = listOf(
                    Question(sockQuantity = 3, sockPrice = 35, paymentAmount = 200, timeLimit = 30),  // 105元，找95
                    Question(sockQuantity = 3, sockPrice = 45, paymentAmount = 200, timeLimit = 25),  // 135元，找65
                    Question(sockQuantity = 3, sockPrice = 40, paymentAmount = 150, timeLimit = 20)   // 120元，找30
                )
            ),

            // 第 9 關：困難（輸入答案 + 3雙 + 高金額）
            GameLevel(
                levelNumber = 9,
                difficulty = Difficulty.HARD,
                customerCount = 3,
                questions = listOf(
                    Question(sockQuantity = 3, sockPrice = 60, paymentAmount = 200, timeLimit = 30),  // 180元，找20
                    Question(sockQuantity = 3, sockPrice = 55, paymentAmount = 200, timeLimit = 25),  // 165元，找35
                    Question(sockQuantity = 3, sockPrice = 50, paymentAmount = 500, timeLimit = 20)   // 150元，找350
                )
            )
        )

        /**
         * 取得特定關卡
         */
        fun getLevel(levelNumber: Int): GameLevel? {
            return getAllLevels().find { it.levelNumber == levelNumber }
        }
    }
}