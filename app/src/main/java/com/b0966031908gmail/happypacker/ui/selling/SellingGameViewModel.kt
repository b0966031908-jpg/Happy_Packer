package com.b0966031908gmail.happypacker.ui.selling

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.b0966031908gmail.happypacker.data.model.Customer
import com.b0966031908gmail.happypacker.data.model.GameLevel
import com.b0966031908gmail.happypacker.data.model.Sock
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SellingGameViewModel : ViewModel() {

    // --- 狀態定義 ---
    private val _currentLevel = MutableStateFlow<GameLevel?>(null)
    val currentLevel: StateFlow<GameLevel?> = _currentLevel

    private val _currentCustomer = MutableStateFlow<Customer?>(null)
    val currentCustomer: StateFlow<Customer?> = _currentCustomer

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex

    private val _currentQuestion = MutableStateFlow<GameLevel.Question?>(null)
    val currentQuestion: StateFlow<GameLevel.Question?> = _currentQuestion

    private val _totalScore = MutableStateFlow(0)
    val totalScore: StateFlow<Int> = _totalScore

    private val _timeRemaining = MutableStateFlow(30)
    val timeRemaining: StateFlow<Int> = _timeRemaining

    private val _gameState = MutableStateFlow(GameState.LEVEL_SELECT)
    val gameState: StateFlow<GameState> = _gameState

    private var timerJob: Job? = null

    enum class GameState {
        LEVEL_SELECT, CUSTOMER_SPEAKS, SELECT_SOCK, CALCULATE_CHANGE,
        CORRECT_ANSWER, WRONG_ANSWER, TIME_UP, LEVEL_COMPLETE
    }

    // --- 外部呼叫方法 ---

    fun startLevel(levelNumber: Int) {
        val level = GameLevel.getLevel(levelNumber)
        if (level != null) {
            _currentLevel.value = level
            _currentQuestionIndex.value = 0
            _totalScore.value = 0
            loadQuestion()
        }
    }

    private fun loadQuestion() {
        val level = _currentLevel.value ?: return
        val index = _currentQuestionIndex.value

        if (index < level.questions.size) {
            val question = level.questions[index]
            _currentQuestion.value = question
            // 同步建立客人
            _currentCustomer.value = Customer.createRandomCustomer(
                quantity = question.sockQuantity,
                price = question.sockPrice
            )
            _gameState.value = GameState.CUSTOMER_SPEAKS
        } else {
            _gameState.value = GameState.LEVEL_COMPLETE
        }
    }

    fun customerFinishedSpeaking() {
        _gameState.value = GameState.SELECT_SOCK
    }

    fun selectSock(sock: Sock) {
        val customer = _currentCustomer.value ?: return
        // 假設 Customer model 有 wantedSock 屬性或是比對 ID
        if (sock.id == customer.wantedSock.id) {
            _gameState.value = GameState.CALCULATE_CHANGE
            startTimer()
        } else {
            _gameState.value = GameState.WRONG_ANSWER
        }
    }

    fun answerChange(answer: Int) {
        stopTimer()
        val question = _currentQuestion.value ?: return
        if (answer == question.correctChange) {
            val bonus = _timeRemaining.value
            _totalScore.value += (10 + bonus)
            _gameState.value = GameState.CORRECT_ANSWER
        } else {
            _gameState.value = GameState.WRONG_ANSWER
        }
    }

    fun answerChangeByInput(input: String) {
        val valAnswer = input.toIntOrNull() ?: -1
        answerChange(valAnswer)
    }

    fun nextQuestion() {
        if (_gameState.value == GameState.TIME_UP) {
            retryAfterTimeUp()
        } else {
            _currentQuestionIndex.value += 1
            loadQuestion()
        }
    }

    fun retry() {
        _gameState.value = GameState.SELECT_SOCK
    }

    private fun retryAfterTimeUp() {
        _gameState.value = GameState.CALCULATE_CHANGE
        startTimer()
    }

    fun backToLevelSelect() {
        stopTimer()
        _gameState.value = GameState.LEVEL_SELECT
    }

    fun updateScore(newScore: Int) {
        _totalScore.value = newScore
    }

    // --- 工具方法 ---

    private fun startTimer() {
        timerJob?.cancel()
        val limit = _currentQuestion.value?.timeLimit ?: 30
        _timeRemaining.value = limit

        timerJob = viewModelScope.launch {
            while (_timeRemaining.value > 0) {
                delay(1000)
                _timeRemaining.value -= 1
            }
            if (_gameState.value == GameState.CALCULATE_CHANGE) {
                _gameState.value = GameState.TIME_UP
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
    }

    fun getChangeOptions(): List<Int> {
        val correct = _currentQuestion.value?.correctChange ?: 0
        val options = mutableSetOf(correct)
        while (options.size < 3) {
            val offset = listOf(-10, -5, 5, 10, 2, -2).random()
            val wrong = correct + offset
            if (wrong > 0 && wrong != correct) options.add(wrong)
        }
        return options.toList().shuffled()
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}